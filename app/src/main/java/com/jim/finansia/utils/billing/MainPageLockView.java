package com.jim.finansia.utils.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;

import javax.inject.Inject;

public class MainPageLockView extends RelativeLayout{
    private int page;
    private ImageView ivLeftIcon, ivRightIcon, ivLock;
    private ImageView topBig,
                        topMiddle,
                        topSmall,
                        bottomBig,
                        bottomMiddle,
                        bottomSmall;
    private LockViewButtonClickListener buttonClickListener;
    private TextView tvLockPage;
    private RelativeLayout lockRoot, rightClick, leftClick;
    @Inject PurchaseImplementation purchaseImplementation;
    @Inject FinansiaFirebaseAnalytics analytics;
    public MainPageLockView(Context context, int page) {
        super(context);
        init(context, page);
    }
    public MainPageLockView(Context context, AttributeSet attrs, int page) {
        super(context, attrs);
        init(context, page);
    }
    public MainPageLockView(Context context, AttributeSet attrs, int defStyleAttr, int page) {
        super(context, attrs, defStyleAttr);
        init(context, page);
    }
    @SuppressLint("NewApi")
    public MainPageLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int page) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, page);
    }

    private void init(final Context context, int page) {
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        LayoutInflater.from(context).inflate(R.layout.lock_layout, this, true);
        this.page = page;
        lockRoot = (RelativeLayout) findViewById(R.id.lockRoot);
        lockRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                analytics.sendText("User wants to buy page, number is " + MainPageLockView.this.page);
                purchaseImplementation.buyPage(MainPageLockView.this.page);
            }
        });
        ivLeftIcon = (ImageView) findViewById(R.id.ivLeftIcon);
        ivRightIcon = (ImageView) findViewById(R.id.ivRightIcon);
        ivLock = (ImageView) findViewById(R.id.lock);
        tvLockPage = (TextView) findViewById(R.id.tvLockPage);
        topBig = (ImageView) findViewById(R.id.ivTopBig);
        topMiddle = (ImageView) findViewById(R.id.ivTopMiddle);
        topSmall = (ImageView) findViewById(R.id.ivTopSmall);
        bottomBig = (ImageView) findViewById(R.id.ivBottomBig);
        bottomMiddle = (ImageView) findViewById(R.id.ivBottomMiddle);
        bottomSmall = (ImageView) findViewById(R.id.ivBottomSmall);
        leftClick = (RelativeLayout) findViewById(R.id.leftClick);
        leftClick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonClickListener != null)
                    buttonClickListener.onLockViewButtonClickListener(false);
            }
        });
        rightClick = (RelativeLayout) findViewById(R.id.rightClick);
        rightClick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonClickListener != null)
                    buttonClickListener.onLockViewButtonClickListener(true);
            }
        });
        hideClouds();
    }

    public void setLockViewButtonClickListener(LockViewButtonClickListener lockViewButtonClickListener) {
        this.buttonClickListener = lockViewButtonClickListener;

    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            AlphaAnimation wholeAnim = new AlphaAnimation(0.0f, 1.0f);
            wholeAnim.setDuration(150);
            startAnimation(wholeAnim);
            animateClouds();
        } else {
            AlphaAnimation wholeAnim = new AlphaAnimation(1.0f, 0.0f);
            wholeAnim.setDuration(150);
            startAnimation(wholeAnim);
        }
    }
    public void animateClouds() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int topBeginPoint = width + width/4;
        int bottomBeginPoint = 0 - width/3;
        final int topBigDuration = 500,
                topMiddleDuration = 350,
                topMiddleOffset = 150,
                topSmallDuration = 400,
                topSmallOffset = 100,
                bottomBigDuration = 450,
                bottomBigOffset = 50,
                bottomMiddleDuration = 500,
                bottomSmallDuration = 300,
                bottomSmallOffset = 200;

        //top big imageviews animation
        TranslateAnimation aTopBig = new TranslateAnimation(topBeginPoint, 0, 0, 0);
        aTopBig.setDuration(topBigDuration);

        //top middle imageviews animation
        TranslateAnimation aTopMiddle = new TranslateAnimation(topBeginPoint, 0, 0, 0);
        aTopMiddle.setDuration(topMiddleDuration);
        aTopMiddle.setStartOffset(topMiddleOffset);

        //top small imageviews animation
        TranslateAnimation aTopSmall = new TranslateAnimation(topBeginPoint, 0, 0, 0);
        aTopSmall.setDuration(topSmallDuration);
        aTopSmall.setStartOffset(topSmallOffset);

        //bottom big imageviews animation
        TranslateAnimation aBottomBig = new TranslateAnimation(bottomBeginPoint, 0, 0, 0);
        aBottomBig.setDuration(bottomBigDuration);
        aBottomBig.setStartOffset(bottomBigOffset);

        //bottom middle imageviews animtion
        TranslateAnimation aBottomMiddle = new TranslateAnimation(bottomBeginPoint, 0, 0, 0);
        aBottomMiddle.setDuration(bottomMiddleDuration);

        //bottom small imageviews animtion
        TranslateAnimation aBottomSmall = new TranslateAnimation(bottomBeginPoint, 0, 0, 0);
        aBottomSmall.setDuration(bottomSmallDuration);
        aBottomSmall.setStartOffset(bottomSmallOffset);

        //starting all animations
        topBig.startAnimation(aTopBig);
        topMiddle.startAnimation(aTopMiddle);
        topSmall.startAnimation(aTopSmall);
        bottomBig.startAnimation(aBottomBig);
        bottomMiddle.startAnimation(aBottomMiddle);
        bottomSmall.startAnimation(aBottomSmall);
        topBig.setVisibility(VISIBLE);
        topMiddle.setVisibility(VISIBLE);
        topSmall.setVisibility(VISIBLE);
        bottomBig.setVisibility(VISIBLE);
        bottomMiddle.setVisibility(VISIBLE);
        bottomSmall.setVisibility(VISIBLE);
    }
    public void setPage(int page) {
        this.page = page;
        tvLockPage.setText(Integer.toString(page+1) + " " + getContext().getString(R.string.page));
    }

    public void hideClouds() {
        topBig.setVisibility(GONE);
        topMiddle.setVisibility(GONE);
        topSmall.setVisibility(GONE);
        bottomBig.setVisibility(GONE);
        bottomMiddle.setVisibility(GONE);
        bottomSmall.setVisibility(GONE);
    }

    public void animateButtons(boolean leftButton) {
        if (leftButton) {
            Animation left = AnimationUtils.loadAnimation(getContext(), R.anim.lock_left_arrow);
            ivLeftIcon.startAnimation(left);
        }
        else {
            Animation right = AnimationUtils.loadAnimation(getContext(), R.anim.lock_right_arrow);
            ivRightIcon.startAnimation(right);
        }
        Animation lock = AnimationUtils.loadAnimation(getContext(), R.anim.lock_anim);
        ivLock.startAnimation(lock);
    }

}
