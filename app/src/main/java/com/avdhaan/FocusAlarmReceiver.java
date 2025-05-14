
package com.avdhaan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FocusAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent blockIntent = new Intent(context, AppBlockService.class);
        context.startService(blockIntent);
    }
}
