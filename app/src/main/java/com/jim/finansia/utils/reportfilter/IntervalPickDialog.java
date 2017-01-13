package com.jim.finansia.utils.reportfilter;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.jim.finansia.R;

import java.text.SimpleDateFormat;

public class IntervalPickDialog extends Dialog {
    private IntervalPickerView ipvDialog;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    public IntervalPickDialog(Context context) {
        super(context);
        init();
    }
    public IntervalPickDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }
    protected IntervalPickDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }
    void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.interval_pick_view_dialog);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        ipvDialog = (IntervalPickerView) findViewById(R.id.ipvDialog);
    }
    public void setListener(IntervalPickerView.IntervalPickListener listener) {
        if (ipvDialog != null)
            ipvDialog.setListener(listener);
    }
    public void saveState() {
        ipvDialog.saveState();
    }
}
