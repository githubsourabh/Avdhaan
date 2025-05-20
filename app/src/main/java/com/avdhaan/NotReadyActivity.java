package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.utils.OnboardingUtils;

public class NotReadyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_ready);

        Button buttonAgree = findViewById(R.id.btn_agree_grant_usage_access);
        Button buttonDecline = findViewById(R.id.btn_decline_grant_usage_access);

        buttonAgree.setOnClickListener(v -> {
            startActivity(new Intent(this, UsageAccessPermissionActivity.class));
            finish();
        });

        buttonDecline.setOnClickListener(v -> {
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