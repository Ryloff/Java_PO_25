package com.example.tracker.service;

public class DateRangeFilter implements ReportFilter {
    private final long startTime;
    private final long endTime;

    public DateRangeFilter(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public long getStartTime() { return startTime; }

    @Override
    public long getEndTime() { return endTime; }
}
