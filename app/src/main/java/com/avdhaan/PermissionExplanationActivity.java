package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.OnboardingUtils;

public class PermissionExplanationActivity extends AppCompatActivity {

    private static final String TAG = "PermissionExplanation";
    private boolean isCheckingPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_explanation);

        TextView titleTextView = findViewById(R.id.textViewTitle);
        TextView messageTextView = findViewById(R.id.textViewMessage);
        Button buttonGrantAccess = findViewById(R.id.buttonGrantAccess);
        Button buttonSkipAccess = findViewById(R.id.buttonSkipAccess);

        titleTextView.setText(R.string.grant_accessibility_title);
        messageTextView.setText(R.string.grant_accessibility_message);

        buttonGrantAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        buttonSkipAccess.setOnClickListener(v -> {
            startActivity(new Intent(this, OnboardingAccessDeniedContinueForUsage.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCheckingPermission) {
            return;
        }
        
        isCheckingPermission = true;
        Log.d(TAG, "onResume: Checking accessibility permission");
        
        // Check if accessibility service is enabled
        String serviceName = getString(R.string.accessibility_service_name);
        Log.d(TAG, "Checking for service: " + serviceName);
        
        if (OnboardingUtils.hasAccessibilityPermission(this, serviceName)) {
            Log.d(TAG, "onResume: Accessibility permission granted, proceeding to next screen");
            // Update preferences when accessibility is granted
            OnboardingUtils.updateAccessibilityState(this);
            // If accessibility service is enabled, proceed to usage access permission
            Intent intent = new Intent(this, UsageAccessPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "onResume: Accessibility permission not granted yet");
            Toast.makeText(this, "Please enable accessibility service", Toast.LENGTH_SHORT).show();
        }
        isCheckingPermission = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isCheckingPermission = false;
    }
}