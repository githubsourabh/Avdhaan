package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.OnboardingUtils;
import com.avdhaan.UsageTrackingPreferences;

public class UsageAccessPermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_access_request);

        Button buttonGrantAccess = findViewById(R.id.btn_grant_usage_access);
        Button buttonDeclineAccess = findViewById(R.id.btn_decline_usage_access);

        buttonGrantAccess.setOnClickListener(v -> {
            startActivity(OnboardingUtils.getUsageAccessSettingsIntent());
        });

        buttonDeclineAccess.setOnClickListener(v -> {
            // Set tracking as disabled when user declines
            new UsageTrackingPreferences(this).setTrackingEnabled(false);
            startActivity(new Intent(this, OnboardingAccessGrantedUsageDenied.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OnboardingUtils.hasUsageStatsPermission(this)) {
            // Update preferences when usage stats permission is granted
            OnboardingUtils.updateUsageStatsState(this);
            // Set tracking as enabled when permission is granted
            new UsageTrackingPreferences(this).setTrackingEnabled(true);
            startActivity(new Intent(this, AppSelectionOnboardingActivity.class));
            finish();
        }
    }
} 