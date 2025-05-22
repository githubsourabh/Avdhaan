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

import static com.avdhaan.PreferenceConstants.*;

public class AppBlockService extends AccessibilityService {

    private static final String TAG = "AppBlockService";
    private static final String BLOCKED_PREFS_NAME = "BlockedPrefs";

    private Set<String> blockedApps = new HashSet<>();
    private SharedPreferences prefs;
    private SharedPreferences blockedPrefs;
    private boolean isServiceConnected = false;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeService();
    }

    private void initializeService() {
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        blockedPrefs = getSharedPreferences(BLOCKED_PREFS_NAME, MODE_PRIVATE);
        
        prefsListener = (sharedPreferences, key) -> {
            if (KEY_BLOCKED_APPS.equals(key)) {
                loadBlockedApps();
            }
        };
        blockedPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        
        loadBlockedApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (blockedPrefs != null && prefsListener != null) {
            blockedPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
        isServiceConnected = false;
    }

    private boolean isFocusModeOn() {
        return prefs.getBoolean(KEY_FOCUS_MODE, false);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        isServiceConnected = true;
        initializeService();
        
        Intent intent = new Intent("com.avdhaan.SERVICE_CONNECTED");
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceConnected = false;
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
            Log.d(TAG, "Not a window state change event, skipping");
            return;
        }
        
        String packageName = String.valueOf(event.getPackageName());
        Log.d(TAG, "Checking package: " + packageName + ", Blocked apps: " + blockedApps);

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
        // Implementation needed
    }
}
