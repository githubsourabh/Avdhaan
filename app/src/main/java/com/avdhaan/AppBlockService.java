package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.List;

public class AppBlockService extends AccessibilityService {

    // Hardcoded list of apps to block for now
    private final List<String> blockedApps = Arrays.asList(
            "com.instagram.android",
            "com.facebook.katana",
            "com.netflix.mediaclient"
    );

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        String packageName = String.valueOf(event.getPackageName());

        if (blockedApps.contains(packageName)) {
            Log.d("AppBlockService", "Blocking: " + packageName);

            Intent intent = new Intent(this, BlockScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }

    @Override
    public void onInterrupt() {
        Log.d("AppBlockService", "Service Interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AppBlockService", "Accessibility Service Connected");
    }
}
