package com.avdhaan;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.os.Process;
import android.content.pm.PackageManager;

import java.util.List;

import static com.avdhaan.PreferenceConstants.*;

public class OnboardingUtils {
    private static final String TAG = "OnboardingUtils";

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        
        String expectedComponentName = context.getPackageName() + "/" + AppBlockService.class.getCanonicalName();
        
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().equals(expectedComponentName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent getAccessibilitySettingsIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    public static Intent getUsageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

/*    public static void updateAccessibilityState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (isAccessibilityServiceEnabled(context)) {
            prefs.edit().putBoolean(KEY_FOCUS_MODE, true).apply();
        }
    }
*/
    public static void updateUsageStatsState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (hasUsageStatsPermission(context)) {
            prefs.edit().putBoolean(KEY_USAGE_TRACKING, true).apply();
        }
    }
} 