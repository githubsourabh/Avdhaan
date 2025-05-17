package com.avdhaan.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

@Entity(tableName = "app_usage_summary")
public class AppUsageSummary {

    @NonNull
    @ColumnInfo(name = "packageName")
    public String packageName;

    @ColumnInfo(name = "totalDuration")
    public long totalDuration;

    @ColumnInfo(name = "startOfPeriod")
    public String startOfPeriod;

    public AppUsageSummary(@NonNull String packageName, long totalDuration, String startOfPeriod) {
        this.packageName = packageName;
        this.totalDuration = totalDuration;
        this.startOfPeriod = startOfPeriod;
    }
}
