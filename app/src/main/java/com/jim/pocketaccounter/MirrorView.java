package com.jim.pocketaccounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MirrorView extends View {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    private Bitmap mBitmap;
    private final float[] mVerts = new float[COUNT*2];
    private final float[] mOrig = new float[COUNT*2];

    private final Matrix mMatrix = new Matrix();
    private final Matrix mInverse = new Matrix();

    public MirrorView(Context context) {
        super(context);
        init(context);
    }

    public MirrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MirrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @SuppressLint("NewApi")
    public MirrorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFCCCCCC);
        canvas.concat(mMatrix);
        canvas.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setTextSize(14.0f);
        for (int i = 0; i < COUNT*2; i += 2) {
            float x = mVerts[i+0];
            float y = mVerts[i+1];
            canvas.drawText(String.valueOf(i), x, y, paint);
        }
    }

    private void init(Context context) {
        setFocusable(true);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        float w = mBitmap.getWidth();
        float h = mBitmap.getHeight();
        // construct our mesh
        int index = 0;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = h * y / HEIGHT;
            for (int x = 0; x <= WIDTH; x++) {
                float fx = w * x / WIDTH;
                setXY(mVerts, index, fx, fy);
                setXY(mOrig, index, fx, fy);
                index += 1;
            }
        }

        mMatrix.setTranslate(10, 10);
        mMatrix.invert(mInverse);
    }

    private static void setXY(float[] array, int index, float x, float y) {
        array[index*2 + 0] = x;
        array[index*2 + 1] = y;
    }



    private void warp(float cx, float cy) {
        final float K = 10000;
        float[] src = mOrig;
        float[] dst = mVerts;
        for (int i = 0; i < COUNT*2; i += 2) {
            float x = src[i+0];
            float y = src[i+1];
            float dx = cx - x;
            float dy = cy - y;
            float dd = dx*dx + dy*dy;
            float d = (float) Math.sqrt(dd);
            float pull = K / (dd + 0.000001f);

            pull /= (d + 0.000001f);
            //   android.util.Log.d("skia", "index " + i + " dist=" + d + " pull=" + pull);
            if (pull >= 1) {
                dst[i+0] = cx;
                dst[i+1] = cy;
            } else {
                dst[i+0] = x + dx * pull;
                dst[i+1] = y + dy * pull;
            }
        }
    }

    private void warp() {
        float[] src = mOrig;
        float[] dst = mVerts;
        for (int i = 0; i < COUNT*2; i += 2) {
            float x = src[i+0];
            float y = src[i+1];
            float dx = x + (i % WIDTH)*4;
            float dy = y + dx ;
            dst[i+0] = dx;
            dst[i+1] = dy;
        }
        invalidate();
    }

    private int mLastWarpX = -9999; // don't match a touch coordinate
    private int mLastWarpY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float[] pt = { event.getX(), event.getY() };
        mInverse.mapPoints(pt);

        int x = (int)pt[0];
        int y = (int)pt[1];
        if (mLastWarpX != x || mLastWarpY != y) {
            mLastWarpX = x;
            mLastWarpY = y;
//            warp(pt[0], pt[1]);
            warp();
            invalidate();
        }
        return true;
    }
}
