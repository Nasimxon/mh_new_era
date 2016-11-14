package com.jim.pocketaccounter.utils.viewpagers;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class PAHorizontalViewPager extends ViewPager {
    public PAHorizontalViewPager(Context context) {
        super(context);
    }

    public PAHorizontalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    private float oldY;
//
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            oldY = ev.getY();
//        }
//        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//            if (Math.abs(oldY-getY()) >= 150)
//                ((PAVerticalViewPager) getParent().getParent()).setPaging(false, ev);
//        }
        return super.onTouchEvent(ev);
    }
}