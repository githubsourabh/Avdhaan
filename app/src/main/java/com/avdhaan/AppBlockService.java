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
    private boolean isServiceConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        blockedPrefs = getSharedPreferences(BLOCKED_PREFS_NAME, MODE_PRIVATE);
        Log.d(TAG, "Service created");
        loadBlockedApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Disable focus mode when service is destroyed
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_FOCUS_MODE, false).apply();
            Log.d(TAG, "Focus mode disabled in onDestroy");
        }
        isServiceConnected = false;
        Log.d(TAG, "Service destroyed");
    }

    private boolean isFocusModeOn() {
        boolean isOn = prefs.getBoolean(KEY_FOCUS_MODE, false);
        Log.d(TAG, "Focus mode is " + (isOn ? "ON" : "OFF"));
        return isOn;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        isServiceConnected = true;
        loadBlockedApps();
        Log.d(TAG, "Service connected. Blocked apps count: " + blockedApps.size());
        Log.d(TAG, "Focus mode state: " + (isFocusModeOn() ? "ON" : "OFF"));
        
        // Send broadcast to notify that service is connected
        Intent intent = new Intent("com.avdhaan.SERVICE_CONNECTED");
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceConnected = false;
        // Don't disable focus mode on unbind, as the service might be temporarily unbound
        Log.d(TAG, "Service unbound");
        return super.onUnbind(intent);
    }

    private void loadBlockedApps() {
        Set<String> savedSet = blockedPrefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        blockedApps = savedSet != null ? new HashSet<>(savedSet) : new HashSet<>();
        Log.d(TAG, "Loaded blocked apps: " + blockedApps);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isServiceConnected) {
            Log.d(TAG, "Service not connected, skipping event");
            return;
        }

        if (!isFocusModeOn()) {
            Log.d(TAG, "Focus mode is off, skipping event");
            return;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        
        String packageName = String.valueOf(event.getPackageName());
        Log.d(TAG, "Checking package: " + packageName);

        // Skip if it's our own app
        if (packageName.equals(getPackageName())) {
            Log.d(TAG, "Skipping our own app: " + packageName);
            return;
        }

        if (!blockedApps.contains(packageName)) {
            Log.d(TAG, "Package not in blocked list: " + packageName);
            return;
        }

        if (!isWithinFocusTime()) {
            Log.d(TAG, "Not within focus time for package: " + packageName);
            return;
        }

        if (BlockScreenActivity.isShowing) {
            Log.d(TAG, "Block screen already showing");
            return;
        }

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
        Log.d(TAG, "Checking schedules. Current time: " + currentHour + ":" + currentMinute + 
              ", Day: " + currentDay + ", Total schedules: " + schedules.size());

        for (FocusSchedule schedule : schedules) {
            if (schedule.dayOfWeek != currentDay) {
                Log.d(TAG, "Schedule day " + schedule.dayOfWeek + " doesn't match current day " + currentDay);
                continue;
            }

            int start = schedule.startHour * 60 + schedule.startMinute;
            int end = schedule.endHour * 60 + schedule.endMinute;

            Log.d(TAG, "Checking schedule: " + schedule.startHour + ":" + schedule.startMinute + 
                  " to " + schedule.endHour + ":" + schedule.endMinute);

            if (nowMinutes >= start && nowMinutes <= end) {
                Log.d(TAG, "Within schedule time");
                return true;
            }
        }
        Log.d(TAG, "Not within any schedule time");
        return false;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }
}
