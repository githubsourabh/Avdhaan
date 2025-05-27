package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppUsageDao {

    @Insert
    void insert(com.avdhaan.db.AppUsage appUsage);

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC")
    List<com.avdhaan.db.AppUsage> getAllUsages();

    @Query("DELETE FROM app_usage")
    void clearAll();

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC LIMIT :limit")
    List<com.avdhaan.db.AppUsage> getRecentLogs(int limit);

}