package com.jim.finansia.utils.calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jim.finansia.R;
import com.jim.finansia.utils.GetterAttributColors;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ArcBitmapChooserView extends View implements GestureDetector.OnGestureListener{

    public static final int NOTHING_SELECTED = -1;
    private int selectedPos = NOTHING_SELECTED;
    private int arcColor = Color.BLACK;
    private List<Bitmap> bitmaps;
    private float sectionDegree = 30.0f;
    private float degreeStep = sectionDegree/7.0f;
    private float bottomMargin = 0.0f;
    private PointF centerPoint;
    private float mainRadius = 0.0f;
    private int fullAlpha = 0xFF;
    private float beginDegree = 0.0f;
    private float rotateDegree = 0.0f;
    private float lastDegree = 0.0f;
    private float maxDegree = 0.0f;
    private Long lastTime = 0L;
    private float deltaTime = 0.0f, deltaPhi = 0.0f;
    private boolean forward = true;
    private boolean stop = false;
    private float touchRadius = 0.0f;
    private GestureDetectorCompat mDetector;
    private Long inertiaTotalTime = 300L;
    private boolean takePositionStop = true;
    private Long takingPositionTime = 50L;
    private boolean fling = false;
    private float bitmapMargin = 0.0f;
    private float chooserSide = 0.0f;
    private float marginBetweenChooserAndBitmap = 0.0f;
    private OnCategoryChooseListener listener;
    private int lastPosition = NOTHING_SELECTED;
    private int resPosition = NOTHING_SELECTED;
    public ArcBitmapChooserView(Context context) {
        super(context);
        init();
    }
    public ArcBitmapChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ArcBitmapChooserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    @SuppressLint("NewApi")
    public ArcBitmapChooserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init() {
        mDetector = new GestureDetectorCompat(getContext(), this);
        arcColor = GetterAttributColors.fetchHeadColor(getContext());
        bitmapMargin = getResources().getDimension(R.dimen.twenty_dp);
        setMinimumHeight((int) getResources().getDimension(R.dimen.one_hundred_fourty_two_dp));
        bottomMargin = getResources().getDimension(R.dimen.thirty_dp);
        chooserSide = getResources().getDimension(R.dimen.fifteen_dp);
        marginBetweenChooserAndBitmap = getResources().getDimension(R.dimen.ten_dp);
        centerPoint = new PointF();
        setClickable(true);
    }
    public void setArcColor(int color) {
        this.arcColor = color;
        invalidate();
    }
    public void setBitmaps(List<Bitmap> bitmaps) {
        if (bitmaps != null) {
            this.bitmaps = bitmaps;
            maxDegree = (bitmaps.size() == 1 || bitmaps.size() == 0 ? 1 : bitmaps.size()) * degreeStep;
        }
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawDatas(canvas);
    }
    private void drawDatas(Canvas canvas) {
        if (bitmaps == null || bitmaps.isEmpty()) return;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        ColorFilter filter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        int tempPos = Math.round(rotateDegree/degreeStep) > bitmaps.size()-1 ?
                bitmaps.size()-1 :
                Math.round(rotateDegree/degreeStep);
        for (int i = 0; i < bitmaps.size(); i++) {
            float alphaKoeff = 1.0f;
            if (i == tempPos - 4)
                alphaKoeff = 0.0f;
            else if (i == tempPos - 3)
                alphaKoeff = 0.25f;
            else if (i == tempPos - 2)
                alphaKoeff = 0.5f;
            else if (i == tempPos - 1)
                alphaKoeff = 0.75f;
            else if (i == tempPos)
                alphaKoeff = 1.0f;
            else if (i == tempPos + 1)
                alphaKoeff = 0.75f;
            else if (i == tempPos + 2)
                alphaKoeff = 0.5f;
            else if (i == tempPos + 3)
                alphaKoeff = 0.25f;
            else if (i == tempPos + 4)
                alphaKoeff = 0.0f;
            paint.setAlpha((int) (alphaKoeff*fullAlpha));
            float degree = -rotateDegree + ((float)i)*degreeStep;
            float distance = mainRadius - bitmapMargin - bitmaps.get(i).getHeight();
            float x = (float) (centerPoint.x + distance * Math.sin(Math.toRadians(degree)));
            float y = (float) (centerPoint.y + distance * Math.cos(Math.toRadians(degree)));
            canvas.drawBitmap(
                    bitmaps.get(i),
                    x - bitmaps.get(i).getWidth()/2,
                    y + bitmaps.get(i).getHeight()/2,
                    paint
            );
        }
        paint.setColor(Color.WHITE);
        paint.setAlpha(fullAlpha);
        Path triangle = new Path();
        float begX = getWidth()/2;
        float begY = getHeight() - bottomMargin - bitmapMargin/2 - bitmaps.get(0).getHeight() - marginBetweenChooserAndBitmap;
        triangle.moveTo(begX, begY);
        triangle.lineTo(begX - chooserSide/2, begY - chooserSide / 2);
        triangle.lineTo(begX + chooserSide/2, begY - chooserSide / 2);
        triangle.close();
        canvas.drawPath(triangle, paint);
        float delay = 1.0f;
        resPosition = NOTHING_SELECTED;
        int position = Math.round(rotateDegree/degreeStep) > bitmaps.size()-1 ? bitmaps.size()-1 : Math.round(rotateDegree/degreeStep);
        if (position*degreeStep - delay < rotateDegree && position*degreeStep + delay > rotateDegree) {
            resPosition = position;
        }
        if (listener != null && lastPosition != resPosition) {
            if (resPosition != NOTHING_SELECTED) {
                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(5);
            }
            listener.onCategoryChoose(resPosition);
        }
        lastPosition = resPosition;
    }
    public void setListener(OnCategoryChooseListener listener) {
        this.listener = listener;
    }
    public void takePosition() {
        if (!stop) return;
        selectedPos = Math.round(rotateDegree/degreeStep) > bitmaps.size()-1 ? bitmaps.size()-1 : Math.round(rotateDegree/degreeStep);
        takePositionStop = false;
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int size = 30;
                float takingPositionDifference = selectedPos*degreeStep - rotateDegree;
                float fullCircleDegree = 90.0f;
                for (int i = 0; i < size; i++) {
                    if (takePositionStop) return;
                    try {
                        Thread.sleep(takingPositionTime/size);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        float tempDegree = fullCircleDegree/size;
                        float step = (float) (takingPositionDifference * Math.sin(Math.toRadians(tempDegree)));

                        rotateDegree += step;
                        if (i == size-1) {
                            rotateDegree = selectedPos*degreeStep;
                        }
                        Log.d("sss", "rotateDegree: " + rotateDegree + " step: " + step);
                        postInvalidate();
                    }
                }
                takePositionStop = true;
            }
        });
    }
    private void drawBackground(Canvas canvas) {
        mainRadius = ((float)getWidth()/(2*(float)Math.sin(Math.toRadians(sectionDegree/2.0f))));
        centerPoint.x = getWidth()/2;
        centerPoint.y = getHeight() - bottomMargin - mainRadius;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(arcColor);
        RectF arcRect = new RectF();
        arcRect.set(centerPoint.x - mainRadius, centerPoint.y - mainRadius, centerPoint.x + mainRadius, centerPoint.y+mainRadius);
        beginDegree = 90.0f - sectionDegree/2.0f;
        canvas.drawArc(arcRect, beginDegree, sectionDegree, true, paint);
        float bissectriss = (float) (mainRadius * Math.cos(Math.toRadians(sectionDegree/2.0f)));
        float tempMargin = mainRadius - bissectriss;
        RectF fillRestRect = new RectF(0, 0, getWidth(), getHeight() - bottomMargin - tempMargin);
        canvas.drawRect(fillRestRect, paint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dx = 0.0f, dy = 0.0f;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stop = true;
                fling = false;
                takePositionStop = true;
                dx = centerPoint.x - event.getX();
                dy = centerPoint.y - event.getY();
                touchRadius = (float) Math.sqrt(dx*dx + dy*dy);
                if (touchRadius < mainRadius) {
                    float degree = (float) -Math.toDegrees(Math.asin(Math.sin(dx/touchRadius))) + sectionDegree/2.0f;
                    if (degree > sectionDegree)
                        degree = sectionDegree;
                    else if (degree < 0.0f)
                        degree = 0.0f;
                    lastDegree = degree;
                }
                lastTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                dx = centerPoint.x - event.getX();
                dy = centerPoint.y - event.getY();
                touchRadius = (float) Math.sqrt(dx*dx + dy*dy);
                if (touchRadius < mainRadius) {
                    float degree = (float) -Math.toDegrees(Math.asin(Math.sin(dx/touchRadius))) + sectionDegree / 2.0f;
                    if (degree > sectionDegree)
                        degree = sectionDegree;
                    else if (degree < 0.0f)
                        degree = 0.0f;
                    if (rotateDegree + (lastDegree - degree) < 0.0f) {
                        rotateDegree = 0.0f;
                        lastDegree = degree;
                        invalidate();
                        return false;
                    } else if (rotateDegree + (lastDegree - degree) > maxDegree) {
                        rotateDegree = maxDegree;
                        lastDegree = degree;
                        invalidate();
                        return false;
                    } else {
                        rotateDegree += lastDegree - degree;
                        invalidate();
                        lastDegree = degree;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (stop && takePositionStop && !fling)
                    takePosition();
                break;
        }
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    public void rotateWithIntertia(float deltaTime, float deltaPhi, final boolean forward) {
        float speed = deltaPhi/deltaTime;
        if (speed < 0.03f) return;
        final float actionDistance = speed * (float)inertiaTotalTime;
        final float fullCircleDegree = 90.0f;
        stop = false;
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int size = 30;
                for (int i = 1 ; i <= size; i++) {
                    if (stop) break;
                    try {
                        Thread.sleep(inertiaTotalTime/size);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        float tempDegree = fullCircleDegree/size;
                        float tempDistance = (float) (actionDistance * Math.sin(Math.toRadians(tempDegree)));
                        if (forward && rotateDegree + tempDegree < maxDegree)
                            rotateDegree += tempDistance;
                        else if (!forward && rotateDegree - tempDegree >= 0)
                            rotateDegree -= tempDistance;
                        postInvalidate();
                    }
                }
                stop = true;
                fling = true;
                takePosition();
            }
        });
    }
    public void setSelectedPosition(int position) {
        if (bitmaps != null && position < bitmaps.size())
        this.selectedPos = position;
        rotateDegree = selectedPos*degreeStep;
        invalidate();
    }
    public void setSectionDegree(float sectionDegree) {
        this.sectionDegree = sectionDegree > 90.0f
                || sectionDegree < 0
                ? 90.0f
                : sectionDegree;
        invalidate();
    }
    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent begin, MotionEvent current, float x, float y) {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent motionEvent) {}
    @Override
    public boolean onFling(MotionEvent begin, MotionEvent current, float deltaX, float deltaY) {
        forward = deltaX < 0;
        deltaTime = System.currentTimeMillis() - lastTime;
        float f1, f2, f3;
        f1  = (float) Math.sqrt((centerPoint.x - begin.getX())*(centerPoint.x - begin.getX()) + (centerPoint.y - begin.getY())*(centerPoint.y - begin.getY()));
        f2  = (float) Math.sqrt((centerPoint.x - current.getX())*(centerPoint.x - current.getX()) + (centerPoint.y - current.getY())*(centerPoint.y - current.getY()));
        f3  = (float) Math.sqrt((begin.getX() - current.getX())*(begin.getX() - current.getX()) + (begin.getY() - current.getY())*(begin.getY() - current.getY()));
        deltaPhi = 180.0f - (float) Math.toDegrees(Math.acos((f3*f3 - f1*f1 - f2*f2)/(2*f1*f2)));
        rotateWithIntertia(deltaTime, deltaPhi, forward);
        return false;
    }

    public interface OnCategoryChooseListener {
        public void onCategoryChoose(int position);
    }
}
