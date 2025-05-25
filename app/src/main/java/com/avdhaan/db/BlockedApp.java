package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_apps")
public class BlockedApp {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String packageName;
    public int groupId; // FK for BlockedAppGroup
}