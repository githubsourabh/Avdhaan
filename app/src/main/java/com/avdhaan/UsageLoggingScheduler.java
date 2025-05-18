package com.avdhaan.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class UsageLoggingScheduler {

    private static final String USAGE_LOGGING_WORK = "usage_logging_work";
    // 55-minute interval provides 5-minute overlap with 1-hour lookback window
    private static final long USAGE_LOGGING_INTERVAL_MINUTES = 55;

    public static void schedule(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                UsageLoggingWorker.class,
                USAGE_LOGGING_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                USAGE_LOGGING_WORK,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }
}