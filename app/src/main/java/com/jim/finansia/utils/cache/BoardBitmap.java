package com.jim.finansia.utils.cache;

import android.graphics.Bitmap;

/**
 * Created by root on 9/26/16.
 */

public class BoardBitmap {
    private Bitmap bitmap;
    private Integer pos;
    public Bitmap getBitmap() {
        return bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    public Integer getPos() {
        return pos;
    }
    public void setPos(Integer pos) {
        this.pos = pos;
    }
}
