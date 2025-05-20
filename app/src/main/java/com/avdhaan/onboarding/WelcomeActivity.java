package com.avdhaan.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.PermissionExplanationActivity;
import com.avdhaan.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button buttonGetStarted = findViewById(R.id.button_get_started);
        buttonGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, PermissionExplanationActivity.class));
            finish();
        });
    }
}