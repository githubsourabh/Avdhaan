package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FinalOnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_onboarding);

        Button continueButton = findViewById(R.id.button_go_to_main);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(FinalOnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}