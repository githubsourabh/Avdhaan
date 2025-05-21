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
        String currentPackage = context.getPackageName();

        for (ApplicationInfo app : installedApps) {
            // Skip our own app
            if (app.packageName.equals(currentPackage)) {
                continue;
            }

            // Check if app has a launch intent
            boolean hasLaunchIntent = packageManager.getLaunchIntentForPackage(app.packageName) != null;
            
            // Include app if:
            // 1. It's a user-installed app
            // 2. It has a launch intent
            boolean isUserApp = (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0;

            if (isUserApp || hasLaunchIntent) {
                String appName = packageManager.getApplicationLabel(app).toString();
                Drawable icon;
                try {
                    icon = packageManager.getApplicationIcon(app);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting icon for " + app.packageName, e);
                    icon = context.getResources().getDrawable(R.mipmap.ic_launcher, context.getTheme());
                }
                userApps.add(new AppInfo(appName, app.packageName, icon));
            }
        }

        Collections.sort(userApps, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return userApps;
    }
} 