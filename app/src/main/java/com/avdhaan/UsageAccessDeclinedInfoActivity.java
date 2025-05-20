package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UsageAccessDeclinedInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_denied_info);

        Button buttonGrantAccess = findViewById(R.id.button_grant_access);
        Button buttonSkipAccess = findViewById(R.id.button_skip_usage_access);

        buttonGrantAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, UsageAccessSettingsActivity.class));
            finish();
        });

        buttonSkipAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
} 