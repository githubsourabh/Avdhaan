
package com.avdhaan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FocusEndReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent stopIntent = new Intent(context, AppBlockService.class);
        context.stopService(stopIntent);
    }
}
