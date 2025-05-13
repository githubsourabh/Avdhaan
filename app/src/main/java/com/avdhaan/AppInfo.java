package com.avdhaan;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String name;
    public String packageName;
    public Drawable icon;
    public boolean isBlocked;

    public AppInfo(String name, String packageName, Drawable icon, boolean isBlocked) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isBlocked = isBlocked;
    }
}
