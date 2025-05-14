package com.avdhaan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import android.content.SharedPreferences;

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

        // Initial state load
        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);
        focusSwitch.setChecked(isFocusOn);

        focusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!isAccessibilityEnabled()) {
                    Toast.makeText(this, "Please enable accessibility service for Avdhaan", Toast.LENGTH_LONG).show();
                    requestedEnable = true;
                    focusSwitch.setChecked(false);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } else {
                    saveFocusModeState(true);
                    Toast.makeText(this, "Focus Mode Enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                saveFocusModeState(false);
                Toast.makeText(this, "Focus Mode Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        scheduleButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ScheduleListActivity.class));
        });

        selectAppsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectAppsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isAccessibilityOn = isAccessibilityEnabled();
        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);

        // If user requested to enable focus mode and has now enabled accessibility
        if (requestedEnable && isAccessibilityOn) {
            focusSwitch.setChecked(true);
            saveFocusModeState(true);
            Toast.makeText(this, "Focus Mode Enabled", Toast.LENGTH_SHORT).show();
            requestedEnable = false;
        } else {
            focusSwitch.setChecked(isFocusOn);
        }
    }

    private boolean isAccessibilityEnabled() {
        String expectedComponent = new android.content.ComponentName(this, AppBlockService.class)
                .flattenToString();
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
}
