package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.FocusSchedule;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ScheduleSetupActivity extends BaseScheduleActivity {
    private static final String TAG = "ScheduleSetupActivity";
    private RecyclerView scheduleList;
    private ScheduleListAdapter adapter;
    private ExecutorService executor;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_schedule_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing ScheduleSetupActivity");
        executor = AppDatabase.databaseWriteExecutor;
        setupNextButton();
        setupScheduleList();
    }

    private void setupScheduleList() {
        scheduleList = findViewById(R.id.scheduleList);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));

        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            runOnUiThread(() -> {
                adapter = new ScheduleListAdapter(this, schedules, new ScheduleListAdapter.OnScheduleUpdated() {
                    @Override
                    public void onEdit(int position) {
                        // Handle edit if needed
                    }

                    @Override
                    public void onDelete(int position) {
                        executor.execute(() -> {
                            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(ScheduleSetupActivity.this);
                            schedules.remove(position);
                            ScheduleStorage.saveSchedules(ScheduleSetupActivity.this, schedules);
                            runOnUiThread(() -> adapter.updateSchedules(schedules));
                        });
                    }

                    @Override
                    public void onScheduleUpdated() {
                        executor.execute(() -> {
                            List<FocusSchedule> updatedSchedules = ScheduleStorage.loadSchedules(ScheduleSetupActivity.this);
                            runOnUiThread(() -> adapter.updateSchedules(updatedSchedules));
                        });
                    }
                });
                scheduleList.setAdapter(adapter);
            });
        });
    }

    private void setupNextButton() {
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            executor.execute(() -> {
                List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
                Log.d(TAG, "Checking saved schedules: " + schedules.size());

                runOnUiThread(() -> {
                    if (schedules.isEmpty()) {
                        Log.d(TAG, "No schedules found, showing toast");
                        Toast.makeText(this, "Please save at least one schedule", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Navigating to OnboardingCongratulationsAfterSchedule");
                        Intent intent = new Intent(this, OnboardingCongratulationsAfterSchedule.class);
                        startActivity(intent);
                        finish();
                    }
                });
            });
        });
    }

    @Override
    protected void onScheduleSaved() {
        Log.d(TAG, "onScheduleSaved called");
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setEnabled(true);
        Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            runOnUiThread(() -> {
                adapter.updateSchedules(schedules);
                Log.d(TAG, "Schedules after saving: " + schedules.size());
                for (FocusSchedule schedule : schedules) {
                    Log.d(TAG, "Schedule: Day=" + schedule.getDayOfWeek() +
                            ", Start=" + schedule.getStartHour() + ":" + schedule.getStartMinute() +
                            ", End=" + schedule.getEndHour() + ":" + schedule.getEndMinute());
                }
                
             });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}