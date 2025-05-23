package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DEPRECATED_AppBlockerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        Button buttonProceedToSchedule = findViewById(R.id.button_proceed_schedule);

        buttonProceedToSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, ScheduleBlockingActivity.class));
            finish();
        });
    }
} 