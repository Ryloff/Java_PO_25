package com.example.tracker;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Запускаем графический интерфейс в отдельном потоке (стандарт для Swing)
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        // Создаем главное окно
        JFrame frame = new JFrame("Личный тайм-трекер");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Размещаем по центру экрана

        // Создаем простую панель с текстом, чтобы проверить работу
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel helloLabel = new JLabel("Приложение успешно запущено!", SwingConstants.CENTER);
        helloLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton startButton = new JButton("Создать задачу (Заглушка)");
        startButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Кнопка работает!"));

        panel.add(helloLabel, BorderLayout.CENTER);
        panel.add(startButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }
}