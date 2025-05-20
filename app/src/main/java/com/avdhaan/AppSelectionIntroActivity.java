package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AppSelectionIntroActivity extends AppCompatActivity {

    private Button buttonSelectApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection_intro);

        buttonSelectApps = findViewById(R.id.button_select_apps);

        buttonSelectApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSelectionIntroActivity.this, AppBlockerSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}