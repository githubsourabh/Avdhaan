package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppUsageDao {

    @Insert
    void insert(AppUsage appUsage);

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC")
    List<AppUsage> getAllUsages();

    @Query("DELETE FROM app_usage")
    void clearAll();

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC LIMIT :limit")
    List<AppUsage> getRecentLogs(int limit);

    @Query("SELECT packageName, " +
           "SUM(usageTimeMillis) AS totalUsage, " +
           "SUM(openAttempts) AS totalAttempts " +
           "FROM app_usage " +
           "WHERE timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY packageName " +
           "ORDER BY totalUsage DESC")
    List<AppUsageSummary> getUsageSummary(long startTime, long endTime);

    @Query("SELECT packageName, " +
           "SUM(usageTimeMillis) AS totalUsage, " +
           "SUM(openAttempts) AS totalAttempts " +
           "FROM app_usage " +
           "WHERE timestamp BETWEEN :startTime AND :endTime " +
           "AND isBlocked = 1 " +
           "AND duringFocus = 1 " +
           "GROUP BY packageName " +
           "ORDER BY totalUsage DESC")
    List<AppUsageSummary> getBlockedFocusUsageSummary(long startTime, long endTime);
}