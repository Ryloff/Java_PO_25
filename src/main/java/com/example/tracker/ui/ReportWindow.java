package com.example.tracker.ui;

import com.example.tracker.service.DateRangeFilter;
import com.example.tracker.service.ReportFilter;
import com.example.tracker.service.StorageManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ReportWindow extends JFrame {
    private JPanel chartContainer;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    public ReportWindow() {
        setTitle("Отчеты по категориям");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date defaultStartDate = cal.getTime();

        startDateSpinner = new JSpinner(new SpinnerDateModel(defaultStartDate, null, null, Calendar.DAY_OF_MONTH));
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy"));

        endDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "dd.MM.yyyy"));

        JButton buildBtn = new JButton("Построить");
        buildBtn.addActionListener(e -> buildChart());

        filterPanel.add(new JLabel("С:"));
        filterPanel.add(startDateSpinner);
        filterPanel.add(new JLabel("По:"));
        filterPanel.add(endDateSpinner);
        filterPanel.add(buildBtn);

        add(filterPanel, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout());
        add(chartContainer, BorderLayout.CENTER);

        buildChart();
    }

    private void buildChart() {
        chartContainer.removeAll();

        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59);

        ReportFilter filter = new DateRangeFilter(startCal.getTimeInMillis(), endCal.getTimeInMillis());

        Map<String, Integer> data = StorageManager.getInstance().getTimeByCategory(filter);

        if (data.isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет данных за выбранный период. Запустите таймер!", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            chartContainer.add(emptyLabel, BorderLayout.CENTER);
        } else {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int sec = entry.getValue();
                String label = sec >= 60 ? entry.getKey() + " (" + (sec / 60) + " мин)" : entry.getKey() + " (" + sec + " сек)";
                dataset.setValue(label, sec);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                    "Распределение времени (" + new SimpleDateFormat("dd.MM.yyyy").format(startDate) + " - " + new SimpleDateFormat("dd.MM.yyyy").format(endDate) + ")",
                    dataset,
                    true, true, false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartContainer.add(chartPanel, BorderLayout.CENTER);
        }

        chartContainer.revalidate();
        chartContainer.repaint();
    }
}