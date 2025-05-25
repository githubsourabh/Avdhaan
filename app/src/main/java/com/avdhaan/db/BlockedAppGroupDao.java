package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.avdhaan.db.BlockedAppGroup;

import java.util.List;

// DAO for BlockedAppGroup
@Dao
public interface BlockedAppGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroup(BlockedAppGroup group);

    @Query("SELECT * FROM app_groups")
    List<BlockedAppGroup> getAllGroups();

    @Query("SELECT * FROM app_groups WHERE groupId = :groupId")
    BlockedAppGroup getGroupById(int groupId);

    @Delete
    void deleteGroup(BlockedAppGroup group);
}

