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
import com.avdhaan.db.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.avdhaan.PreferenceConstants.*;
//import com.avdhaan.OnboardingUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;
    private AppUsageLogger appUsageLogger;

    private Switch focusSwitch;
    private Switch usageTrackingSwitch;
    private PermissionManager permissionManager;
    private UsageTrackingPreferences trackingPreferences;
    private boolean requestedAccessibilityEnable = false;
    private boolean requestedUsageEnable = false;

    private ContentObserver permissionObserver;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionManager = new PermissionManager(getApplicationContext());
        trackingPreferences = new UsageTrackingPreferences(getApplicationContext());

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize UI components first
        setupPermissionObserver();
        setupUsageTracking();
        initializeFocusMode();
        initializeButtons();

        // Check if coming from onboarding flow
        if (prefs.getBoolean(KEY_FIRST_TIME, true)) {
            // If accessibility was granted during onboarding, set focus mode ON
            if (prefs.getBoolean(KEY_FOCUS_MODE, false)) {
                focusSwitch.setChecked(true);
                startAppBlockService();
            }
            
            // If usage stats was granted during onboarding, set usage tracking ON
            if (prefs.getBoolean(KEY_USAGE_TRACKING, false)) {
                usageTrackingSwitch.setChecked(true);
            }
            
            // Turn off first time flag since we've handled onboarding
            prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();
        }

        // Ensure logging is scheduled and perform initial log
        UsageLoggingScheduler.schedule(getApplicationContext());
        appUsageLogger = new AppUsageLogger(getApplicationContext());
        if (permissionManager.hasUsageStatsPermission()) {
            executor.execute(() -> {
                appUsageLogger.logUsage();
            });
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
        Log.d("MainActivity in setupUsageTracking called by onCreate", "Usage tracking is " + (isTrackingEnabled ? "enabled" : "disabled"));

        usageTrackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!permissionManager.hasUsageStatsPermission()) {
                    // Need to request permission
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                    requestedUsageEnable = true;
                    // Don't update preferences yet, wait for onResume
                    usageTrackingSwitch.setChecked(false);
                } else {
                    updateUsageTracking(true);
                }
            } else {
                updateUsageTracking(false);
            }
        });
    }

    private void updateUsageTracking(boolean enabled) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USAGE_TRACKING, enabled)
            .apply();
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
        boolean isFocusOn = prefs.getBoolean(KEY_FOCUS_MODE, false);
        Log.d(TAG, "initializeFocusMode: Initial focus state from prefs: " + isFocusOn);

        if (isAccessibilityEnabled()) {
            focusSwitch.setChecked(isFocusOn);
            if (isFocusOn) {
                Log.d(TAG, "initializeFocusMode: Starting AppBlockService as focus is ON");
                startAppBlockService();
            }
        } else {
            Log.d(TAG, "initializeFocusMode: Accessibility not enabled, setting focus switch to false");
            focusSwitch.setChecked(false);
            if (isFocusOn) {
                Log.d(TAG, "initializeFocusMode: Accessibility disabled but focus was ON, saving state as false");
                saveFocusModeState(false);
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
                    requestedAccessibilityEnable = true;
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
        boolean isFocusOn = prefs.getBoolean(KEY_FOCUS_MODE, false);

        if (!isAccessibilityOn && isFocusOn) {
            focusSwitch.setChecked(false);
            saveFocusModeState(false);
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
                if (permissionManager.hasUsageStatsPermission()) {
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
        // Don't shutdown the executor as it's shared across the app
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
        prefs.edit()
            .putBoolean(KEY_FOCUS_MODE, enabled)
            .apply();
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void checkAccessibilityState() {
        boolean isAccessibilityOn = isAccessibilityEnabled();
        boolean isFocusOn = prefs.getBoolean(KEY_FOCUS_MODE, false);
        
        if (!isAccessibilityOn) {
            // If accessibility is off, disable focus mode
            if (isFocusOn) {
                focusSwitch.setChecked(false);
                saveFocusModeState(false);
                Toast.makeText(this, R.string.MAKE_FOCUS_MODE_OFF_WHEN_ACC_SVC_DISABLED_MSG, Toast.LENGTH_SHORT).show();
            }
            // Show the accessibility prompt
            showAccessibilityPrompt();
        } else {
            // If accessibility is on, restore focus mode state from preferences
            boolean savedFocusState = prefs.getBoolean(KEY_FOCUS_MODE, false);
            if (savedFocusState != isFocusOn) {
                focusSwitch.setChecked(savedFocusState);
            }
        }
    }

    private void showAccessibilityPrompt() {
        // Implementation of showAccessibilityPrompt method
    }
}