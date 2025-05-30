package com.avdhaan;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListUtils {
    private static final String TAG = "AppListUtils";

    public static List<AppInfo> loadApps(Context context, PackageManager packageManager) {
        // Get all installed apps with more flags
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA | 
            PackageManager.GET_UNINSTALLED_PACKAGES |
            PackageManager.GET_ACTIVITIES |
            PackageManager.MATCH_ALL
        );
        
        List<AppInfo> userApps = new ArrayList<>();
        List<AppInfo> updatedSystemApps = new ArrayList<>();
        String currentPackage = context.getPackageName();

        for (ApplicationInfo app : installedApps) {
            // Skip our own app
            if (app.packageName.equals(currentPackage)) {
                continue;
            }

            // Check if app has a launch intent
            boolean hasLaunchIntent = packageManager.getLaunchIntentForPackage(app.packageName) != null;
            if (!hasLaunchIntent) {
                continue;
            }
            
            // Check if it's a system app
            boolean isSystemApp = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystemApp = (app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            
            // Skip if it's a system app that hasn't been updated
            if (isSystemApp && !isUpdatedSystemApp) {
                continue;
            }

            String appName = packageManager.getApplicationLabel(app).toString();
            Drawable icon;
            try {
                icon = packageManager.getApplicationIcon(app);
            } catch (Exception e) {
                Log.e(TAG, "Error getting icon for " + app.packageName, e);
                icon = context.getResources().getDrawable(R.mipmap.ic_launcher, context.getTheme());
            }
            
            AppInfo appInfo = new AppInfo(appName, app.packageName, icon);
            if (!isSystemApp) {
                userApps.add(appInfo);
            } else {
                updatedSystemApps.add(appInfo);
            }
        }

        // Sort each list alphabetically
        Collections.sort(userApps, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        Collections.sort(updatedSystemApps, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        // Combine lists with user apps first
        List<AppInfo> allApps = new ArrayList<>();
        allApps.addAll(userApps);
        allApps.addAll(updatedSystemApps);
        
        return allApps;
    }
} 