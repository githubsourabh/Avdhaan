package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_usage")
public class AppUsage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String packageName;

    public long duration; // Renamed from usageTimeMillis to match the DAO queries

    public long timestamp;

    public AppUsage(String packageName, long duration, long timestamp) {
        this.packageName = packageName;
        this.duration = duration;
        this.timestamp = timestamp;
    }
}