package com.avdhaan;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppBlockService extends AccessibilityService {

    private Set<String> blockedApps = new HashSet<>();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadBlockedApps();
    }

    private void loadBlockedApps() {
        SharedPreferences prefs = getSharedPreferences("BlockedPrefs", MODE_PRIVATE);
        blockedApps = prefs.getStringSet("blockedApps", new HashSet<>());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        String packageName = String.valueOf(event.getPackageName());

        if (blockedApps.contains(packageName)) {
            Log.d("AppBlockService", "Blocking app: " + packageName);

            Intent intent = new Intent(this, BlockScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    @Override
    public void onInterrupt() {
        Log.d("AppBlockService", "Service Interrupted");
    }


}
