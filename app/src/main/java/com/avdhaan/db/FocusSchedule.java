package com.avdhaan.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_schedules")
public class FocusSchedule {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int groupId; // FK for BlockedAppGroup
    public int dayOfWeek; // 1 (Sunday) to 7 (Saturday)
    public String startTime; // Format "HH:mm"
    public String endTime; // Format "HH:mm"
}
