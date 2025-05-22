package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FinalFallbackActivity extends AppCompatActivity {

    private Button buttonProceedAnyway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_fallback);

        buttonProceedAnyway = findViewById(R.id.button_proceed_anyway);

        buttonProceedAnyway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinalFallbackActivity.this, FinalActivityBeforeMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}