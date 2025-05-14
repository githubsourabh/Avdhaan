package com.avdhaan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class FocusEndReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show a toast or perform cleanup
        Toast.makeText(context, "Focus mode ended", Toast.LENGTH_SHORT).show();

        // Optionally stop your AppBlockService or set a flag to disable blocking
        Intent serviceIntent = new Intent(context, AppBlockService.class);
        context.stopService(serviceIntent);
    }
}
