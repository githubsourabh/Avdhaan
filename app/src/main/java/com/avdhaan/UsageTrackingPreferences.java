package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;

public class UsageTrackingPreferences {
    private static final String PREFS_NAME = "usage_tracking_prefs";
    private static final String KEY_TRACKING_ENABLED = "tracking_enabled";
    private static final String KEY_FIRST_TIME_USER = "first_time_user";
    private static final String KEY_PREVIOUS_TRACKING_STATE = "previous_tracking_state";

    private final SharedPreferences prefs;

    public UsageTrackingPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isTrackingEnabled() {
        return prefs.getBoolean(KEY_TRACKING_ENABLED, false);
    }

    public void setTrackingEnabled(boolean enabled) {
        if (!enabled) {
            // Store the previous state when disabling
            prefs.edit()
                .putBoolean(KEY_PREVIOUS_TRACKING_STATE, isTrackingEnabled())
                .putBoolean(KEY_TRACKING_ENABLED, false)
                .apply();
        } else {
            prefs.edit()
                .putBoolean(KEY_TRACKING_ENABLED, true)
                .apply();
        }
    }

    public boolean isFirstTimeUser() {
        boolean isFirst = prefs.getBoolean(KEY_FIRST_TIME_USER, true);
        if (isFirst) {
            prefs.edit().putBoolean(KEY_FIRST_TIME_USER, false).apply();
        }
        return isFirst;
    }

    public boolean wasTrackingEnabledBefore() {
        return prefs.getBoolean(KEY_PREVIOUS_TRACKING_STATE, false);
    }
} 