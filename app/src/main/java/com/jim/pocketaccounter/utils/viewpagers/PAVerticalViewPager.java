package com.jim.pocketaccounter.utils.viewpagers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import me.kaelaela.verticalviewpager.VerticalViewPager;

public class PAVerticalViewPager extends VerticalViewPager {
    float oldX, oldY;
    boolean paging = false;

    public PAVerticalViewPager(Context context) {
        super(context);

    }

    public void setPaging(boolean paging, MotionEvent event) {
        this.paging = paging;
        dispatchTouchEvent(event);
    }

    public boolean isPagin() {
        return paging;
    }

    public PAVerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("sss", "dispatcher");
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("sss", "intercept " + paging);
        if (paging) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d("sss", "onTouchEvent");
        super.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            oldX = ev.getX();
            oldY = ev.getY();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(oldX - ev.getX()) >= 50) {
                paging = true;
                super.onInterceptTouchEvent(ev);

            }
            if (Math.abs(oldY - ev.getY()) >= 50) {
                paging = false;
                super.onInterceptTouchEvent(ev);
            }
            super.requestDisallowInterceptTouchEvent(false);
        }
        return super.onTouchEvent(ev);
    }
}
