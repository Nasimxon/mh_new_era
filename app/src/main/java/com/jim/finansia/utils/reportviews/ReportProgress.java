package com.jim.finansia.utils.reportviews;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.finansia.R;

public class ReportProgress extends RelativeLayout {
    private ImageView ivReportByCategoryPercentSubcat;
    private LinearLayout llRepByCatPercent, llRepByCatBg;
    private TextView tvReportAmount, tvTitle;
    private float maxValue = 10000.0f, value = 0.0f, fromValue = 0.0f;
    private String unit = "$", title = "";
    private ValueAnimator animator;
    private int duration = 500;
    private float minValue = 0.0f;
    public ReportProgress(Context context) {
        super(context);
        init(context);
    }
    public ReportProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ReportProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @SuppressLint("NewApi")
    public ReportProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.report_by_cat_progress, this, true);
        ivReportByCategoryPercentSubcat = (ImageView) findViewById(R.id.ivReportByCategoryPercentSubcat);
        llRepByCatPercent = (LinearLayout) findViewById(R.id.llRepByCatPercent);
        llRepByCatBg = (LinearLayout) findViewById(R.id.llRepByCatBg);
        tvReportAmount = (TextView) findViewById(R.id.tvReportAmount);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        minValue = getResources().getDimension(R.dimen.two_dp);
    }
    public float getMaxValue() { return maxValue; }
    public void setMaxValue(float maxValue) { this.maxValue = maxValue; }
    public float getValue() { return value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setValue(final float value) {
        this.value = value;
        tvReportAmount.setText(value + unit);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxWidth = llRepByCatBg.getWidth();
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llRepByCatPercent.getLayoutParams();
                float ratio = value/maxValue;
                if (value == 0.0f)
                    lp.width = (int) minValue;
                else
                    lp.width = (int) (ratio * maxWidth);
            }
        });
    }
    public void setImage(String icon) {
        int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
        ivReportByCategoryPercentSubcat.setImageDrawable(null);
        ivReportByCategoryPercentSubcat.setImageResource(resId);
    }
    public void animateProgress() {
        if (animator != null && animator.isStarted() && fromValue < value) return;
        animator = ValueAnimator.ofFloat(fromValue, value);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                setValue(value);
            }
        });
        animator.start();
    }
    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        tvTitle.setText(title);
    }
}
