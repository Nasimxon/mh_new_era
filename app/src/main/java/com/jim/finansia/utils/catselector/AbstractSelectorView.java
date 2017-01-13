package com.jim.finansia.utils.catselector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class AbstractSelectorView extends LinearLayout {
    protected int position = 0, count, lastPosition = 0;
    public AbstractSelectorView(Context context) {
        super(context);
    }

    public AbstractSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public AbstractSelectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    void incPosition() {
        lastPosition = position;
        if (position == count-1)
            position = 0;
        else
            position++;
    }
    void decPosition() {
        lastPosition = position;
        if (position == 0)
            position = count-1;
        else
            position--;
    }
}
