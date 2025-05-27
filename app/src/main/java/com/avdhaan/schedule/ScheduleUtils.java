package com.avdhaan.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppGroup;
import com.avdhaan.db.FocusSchedule;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.avdhaan.PreferenceConstants.PREF_NAME;
import static com.avdhaan.PreferenceConstants.KEY_FOCUS_MODE;

public class ScheduleUtils {
    private static final String TAG = "ScheduleUtils";

    public static boolean isWithinScheduledFocusPeriod(Context context, String packageName) {
        try {
            // First check if Focus Mode is enabled
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            boolean isFocusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
            if (!isFocusMode) {
                return false; // Focus Mode is not enabled
            }

            // Get current time
            Calendar now = Calendar.getInstance();
            int currentDay = now.get(Calendar.DAY_OF_WEEK);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            int currentTimeInMinutes = currentHour * 60 + currentMinute;

            // Get database instance
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Get all blocked apps
            List<BlockedApp> blockedApps = db.blockedAppDao().getAllBlockedApps();
            
            // Find which group this app belongs to
            int groupId = -1;
            for (BlockedApp app : blockedApps) {
                if (app.getPackageName().equals(packageName)) {
                    groupId = app.getGroupId();
                    break;
                }
            }
            
            if (groupId == -1) {
                return false; // App is not in any blocked group
            }

            // Get all schedules for this group
            List<FocusSchedule> schedules = db.focusScheduleDao().getSchedulesForGroup(groupId);
            
            // Check if any schedule is active
            for (FocusSchedule schedule : schedules) {
                if (schedule.getDayOfWeek() == currentDay) {
                    int startTimeInMinutes = schedule.getStartHour() * 60 + schedule.getStartMinute();
                    int endTimeInMinutes = schedule.getEndHour() * 60 + schedule.getEndMinute();
                    
                    if (currentTimeInMinutes >= startTimeInMinutes && 
                        currentTimeInMinutes <= endTimeInMinutes) {
                        return true; // Found an active schedule
                    }
                }
            }
            
            return false; // No active schedule found
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking schedule for " + packageName, e);
            return false;
        }
    }
} 