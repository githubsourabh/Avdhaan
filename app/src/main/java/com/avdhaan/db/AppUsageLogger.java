package com.avdhaan.db;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.app.AppOpsManager;
import com.avdhaan.UsageTrackingPreferences;
import com.avdhaan.schedule.ScheduleUtils;

//import com.avdhaan.schedule.ScheduleUtils;

//import com.avdhaan.schedule.ScheduleUtils;

//import com.avdhaan.schedule.ScheduleUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.avdhaan.PreferenceConstants.KEY_FOCUS_MODE;
import static com.avdhaan.PreferenceConstants.PREF_NAME;

public class AppUsageLogger {

    private static final String TAG = "AppUsageLogger";
    private static final long MIN_USAGE_THRESHOLD_MS = 1000;
    private final ExecutorService executor;
    private volatile boolean isShutdown = false;

    private final Context context;
    private final PackageManager packageManager;
    private final Set<String> systemApps;
    private final Map<String, Boolean> appCache;
    private final Map<String, Long> lastLoggedTime;

    public AppUsageLogger(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.systemApps = new HashSet<>();
        this.appCache = new HashMap<>();
        this.lastLoggedTime = new HashMap<>();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void logUsage() {
        if (isShutdown) {
            Log.w(TAG, "Logger is shutdown, ignoring request");
            return;
        }

        executor.execute(() -> {
            try {
                logUsageInternal();
            } catch (Exception e) {
                Log.e(TAG, "Error logging usage", e);
            }
        });
    }

    private void logUsageInternal() {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            Log.e(TAG, "UsageStatsManager is null");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -1);  // last 1 hour
        long startTime = calendar.getTimeInMillis();

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        if (events == null) {
            Log.e(TAG, "Failed to query usage events");
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isFocusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
        Set<String> blockedApps = prefs.getStringSet("blocked_apps", new HashSet<>());

        UsageEvents.Event event = new UsageEvents.Event();
        AppDatabase db = AppDatabase.getInstance(context);
        Map<String, Long> foregroundTimestamps = new HashMap<>();
        Map<String, Integer> appAttempts = new HashMap<>();

        while (events.hasNextEvent() && !isShutdown) {
            events.getNextEvent(event);

            String packageName = event.getPackageName();
            if (packageName == null || packageName.equals(context.getPackageName())) continue;

            long eventTime = event.getTimeStamp();
            int eventType = event.getEventType();

            switch (eventType) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    foregroundTimestamps.put(packageName, eventTime);
                    appAttempts.put(packageName, appAttempts.getOrDefault(packageName, 0) + 1);
                    break;

                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    Long start = foregroundTimestamps.get(packageName);
                    if (start != null) {
                        int attempts = appAttempts.getOrDefault(packageName, 1);
                        boolean isBlocked = blockedApps.contains(packageName);
                        boolean isInSchedule = ScheduleUtils.isWithinScheduledFocusPeriod(context, packageName);
                        processUsageEvent(packageName, start, eventTime, db, isFocusMode, attempts, isBlocked, isInSchedule);
                        foregroundTimestamps.remove(packageName);
                        appAttempts.remove(packageName);
                    }
                    break;
            }
        }
    }

    private void processUsageEvent(String packageName, long startTime, long endTime,
                                   AppDatabase db, boolean isFocusMode, int openAttempts,
                                   boolean isBlocked, boolean isInSchedule) {

        long usageDuration = endTime - startTime;
        Long lastLogged = lastLoggedTime.get(packageName);

        if (usageDuration >= MIN_USAGE_THRESHOLD_MS &&
                (lastLogged == null || (endTime - lastLogged) >= MIN_USAGE_THRESHOLD_MS)) {

            try {
                if (shouldTrackApp(packageName)) {
                    AppUsage usage = new AppUsage(
                            packageName,
                            usageDuration,
                            endTime,
                            isFocusMode,
                            openAttempts,
                            isBlocked,
                            isInSchedule
                    );
                    db.appUsageDao().insert(usage);
                    lastLoggedTime.put(packageName, endTime);
                    Log.d(TAG, "Inserted usage for " + packageName + ": " + usageDuration +
                            "ms, focus=" + isFocusMode + ", attempts=" + openAttempts +
                            ", blocked=" + isBlocked + ", scheduled=" + isInSchedule);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting usage for " + packageName, e);
            }
        }
    }

    private boolean shouldTrackApp(String packageName) {
        Boolean cached = appCache.get(packageName);
        if (cached != null) return cached;

        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

            // Track the app if any of these conditions are true:
            // 1. It's not a system app
            // 2. It's a system app but has been updated (user version installed)
            // 3. It has a launcher activity (user can open it)
            // 4. It's categorized as a game
            boolean shouldTrack = true;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                boolean isUpdatedSystemApp = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
                boolean hasLauncherActivity = packageManager.getLaunchIntentForPackage(packageName) != null;
                boolean isGame = (appInfo.category == ApplicationInfo.CATEGORY_GAME);

                // Only filter out system apps that are:
                // - Not updated by user
                // - Not launchable
                // - Not games
                shouldTrack = isUpdatedSystemApp || hasLauncherActivity || isGame;
            }

            appCache.put(packageName, shouldTrack);
            if (!shouldTrack) systemApps.add(packageName);
            return shouldTrack;

        } catch (PackageManager.NameNotFoundException e) {
            appCache.put(packageName, true);
            return true;
        }
    }

    public void shutdown() {
        isShutdown = true;
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                Log.w(TAG, "Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}