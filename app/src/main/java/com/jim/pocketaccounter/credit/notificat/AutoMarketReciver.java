package com.jim.pocketaccounter.credit.notificat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by root on 9/20/16.
 */
public class AutoMarketReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "keldi", Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals("android.intent.action.TIME_TICK")) {
            Toast.makeText(context, "time set", Toast.LENGTH_SHORT).show();
        }
        if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
            Toast.makeText(context, "data change", Toast.LENGTH_SHORT).show();
        }
        intent = new Intent(context, AutoMarketService.class);
        context.startService(intent);
    }
}
