package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class NoUsageAccessMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_not_granted_info);

        Button buttonGrantAccess = findViewById(R.id.button_grant_access);
        Button buttonSkipAccess = findViewById(R.id.button_skip_usage_access);

        buttonGrantAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, UsageAccessPermissionActivity.class));
            finish();
        });

        buttonSkipAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OnboardingUtils.hasUsageStatsPermission(this)) {
            startActivity(new Intent(this, AppSelectionOnboardingActivity.class));
            finish();
        }
    }
} 