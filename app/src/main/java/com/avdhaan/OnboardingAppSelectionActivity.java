package com.avdhaan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.avdhaan.PreferenceConstants.*;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class OnboardingAppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingAppSelection";
    
    protected RecyclerView recyclerView;
    protected AppAdapter appAdapter;
    protected List<AppInfo> appList;
    protected Button buttonDeselectAll;
    protected Button buttonProceed;
    protected Button buttonProceedSchedule;
    protected SharedPreferences prefs;
    protected Set<String> blockedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection_onboarding);

        prefs = getSharedPreferences(BLOCKED_PREFS_NAME, Context.MODE_PRIVATE);
        loadBlockedAppsFromPrefs();
        setupViews();
    }

    protected void setupViews() {
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewApps);
        buttonProceedSchedule = findViewById(R.id.button_proceed_schedule);
        buttonDeselectAll = findViewById(R.id.button_deselect_all);
        buttonProceed = findViewById(R.id.button_proceed_schedule);
        Button buttonProceedWithoutSelection = findViewById(R.id.button_proceed_without_selection);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appList = new ArrayList<>();
        
        // Load apps first
        loadApps();
        
        // Create adapter with loaded apps
        appAdapter = new AppAdapter(appList);
        recyclerView.setAdapter(appAdapter);

        // Setup button click listeners
        buttonDeselectAll.setOnClickListener(v -> {
            for (AppInfo app : appList) {
                app.setSelected(false);
            }
            appAdapter.notifyDataSetChanged();
        });

        buttonProceedSchedule.setOnClickListener(v -> {
            List<String> selectedApps = new ArrayList<>();
            for (AppInfo app : appList) {
                if (app.isSelected()) {
                    selectedApps.add(app.getPackageName());
                }
            }

            if (selectedApps.isEmpty()) {
                Toast.makeText(this, "Please select at least one app", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save selected apps to preferences
            saveSelectedApps(selectedApps);

            // Call the protected method for navigation
            onProceedClick();
        });

        buttonProceedWithoutSelection.setOnClickListener(v -> showProceedWithoutSelectionDialog());
    }

    protected void onProceedClick() {
        // Base implementation does nothing
        // Subclasses should override this to handle navigation
    }

    protected void loadApps() {
        PackageManager pm = getPackageManager();
        appList = AppListUtils.loadApps(this, pm);
        Log.d(TAG, "Total apps loaded: " + appList.size());
    }

    private void loadBlockedAppsFromPrefs() {
        Set<String> savedSet = prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        blockedApps = new HashSet<>(savedSet != null ? savedSet : new HashSet<>());
    }

    protected void saveSelectedApps(List<String> selectedApps) {
        // Save to the same SharedPreferences as AppBlockService
        prefs.edit()
            .putStringSet(KEY_BLOCKED_APPS, new HashSet<>(selectedApps))
            .commit(); // Use commit() instead of apply() for immediate write
        Log.d(TAG, "Saved selected apps: " + selectedApps);
        
        // Verify the save
        Set<String> savedSet = prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        Log.d(TAG, "Verified saved apps: " + savedSet);
    }

    private void showProceedWithoutSelectionDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_proceed_without_selection_title)
            .setMessage(R.string.dialog_proceed_without_selection_message)
            .setPositiveButton(R.string.dialog_continue, (dialog, which) -> {
                // Navigate to FinalActivityBeforeMainActivity
                startActivity(new Intent(this, FinalActivityBeforeMainActivity.class));
                finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}