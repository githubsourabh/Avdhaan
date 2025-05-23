package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DEPRECATED_AccessGrantedActivity extends AppCompatActivity {

    private Button buttonSelectApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_granted);

        buttonSelectApps = findViewById(R.id.button_select_apps);
        buttonSelectApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DEPRECATED_AccessGrantedActivity.this, AppSelectionOnboardingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}