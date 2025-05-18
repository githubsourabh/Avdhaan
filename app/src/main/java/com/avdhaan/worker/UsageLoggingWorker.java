package com.avdhaan.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.avdhaan.db.AppUsageLogger;

public class UsageLoggingWorker extends Worker {

    private static final String TAG = "UsageLoggingWorker";

    public UsageLoggingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "UsageLoggingWorker started");

        try {
            AppUsageLogger appUsageLogger = new AppUsageLogger(getApplicationContext());
            appUsageLogger.logUsage();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Usage logging failed", e);
            return Result.retry();
        }
    }
}