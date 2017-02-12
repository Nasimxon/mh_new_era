package com.jim.finansia.managers;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;
import com.jim.finansia.fragments.SearchFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.jim.finansia.R.color.toolbar_text_color;

public class ToolbarManager {
    private Toolbar toolbar;
    private Context context;
    private ImageView ivToolbarFirst, ivToolbarSecond, ivToolbarStart;
    private EditText etToolbarSearch;
    private Handler whenKeyboardClosed;
    private TextView tvToolbarTitle, tvToolbarSubtitle;
    private ImageView ivSubtitle;
    private LinearLayout llToolbarTitle;
    private View.OnClickListener listener;
    public void setTitle(String title){
        toolbar.setTitle("");
        toolbar.setSubtitle(" ");
        tvToolbarTitle.setText(title);
    }
    public ImageView getSubtitleIcon() {
        return ivSubtitle;
    }
    public void setSubtitle(String title) {
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        if (title == null || title.equals("")) {
            tvToolbarSubtitle.setVisibility(View.GONE);
            ivSubtitle.setVisibility(View.GONE);
        }
        else {
            ivSubtitle.setVisibility(View.VISIBLE);
            tvToolbarSubtitle.setVisibility(View.VISIBLE);
            tvToolbarSubtitle.setText(title);
        }
    }
    public void setBackgroundColor(int color) {
        toolbar.setBackgroundColor(color);
    }
    public void setSubtitleIconVisibility(int visibility) {
        ivSubtitle.setVisibility(visibility);
    }
    public ToolbarManager(Context context, Toolbar toolbar) {
        this.context = context;
        this.toolbar = toolbar;
        Log.d("tolbartest", "ToolbarManager: " +toolbar==null?"Toolbar is null":"Toolbar not null");
        ivToolbarFirst = (ImageView) toolbar.findViewById(R.id.ivToolbarExcel);
        ivToolbarSecond = (ImageView) toolbar.findViewById(R.id.ivToolbarMostRight);
        ivToolbarStart = (ImageView) toolbar.findViewById(R.id.ivToolbarSearch);
        etToolbarSearch = (EditText) toolbar.findViewById(R.id.etToolbarSearch);
        tvToolbarTitle = (TextView) toolbar.findViewById(R.id.tvToolbarTitle);
        tvToolbarSubtitle = (TextView) toolbar.findViewById(R.id.tvToolbarSubtitle);
        ivSubtitle = (ImageView) toolbar.findViewById(R.id.ivSubtitle);
        llToolbarTitle = (LinearLayout) toolbar.findViewById(R.id.llToolbarTitle);
        this.toolbar.setTitle("");
        this.toolbar.setSubtitle("");
    }
    public void setOnSearchButtonClickListener(View.OnClickListener clickListener) {
        if (clickListener != null)
            ivToolbarStart.setOnClickListener(clickListener);
    }
    public void init() {
        ((PocketAccounter) context).setSupportActionBar(toolbar);
        tvToolbarTitle.setTextColor(ContextCompat.getColor(context, toolbar_text_color));
        ((PocketAccounter) context).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setOnFirstImageClickListener(View.OnClickListener listener) {
        ivToolbarFirst.setOnClickListener(listener);
    }
    public void setOnSecondImageClickListener(View.OnClickListener listener) {
        ivToolbarSecond.setOnClickListener(listener);
    }
    public void setVisiblityEditSearch(){
        etToolbarSearch.setVisibility(View.GONE);
    }
    public void setOnHomeButtonClickListener(View.OnClickListener listener) {
        toolbar.setNavigationOnClickListener(listener);
    }
    public void setToolbarIconsVisibility(int start, int first, int second) {
        ivToolbarFirst.setVisibility(first);
        ivToolbarSecond.setVisibility(second);
        ivToolbarStart.setVisibility(start);
    }
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    public EditText getToolbarSearch() {
        return etToolbarSearch;
    }
    public void enableSearchMode( ){
        setImageToHomeButton(R.drawable.ic_back_button);
        etToolbarSearch.setVisibility(View.VISIBLE);
        etToolbarSearch.setFocusableInTouchMode(true);
        tvToolbarTitle.setVisibility(View.GONE);
        ivSubtitle.setVisibility(View.GONE);
        tvToolbarSubtitle.setVisibility(View.GONE);
        setToolbarIconsVisibility(View.VISIBLE,View.GONE,View.GONE);
        ivToolbarStart.setImageResource(R.drawable.ic_close_black_24dp);
    }

    public void disableSearchMode(){
        etToolbarSearch.setVisibility(View.GONE);
        etToolbarSearch.setText("");
        setImageToHomeButton(R.drawable.ic_drawer);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        tvToolbarSubtitle.setVisibility(View.VISIBLE);
        ivToolbarFirst.setVisibility(View.GONE);
        ivToolbarSecond.setVisibility(View.VISIBLE);
        ivToolbarStart.setImageResource(R.drawable.ic_search_black_24dp);
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null)
            imm.hideSoftInputFromWindow(etToolbarSearch.getWindowToken(), 0);
    }

    public void setImageToStartImage(int resId) {
        ivToolbarStart.setImageDrawable(null);
        ivToolbarStart.setImageResource(resId);
    }
    public void setImageToFirstImage(int resId) {
        ivToolbarFirst.setImageDrawable(null);
        ivToolbarFirst.setImageResource(resId);
    }
    public ImageView setImageToSecondImage(int resId) {
        ivToolbarSecond.setImageDrawable(null);
        ivToolbarSecond.setImageResource(resId);
        return null;
    }
    public void setImageToHomeButton(int resId) {
        ((PocketAccounter) context).getSupportActionBar().setHomeAsUpIndicator(resId);
    }
    public void setOnTitleClickListener(View.OnClickListener listener) {
        this.listener = listener;
        llToolbarTitle.setOnClickListener(this.listener);
    }
    public void disableTitleClick() {
        llToolbarTitle.setOnClickListener(null);
    }
}
