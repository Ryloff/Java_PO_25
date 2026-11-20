package com.example.tracker.service;

public class AnalyticsService {

    public String getAdvice() {
        String[] advices = {
                "Вы часто прерываетесь на соцсети. Попробуйте технику Pomodoro (25 минут работы).",
                "Отличная работа! Вы сохраняете высокую концентрацию.",
                "Вы слишком долго работаете без перерыва. Сделайте 5-минутную паузу.",
                "Запланируйте более мелкие задачи, чтобы чаще их завершать."
        };
        int index = (int) (Math.random() * advices.length);
        return advices[index];
    }
}
