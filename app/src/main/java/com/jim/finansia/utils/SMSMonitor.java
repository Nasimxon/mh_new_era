package com.jim.finansia.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;

public class SMSMonitor extends BroadcastReceiver {
    private static final String SMS_EXTRA_NAME = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String messages = "";
        if (extras != null) {
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
            String address = "";
            for (int i = 0; i < smsExtra.length; ++i) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                String body = sms.getMessageBody().toString();
                address = sms.getOriginatingAddress();
                messages += body;
                Intent myIntent = new Intent(context, SmsService.class);
                myIntent.putExtra("number", address);
                myIntent.putExtra("body", messages.replace("\n", " "));
                myIntent.putExtra("date", sms.getTimestampMillis());
                context.startService(myIntent);
            }
        }
    }
}
