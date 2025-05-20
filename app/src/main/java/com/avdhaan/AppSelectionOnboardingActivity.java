package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;

public class AppSelectionOnboardingActivity extends AppSelectionActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection_onboarding);
        
        // Initialize views and setup click listeners
        setupViews();
    }

    @Override
    protected void onProceedClick() {
        // Navigate to schedule setup
        Intent intent = new Intent(this, ScheduleSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
} 