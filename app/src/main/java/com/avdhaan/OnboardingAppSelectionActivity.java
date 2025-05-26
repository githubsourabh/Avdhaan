package com.avdhaan;

import android.content.Context;
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

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppDao;
import com.avdhaan.db.BlockedAppGroup;
import com.avdhaan.db.BlockedAppGroupDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class OnboardingAppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingAppSelection";
    
    protected RecyclerView recyclerView;
    protected AppAdapter appAdapter;
    protected List<AppInfo> appList;
    protected Button buttonDeselectAll;
    protected Button buttonProceed;
    protected Button buttonProceedSchedule;
    protected Set<String> blockedApps;
    protected BlockedAppDao blockedAppDao;
    protected BlockedAppGroupDao groupDao;
    protected ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection_onboarding);

        executor = AppDatabase.databaseWriteExecutor;
        AppDatabase db = AppDatabase.getInstance(this);
        blockedAppDao = db.blockedAppDao();
        groupDao = db.blockedAppGroupDao();

        // Create default group if not exists
        executor.execute(() -> {
            List<BlockedAppGroup> groups = groupDao.getAllGroups();
            if (groups.isEmpty()) {
                BlockedAppGroup defaultGroup = new BlockedAppGroup();
                defaultGroup.groupName = "Default";
                defaultGroup.isDefault = true;
                groupDao.insertGroup(defaultGroup);
            }
        });

        blockedApps = new HashSet<>();
        setupViews();
    }

    protected void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewApps);
        buttonProceedSchedule = findViewById(R.id.button_proceed_schedule);
        buttonDeselectAll = findViewById(R.id.button_deselect_all);
        buttonProceed = findViewById(R.id.button_proceed_schedule);
        Button buttonProceedWithoutSelection = findViewById(R.id.button_proceed_without_selection);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appList = new ArrayList<>();
        
        loadApps();
        
        appAdapter = new AppAdapter(appList);
        recyclerView.setAdapter(appAdapter);

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

            saveSelectedApps(selectedApps);
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

    protected void saveSelectedApps(List<String> selectedApps) {
        executor.execute(() -> {
            // Get default group
            List<BlockedAppGroup> groups = groupDao.getAllGroups();
            if (!groups.isEmpty()) {
                int groupId = groups.get(0).groupId;
                
                // Save to Room database
                for (String packageName : selectedApps) {
                    BlockedApp app = new BlockedApp(packageName, groupId);
                    blockedAppDao.insertBlockedApp(app);
                }
                Log.d(TAG, "Saved selected apps: " + selectedApps);
            }
        });
    }

    private void showProceedWithoutSelectionDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_proceed_without_selection_title)
            .setMessage(R.string.dialog_proceed_without_selection_message)
            .setPositiveButton(R.string.dialog_continue, (dialog, which) -> {
                startActivity(new Intent(this, FinalActivityBeforeMainActivity.class));
                finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown the executor as it's shared across the app
    }
}