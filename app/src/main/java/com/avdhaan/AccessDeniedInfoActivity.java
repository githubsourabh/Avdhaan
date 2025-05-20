package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccessDeniedInfoActivity extends AppCompatActivity {

    private Button buttonGrantUsageAccess, buttonSkipUsageAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_denied_info);

        buttonGrantUsageAccess = findViewById(R.id.button_grant_usage_access);
        buttonSkipUsageAccess = findViewById(R.id.button_skip_usage_access);

        buttonGrantUsageAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        buttonSkipUsageAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccessDeniedInfoActivity.this, FinalFallbackActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}