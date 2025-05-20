package com.avdhaan;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UsageAccessRequestActivity extends AppCompatActivity {

    private Button btnGrantAccess;
    private Button btnDeclineAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_access_request);

        btnGrantAccess = findViewById(R.id.btn_grant_usage_access);
        btnDeclineAccess = findViewById(R.id.btn_decline_usage_access);

        btnGrantAccess.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        });

        btnDeclineAccess.setOnClickListener(view -> {
            Intent intent = new Intent(UsageAccessRequestActivity.this, NotReadyActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission(this)) {
            Intent intent = new Intent(this, FinalOnboardingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.unsafeCheckOpNoThrow("android:get_usage_stats",
                Binder.getCallingUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}