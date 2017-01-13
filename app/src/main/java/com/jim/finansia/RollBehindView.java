package com.jim.finansia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RollBehindView extends View {
    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);
    private final float[] mVerts = new float[COUNT*2];
    private final float[] mOrig = new float[COUNT*2];
    public static final int VERTICAL_TO_UP = 0, VERTICAL_TO_BOTTOM = 1, HORIZONTAL_TO_RIGHT = 2, HORIZONTAL_TO_LEFT = 3;
    private Matrix mMatrix = new Matrix();
    private Matrix mInverse = new Matrix();
    private Bitmap bitmap;
    private int orientation = VERTICAL_TO_UP;

    private float height, width;
    private boolean isShadowVisible = true;
    public RollBehindView(Context context) {
        super(context);
        init();
    }
    public RollBehindView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RollBehindView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    @SuppressLint("NewApi")
    public RollBehindView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setFocusable(true);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        // construct our mesh
        int index = 0;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = height * y / HEIGHT;
            for (int x = 0; x <= WIDTH; x++) {
                float fx = width * x / WIDTH;
                setXY(mVerts, index, fx, fy);
                setXY(mOrig, index, fx, fy);
                index += 1;
            }
        }
        mMatrix.setTranslate(0, 0);
        mMatrix.invert(mInverse);
    }

    private static void setXY(float[] array, int index, float x, float y) {
        array[index*2 + 0] = x;
        array[index*2 + 1] = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        drawShadow(canvas);
        mMatrix.setTranslate(0, 0);
        canvas.concat(mMatrix);
        canvas.drawBitmapMesh(bitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.BLUE);
//        paint.setTextSize(14.0f);
//        for (int i = 0; i < COUNT*2; i += 2) {
//            float x = mVerts[i+0];
//            float y = mVerts[i+1];
//            canvas.drawText(String.valueOf(i), x, y, paint);
//        }
    }

    private void drawShadow(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        int portion = 50;
        int fullAlpha = 0xFF;
        switch (orientation) {
            case VERTICAL_TO_UP:
                float y = 0.0f;
                int alpha = (int) (0.7*fullAlpha);
                while (y + height/portion < height) {
                    paint.setAlpha(alpha);
                    RectF rectF = new RectF(0, y, width, y + height/portion);
                    canvas.drawRect(rectF, paint);

                }
                break;
            case VERTICAL_TO_BOTTOM:

                break;
            case HORIZONTAL_TO_LEFT:

                break;
            case HORIZONTAL_TO_RIGHT:

                break;
        }
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        warp();
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        // construct our mesh
        int index = 0;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = height * y / HEIGHT;
            for (int x = 0; x <= WIDTH; x++) {
                float fx = width * x / WIDTH;
                setXY(mVerts, index, fx, fy);
                setXY(mOrig, index, fx, fy);
                index += 1;
            }
        }
        mMatrix.setTranslate(10, 10);
        mMatrix.invert(mInverse);
        warp();
        invalidate();
    }

    public void setShadowVisibility(boolean isShadowVisible) {
        this.isShadowVisible = isShadowVisible;
        warp();
        invalidate();
    }

    private void warp() {
        float[] src = mOrig;
        float[] dst = mVerts;
        double begDegree = 0.0;
        float dx = 0, dy = 0;
        switch (orientation) {
            case VERTICAL_TO_UP:
                for (int i = 0; i < COUNT*2; i += 2) {
                    float x = src[i+0];
                    float y = src[i+1];
                    if (i != 0 && i % (WIDTH+1)*2 == 0)
                        begDegree += 3.0;
                    float koeff = (float) Math.cos(Math.toRadians(begDegree));
                    dx = x;
                    dy = height*(1-koeff);
                    dst[i+0] = dx;
                    dst[i+1] = dy;
                }
                break;
            case VERTICAL_TO_BOTTOM:
                for (int i = 0; i < COUNT*2; i += 2) {
                    float x = src[i+0];
                    float y = src[i+1];
                    if (i != 0 && i % (WIDTH+1)*2 == 0)
                        begDegree += 3.0;
                    float koeff = (float) Math.cos(Math.toRadians(begDegree));
                    dx = x;
                    dy = height*koeff;
                    dst[i+0] = dx;
                    dst[i+1] = dy;
                }
                break;
            case HORIZONTAL_TO_LEFT:
                for (int i = 0; i < COUNT*2; i += 2) {
                    float x = src[i+0];
                    float y = src[i+1];
                    if (i != 0 && i % (HEIGHT+1)*2 == 0)
                        begDegree += 3.0;
                    float koeff = (float) Math.cos(Math.toRadians(begDegree));
                    dy = y;
                    dx = height*(1-koeff);
                    dst[i+0] = dx;
                    dst[i+1] = dy;
                }
                break;
            case HORIZONTAL_TO_RIGHT:
                for (int i = 0; i < COUNT*2; i += 2) {
                    float x = src[i+0];
                    float y = src[i+1];
                    if (i != 0 && i % (HEIGHT+1)*2 == 0)
                        begDegree += 3.0;
                    float koeff = (float) Math.cos(Math.toRadians(begDegree));
                    dy = y;
                    dx = height*koeff;
                    dst[i+0] = dx;
                    dst[i+1] = dy;
                }
                break;
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
