package com.avdhaan;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {
    private static final String TAG = "ScheduleStorage";
    private static final String FILE_NAME = "focus_schedules.dat";
    private static volatile List<FocusSchedule> cachedSchedules = null;

    public static synchronized void saveSchedules(Context context, List<FocusSchedule> schedules) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(new ArrayList<>(schedules)); // Create defensive copy
            cachedSchedules = new ArrayList<>(schedules);
        } catch (IOException e) {
            Log.e(TAG, "Error saving schedules", e);
            clearCache(); // Invalidate cache on error
        }
    }

    public static synchronized List<FocusSchedule> loadSchedules(Context context) {
        if (cachedSchedules != null) {
            return new ArrayList<>(cachedSchedules);
        }

        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            cachedSchedules = new ArrayList<>();
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            @SuppressWarnings("unchecked")
            List<FocusSchedule> schedules = (List<FocusSchedule>) in.readObject();
            cachedSchedules = new ArrayList<>(schedules);
            return new ArrayList<>(schedules);
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading schedules", e);
            clearCache(); // Invalidate cache on error
            return new ArrayList<>();
        }
    }

    public static synchronized void clearCache() {
        cachedSchedules = null;
    }
}
