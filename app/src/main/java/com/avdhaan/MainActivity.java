
package com.avdhaan;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.avdhaan.db.AppUsageLogger;

import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "FocusPrefs";
    private static final String KEY_FOCUS_MODE = "focusEnabled";

    private Switch focusSwitch;
    private boolean requestedEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        focusSwitch = findViewById(R.id.focus_swtich);
        Button scheduleButton = findViewById(R.id.schedule_button);
        Button selectAppsButton = findViewById(R.id.btn_select_apps);
        Button viewUsageButton = findViewById(R.id.btn_view_usage);

        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);

        if (isAccessibilityEnabled()) {
            focusSwitch.setChecked(isFocusOn);
        } else {
            focusSwitch.setChecked(false);
            if (isFocusOn) {
                saveFocusModeState(false);
                Toast.makeText(this, R.string.MAKE_FOCUS_MODE_OFF_WHEN_ACC_SVC_DISABLED_MSG, Toast.LENGTH_SHORT).show();
            }
        }

        focusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!isAccessibilityEnabled()) {
                    Toast.makeText(this, getString(R.string.please_enable_accessibility), Toast.LENGTH_LONG).show();
                    requestedEnable = true;
                    focusSwitch.setChecked(false);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } else {
                    saveFocusModeState(true);
                    Toast.makeText(this, getString(R.string.focus_mode_enabled), Toast.LENGTH_SHORT).show();
                }
            } else {
                saveFocusModeState(false);
                Toast.makeText(this, getString(R.string.focus_mode_disabled), Toast.LENGTH_SHORT).show();
            }
        });

        scheduleButton.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleListActivity.class))
        );

        selectAppsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SelectAppsActivity.class))
        );

        viewUsageButton.setOnClickListener(v ->
                startActivity(new Intent(this, AppUsageListActivity.class))
        );

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasUsageStatsPermission(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Please enable Usage Access for Avdhaan", Toast.LENGTH_LONG).show();
        }

        boolean isAccessibilityOn = isAccessibilityEnabled();
        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);

        if (requestedEnable && isAccessibilityOn) {
            focusSwitch.setChecked(true);
            saveFocusModeState(true);
            Toast.makeText(this, getString(R.string.focus_mode_enabled), Toast.LENGTH_SHORT).show();
            requestedEnable = false;
        } else if (!isAccessibilityOn && isFocusOn) {
            focusSwitch.setChecked(false);
            saveFocusModeState(false);
            Toast.makeText(this, R.string.MAKE_FOCUS_MODE_OFF_WHEN_ACC_SVC_DISABLED_MSG, Toast.LENGTH_SHORT).show();
        } else {
            focusSwitch.setChecked(isFocusOn);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            new AppUsageLogger(this).logUsage();
        });


    }

    private boolean isAccessibilityEnabled() {
        String expectedComponent = new ComponentName(this, AppBlockService.class).flattenToString();
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        return enabledServices != null && enabledServices.contains(expectedComponent);
    }

    private void saveFocusModeState(boolean enabled) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_FOCUS_MODE, enabled);
        editor.apply();
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
