package com.example.tracker.ui;

import com.example.tracker.service.TimerObserver;
import com.example.tracker.service.TimerService;

import java.awt.*;

public class TrayWidget implements TimerObserver {
    private TrayIcon trayIcon;
    private final TimerService timerService;

    public TrayWidget(TimerService timerService) {
        this.timerService = timerService;

        if (!SystemTray.isSupported()) {
            System.out.println("Системный трей не поддерживается!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);

        trayIcon = new TrayIcon(image, "Тайм-трекер: Ожидание");
        trayIcon.setImageAutoSize(true);

        // Создаем меню для иконки
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));

        MenuItem openItem = new MenuItem("Открыть");
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Не удалось добавить иконку в трей.");
        }
    }

    @Override
    public void onTick(int secondsPassed) {
        if (trayIcon != null) {
            trayIcon.setToolTip("Время: " + timerService.getFormattedTime());
        }
    }
}
