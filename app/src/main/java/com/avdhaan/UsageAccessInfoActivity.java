package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UsageAccessInfoActivity extends AppCompatActivity {

    private Button buttonGrantAccess;
    private Button buttonSkipAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_access_info);

        buttonGrantAccess = findViewById(R.id.button_grant_access);
        buttonSkipAccess = findViewById(R.id.button_skip_access);

        buttonGrantAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsageAccessInfoActivity.this, UsageAccessSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonSkipAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsageAccessInfoActivity.this, UsageAccessDeclinedInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}