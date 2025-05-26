package com.avdhaan;

public class PreferenceConstants {
    // Retained only if other UI/shared flags are still stored temporarily
    public static final String PREF_NAME = "AvdhaanPrefs";
    public static final String KEY_FIRST_TIME = "isFirstTime";
    public static final String KEY_FOCUS_MODE = "focusEnabled";


    // Deprecated: previously used for blocked apps and schedules via SharedPreferences

    @Deprecated
    public static final String BLOCKED_PREFS_NAME = "BlockedPrefs";
    @Deprecated
    public static final String KEY_BLOCKED_APPS = "blockedApps";

    /*
    @Deprecated
    public static final String KEY_FOCUS_SCHEDULES = "focusSchedules";
    */

    // Usage tracking keys
    public static final String KEY_USAGE_TRACKING = "usageTrackingEnabled";
    public static final String KEY_FIRST_TIME_USER = "firstTimeUser";
    public static final String KEY_PREVIOUS_TRACKING_STATE = "previousTrackingState";
}