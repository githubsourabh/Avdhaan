package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppBlockService extends AccessibilityService {

    private static final String TAG = "AppBlockService";
    private static final String PREFS_NAME = "FocusPrefs";
    private static final String BLOCKED_PREFS_NAME = "BlockedPrefs";
    private static final String KEY_FOCUS_MODE = "focusEnabled";
    private static final String KEY_BLOCKED_APPS = "blockedApps";

    private Set<String> blockedApps = new HashSet<>();
    private SharedPreferences prefs;
    private SharedPreferences blockedPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        blockedPrefs = getSharedPreferences(BLOCKED_PREFS_NAME, MODE_PRIVATE);
    }

    private boolean isFocusModeOn() {
        return prefs.getBoolean(KEY_FOCUS_MODE, false);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadBlockedApps();
        Log.d(TAG, "Service connected");
    }

    private void loadBlockedApps() {
        Set<String> savedSet = blockedPrefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        blockedApps = savedSet != null ? new HashSet<>(savedSet) : new HashSet<>();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isFocusModeOn()) return;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        
        String packageName = String.valueOf(event.getPackageName());
        if (!blockedApps.contains(packageName)) return;

        if (!isWithinFocusTime()) return;

        if (BlockScreenActivity.isShowing) return;

        Log.d(TAG, "Blocking app: " + packageName);

        Intent intent = new Intent(this, BlockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isWithinFocusTime() {
        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_WEEK);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int nowMinutes = currentHour * 60 + currentMinute;

        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        for (FocusSchedule schedule : schedules) {
            if (schedule.dayOfWeek != currentDay) continue;

            int start = schedule.startHour * 60 + schedule.startMinute;
            int end = schedule.endHour * 60 + schedule.endMinute;

            if (nowMinutes >= start && nowMinutes <= end) return true;
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service Interrupted");
    }
}
