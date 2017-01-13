package com.jim.finansia.utils.viewpagers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

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
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            oldX = event.getX();
            oldY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(oldX - event.getX()) >= 30) {
                return super.onInterceptTouchEvent(event);
            } else if (Math.abs(oldY - event.getY()) >= 30) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            oldX = ev.getX();
            oldY = ev.getY();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(oldY - ev.getY()) >= 30) {
                onInterceptTouchEvent(ev);
            }
        }
        return super.onTouchEvent(ev);
    }
}
