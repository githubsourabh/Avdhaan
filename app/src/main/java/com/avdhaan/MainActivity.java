package com.avdhaan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(this, "MainActivity loaded", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.open_accessibility);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }

        });

        Button scheduleButton = findViewById(R.id.schedule_button);
        scheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            startActivity(intent);

        });

        Button selectAppsButton = findViewById(R.id.btn_select_apps);
        selectAppsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectAppsActivity.class);
            startActivity(intent);
        });


    }


}
