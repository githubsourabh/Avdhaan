package com.avdhaan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.avdhaan.worker.UsageLoggingScheduler;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Received null intent or action");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device rebooted — rescheduling usage logging");
            UsageLoggingScheduler.schedule(context);
        } else {
            Log.w(TAG, "Received unexpected action: " + intent.getAction());
        }
    }
}