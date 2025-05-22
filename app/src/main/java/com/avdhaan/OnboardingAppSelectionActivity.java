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

public class OnboardingAppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingAppSelection";
    
    protected RecyclerView recyclerView;
    protected AppAdapter appAdapter;
    protected List<AppInfo> appList;
    protected Button buttonSelectAll;
    protected Button buttonDeselectAll;
    protected Button buttonProceed;
    protected SharedPreferences prefs;
    protected Set<String> blockedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        prefs = getSharedPreferences(BLOCKED_PREFS_NAME, Context.MODE_PRIVATE);
        loadBlockedAppsFromPrefs();
        setupViews();
    }

    protected void setupViews() {
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewApps);
        buttonSelectAll = findViewById(R.id.button_select_all);
        buttonDeselectAll = findViewById(R.id.button_deselect_all);
        buttonProceed = findViewById(R.id.button_proceed_schedule);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appList = new ArrayList<>();
        
        // Load apps first
        loadApps();
        
        // Create adapter with loaded apps
        appAdapter = new AppAdapter(appList);
        recyclerView.setAdapter(appAdapter);

        // Setup button click listeners
        buttonSelectAll.setOnClickListener(v -> {
            for (AppInfo app : appList) {
                app.setSelected(true);
            }
            appAdapter.notifyDataSetChanged();
        });

        buttonDeselectAll.setOnClickListener(v -> {
            for (AppInfo app : appList) {
                app.setSelected(false);
            }
            appAdapter.notifyDataSetChanged();
        });

        buttonProceed.setOnClickListener(v -> {
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
}