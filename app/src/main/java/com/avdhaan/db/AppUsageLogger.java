package com.avdhaan.db;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.AppUsage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppUsageLogger {

    private final Context context;

    public AppUsageLogger(Context context) {
        this.context = context;
    }

    public void logUsage() {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) return;

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, -5);  // check past 5 minutes
        long startTime = calendar.getTimeInMillis();

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();

        AppDatabase db = AppDatabase.getInstance(context);

        // Track start time for each app
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

                    Log.d("AppUsageLogger", "Logging " + packageName + " for " + usageDuration + "ms");

                    AppUsage usage = new AppUsage(
                            packageName,
                            usageDuration,
                            eventTime  // you may also want to store `start` if preferred
                    );
                    db.appUsageDao().insert(usage);
                    foregroundTimestamps.remove(packageName);


                }
            }
        }

    }
}
