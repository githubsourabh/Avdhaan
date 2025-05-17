package com.avdhaan.ui;

import android.graphics.drawable.Drawable;

public class AppUsageDisplayItem {
    public String appName;
    public String packageName;
    public Drawable icon;
    public long totalDuration;

    public AppUsageDisplayItem(String appName, String packageName, Drawable icon, long totalDuration) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.totalDuration = totalDuration;
    }
}
