package com.jim.finansia.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by developer on 17.01.2017.
 */

public class TintImageView extends ImageView {
    public TintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList imageTintList = getImageTintList();
            if (imageTintList == null) {
                return;
            }

            setColorFilter(imageTintList.getDefaultColor(), PorterDuff.Mode.SRC_IN);
        }
    }

}
