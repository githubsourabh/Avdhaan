package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.avdhaan.db.BlockedApp;

import java.util.List;

// DAO for BlockedApp
@Dao
public interface BlockedAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBlockedApp(BlockedApp blockedApp);

    @Query("SELECT * FROM blocked_apps WHERE groupId = :groupId")
    List<BlockedApp> getBlockedAppsByGroup(int groupId);

    @Query("SELECT * FROM blocked_apps")
    List<BlockedApp> getAllBlockedApps();

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    void deleteByPackageName(String packageName);

    @Query("DELETE FROM blocked_apps")
    void deleteAll();

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName)")
    boolean isBlocked(String packageName);
}
