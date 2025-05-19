package com.avdhaan;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class UsageTrackingDialog {
    private final Context context;
    private final UsageTrackingPreferences preferences;
    private final PermissionManager permissionManager;
    private final Callback callback;

    public interface Callback {
        void onTrackingEnabled();
        void onTrackingDisabled();
    }

    public UsageTrackingDialog(Context context, Callback callback) {
        this.context = context;
        this.preferences = new UsageTrackingPreferences(context);
        this.permissionManager = new PermissionManager(context);
        this.callback = callback;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_usage_tracking, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.usage_tracking_title)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button enableButton = dialogView.findViewById(R.id.button_enable);
        Button disableButton = dialogView.findViewById(R.id.button_disable);

        enableButton.setOnClickListener(v -> {
            if (!permissionManager.hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                context.startActivity(intent);
            }
            preferences.setTrackingEnabled(true);
            callback.onTrackingEnabled();
            Toast.makeText(context, R.string.tracking_enabled, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        disableButton.setOnClickListener(v -> {
            preferences.setTrackingEnabled(false);
            callback.onTrackingDisabled();
            Toast.makeText(context, R.string.tracking_disabled, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
} 