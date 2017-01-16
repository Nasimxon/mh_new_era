package com.jim.finansia.utils;

import android.animation.ValueAnimator;
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

public class PercentView extends RelativeLayout {

    private TextView tvNotActiveSide, tvActiveSide, tvPercent;
    private ImageView ivPercentStripe, ivEmptySide;
    private int percent, tempPercent;
    private ValueAnimator animator;
    private RelativeLayout rlBowArrow, rlTop;
    private boolean isBowArrowVisible = true;
    private boolean isPercentStripeVisible = true;

    private RelativeLayout llPercentRoot;
    public PercentView(Context context) {
        super(context);
        init(context);
    }

    public PercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("NewApi")
    public PercentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.percent_view, this, true);
        tvNotActiveSide = (TextView) findViewById(R.id.tvNotActiveSide);
        tvActiveSide = (TextView) findViewById(R.id.tvActiveSide);
        ivPercentStripe = (ImageView) findViewById(R.id.ivPercentStripe);
        ivEmptySide = (ImageView) findViewById(R.id.ivEmptySide);
        tvPercent = (TextView) findViewById(R.id.tvPercent);
        rlBowArrow = (RelativeLayout) findViewById(R.id.rlBowArrow);
        rlTop = (RelativeLayout) findViewById(R.id.rlTop);
        llPercentRoot = (RelativeLayout) findViewById(R.id.llPercentRoot);
    }

    public void setPercent(final int percent) {
        this.percent = percent;
        tvPercent.setText(percent + "%");
        this.tempPercent = percent;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //for percent indicator
                int temp = tempPercent*getWidth()/100;
                if (temp == 0)
                    temp = (int) getResources().getDimension(R.dimen.two_dp);
                LinearLayout.LayoutParams percentLp = (LinearLayout.LayoutParams) ivPercentStripe.getLayoutParams();
                percentLp.width = temp;
                ivPercentStripe.setLayoutParams(percentLp);
                LinearLayout.LayoutParams notActiveSideLp = (LinearLayout.LayoutParams) ivEmptySide.getLayoutParams();
                notActiveSideLp.width = getWidth() - temp;
                ivEmptySide.setLayoutParams(notActiveSideLp);
                //bow arrow percent
                if (isBowArrowVisible) {
                    int realWidth = getWidth() - (int) getResources().getDimension(R.dimen.twenty_dp);
                    LayoutParams lpArrow = (LayoutParams) rlBowArrow.getLayoutParams();
                    lpArrow.width = tempPercent*realWidth/100 <= (int) getResources().getDimension(R.dimen.twenty_dp) ?
                            (int) getResources().getDimension(R.dimen.twenty_dp) :
                            tempPercent*realWidth/100;
                    rlBowArrow.setLayoutParams(lpArrow);
                }
            }
        });
    }

    public void setTopText(String topText) {
        tvActiveSide.setText(topText);
    }

    public void setBottomText(String bottomText) {
        tvNotActiveSide.setText(bottomText);
    }

    public void animatePercent(int fromPercent, int duration) {
        if (animator != null && animator.isStarted() && fromPercent < percent) return;
        animator = ValueAnimator.ofInt(fromPercent, percent);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) (animation.getAnimatedValue())).intValue();
                setPercent(value);
            }
        });
        animator.start();
    }

    public void setBowArrowVisibility(boolean isBowArrowVisible) {
        this.isBowArrowVisible = isBowArrowVisible;
        rlTop.setVisibility(isBowArrowVisible ? VISIBLE : GONE);
    }
    public void setVisibilityOfPercent(boolean isVisible) {
        if (isVisible)
            tvPercent.setVisibility(VISIBLE);
        else
            tvPercent.setVisibility(GONE);
    }
    public void setNotActiveSideTextVisibility(boolean isVisible) {
        if (isVisible)
            tvNotActiveSide.setVisibility(VISIBLE);
        else
            tvNotActiveSide.setVisibility(GONE);
    }
    public void setPercetStripeVisibility(boolean isPercentStripeVisible) {
        this.isPercentStripeVisible = isPercentStripeVisible;
        llPercentRoot.setVisibility(isPercentStripeVisible ? VISIBLE : GONE);
    }
}
