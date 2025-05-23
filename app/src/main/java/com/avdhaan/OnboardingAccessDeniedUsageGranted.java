package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingAccessDeniedUsageGranted extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_access_denied_usage_granted);

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FinalActivityBeforeMainActivity.class);
            startActivity(intent);
            finish();
        });
    }
} 