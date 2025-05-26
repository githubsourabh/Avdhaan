package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_apps")
public class BlockedApp {
    public static final int DEFAULT_GROUP_ID = 0;  // Default group ID for all apps until grouping is implemented

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String packageName;
    private int groupId; // FK for BlockedAppGroup

    // Required no-args constructor for Room
    public BlockedApp() {
        this.groupId = DEFAULT_GROUP_ID;
    }

    // Convenience constructor
    @Ignore
    public BlockedApp(String packageName) {
        this.packageName = packageName;
        this.groupId = DEFAULT_GROUP_ID;
    }

    @Ignore
    public BlockedApp(String packageName, int groupId) {
        this.packageName = packageName;
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}