
package com.avdhaan;

import java.io.Serializable;

public class FocusSchedule implements Serializable {
    public int dayOfWeek;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public FocusSchedule(int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public boolean matches(int day, int hour, int minute) {
        if (day != dayOfWeek) return false;
        int startTotal = startHour * 60 + startMinute;
        int endTotal = endHour * 60 + endMinute;
        int currentTotal = hour * 60 + minute;
        return currentTotal >= startTotal && currentTotal <= endTotal;
    }
}
