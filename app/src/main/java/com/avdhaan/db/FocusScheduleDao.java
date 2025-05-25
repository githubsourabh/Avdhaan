package com.avdhaan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.avdhaan.db.FocusSchedule;

import java.util.List;

// DAO for FocusSchedule
@Dao
public interface FocusScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSchedule(FocusSchedule schedule);

    @Query("SELECT * FROM focus_schedules WHERE groupId = :groupId")
    List<FocusSchedule> getSchedulesForGroup(int groupId);

    @Query("DELETE FROM focus_schedules WHERE id = :scheduleId")
    void deleteScheduleById(int scheduleId);

    @Query("DELETE FROM focus_schedules")
    void deleteAll();
}