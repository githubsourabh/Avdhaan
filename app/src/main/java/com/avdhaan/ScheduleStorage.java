package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {
    private static final String PREF_NAME = "FocusPrefs";
    private static final String KEY_SCHEDULES = "FocusSchedules";

    public static void saveSchedules(Context context, List<FocusSchedule> schedules) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(schedules);
        editor.putString(KEY_SCHEDULES, json);
        editor.apply();
    }

    public static List<FocusSchedule> loadSchedules(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SCHEDULES, null);
        if (json == null) return new ArrayList<>();

        Gson gson = new Gson();
        Type listType = new TypeToken<List<FocusSchedule>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}
