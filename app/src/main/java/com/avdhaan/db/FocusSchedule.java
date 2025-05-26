package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.io.Serializable;

@Entity(tableName = "focus_schedules")
public class FocusSchedule implements Serializable {
    private static final int MINUTES_PER_HOUR = 60;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int groupId; // FK for BlockedAppGroup
    private int dayOfWeek; // 1 (Sunday) to 7 (Saturday)
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    // Required no-args constructor for Room
    public FocusSchedule() {
    }

    // Convenience constructor
    @Ignore
    public FocusSchedule(int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    // Time calculation methods
    public boolean isTimeInRange(int hour, int minute) {
        int currentTotal = convertToMinutes(hour, minute);
        int startTotal = convertToMinutes(startHour, startMinute);
        int endTotal = convertToMinutes(endHour, endMinute);
        return currentTotal >= startTotal && currentTotal <= endTotal;
    }

    private static int convertToMinutes(int hours, int minutes) {
        return hours * MINUTES_PER_HOUR + minutes;
    }

    public boolean matches(int day, int hour, int minute) {
        if (day != dayOfWeek) return false;
        return isTimeInRange(hour, minute);
    }
}
