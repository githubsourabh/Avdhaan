package com.avdhaan;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appName;
    private String packageName;
    private Drawable icon;
    private boolean selected;
    private boolean isBlocked;

    public AppInfo(String appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.selected = false;
        this.isBlocked = false;
    }

    public AppInfo(String appName, String packageName, Drawable icon, boolean isBlocked) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.selected = false;
        this.isBlocked = isBlocked;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    // For backward compatibility
    public String getName() {
        return appName;
    }
}
