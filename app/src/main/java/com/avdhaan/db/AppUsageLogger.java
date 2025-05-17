package com.avdhaan.db;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppUsageLogger {

    private static final String TAG = "AppUsageLogger";
    private static final long MIN_USAGE_THRESHOLD_MS = 5000; // 5 seconds
    private final ExecutorService executor;
    private volatile boolean isShutdown = false;

    private final Context context;
    private final PackageManager packageManager;
    private final Set<String> systemApps;
    private final Map<String, Boolean> appCache;

    public AppUsageLogger(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.systemApps = new HashSet<>();
        this.appCache = new HashMap<>();
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

        UsageEvents.Event event = new UsageEvents.Event();
        AppDatabase db = AppDatabase.getInstance(context);
        Map<String, Long> foregroundTimestamps = new HashMap<>();

        while (events.hasNextEvent() && !isShutdown) {
            events.getNextEvent(event);

            String packageName = event.getPackageName();
            if (packageName == null) continue;

            long eventTime = event.getTimeStamp();

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundTimestamps.put(packageName, eventTime);
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                Long start = foregroundTimestamps.get(packageName);
                if (start != null) {
                    long usageDuration = eventTime - start;

                    if (usageDuration >= MIN_USAGE_THRESHOLD_MS && 
                        !isSystemApp(packageName) && 
                        !packageName.equals(context.getPackageName())) {
                        
                        try {
                            AppUsage usage = new AppUsage(
                                    packageName,
                                    usageDuration,
                                    eventTime
                            );
                            db.appUsageDao().insert(usage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error inserting usage for " + packageName, e);
                        }
                    }
                    foregroundTimestamps.remove(packageName);
                }
            }
        }
    }

    private boolean isSystemApp(String packageName) {
        Boolean cached = appCache.get(packageName);
        if (cached != null) return cached;
        
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            appCache.put(packageName, isSystem);
            if (isSystem) {
                systemApps.add(packageName);
            }
            return isSystem;
        } catch (PackageManager.NameNotFoundException e) {
            appCache.put(packageName, true);
            systemApps.add(packageName);
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
