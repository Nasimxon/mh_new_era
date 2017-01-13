package com.jim.finansia.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;

import javax.inject.Inject;

public class SubcatAdapterCircles extends RelativeLayout {
    private ImageView ivBottom, ivMiddle;
    private ImageView ivTop;
    @Inject
    SharedPreferences preferences;
    public SubcatAdapterCircles(Context context) {
        super(context);
        init(context);
    }

    public SubcatAdapterCircles(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SubcatAdapterCircles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @SuppressLint("NewApi")
    public SubcatAdapterCircles(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        int bottom = (int) getResources().getDimension(R.dimen.twentytwo_dp);
        int middle = (int) getResources().getDimension(R.dimen.eightteen_dp);
        int top = (int) getResources().getDimension(R.dimen.fourteen_dp);
        LayoutParams bottomLp = new LayoutParams(bottom, bottom);
        bottomLp.addRule(CENTER_IN_PARENT);
        ivBottom = new ImageView(getContext());
        ivBottom.setColorFilter(Color.parseColor("#f1f1f1"));
        ivBottom.setLayoutParams(bottomLp);
        ivBottom.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle));
        LayoutParams middleLp = new LayoutParams(middle, middle);
        middleLp.addRule(CENTER_IN_PARENT);
        ivMiddle = new ImageView(getContext());
        ivMiddle.setLayoutParams(middleLp);
        ivMiddle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle));
        LayoutParams topLp = new LayoutParams(top, top);
        topLp.addRule(CENTER_IN_PARENT);
        ivTop = new ImageView(getContext());
        ivTop.setLayoutParams(topLp);
        ivTop.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_with_head_accendent));
        addView(ivBottom);
        addView(ivMiddle);
        addView(ivTop);
    }
}
