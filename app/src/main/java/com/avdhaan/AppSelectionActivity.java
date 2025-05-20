package com.avdhaan;

import android.content.Intent;
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
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "AppSelectionActivity";
    protected RecyclerView recyclerView;
    protected AppAdapter appAdapter;
    protected List<AppInfo> appList;
    protected Button buttonSelectAll;
    protected Button buttonDeselectAll;
    protected Button buttonProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

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

            // Save selected apps to preferences or database
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
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d(TAG, "Total installed apps: " + packages.size());
        
        appList.clear(); // Clear the list before adding new apps
        
        for (ApplicationInfo packageInfo : packages) {
            // Skip our own app
            if (packageInfo.packageName.equals(getPackageName())) {
                continue;
            }

            try {
                // Only skip system apps that are not launchable
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) == null) {
                        continue;
                    }
                }

                String appName = pm.getApplicationLabel(packageInfo).toString();
                AppInfo app = new AppInfo(
                    appName,
                    packageInfo.packageName,
                    pm.getApplicationIcon(packageInfo)
                );
                appList.add(app);
                Log.d(TAG, "Added app: " + appName + " (" + packageInfo.packageName + ")");
            } catch (Exception e) {
                Log.e(TAG, "Error loading app: " + packageInfo.packageName, e);
            }
        }
        
        Log.d(TAG, "Total apps added to list: " + appList.size());
    }

    protected void saveSelectedApps(List<String> selectedApps) {
        // TODO: Implement saving selected apps to preferences or database
        // For now, we'll just log them
        Log.d(TAG, "Selected apps: " + selectedApps);
    }
}