package com.avdhaan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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

import static com.avdhaan.PreferenceConstants.*;

public class SelectAppsActivity extends AppCompatActivity {

    private static final String TAG = "SelectAppsActivity";
    private ExecutorService executor;

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private Set<String> blockedApps = new HashSet<>();
    private SharedPreferences prefs;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        executor = Executors.newSingleThreadExecutor();
        recyclerView = findViewById(R.id.apps_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        prefs = getSharedPreferences(BLOCKED_PREFS_NAME, Context.MODE_PRIVATE);
        packageManager = getPackageManager();

        loadBlockedAppsFromPrefs();
        loadInstalledApps();
    }

    private void loadBlockedAppsFromPrefs() {
        Set<String> savedSet = prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        blockedApps = new HashSet<>(savedSet != null ? savedSet : new HashSet<>());
    }

    private void saveBlockedAppsToPrefs() {
        prefs.edit()
            .putStringSet(KEY_BLOCKED_APPS, new HashSet<>(blockedApps))
            .commit();
        Toast.makeText(this, R.string.blocked_apps_updated, Toast.LENGTH_SHORT).show();
        
        Set<String> savedSet = prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        Log.d(TAG, "Verified saved apps: " + savedSet);
    }

    private void loadInstalledApps() {
        if (executor != null && !executor.isShutdown()) {
            executor.execute(() -> {
                List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                List<AppInfo> userApps = new ArrayList<>();
                String currentPackage = getPackageName();

                for (ApplicationInfo app : installedApps) {
                    // Skip only our own app
                    if (app.packageName.equals(currentPackage)) {
                        continue;
                    }
                    // Show all launchable apps (system and user)
                    if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                        String appName = packageManager.getApplicationLabel(app).toString();
                        Drawable icon;
                        try {
                            icon = packageManager.getApplicationIcon(app);
                        } catch (Exception e) {
                            // If there's an error loading the icon, use a default icon
                            icon = getResources().getDrawable(R.mipmap.ic_launcher, getTheme());
                        }
                        boolean isBlocked = blockedApps.contains(app.packageName);

                        userApps.add(new AppInfo(appName, app.packageName, icon, isBlocked));
                    }
                }

                Collections.sort(userApps, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

                runOnUiThread(() -> {
                    adapter = new AppListAdapter(userApps, updatedBlockedApps -> {
                        blockedApps = updatedBlockedApps;
                        saveBlockedAppsToPrefs();
                    });
                    recyclerView.setAdapter(adapter);
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
}
