package com.jim.pocketaccounter.managers;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FinansiaFirebaseAnalytics {
    private FirebaseAnalytics mFirebaseAnalytics;
    private Context context;
    public FinansiaFirebaseAnalytics(Context context) {
        this.context = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void sendText(String text) {
        Bundle params = new Bundle();
        params.putString("model_name", Build.MODEL);
        params.putString("core_count", "Core count: " + Runtime.getRuntime().availableProcessors());
        params.putString("memory_size", "Free memory size: " + Runtime.getRuntime().freeMemory() +
                "\n Max memory size: " + Runtime.getRuntime().maxMemory());
        params.putString("boot_loader", Build.BOOTLOADER);
        params.putString("device", Build.DEVICE);
        params.putString("brand", Build.BRAND);
        params.putString("display", Build.DISPLAY);
        params.putString("hardware", Build.HARDWARE);
        params.putString("user", Build.USER);
        params.putString("product", Build.PRODUCT);
        params.putString("full_text", text);
        if (mFirebaseAnalytics == null)
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.logEvent("analytics_data", params);
    }
}
