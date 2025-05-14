package com.avdhaan;

public class FocusSchedule {
    private int dayOfWeek;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public FocusSchedule(int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

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

    @Override
    public String toString() {
        return String.format("Day %d: %02d:%02d - %02d:%02d",
                dayOfWeek, startHour, startMinute, endHour, endMinute);
    }
}
