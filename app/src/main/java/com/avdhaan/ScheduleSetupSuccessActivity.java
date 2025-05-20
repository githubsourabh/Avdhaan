package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ScheduleSetupSuccessActivity extends AppCompatActivity {

    private Button buttonGoToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_setup_success);

        buttonGoToMain = findViewById(R.id.button_go_to_main);

        buttonGoToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScheduleSetupSuccessActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}