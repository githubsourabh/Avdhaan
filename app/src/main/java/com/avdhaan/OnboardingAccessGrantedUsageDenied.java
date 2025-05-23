package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingAccessGrantedUsageDenied extends AppCompatActivity {

    private Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_access_granted_usage_denied);

        buttonContinue = findViewById(R.id.button_continue);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OnboardingAccessGrantedUsageDenied.this, AppSelectionOnboardingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
} 