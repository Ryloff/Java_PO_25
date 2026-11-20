package com.example.tracker.service;

import com.example.tracker.model.Task;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class TimerService {
    private final List<TimerObserver> observers = new ArrayList<>();
    private Timer timer;
    private int secondsPassed = 0;
    private Task currentTask;
    private long sessionStartTime;

    public void addObserver(TimerObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (TimerObserver observer : observers) {
            observer.onTick(secondsPassed);
        }
    }

    public void start(Task task) {
        if (timer != null && timer.isRunning()) return;

        this.currentTask = task;
        this.sessionStartTime = System.currentTimeMillis();

        // Таймер срабатывает каждую секунду (1000 мс)
        timer = new Timer(1000, e -> {
            secondsPassed++;
            notifyObservers();
        });
        timer.start();
    }

    public void pause() {
        if (timer != null) timer.stop();
    }

    public void resume() {
        if (timer != null && !timer.isRunning()) timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            if (currentTask != null) {
                StorageManager.getInstance().saveSession(
                        currentTask.getId(),
                        sessionStartTime,
                        System.currentTimeMillis(),
                        "Завершено вручную"
                );
            }
            secondsPassed = 0;
            notifyObservers();
        }
    }

    public String getFormattedTime() {
        int hours = secondsPassed / 3600;
        int minutes = (secondsPassed % 3600) / 60;
        int secs = secondsPassed % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
