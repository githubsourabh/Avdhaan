
package com.avdhaan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FocusAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("FocusAlarmReceiver", "Focus schedule triggered");

        // Start the app block service
        Intent serviceIntent = new Intent(context, AppBlockService.class);
        context.startService(serviceIntent);

        // Optional: show a notification or toast (simplified for now)
    }
}
