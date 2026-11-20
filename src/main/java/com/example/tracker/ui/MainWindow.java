package com.example.tracker.ui;

import com.example.tracker.model.Category;
import com.example.tracker.model.Task;
import com.example.tracker.service.AnalyticsService;
import com.example.tracker.service.StorageManager;
import com.example.tracker.service.TimerObserver;
import com.example.tracker.service.TimerService;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame implements TimerObserver {
    private final TimerService timerService;
    private final StorageManager storage;
    private final AnalyticsService analyticsService;
    private JLabel timeLabel;
    private JComboBox<Task> taskComboBox;
    private Task currentTask;

    public MainWindow() {
        this.timerService = new TimerService();
        this.storage = StorageManager.getInstance();
        this.analyticsService = new AnalyticsService();

        timerService.addObserver(this);
        new TrayWidget(timerService);

        initUI();
    }

    private void initUI() {
        setTitle("Личный тайм-трекер");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        taskComboBox = new JComboBox<>();
        loadTasksToComboBox();

        JButton createBtn = new JButton("Создать задачу");
        createBtn.addActionListener(e -> createTaskDialog());

        JButton reportBtn = new JButton("Отчеты");
        reportBtn.addActionListener(e -> new ReportWindow().setVisible(true));

        topPanel.add(new JLabel("Задача:"));
        topPanel.add(taskComboBox);
        topPanel.add(createBtn);
        topPanel.add(reportBtn);
        add(topPanel, BorderLayout.NORTH);

        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        add(timeLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton startBtn = new JButton("Старт");
        JButton pauseBtn = new JButton("Пауза (с причиной)");
        JButton stopBtn = new JButton("Стоп и совет");

        startBtn.addActionListener(e -> {
            currentTask = (Task) taskComboBox.getSelectedItem();
            if (currentTask != null) {
                timerService.start(currentTask);
            } else {
                JOptionPane.showMessageDialog(this, "Сначала создайте задачу!");
            }
        });

        pauseBtn.addActionListener(e -> {
            timerService.pause();
            String reason = JOptionPane.showInputDialog(this, "На что вы отвлеклись (причина прерывания)?");
            if (reason == null) reason = "Без причины";
            // Здесь мы бы сохранили причину в БД для дальнейшей аналитики
            System.out.println("Сохранено прерывание: " + reason);
        });

        stopBtn.addActionListener(e -> {
            timerService.stop();
            String advice = analyticsService.getAdvice();
            JOptionPane.showMessageDialog(this,
                    "Задача завершена!\n\nСовет от аналитика:\n" + advice,
                    "Аналитика и Советы",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        bottomPanel.add(startBtn);
        bottomPanel.add(pauseBtn);
        bottomPanel.add(stopBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadTasksToComboBox() {
        taskComboBox.removeAllItems();
        for (Task task : storage.loadTasks()) {
            taskComboBox.addItem(task);
        }
    }

    private void createTaskDialog() {
        java.util.List<Category> categories = storage.loadCategories();

        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ошибка: нет доступных категорий!");
            return;
        }

        // Создаем поля для ввода
        JTextField nameField = new JTextField();
        JComboBox<Category> categoryBox = new JComboBox<>(categories.toArray(new Category[0]));
        JTextField estimatedField = new JTextField("60");

        Object[] message = {
                "Название задачи:", nameField,
                "Категория:", categoryBox,
                "Оценка времени (в минутах):", estimatedField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Создание новой задачи",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Название задачи не может быть пустым!");
                return;
            }

            Category selectedCategory = (Category) categoryBox.getSelectedItem();

            int estimatedMinutes = 60;
            try {
                estimatedMinutes = Integer.parseInt(estimatedField.getText().trim());
            } catch (NumberFormatException ex) {
            }

            Task newTask = new Task.Builder(name)
                    .description("Без описания")
                    .category(selectedCategory)
                    .estimatedMinutes(estimatedMinutes)
                    .build();

            storage.saveTask(newTask);
            loadTasksToComboBox();
        }
    }

    @Override
    public void onTick(int secondsPassed) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText(timerService.getFormattedTime());
        });
    }
}