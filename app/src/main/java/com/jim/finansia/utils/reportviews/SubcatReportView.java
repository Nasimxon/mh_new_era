package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.jim.finansia.R;

import java.text.DecimalFormat;

public class SubcatReportView extends View {
    private SubcatData subcatData;
    private String noDataText = "no data";
    private float thickness = 0.0f;
    private float marginBetweenMainAndInnerCircles;
    public SubcatReportView(Context context) {
        super(context);
        init();
    }
    public SubcatReportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SubcatReportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    @SuppressLint("NewApi")
    public SubcatReportView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init() {
        thickness = getResources().getDimension(R.dimen.six_dp);
        marginBetweenMainAndInnerCircles = getResources().getDimension(R.dimen.three_dp);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (subcatData != null) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            boolean isVertical = getWidth() < getHeight();
            float side, left, top, right, bottom;
            if (isVertical) {
                side = getWidth();
                left = 0.0f;
                top = (getHeight() - side) / 2;
            } else {
                side = getHeight();
                left = (getWidth() - side) / 2;
                top = 0.0f;
            }
            right = left + side;
            bottom = top + side;
            RectF outerContainer = new RectF(left, top, right, bottom);
            paint.setColor(subcatData.getColor());
            float angle = 3.6f * subcatData.getPercent();
            canvas.drawArc(outerContainer, 270.0f, angle, true, paint);
            paint.setColor(Color.WHITE);
            RectF innerContainer = new RectF(
                    outerContainer.left + thickness,
                    outerContainer.top + thickness,
                    outerContainer.right - thickness,
                    outerContainer.bottom - thickness
            );
            canvas.drawArc(innerContainer, 270.0f, 360.0f, true, paint);
            Paint innerCirclePaint = new Paint();
            innerCirclePaint.setAntiAlias(true);
            innerCirclePaint.setStyle(Paint.Style.STROKE);
            innerCirclePaint.setStrokeWidth(2*marginBetweenMainAndInnerCircles/3);
            innerCirclePaint.setColor(subcatData.getColor());
            RectF inner = new RectF(innerContainer.left + marginBetweenMainAndInnerCircles,
                                    innerContainer.top + marginBetweenMainAndInnerCircles,
                                    innerContainer.right - marginBetweenMainAndInnerCircles,
                                    innerContainer.bottom - marginBetweenMainAndInnerCircles);
            canvas.drawArc(inner, 270.0f, 360.0f, true, innerCirclePaint);
            Paint textPaint = new Paint();
            textPaint.setAntiAlias(true);
            float textSize = side/6;
            textPaint.setTextSize(textSize);
            textPaint.setColor(subcatData.getColor());
            DecimalFormat format = new DecimalFormat("0.##");
            String percent = format.format(subcatData.getPercent()) + "%";
            Rect textContainer = new Rect();
            textPaint.getTextBounds(percent, 0, percent.length(), textContainer);
            canvas.drawText(percent, (getWidth() - textContainer.width())/2, (getHeight() + textContainer.height())/2, textPaint);
        }
    }
    public void setThickness(float thickness) {
        this.thickness = thickness;
    }
    public void setData(SubcatData subcatData){
        this.subcatData = subcatData;
        invalidate();
    }
    public void setTextNoDataShow(String noDataText) {
        this.noDataText = noDataText;
    }
}
