package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.AppUsage;
import com.avdhaan.db.BlockedAppDao;
import com.avdhaan.db.FocusSchedule;
import com.avdhaan.db.FocusScheduleDao;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppBlockService extends AccessibilityService {

    private static final String TAG = "AppBlockService";
    private ExecutorService executorService;
    private BlockedAppDao blockedAppDao;
    private FocusScheduleDao scheduleDao;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getInstance(this);
        blockedAppDao = db.blockedAppDao();
        scheduleDao = db.focusScheduleDao();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // First check if Focus Mode is ON
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREF_NAME, MODE_PRIVATE);
        boolean isFocusModeOn = prefs.getBoolean(PreferenceConstants.KEY_FOCUS_MODE, false);
        
        // If Focus Mode is OFF, don't do anything
        if (!isFocusModeOn) {
            Log.d(TAG, "Focus mode is off, skipping event");
            return;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        CharSequence packageNameCS = event.getPackageName();
        if (packageNameCS == null) return;
        String packageName = packageNameCS.toString();

        executorService.execute(() -> {
            try {
                boolean isBlocked = blockedAppDao.getBlockedApp(packageName) != null;
                Log.d(TAG, "Package checked: " + packageName + " | isBlocked: " + isBlocked);

                if (isBlocked && isWithinFocusTime()) {
                    Log.d(TAG, "Blocked app launched during focus time: " + packageName);

                    // Launch block screen
                    Intent intent = new Intent(this, BlockScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Log blocked attempt
                    AppUsage entry = new AppUsage(
                            packageName,
                            0, // usageTimeMillis
                            System.currentTimeMillis(),
                            true, // duringFocus
                            1,    // openAttempts
                            true, // isBlocked
                            true  // isInSchedule
                    );

                    AppDatabase.getInstance(getApplicationContext()).appUsageDao().insert(entry);
                    Log.d(TAG, "Blocked usage attempt logged for: " + packageName);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in accessibility event handling", e);
            }
        });
    }

    private boolean isWithinFocusTime() {
        try {
            List<FocusSchedule> schedules = scheduleDao.getAllSchedules();
            Calendar now = Calendar.getInstance();
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);

            for (FocusSchedule schedule : schedules) {
                if (schedule.matches(dayOfWeek, currentHour, currentMinute)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking focus time", e);
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}