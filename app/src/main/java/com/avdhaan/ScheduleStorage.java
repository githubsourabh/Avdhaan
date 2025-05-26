package com.avdhaan;

import android.content.Context;
import android.util.Log;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.FocusSchedule;
import com.avdhaan.db.FocusScheduleDao;

import java.util.List;

public class ScheduleStorage {
    private static final String TAG = "ScheduleStorage";

    public static void saveSchedules(Context context, List<FocusSchedule> schedules) {
        Log.d(TAG, "Saving " + schedules.size() + " schedules");

        AppDatabase db = AppDatabase.getInstance(context);
        FocusScheduleDao dao = db.focusScheduleDao();

        // Clear existing schedules and insert new ones
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dao.deleteAll();
            for (FocusSchedule schedule : schedules) {
                dao.insertSchedule(schedule);
            }
        });
        Log.d(TAG, "Schedules saved successfully");
    }

    public static List<FocusSchedule> loadSchedules(Context context) {
        Log.d(TAG, "Loading schedules");

        AppDatabase db = AppDatabase.getInstance(context);
        FocusScheduleDao dao = db.focusScheduleDao();

        // Room handles the threading for us in this case
        List<FocusSchedule> schedules = dao.getAllSchedules();
        Log.d(TAG, "Loaded " + schedules.size() + " schedules");
        return schedules;
    }
}