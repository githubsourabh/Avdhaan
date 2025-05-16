
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
import java.util.concurrent.Executors;

public class AppUsageLogger {

    private final Context context;
    private static final long MIN_USAGE_THRESHOLD_MS = 5000; // 5 seconds

    public AppUsageLogger(Context context) {
        this.context = context;
    }

    public void logUsage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            UsageStatsManager usageStatsManager =
                    (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            if (usageStatsManager == null) return;

            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR, -1);  // last 1 hour
            long startTime = calendar.getTimeInMillis();

            UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();

            AppDatabase db = AppDatabase.getInstance(context);
            Map<String, Long> foregroundTimestamps = new HashMap<>();

            while (events.hasNextEvent()) {
                events.getNextEvent(event);

                String packageName = event.getPackageName();
                long eventTime = event.getTimeStamp();

                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundTimestamps.put(packageName, eventTime);
                } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    Long start = foregroundTimestamps.get(packageName);
                    if (start != null) {
                        long usageDuration = eventTime - start;

                        if (usageDuration < MIN_USAGE_THRESHOLD_MS) continue;
                        if (isSystemApp(packageName) || packageName.equals(context.getPackageName())) continue;

                        Log.d("AppUsageLogger", "Logging " + packageName + " for " + usageDuration + "ms");

                        AppUsage usage = new AppUsage(
                                packageName,
                                usageDuration,
                                eventTime
                        );
                        db.appUsageDao().insert(usage);
                        foregroundTimestamps.remove(packageName);
                    }
                }
            }
        });
    }

    private boolean isSystemApp(String packageName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
