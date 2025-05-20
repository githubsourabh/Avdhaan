package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AppSelectionSuccessActivity extends AppCompatActivity {

    private Button buttonProceedToSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection_success);

        buttonProceedToSchedule = findViewById(R.id.button_proceed_to_schedule);

        buttonProceedToSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSelectionSuccessActivity.this, ScheduleBlockingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}