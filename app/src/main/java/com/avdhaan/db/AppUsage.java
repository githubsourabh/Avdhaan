package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_usage")
public class AppUsage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String packageName;
    public long usageTimeMillis;
    public long timestamp;
    public boolean duringFocus;
    public int openAttempts;
    public boolean isBlocked;
    public boolean isInSchedule;

    public AppUsage(String packageName, long usageTimeMillis, long timestamp, 
                   boolean duringFocus, int openAttempts, boolean isBlocked, boolean isInSchedule) {
        this.packageName = packageName;
        this.usageTimeMillis = usageTimeMillis;
        this.timestamp = timestamp;
        this.duringFocus = duringFocus;
        this.openAttempts = openAttempts;
        this.isBlocked = isBlocked;
        this.isInSchedule = isInSchedule;
    }
}