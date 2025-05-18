package com.avdhaan.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class UsageLoggingScheduler {

    private static final String USAGE_LOGGING_WORK = "usage_logging_work";

    public static void schedule(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                UsageLoggingWorker.class,
                15, TimeUnit.MINUTES // ⏱️ Minimum interval supported
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                USAGE_LOGGING_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}