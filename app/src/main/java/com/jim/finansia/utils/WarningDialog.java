package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.jim.finansia.R;

/**
 * Created by DEV on 29.08.2016.
 */

public class WarningDialog extends Dialog {
    private TextView tv;
    private TextView title;
    View dialogView;
    public WarningDialog(Context context) {
        super(context);
        dialogView = getLayoutInflater().inflate(R.layout.warning_dialog, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        tv = ((TextView) dialogView.findViewById(R.id.tvWarningText));
        title = ((TextView) dialogView.findViewById(R.id.tvCatEditName));
        tv.setText(context.getResources().getString(R.string.currency_delete_warning));
    }

    public WarningDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected WarningDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    public void setMyTitle(String title){
        this.title.setText(title);
    }
    public void setText(String text) {
        tv.setText(text);
    }
    public void setOnYesButtonListener(View.OnClickListener yesButtonClickListener) {
        dialogView.findViewById(R.id.btnWarningYes).setOnClickListener(yesButtonClickListener);
    }
    public void setOnNoButtonClickListener(View.OnClickListener noButtonClickListener) {
        dialogView.findViewById(R.id.btnWarningNo).setOnClickListener(noButtonClickListener);
    }
}
