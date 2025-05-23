package com.avdhaan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import static com.avdhaan.PreferenceConstants.*;

public class ScheduleSetupActivity extends BaseScheduleActivity {
    private static final String TAG = "ScheduleSetupActivity";
    private RecyclerView scheduleList;
    private ScheduleListAdapter adapter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_schedule_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing ScheduleSetupActivity");
        setupNextButton();
        setupScheduleList();
    }

    private void setupScheduleList() {
        scheduleList = findViewById(R.id.scheduleList);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleListAdapter(this, ScheduleStorage.loadSchedules(this), new ScheduleListAdapter.OnScheduleUpdated() {
            @Override
            public void onEdit(int position) {
                // Handle edit if needed
            }

            @Override
            public void onDelete(int position) {
                List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(ScheduleSetupActivity.this);
                schedules.remove(position);
                ScheduleStorage.saveSchedules(ScheduleSetupActivity.this, schedules);
                adapter.updateSchedules(schedules);
            }

            @Override
            public void onScheduleUpdated() {
                List<FocusSchedule> updatedSchedules = ScheduleStorage.loadSchedules(ScheduleSetupActivity.this);
                adapter.updateSchedules(updatedSchedules);
            }
        });
        scheduleList.setAdapter(adapter);
    }

    private void setupNextButton() {
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            // Check if any schedule is saved
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            Log.d(TAG, "Checking saved schedules: " + schedules.size());
            
            if (schedules.isEmpty()) {
                Log.d(TAG, "No schedules found, showing toast");
                Toast.makeText(this, "Please save at least one schedule", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Navigate to final activity
            Log.d(TAG, "Preparing to navigate to FinalActivityBeforeMainActivity");
            Intent intent = new Intent(this, FinalActivityBeforeMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "Starting FinalActivityBeforeMainActivity");
            startActivity(intent);
            Log.d(TAG, "Finishing ScheduleSetupActivity");
            finish();
        });
    }

    @Override
    protected void onScheduleSaved() {
        Log.d(TAG, "onScheduleSaved called");
        // Enable next button after schedule is saved
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setEnabled(true);
        Log.d(TAG, "Next button enabled");
        
        // Show success message
        Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show();
        
        // Update the RecyclerView with new schedules
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        adapter.updateSchedules(schedules);
        
        // Log the saved schedules
        Log.d(TAG, "Schedules after saving: " + schedules.size());
        for (FocusSchedule schedule : schedules) {
            Log.d(TAG, "Schedule: Day=" + schedule.dayOfWeek + 
                      ", Start=" + schedule.startHour + ":" + schedule.startMinute +
                      ", End=" + schedule.endHour + ":" + schedule.endMinute);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}