package com.jim.finansia.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by root on 11/13/16.
 */

public class MyVerticalViewPager extends VerticalViewPager {
    private boolean isSwipe = true;

    public MyVerticalViewPager(Context context) {
        super(context);
    }

    public MyVerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isSwipe ? super.onTouchEvent(ev) : false;
    }

    public void setSwipe(boolean isSwipe) {
        this.isSwipe = isSwipe;
    }
}