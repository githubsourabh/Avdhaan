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

    public AppUsage(String packageName, long usageTimeMillis, long timestamp) {
        this.packageName = packageName;
        this.usageTimeMillis = usageTimeMillis;
        this.timestamp = timestamp;
    }
}
