package com.jim.pocketaccounter.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.finance.IconAdapterCategory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by DEV on 29.08.2016.
 */

public class DatePicker extends Dialog {

    private View dialogView;
    private TextView ivDatePickOk;
    private android.widget.DatePicker dp;
    public DatePicker(Context context) {
        super(context);
        dialogView = getLayoutInflater().inflate(R.layout.date_picker, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        dp = (android.widget.DatePicker) dialogView.findViewById(R.id.dp);
        ivDatePickOk = (TextView) dialogView.findViewById(R.id.ivDatePickOk);

        ImageView ivDatePickCancel = (ImageView) dialogView.findViewById(R.id.ivDatePickCancel);
        ivDatePickCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public DatePicker(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DatePicker(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnDatePickListener(final OnDatePickListener onDatePickListener) {
        ivDatePickOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, dp.getYear());
                calendar.set(Calendar.MONTH, dp.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                onDatePickListener.OnDatePick(calendar);
                dismiss();
            }
        });
    }
}
