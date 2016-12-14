package com.jim.pocketaccounter.utils.reportfilter;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.jim.pocketaccounter.R;

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
        ipvDialog = (IntervalPickerView) findViewById(R.id.ipvDialog);
    }
    public void setListener(IntervalPickerView.IntervalPickListener listener) {
        if (ipvDialog != null)
            ipvDialog.setListener(listener);
    }
}
