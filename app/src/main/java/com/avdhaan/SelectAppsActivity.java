package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectAppsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BlockedPrefs";
    private static final String BLOCKED_APPS_KEY = "blockedApps";
    private static final String TAG = "SelectAppsActivity";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private Set<String> blockedApps = new HashSet<>();
    private SharedPreferences prefs;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        recyclerView = findViewById(R.id.apps_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        packageManager = getPackageManager();

        loadBlockedAppsFromPrefs();
        loadInstalledApps();
    }

    private void loadBlockedAppsFromPrefs() {
        Set<String> savedSet = prefs.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
        blockedApps = new HashSet<>(savedSet != null ? savedSet : new HashSet<>());
    }

    private void saveBlockedAppsToPrefs() {
        prefs.edit()
            .putStringSet(BLOCKED_APPS_KEY, new HashSet<>(blockedApps))
            .apply();
        Toast.makeText(this, R.string.blocked_apps_updated, Toast.LENGTH_SHORT).show();
    }

    private void loadInstalledApps() {
        executor.execute(() -> {
            List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppInfo> userApps = new ArrayList<>();
            String currentPackage = getPackageName();

            for (ApplicationInfo app : installedApps) {
                if (packageManager.getLaunchIntentForPackage(app.packageName) != null &&
                        !app.packageName.equals(currentPackage)) {

                    String appName = packageManager.getApplicationLabel(app).toString();
                    Drawable icon = packageManager.getApplicationIcon(app);
                    boolean isBlocked = blockedApps.contains(app.packageName);

                    userApps.add(new AppInfo(appName, app.packageName, icon, isBlocked));
                }
            }

            Collections.sort(userApps, (a, b) -> a.name.compareToIgnoreCase(b.name));

            runOnUiThread(() -> {
                adapter = new AppListAdapter(userApps, updatedBlockedApps -> {
                    blockedApps = updatedBlockedApps;
                    saveBlockedAppsToPrefs();
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
