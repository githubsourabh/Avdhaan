package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {
    private static final String TAG = "ScheduleStorage";
    private static final String PREFS_NAME = "SchedulePrefs";
    private static final String KEY_SCHEDULES = "schedules";

    public static void saveSchedules(Context context, List<FocusSchedule> schedules) {
        Log.d(TAG, "Saving " + schedules.size() + " schedules");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(schedules);
        Log.d(TAG, "Serialized schedules: " + json);
        prefs.edit().putString(KEY_SCHEDULES, json).apply();
        Log.d(TAG, "Schedules saved successfully");
    }

    public static List<FocusSchedule> loadSchedules(Context context) {
        Log.d(TAG, "Loading schedules");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SCHEDULES, null);
        Log.d(TAG, "Loaded JSON: " + json);
        
        if (json == null) {
            Log.d(TAG, "No schedules found, returning empty list");
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<FocusSchedule>>(){}.getType();
        List<FocusSchedule> schedules = gson.fromJson(json, type);
        Log.d(TAG, "Loaded " + schedules.size() + " schedules");
        return schedules;
    }
}
