package com.avdhaan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import static com.avdhaan.PreferenceConstants.*;

public class FinalActivityBeforeMainActivity extends AppCompatActivity {
    private static final String TAG = "FinalActivityBeforeMainActivity";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Mark onboarding as completed
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();

        // Automatically proceed to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}