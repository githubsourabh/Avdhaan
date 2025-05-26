package com.avdhaan.db;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.app.AppOpsManager;
import com.avdhaan.UsageTrackingPreferences;

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
    private static final long MIN_USAGE_THRESHOLD_MS = 1000; // 1 second
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

        UsageEvents.Event event = new UsageEvents.Event();
        AppDatabase db = AppDatabase.getInstance(context);
        Map<String, Long> foregroundTimestamps = new HashMap<>();

        while (events.hasNextEvent() && !isShutdown) {
            events.getNextEvent(event);

            String packageName = event.getPackageName();
            if (packageName == null) continue;

            // Skip own app
            if (packageName.equals(context.getPackageName())) continue;

            long eventTime = event.getTimeStamp();
            int eventType = event.getEventType();

            switch (eventType) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    foregroundTimestamps.put(packageName, eventTime);
                    break;

                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    Long start = foregroundTimestamps.get(packageName);
                    if (start != null) {
                        processUsageEvent(packageName, start, eventTime, db);
                        foregroundTimestamps.remove(packageName);
                    }
                    break;
            }
        }
    }

    private void processUsageEvent(String packageName, long startTime, long endTime, AppDatabase db) {
        long usageDuration = endTime - startTime;
        Long lastLogged = lastLoggedTime.get(packageName);

        if (usageDuration >= MIN_USAGE_THRESHOLD_MS &&
            (lastLogged == null || (endTime - lastLogged) >= MIN_USAGE_THRESHOLD_MS)) {
            
            try {
                if (shouldTrackApp(packageName)) {
                    AppUsage usage = new AppUsage(
                            packageName,
                            usageDuration,
                            endTime
                    );
                    db.appUsageDao().insert(usage);
                    lastLoggedTime.put(packageName, endTime);
                    Log.d(TAG, "Inserted usage for " + packageName + ": " + usageDuration);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting usage for " + packageName, e);
            }
        }
    }

    private boolean shouldTrackApp(String packageName) {
        // Check both system permission and user's tracking preference
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        boolean hasPermission = mode == AppOpsManager.MODE_ALLOWED;
        
        UsageTrackingPreferences preferences = new UsageTrackingPreferences(context);
        boolean isTrackingEnabled = preferences.isTrackingEnabled();
        
        return hasPermission && isTrackingEnabled;
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
