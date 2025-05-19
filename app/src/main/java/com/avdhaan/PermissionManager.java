package com.avdhaan;

import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class PermissionManager {
    private static final String TAG = "PermissionManager";
    private static final String NOTIFICATION_CHANNEL_ID = "permission_alerts";
    private static final int PERMISSION_NOTIFICATION_ID = 1001;

    private final Context context;
    private final UsageTrackingPreferences preferences;

    public PermissionManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = new UsageTrackingPreferences(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.usage_tracking_title),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.usage_tracking_description));
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void checkAndUpdatePermissionState() {
        boolean currentState = hasUsageStatsPermission();
        boolean wasEnabled = preferences.isTrackingEnabled();
        
        if (!currentState && wasEnabled) {
            // Permission was revoked externally
            showPermissionRemovedNotification();
            preferences.setTrackingEnabled(false);
            Log.d(TAG, "Usage access permission revoked externally");
        } else if (currentState && !wasEnabled && preferences.wasTrackingEnabledBefore()) {
            // Permission was granted externally, and tracking was previously enabled
            showPermissionRestoredNotification();
            preferences.setTrackingEnabled(true);
            Log.d(TAG, "Usage access permission restored externally");
        }
    }

    private void showPermissionRestoredNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.usage_access_restored_title))
                .setContentText(context.getString(R.string.usage_access_restored_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(PERMISSION_NOTIFICATION_ID + 1, builder.build());
            Log.d(TAG, "Showing permission restored notification");
        }
    }

    private void showPermissionRemovedNotification() {
        Intent settingsIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.usage_access_removed_title))
                .setContentText(context.getString(R.string.usage_access_removed_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_settings, 
                          context.getString(R.string.enable_usage_access),
                          pendingIntent);

        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(PERMISSION_NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Showing permission removed notification");
        }
    }

    public boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
} 