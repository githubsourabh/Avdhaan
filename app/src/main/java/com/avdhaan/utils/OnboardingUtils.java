package com.avdhaan.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

import static com.avdhaan.PreferenceConstants.*;

public class OnboardingUtils {
    private static final String TAG = "OnboardingUtils";
    
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent getUsageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    public static boolean hasAccessibilityPermission(Context context, String serviceName) {
        try {
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am == null) {
                Log.e(TAG, "AccessibilityManager is null");
                return false;
            }

            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                    AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            
            Log.d(TAG, "Checking for service: " + serviceName);
            Log.d(TAG, "Number of enabled services: " + enabledServices.size());
            
            for (AccessibilityServiceInfo service : enabledServices) {
                Log.d(TAG, "Found service: " + service.getId());
                if (service.getId().contains(serviceName)) {
                    Log.d(TAG, "Found matching service!");
                    return true;
                }
            }
            
            // Also check the settings directly
            int accessibilityEnabled = 0;
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                        context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "Error getting accessibility enabled setting", e);
            }

            if (accessibilityEnabled == 1) {
                String settingValue = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    Log.d(TAG, "Settings value: " + settingValue);
                    return settingValue.contains(serviceName);
                }
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking accessibility permission", e);
            return false;
        }
    }

    public static Intent getAccessibilitySettingsIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    public static void updateAccessibilityState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (hasAccessibilityPermission(context, context.getString(com.avdhaan.R.string.accessibility_service_name))) {
            prefs.edit().putBoolean(KEY_FOCUS_MODE, true).apply();
        }
    }


    public static void updateUsageStatsState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (hasUsageStatsPermission(context)) {
            prefs.edit().putBoolean(KEY_USAGE_TRACKING, true).apply();
        }
    }
} 