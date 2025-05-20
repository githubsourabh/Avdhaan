package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FinalOnboardingActivity extends AppCompatActivity {

    private Button btnGoToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_onboarding);

        btnGoToMain = findViewById(R.id.btn_go_to_main);

        btnGoToMain.setOnClickListener(view -> {
            Intent intent = new Intent(FinalOnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}