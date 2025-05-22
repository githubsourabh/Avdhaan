package com.avdhaan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;

import static com.avdhaan.PreferenceConstants.*;

public class ScheduleSetupActivity extends BaseScheduleActivity {
    private static final String TAG = "ScheduleSetupActivity";
    private static final String ONBOARDING_PREFS = "AvdhaanPrefs";
    private static final String KEY_FIRST_TIME = "isFirstTime";

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_schedule_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing ScheduleSetupActivity");
        setupNextButton();
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

            // Mark onboarding as completed
            SharedPreferences.Editor onboardingEditor = getSharedPreferences(ONBOARDING_PREFS, MODE_PRIVATE).edit();
            onboardingEditor.putBoolean(KEY_FIRST_TIME, false);
            onboardingEditor.apply();
            Log.d(TAG, "Onboarding marked as completed");
            
            // Navigate to main activity
            Log.d(TAG, "Preparing to navigate to MainActivity");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from_onboarding", true);
            Log.d(TAG, "Starting MainActivity with from_onboarding=true");
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
        
        // Log the saved schedules
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
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