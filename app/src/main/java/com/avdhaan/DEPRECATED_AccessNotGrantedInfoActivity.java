package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DEPRECATED_AccessNotGrantedInfoActivity extends AppCompatActivity {

    private Button buttonGrantAccess;
    private Button buttonSkipAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_not_granted_info);

        buttonGrantAccess = findViewById(R.id.button_grant_access);
        buttonSkipAccess = findViewById(R.id.button_skip_access);

        buttonGrantAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
                // You may want to monitor access state and navigate accordingly later
            }
        });

        buttonSkipAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DEPRECATED_AccessNotGrantedInfoActivity.this, DEPRECATED_NoUsageAccessMessageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}