package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.jim.finansia.R;

import java.util.Collections;
import java.util.List;

public class LinearChartWithAverageView extends View {

    private int lineColor = Color.GREEN;
    private float margin = 0.0f;
    private float lineThickness = 0.0f;
    private int averageLineColor = Color.YELLOW;
    private float averageLineThickness = 0.0f;
    private RectF container;
    private List<Double> data;
    private float triangleSide = 0.0f;
    public LinearChartWithAverageView(Context context) {
        super(context);
        init();
    }

    public LinearChartWithAverageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinearChartWithAverageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public LinearChartWithAverageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init() {
        margin = getResources().getDimension(R.dimen.five_dp);
        lineThickness = getResources().getDimension(R.dimen.one_dp);
        averageLineThickness = getResources().getDimension(R.dimen.one_dp);
        container = new RectF();
        triangleSide = getResources().getDimension(R.dimen.five_dp);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null) return;
        //drawing line chart
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineThickness);
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        container.set(margin, margin, getWidth()-margin, getHeight()-margin);
        Double min, max;
        min = Collections.min(data);
        max = Collections.max(data);
        if (data.isEmpty() || (min == 0.0d && max == 0.0d)) {
            canvas.drawLine(0, getHeight()/2, getWidth(), getHeight()/2, paint);
            return;
        }
        float zeroHeight = 0.0f;
        if (max > 0.0d && min >= 0.0d)
            zeroHeight = container.bottom;
        if (max <= 0.0d && min < 0.0d)
            zeroHeight = container.top;
        if (min < 0.0d && max > 0.0d) {
            float height = (float) (Math.abs(min) + Math.abs(max));
            zeroHeight = (float) (container.height()*(1-Math.abs(min)/height));
        }
        float step = container.width()/(float)data.size();
        float lastX = 0.0f, lastY = zeroHeight;
        Path line = new Path();
        line.moveTo(0.0f, zeroHeight);
        for (int i = 0; i < data.size(); i++) {
            float x = container.left + i*step;
            float y = 0.0f;
            if (max > 0.0d && min >= 0.0d)
                y = zeroHeight - (float) (container.height() * (data.get(i)/max));
            if (max <= 0.0d && min < 0.0d)
                y = (float) (zeroHeight + data.get(i)/min);
            if (min < 0.0d && max > 0.0d) {
                float maxHeight = zeroHeight;
                float minHeight = container.bottom - zeroHeight;
                if (data.get(i) == 0.0d)
                    y = zeroHeight;
                if (data.get(i) > 0.0d)
                    y = (float) (zeroHeight - maxHeight*(data.get(i)/max));
                if (data.get(i) < 0.0d)
                    y = (float) (zeroHeight + minHeight*(data.get(i)/min));
            }
            line.lineTo(x, y);
//            canvas.drawLine(lastX, lastY, x, y, paint);
//            lastX = x;
//            lastY = y;
        }
        canvas.drawPath(line, paint);
        float averageLineHeight = 0.0f;
        double averageAmount = 0.0d, sum = 0.0d;
        for (Double amount : data)
            sum += amount;
        averageAmount = sum/data.size();
        paint.setColor(adjustAlpha(averageLineColor, 0.5f));
        paint.setStrokeWidth(averageLineThickness);
        if (max > 0.0d && min >= 0.0d)
            averageLineHeight = zeroHeight - (float) (container.height() * (averageAmount/max));
        if (max <= 0.0d && min < 0.0d)
            averageLineHeight = (float) (zeroHeight + averageAmount/min);
        if (min < 0.0d && max > 0.0d) {
            float maxHeight = zeroHeight;
            float minHeight = container.bottom - zeroHeight;
            if (averageAmount == 0.0d)
                averageLineHeight = zeroHeight;
            if (averageAmount > 0.0d)
                averageLineHeight = (float) (zeroHeight - maxHeight*(averageAmount/max));
            if (averageAmount < 0.0d)
                averageLineHeight = (float) (zeroHeight + minHeight*(averageAmount/min));
        }
        canvas.drawLine(0, averageLineHeight, getWidth(), averageLineHeight, paint);
        paint.setColor(averageLineColor);
        Path leftTriangle = new Path();
        leftTriangle.moveTo(0, averageLineHeight-triangleSide/2);
        leftTriangle.lineTo(0, averageLineHeight+triangleSide/2);
        leftTriangle.lineTo(triangleSide, averageLineHeight);
        leftTriangle.close();
        canvas.drawPath(leftTriangle, paint);
        Path rightTriangle = new Path();
        rightTriangle.moveTo(getWidth(), averageLineHeight-triangleSide/2);
        rightTriangle.lineTo(getWidth(), averageLineHeight+triangleSide/2);
        rightTriangle.lineTo(getWidth() - triangleSide, averageLineHeight);
        rightTriangle.close();
        canvas.drawPath(rightTriangle, paint);
    }

    public void setData(List<Double> data) {
        this.data = data;
        invalidate();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color)*factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }

    public void setAverageLineColor(int averageLineColor) {
        this.averageLineColor = averageLineColor;
    }

    public void setAverageLineThickness(float averageLineThickness) {
        this.averageLineThickness = averageLineThickness;
    }
}
