package com.jim.pocketaccounter.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import com.jim.pocketaccounter.R;

/**
 * Created by developer on 04.11.2016.
 */

public class GetterAttributColors {
    public static int fetchHeadColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.headColor });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int fetchHeadAccedentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.headAccedent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
    public static int fetchStatusBarColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
