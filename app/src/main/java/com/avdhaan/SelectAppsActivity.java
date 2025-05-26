package com.avdhaan;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectAppsActivity extends AppCompatActivity {

    private static final String TAG = "SelectAppsActivity";
    private static final int DEFAULT_GROUP_ID = 1;

    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;
    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private Set<String> blockedApps = new HashSet<>();
    private BlockedAppDao blockedAppDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        blockedAppDao = AppDatabase.getInstance(this).blockedAppDao();

        recyclerView = findViewById(R.id.apps_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBlockedAppsAndPopulate();
    }

    private void loadBlockedAppsAndPopulate() {
        executor.execute(() -> {
            List<BlockedApp> blockedAppList = blockedAppDao.getBlockedAppsByGroup(DEFAULT_GROUP_ID);
            for (BlockedApp app : blockedAppList) {
                blockedApps.add(app.getPackageName());
            }

            List<AppInfo> apps = getInstalledUserApps();
            runOnUiThread(() -> {
                adapter = new AppListAdapter(apps, blockedApps, (packageName, isChecked) -> {
                    executor.execute(() -> {
                        if (isChecked) {
                            blockedAppDao.insertBlockedApp(new BlockedApp(packageName, DEFAULT_GROUP_ID));
                        } else {
                            blockedAppDao.deleteByPackageName(packageName);
                        }
                    });
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private List<AppInfo> getInstalledUserApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApps) {
            // Skip our own app
            if (appInfo.packageName.equals(getPackageName())) {
                continue;
            }
            // Show all launchable apps (system and user)
            if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                Drawable icon = appInfo.loadIcon(pm);
                String name = appInfo.loadLabel(pm).toString();
                appList.add(new AppInfo(name, appInfo.packageName, icon));
            }
        }

        Collections.sort(appList, (a1, a2) -> a1.getAppName().compareToIgnoreCase(a2.getAppName()));
        return appList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown the executor as it's shared across the app
    }
}