package com.android.winifystats.model;

/**
 * Created by izaya_orihara on 7/17/17.
 */

public class StatisticsDTO {

    public long daily;
    public long weekly;
    public long monthly;
    public long workedDays;
    public long timeToWork;

    public long getDaily() {
        return daily;
    }

    public long getWeekly() {
        return weekly;
    }

    public long getMonthly() {
        return monthly;
    }

    public long getWorkedDays() {
        return workedDays;
    }

    public long getTimeToWork() {
        return timeToWork;
    }
}
