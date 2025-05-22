package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;

import static com.avdhaan.PreferenceConstants.*;

public class UsageTrackingPreferences {
    private final SharedPreferences prefs;

    public UsageTrackingPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isTrackingEnabled() {
        return prefs.getBoolean(KEY_USAGE_TRACKING, false);
    }

    public void setTrackingEnabled(boolean enabled) {
        if (!enabled) {
            // Store the previous state when disabling
            prefs.edit()
                .putBoolean(KEY_PREVIOUS_TRACKING_STATE, isTrackingEnabled())
                .putBoolean(KEY_USAGE_TRACKING, false)
                .apply();
        } else {
            prefs.edit()
                .putBoolean(KEY_USAGE_TRACKING, true)
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