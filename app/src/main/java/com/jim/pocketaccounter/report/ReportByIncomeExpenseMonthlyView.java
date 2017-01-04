package com.jim.pocketaccounter.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jim.pocketaccounter.R;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ReportByIncomeExpenseMonthlyView extends View {

    private Map<Integer, Float> datas;
    private float side = 0.0f;
    private float sideMargin = 0.0f;
    private float clickFullRadius = 0.0f;
    private float maxValue = 100.0f;
    private float minValueHeight = 0.0f;
    private int totalShape = 400, elapsedShape = 400, interimShape = 10, frameShape = 10,
                totalClick = 300, elapsedClick = 300, interimClick = 5, frameClick = 5;
    private int NOT_STARTED = 0, STARTED = 1, FINISHED = 2;
    private int shapeAnimation = NOT_STARTED;
    private List<ItemShape> items;
    private int[] colors = {
            Color.parseColor("#0d3c55"),
            Color.parseColor("#0f5b78"),
            Color.parseColor("#117899"),
            Color.parseColor("#1395ba"),
            Color.parseColor("#5ca793"),
            Color.parseColor("#a2b86c"),
            Color.parseColor("#ebc844"),
            Color.parseColor("#ecaa38"),
            Color.parseColor("#ef8b2c"),
            Color.parseColor("#f16c20"),
            Color.parseColor("#d94e1f"),
            Color.parseColor("#c02e1d")
            };
    private int selectedPos = 0, lastPosition = 0;
    private float touchMargin = 0.0f;
    private float touchX, touchY;
    private OnMonthlyItemSelectedListener listener;
    private int selectingTriangleColor = Color.WHITE;
    private float topMargin = 0.0f;
    private boolean active = true;
    public ReportByIncomeExpenseMonthlyView(Context context) {
        super(context);
        init();
    }

    public ReportByIncomeExpenseMonthlyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReportByIncomeExpenseMonthlyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public ReportByIncomeExpenseMonthlyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        sideMargin = getResources().getDimension(R.dimen.ten_dp);
        minValueHeight = getResources().getDimension(R.dimen.ten_dp);
        items = new ArrayList<>();
        touchMargin = getResources().getDimension(R.dimen.five_dp);
        topMargin = getResources().getDimension(R.dimen.thirty_dp);
        setClickable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datas == null || datas.isEmpty()) return;
        float shapeRatio = (float) elapsedShape / (float) totalShape;
        float clickRatio = (float) elapsedClick / (float) totalClick;
        side = (getWidth() - 2*sideMargin)/(float)datas.size();
        clickFullRadius = (float) Math.sqrt(getWidth()*getWidth() + getHeight()*getHeight());
        PointF beginPoint = new PointF();
        beginPoint.x = sideMargin;
        beginPoint.y = getHeight() - minValueHeight;
        float drawingSpaceHeight = beginPoint.y - topMargin;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        items.clear();
        if (!active) {
            for (int i = 0; i < 12; i++) {
                RectF container = new RectF();
                float left = beginPoint.x + i*side;
                float top = beginPoint.y;
                float right = beginPoint.x + (i+1)*side;
                float bottom = getHeight();
                container.set(left, top, right, bottom);
                ItemShape shape = new ItemShape();
                shape.setContainer(container);
                items.add(shape);
            }
            for (int i = 0; i < items.size(); i++) {
                paint.setColor(colors[i]);
                canvas.drawRect(items.get(i).getContainer(), paint);
                canvas.drawCircle(items.get(i).getCircleCenter().x, items.get(i).getCircleCenter().y, items.get(i).getContainer().width() / 2, paint);
            }
            return;
        }
        for (int i = 0; i < datas.size(); i++) {
            RectF container = new RectF();
            float left = beginPoint.x + i*side;
            float top = beginPoint.y - drawingSpaceHeight*shapeRatio*(datas.get(i)/maxValue);
            float right = beginPoint.x + (i+1)*side;
            float bottom = getHeight();
            container.set(left, top, right, bottom);
            ItemShape shape = new ItemShape();
            shape.setContainer(container);
            items.add(shape);
        }
        if (shapeAnimation == FINISHED && selectedPos != -1) {
            PointF pointF = items.get(selectedPos).getCircleCenter();
            int color = adjustAlpha(colors[selectedPos], 0.1f);
            paint.setColor(color);
            canvas.drawCircle(pointF.x, pointF.y, clickRatio * clickFullRadius, paint);
        }
        for (int i = 0; i < items.size(); i++) {
            paint.setColor(colors[i]);
            canvas.drawRect(items.get(i).getContainer(), paint);
            canvas.drawCircle(items.get(i).getCircleCenter().x, items.get(i).getCircleCenter().y, items.get(i).getContainer().width()/2, paint);
            if (selectedPos == i) {
                RectF container = items.get(i).getContainer();
                paint.setColor(selectingTriangleColor);
                Path triangle = new Path();
                triangle.moveTo(container.left + container.width()/5, container.bottom);
                triangle.lineTo(container.right - container.width()/5, container.bottom);
                triangle.lineTo(container.centerX(), container.bottom - container.width()/5);
                triangle.close();
                canvas.drawPath(triangle, paint);
            }
        }
    }

    public void setSelectingTriangleColor(int selectingTriangleColor) {
        this.selectingTriangleColor = selectingTriangleColor;
        invalidate();
    }

    public void animateShapes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                elapsedShape = 0;
                shapeAnimation = STARTED;
                while(totalShape > elapsedShape) {
                    try {
                        Thread.sleep(interimShape);
                        elapsedShape += frameShape;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        postInvalidate();
                    }
                }
                elapsedShape = totalShape;
                shapeAnimation = FINISHED;
            }
        }).start();
    }

    private void selectMonth(int month) {
        selectedPos = month;
    }

    public void animateClick(int selectedItem) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(5);
        selectedPos = selectedItem;
        new Thread(new Runnable() {
            @Override
            public void run() {
                elapsedClick = 0;
                while (totalClick > elapsedClick) {
                    try {
                        Thread.sleep(interimClick);
                        elapsedClick += frameClick;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        postInvalidate();
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float dx = touchX - event.getX(),
                        dy = touchY - event.getY();
                float distance = (float) Math.sqrt(dx*dx + dy*dy);
                if (distance < touchMargin) {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).contains(touchX, touchY)) {
                            selectedPos = i;
                            if (listener != null && lastPosition != selectedPos) {
                                animateClick(selectedPos);
                                listener.onMonthlyItemSelected(selectedPos);
                            }
                            lastPosition = selectedPos;
                            break;
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setDatas(Map<Integer, Float> datas) {
        this.datas = datas;
        float tempValue = 0.0f;
        for (Integer pos : this.datas.keySet()) {
            if (tempValue < this.datas.get(pos))
                tempValue = this.datas.get(pos);
        }
        maxValue = tempValue == 0.0f ? 100.f : tempValue;
        invalidate();
    }

    public void active(boolean active) {
        this.active = active;
        if (active)
            animateShapes();
        else
            invalidate();
    }

    class ItemShape {
        private PointF circleCenter;
        private RectF container;
        public ItemShape() {
            container = new RectF();
            circleCenter = new PointF();
        }
        public void setContainer(RectF container) {
            this.container = container;
            circleCenter.x = container.left + container.width()/2;
            circleCenter.y = container.top;
        }
        public RectF getContainer() { return container; }
        public PointF getCircleCenter() { return circleCenter; }
        public boolean contains(float x, float y) {
            if (container == null || circleCenter == null)
                return false;
            float dx = circleCenter.x - x;
            float dy = circleCenter.y - y;
            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            if (container.contains(x, y) || container.width()/2 >= distance)
                return true;
            return false;
        }
    }
    public void setListener(OnMonthlyItemSelectedListener listener) {
        this.listener = listener;
    }
    public interface OnMonthlyItemSelectedListener {
        public void onMonthlyItemSelected(int position);
    }
}
