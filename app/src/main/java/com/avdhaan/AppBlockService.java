package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppDao;
import com.avdhaan.db.FocusSchedule;
import com.avdhaan.db.FocusScheduleDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        blockedAppDao = db.blockedAppDao();
        scheduleDao = db.focusScheduleDao();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        final CharSequence packageName = event.getPackageName();
        if (packageName == null) return;

        executorService.execute(() -> {
            try {
                List<BlockedApp> blockedApps = blockedAppDao.getAllBlockedApps();
                boolean isBlocked = false;
                for (BlockedApp app : blockedApps) {
                    if (packageName.toString().equals(app.getPackageName())) {
                        isBlocked = true;
                        break;
                    }
                }

                if (isBlocked && isWithinFocusTime()) {
                    Log.d(TAG, "Blocked app launched during focus time: " + packageName);
                    Intent intent = new Intent(this, BlockScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
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