package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppBlockService extends AccessibilityService {

    private Set<String> blockedApps = new HashSet<>();
    private String lastBlockedPackage = "";
    private long lastBlockTimestamp = 0;
    private static final long BLOCK_THRESHOLD_MS = 1500;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadBlockedApps();
        Log.d("AppBlockService", "Service connected");
    }

    private void loadBlockedApps() {
        SharedPreferences prefs = getSharedPreferences("BlockedPrefs", MODE_PRIVATE);
        blockedApps = prefs.getStringSet("blockedApps", new HashSet<>());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        String packageName = String.valueOf(event.getPackageName());
        if (!blockedApps.contains(packageName)) return;
        if (!isWithinFocusTime()) return;

        long now = System.currentTimeMillis();

        // Avoid repeated blocking of the same app within short time
        if (packageName.equals(lastBlockedPackage) && now - lastBlockTimestamp < BLOCK_THRESHOLD_MS) {
            return;
        }

        // Android 10+ fix using ActivityManager (replaces BlockScreenActivity static flag)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am != null && !am.getRunningTasks(1).isEmpty()) {
                ActivityManager.RunningTaskInfo taskInfo = am.getRunningTasks(1).get(0);
                if (taskInfo.topActivity != null &&
                        taskInfo.topActivity.getClassName().equals(BlockScreenActivity.class.getName())) {
                    return;
                }
            }
        }

        Log.d("AppBlockService", "Blocking app: " + packageName);

        lastBlockedPackage = packageName;
        lastBlockTimestamp = now;

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
            if (schedule.getDayOfWeek() != currentDay) continue;

            int start = schedule.getStartHour() * 60 + schedule.getStartMinute();
            int end = schedule.getEndHour() * 60 + schedule.getEndMinute();

            if (nowMinutes >= start && nowMinutes <= end) return true;
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        Log.d("AppBlockService", "Service Interrupted");
    }
}
