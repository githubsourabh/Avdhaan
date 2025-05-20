// Final onboarding screen logic based on the latest validated state.

package com.avdhaan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import static com.avdhaan.PreferenceConstants.*;

public class OnboardingActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingActivity";
    private Button startButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Initialize SharedPreferences
            prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);

            if (!isFirstTime) {
                Log.d(TAG, "Not first time, redirecting to MainActivity");
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            setContentView(R.layout.activity_onboarding);

            startButton = findViewById(R.id.button_start);
            if (startButton == null) {
                Log.e(TAG, "Start button not found in layout");
                Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            startButton.setOnClickListener(view -> {
                try {
                    // Navigate to permission step
                    Intent intent = new Intent(OnboardingActivity.this, PermissionExplanationActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error starting PermissionExplanationActivity", e);
                    Toast.makeText(OnboardingActivity.this, "Error starting app", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
