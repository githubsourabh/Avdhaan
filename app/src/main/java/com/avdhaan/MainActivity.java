package com.avdhaan;



import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.avdhaan.db.AppUsageLogger;
import com.avdhaan.worker.UsageLoggingScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.avdhaan.PreferenceConstants.*;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FocusPrefs";
    private static final String KEY_FOCUS_MODE = "focusEnabled";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AppUsageLogger appUsageLogger;

    private Switch focusSwitch;
    private Switch usageTrackingSwitch;
    private PermissionManager permissionManager;
    private UsageTrackingPreferences trackingPreferences;
    private boolean requestedAccessibilityEnable = false;  // New flag for accessibility
    private boolean requestedUsageEnable = false;         // Renamed flag for usage tracking

    private ContentObserver permissionObserver;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionManager = new PermissionManager(getApplicationContext());
        trackingPreferences = new UsageTrackingPreferences(getApplicationContext());

        // Set KEY_FIRST_TIME to false
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();

        setupPermissionObserver();
        setupUsageTracking();
        initializeFocusMode();
        initializeButtons();

        // Ensure logging is scheduled
        UsageLoggingScheduler.schedule(getApplicationContext());

        appUsageLogger = new AppUsageLogger(getApplicationContext());

        // Check if we're coming from onboarding
        boolean isFromOnboarding = getIntent().getBooleanExtra("from_onboarding", false);
        if (isFromOnboarding) {
            // Ensure focus mode is off when coming from onboarding
            saveFocusModeState(false);
            if (focusSwitch != null) {
                focusSwitch.setChecked(false);
            }
            Log.d("MainActivity", "Initialized focus mode to OFF after onboarding");
        }
    }

    private void setupPermissionObserver() {
        permissionObserver = new ContentObserver(mainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                // Check permission state on any settings change
                permissionManager.checkAndUpdatePermissionState();
            }
        };

        // Register the observer for settings changes
        getContentResolver().registerContentObserver(
            Settings.Secure.CONTENT_URI,
            true,
            permissionObserver
        );
    }

    private void setupUsageTracking() {
        usageTrackingSwitch = findViewById(R.id.switch_usage_tracking);
        boolean isTrackingEnabled = trackingPreferences.isTrackingEnabled();
        usageTrackingSwitch.setChecked(isTrackingEnabled);

        usageTrackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !permissionManager.hasUsageStatsPermission()) {
                // Need to request permission
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
                requestedUsageEnable = true;  // Use renamed flag
                // Don't update preferences yet, wait for onResume
                usageTrackingSwitch.setChecked(false);
            } else {
                updateUsageTracking(isChecked);
            }
        });
    }

    private void updateUsageTracking(boolean enabled) {
        trackingPreferences.setTrackingEnabled(enabled);
        if (enabled) {
            UsageLoggingScheduler.schedule(getApplicationContext());
            Toast.makeText(this, R.string.tracking_enabled, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.tracking_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeFocusMode() {
        focusSwitch = findViewById(R.id.focus_swtich);
        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);

        if (isAccessibilityEnabled()) {
            focusSwitch.setChecked(isFocusOn);
            if (isFocusOn) {
                startAppBlockService();
            }
        } else {
            focusSwitch.setChecked(false);
            if (isFocusOn) {
                saveFocusModeState(false);

                // Enhancement: Show warning popup if focus mode was ON but accessibility is OFF
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.focus_mode_disabled_title))
                        .setMessage(getString(R.string.focus_mode_disabled_message))
                        .setPositiveButton(getString(R.string.ok), null)
                        .setCancelable(false)
                        .show();

            }
        }

        focusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!isAccessibilityEnabled()) {
                    Toast.makeText(this, getString(R.string.please_enable_accessibility), Toast.LENGTH_LONG).show();
                    requestedAccessibilityEnable = true;  // Use new flag
                    focusSwitch.setChecked(false);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } else {
                    saveFocusModeState(true);
                    startAppBlockService();
                    Toast.makeText(this, getString(R.string.focus_mode_enabled), Toast.LENGTH_SHORT).show();
                }
            } else {
                saveFocusModeState(false);
                stopAppBlockService();
                Toast.makeText(this, getString(R.string.focus_mode_disabled), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAppBlockService() {
        Intent serviceIntent = new Intent(this, AppBlockService.class);
        startService(serviceIntent);
    }

    private void stopAppBlockService() {
        Intent serviceIntent = new Intent(this, AppBlockService.class);
        stopService(serviceIntent);
    }

    private void initializeButtons() {
        Button scheduleButton = findViewById(R.id.schedule_button);
        Button selectAppsButton = findViewById(R.id.btn_select_apps);
        Button viewUsageButton = findViewById(R.id.btn_view_usage);

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

        // Immediately check permission state
        permissionManager.checkAndUpdatePermissionState();

        // Handle accessibility permission
        if (requestedAccessibilityEnable && isAccessibilityEnabled()) {
            focusSwitch.setChecked(true);
            saveFocusModeState(true);
            startAppBlockService();
            Toast.makeText(this, getString(R.string.focus_mode_enabled), Toast.LENGTH_SHORT).show();
            requestedAccessibilityEnable = false;
        }

        // Handle usage tracking permission
        if (requestedUsageEnable && permissionManager.hasUsageStatsPermission()) {
            usageTrackingSwitch.setChecked(true);
            updateUsageTracking(true);
            requestedUsageEnable = false;
        }

        // Handle focus mode state
        boolean isAccessibilityOn = isAccessibilityEnabled();
        boolean isFocusOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);

        if (!isAccessibilityOn && isFocusOn) {
            focusSwitch.setChecked(false);
            saveFocusModeState(false);
            //Toast.makeText(this, R.string.MAKE_FOCUS_MODE_OFF_WHEN_ACC_SVC_DISABLED_MSG, Toast.LENGTH_SHORT).show();
            // Enhancement: Show warning popup if focus mode was ON but accessibility is OFF

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.focus_mode_disabled_title))
                        .setMessage(getString(R.string.focus_mode_disabled_message))
                        .setPositiveButton(getString(R.string.ok), null)
                        .setCancelable(false)
                        .show();


        } else {
            focusSwitch.setChecked(isFocusOn);
        }

        // Only log usage if permission is granted and logger is initialized
        if (appUsageLogger != null) {
            executor.execute(() -> {
                if (trackingPreferences.isTrackingEnabled() && !permissionManager.hasUsageStatsPermission()) {
                    // Show dialog instead of forcefully redirecting
                    runOnUiThread(() -> {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle(R.string.usage_access_required_title)
                            .setMessage(R.string.usage_access_required_message)
                            .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                startActivity(intent);
                            })
                            .setNegativeButton(R.string.disable_tracking, (dialog, which) -> {
                                trackingPreferences.setTrackingEnabled(false);
                                usageTrackingSwitch.setChecked(false);
                            })
                            .setCancelable(false)
                            .show();
                    });
                } else if (permissionManager.hasUsageStatsPermission()) {
                    appUsageLogger.logUsage();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permissionObserver != null) {
            getContentResolver().unregisterContentObserver(permissionObserver);
        }
        if (appUsageLogger != null) {
            appUsageLogger.shutdown();
        }
        executor.shutdownNow();
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