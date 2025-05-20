package com.avdhaan;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class OnboardingUtils {

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        
        String serviceName = context.getPackageName() + "/" + AppBlockService.class.getCanonicalName();
        
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasUsageStatsPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static Intent getAccessibilitySettingsIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    public static Intent getUsageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }
} 