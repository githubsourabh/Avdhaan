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

public class SelectAppsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BlockedPrefs";
    private static final String BLOCKED_APPS_KEY = "blockedApps";

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private Set<String> blockedApps = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        recyclerView = findViewById(R.id.apps_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBlockedAppsFromPrefs();
        loadInstalledApps();
    }

    private void loadBlockedAppsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
        // Defensive copy to prevent SharedPreferences from returning a mutable reference
        blockedApps = new HashSet<>(savedSet);
    }

    private void saveBlockedAppsToPrefs() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putStringSet(BLOCKED_APPS_KEY, new HashSet<>(blockedApps));
        editor.apply();
        Toast.makeText(this, "Blocked apps updated", Toast.LENGTH_SHORT).show();
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> userApps = new ArrayList<>();

        String currentPackage = getPackageName();

        for (ApplicationInfo app : installedApps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null &&
                    !app.packageName.equals(currentPackage)) {

                String appName = pm.getApplicationLabel(app).toString();
                Drawable icon = pm.getApplicationIcon(app);
                boolean isBlocked = blockedApps.contains(app.packageName);

                userApps.add(new AppInfo(appName, app.packageName, icon, isBlocked));
            }
        }

        Collections.sort(userApps, (a, b) -> a.name.compareToIgnoreCase(b.name));

        adapter = new AppListAdapter(userApps, updatedBlockedApps -> {
            blockedApps = updatedBlockedApps;
            saveBlockedAppsToPrefs();
        });

        recyclerView.setAdapter(adapter);
    }
}
