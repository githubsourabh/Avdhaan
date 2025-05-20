package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.utils.OnboardingUtils;

public class PermissionIntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_explanation);

        Button buttonGrantAccess = findViewById(R.id.button_grant_access);
        Button buttonSkipAccess = findViewById(R.id.button_skip_access);

        buttonGrantAccess.setOnClickListener(v -> {
            startActivity(OnboardingUtils.getAccessibilitySettingsIntent());
        });

        buttonSkipAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, UsageAccessInfoActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OnboardingUtils.hasAccessibilityPermission(this, getString(R.string.accessibility_service_name))) {
            startActivity(new Intent(this, AccessGrantedActivity.class));
            finish();
        }
    }
} 