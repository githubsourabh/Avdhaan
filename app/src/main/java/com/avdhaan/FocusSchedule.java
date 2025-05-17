package com.avdhaan;

import java.io.Serializable;

public class FocusSchedule implements Serializable {
    public int dayOfWeek;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;

    private static final int MINUTES_PER_HOUR = 60;

    public FocusSchedule(int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public boolean matches(int day, int hour, int minute) {
        if (day != dayOfWeek) return false;
        return isTimeInRange(hour, minute);
    }

    public boolean isTimeInRange(int hour, int minute) {
        int currentTotal = convertToMinutes(hour, minute);
        int startTotal = convertToMinutes(startHour, startMinute);
        int endTotal = convertToMinutes(endHour, endMinute);
        return currentTotal >= startTotal && currentTotal <= endTotal;
    }

    private static int convertToMinutes(int hours, int minutes) {
        return hours * MINUTES_PER_HOUR + minutes;
    }
}
