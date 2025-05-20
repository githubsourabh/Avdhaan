package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UsageAccessSettingsActivity extends AppCompatActivity {

    private Button buttonGrantPermission;
    private Button buttonProceedAfterPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_access_settings);

        buttonGrantPermission = findViewById(R.id.button_grant_permission);
        buttonProceedAfterPermission = findViewById(R.id.button_proceed_after_permission);

        buttonGrantPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        buttonProceedAfterPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // After granting access, proceed to app selection
                Intent intent = new Intent(UsageAccessSettingsActivity.this, AppSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}