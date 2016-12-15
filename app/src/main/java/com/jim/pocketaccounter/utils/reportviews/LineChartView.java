package com.jim.pocketaccounter.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.jim.pocketaccounter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineChartView extends View {

    private float margin = 0.0f, topMargin = 0.0f;
    private RectF container;
    private int axisColor = Color.parseColor("#414141");
    private int lineColor = Color.RED;
    private List<Double> data;
    private float begX = 0.0f, begY = 0.0f, endX = 0.0f, endY = 0.0f;
    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        margin = getResources().getDimension(R.dimen.five_dp);
        topMargin = getResources().getDimension(R.dimen.ten_dp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        container = new RectF(margin, topMargin, getWidth() - margin, getHeight() - margin);
        //axises (x, y)
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(axisColor);
        canvas.drawLine(0, container.bottom, getWidth(), container.bottom, paint); //x axis
        canvas.drawLine(container.left, 0, container.left, getHeight(), paint); //y axis
        if (data != null && !data.isEmpty()) {
            Double max = Collections.max(data);
            float step = container.width()/(float)data.size();
            List<PointF> points = new ArrayList<>();
            PointF pointF = new PointF();
            pointF.x = container.left;
            pointF.y = container.bottom;
            points.add(pointF);
            for (int i = 0; i < data.size(); i++) {
                PointF point = new PointF();
                point.x = container.left + (i+1) * step;
                point.y = (float) (container.bottom - container.height() * data.get(i)/max);
                points.add(point);
            }
            paint.setColor(lineColor);
            for (int i = 1; i < points.size(); i++)
                canvas.drawLine(points.get(i-1).x, points.get(i-1).y, points.get(i).x, points.get(i).y, paint);
        }
    }

    public void setData(List<Double> data) {
        this.data = data;
        invalidate();
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        invalidate();
    }
}
