package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;

import java.util.List;

@Dao
public interface AppUsageDao {

    @Insert
    void insert(AppUsage appUsage);

    @Query("SELECT * FROM app_usage ORDER BY duration DESC")
    List<AppUsage> getAll();

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT packageName, SUM(duration) as totalDuration, NULL as startOfPeriod " +
            "FROM app_usage GROUP BY packageName ORDER BY totalDuration DESC")
    List<AppUsageSummary> getUsageSummary();

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT packageName, SUM(duration) as totalDuration, NULL as startOfPeriod " +
            "FROM app_usage GROUP BY packageName ORDER BY totalDuration DESC")
    List<AppUsageSummary> getTotalUsageSummary();

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT packageName, SUM(duration) as totalDuration, " +
            "date(timestamp / 1000, 'unixepoch') as startOfPeriod " +
            "FROM app_usage GROUP BY packageName, startOfPeriod ORDER BY startOfPeriod DESC")
    List<AppUsageSummary> getDailyUsageSummary();
}
