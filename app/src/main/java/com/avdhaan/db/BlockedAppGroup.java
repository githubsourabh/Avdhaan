package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_groups")
public class BlockedAppGroup {
    @PrimaryKey(autoGenerate = true)
    public int groupId;
    public String groupName;
    public boolean isDefault;  // for migration purposes, only one default
}
