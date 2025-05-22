package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.OnboardingUtils;

public class AccessDeniedExplanationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_denied_explanation);

        Button buttonGrantAccess = findViewById(R.id.button_grant_usage_access);
        Button buttonDeclineAccess = findViewById(R.id.button_decline_usage_access);

        buttonGrantAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, UsageAccessPermissionActivity.class));
            finish();
        });

        buttonDeclineAccess.setOnClickListener(v -> {
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