package com.jim.pocketaccounter.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Recking;

import java.util.Calendar;

public class ReportByIncomeExpenseOneDayView extends View {

    private double leftValue = 0.0d, rightValue = 0.0d, maxValue = 0.0d, minValue = 0.0d;
    private float zeroValue = 0;
    private int day = 1, dayOfWeek = Calendar.MONDAY;
    private int increasePercent = 0;
    private int trapezeColor = adjustAlpha(Color.BLACK, 0.2f),
            centerTextColor = Color.WHITE,
            percentPositiveColor = Color.GREEN,
            percentNegativeColor = Color.RED,
            percentNeutralColor = Color.GRAY;
    private float percentTextSize = 0.0f, dayTextSize = 0.0f, weekdayTextSize = 0.0f;
    private int total = 400, elapsed = 400, interim = 5, frame = 5;
    private float percentTextTopBottomPadding = 0.0f, percentTextLeftRightPadding = 0.0f;
    private int fullAlpha = 0xFF;
    private String weekDay = "Mn";
    public ReportByIncomeExpenseOneDayView(Context context) {
        super(context);
        init();
    }

    public ReportByIncomeExpenseOneDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReportByIncomeExpenseOneDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public ReportByIncomeExpenseOneDayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        zeroValue = getResources().getDimension(R.dimen.ten_dp);
        percentTextSize = getResources().getDimension(R.dimen.eight_dp);
        weekdayTextSize = getResources().getDimension(R.dimen.twelve_dp);
        percentTextTopBottomPadding = getResources().getDimension(R.dimen.two_dp);
        percentTextLeftRightPadding = getResources().getDimension(R.dimen.three_dp);
        dayTextSize = getResources().getDimension(R.dimen.twentytwo_dp);
        setMinimumHeight(R.dimen.sixty_dp);
        setMinimumWidth(R.dimen.fourty_dp);
        initWeekDay();
    }

    void initWeekDay() {
        String[] allWeekdays = getResources().getStringArray(R.array.week_day_auto);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                weekDay = allWeekdays[0];
                break;
            case Calendar.TUESDAY:
                weekDay = allWeekdays[1];
                break;
            case Calendar.WEDNESDAY:
                weekDay = allWeekdays[2];
                break;
            case Calendar.THURSDAY:
                weekDay = allWeekdays[3];
                break;
            case Calendar.FRIDAY:
                weekDay = allWeekdays[4];
                break;
            case Calendar.SATURDAY:
                weekDay = allWeekdays[5];
                break;
            case Calendar.SUNDAY:
                weekDay = allWeekdays[6];
                break;
        }
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        initWeekDay();
    }

    public double getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(double leftValue) {
        this.leftValue = leftValue;
        invalidate();
    }

    public double getRightValue() {
        return rightValue;
    }

    public void setRightValue(double rightValue) {
        this.rightValue = rightValue;
        invalidate();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getIncreasePercent() {
        return increasePercent;
    }

    public void setIncreasePercent(int increasePercent) {
        this.increasePercent = increasePercent;
        invalidate();
    }

    public int getTrapezeColor() {
        return trapezeColor;
    }

    public void setTrapezeColor(int trapezeColor) {
        this.trapezeColor = adjustAlpha(trapezeColor, 0.2f);;
        invalidate();
    }

    public int getCenterTextColor() {
        return centerTextColor;
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        invalidate();
    }

    public int getPercentPositiveColor() {
        return percentPositiveColor;
    }

    public void setPercentPositiveColor(int percentPositiveColor) {
        this.percentPositiveColor = percentPositiveColor;
        invalidate();
    }

    public int getPercentNegativeColor() {
        return percentNegativeColor;
    }

    public void setPercentNegativeColor(int percentNegativeColor) {
        this.percentNegativeColor = percentNegativeColor;
        invalidate();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float ratio = (float) elapsed / (float) total;
        //determine bottom point
        PointF bottomPoint = new PointF();
        bottomPoint.x = 0.0f;
        bottomPoint.y = getHeight() - zeroValue;
        //determine left point
        PointF leftPoint = new PointF();
        leftPoint.x = 0.0f;
        leftPoint.y  = maxValue + Math.abs(minValue) == 0.0f ? bottomPoint.y : bottomPoint.y * (1.0f - (float)leftValue*ratio/(float)(maxValue + Math.abs(minValue)));
        //determine right point
        PointF rightPoint = new PointF();
        rightPoint.x = getWidth();
        rightPoint.y = maxValue + Math.abs(minValue) == 0.0f ? bottomPoint.y : bottomPoint.y * (1.0f - (float)rightValue*ratio/(float)(maxValue + Math.abs(minValue)));


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(trapezeColor);

        Path mainTrapeze = new Path();
        mainTrapeze.moveTo(leftPoint.x, leftPoint.y);
        mainTrapeze.lineTo(rightPoint.x, rightPoint.y);
        mainTrapeze.lineTo(rightPoint.x, getHeight());
        mainTrapeze.lineTo(leftPoint.x, getHeight());
        mainTrapeze.close();
        canvas.drawPath(mainTrapeze, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.0f);
        canvas.drawPath(mainTrapeze, paint);
        if (minValue != 0 && minValue < 0 && maxValue != 0) {
            float y = bottomPoint.y * (1.0f - Math.abs((float) minValue)/(float) (maxValue + minValue));
            canvas.drawLine(0, y, getWidth(), y, paint);
        }
        //percent text
        double ratioPercent = Math.abs(increasePercent*ratio) <= 100.0f ? Math.abs(increasePercent * ratio) : 100.0f;
        String percent = String.valueOf((int)ratioPercent) + "%";
        //defining percent
        Rect percentTextBounds = new Rect();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(percentTextSize);
        paint.getTextBounds(percent, 0, percent.length(), percentTextBounds);
        PointF percentTextPoint = new PointF();
        float percentContainerWidth = percentTextLeftRightPadding*2 + percentTextBounds.width();
        float dx = rightPoint.x - leftPoint.x,
                dy = rightPoint.y - leftPoint.y;
        float distance = (float) Math.sqrt(dx*dx + dy*dy);
        float degree = (float) Math.toDegrees(Math.asin(dy/distance));
        PointF criticPoint = new PointF();
        criticPoint.x = getWidth() - percentContainerWidth;
        float tempDy = (float) (percentContainerWidth * Math.tan(Math.toRadians(degree)));
        criticPoint.y = rightPoint.y - tempDy;
        Path percentContainer = new Path();
        percentContainer.moveTo(criticPoint.x, criticPoint.y);
        percentContainer.lineTo(rightPoint.x, rightPoint.y);
        if (degree > 0) {
            percentContainer.lineTo(rightPoint.x, rightPoint.y + percentTextBounds.height() + 2 * percentTextTopBottomPadding);
            percentContainer.lineTo(rightPoint.x - percentContainerWidth, rightPoint.y + percentTextBounds.height() + 2 * percentTextTopBottomPadding);
            percentTextPoint.x = rightPoint.x - percentTextLeftRightPadding - percentTextBounds.width();
            percentTextPoint.y = rightPoint.y + percentTextTopBottomPadding + percentTextBounds.height();

        } else {
            percentContainer.lineTo(rightPoint.x, criticPoint.y + percentTextBounds.height() + 2 * percentTextTopBottomPadding);
            percentContainer.lineTo(rightPoint.x - percentContainerWidth, criticPoint.y + percentTextBounds.height() + 2 * percentTextTopBottomPadding);
            percentTextPoint.x = rightPoint.x - percentTextLeftRightPadding - percentTextBounds.width();
            percentTextPoint.y = criticPoint.y + percentTextTopBottomPadding + percentTextBounds.height();
        }
        percentContainer.close();
        if (increasePercent > 0) paint.setColor(adjustAlpha(percentPositiveColor, 0.5f));
        else if (increasePercent < 0) paint.setColor(adjustAlpha(percentNegativeColor, 0.5f));
        else paint.setColor(adjustAlpha(percentNeutralColor, 0.5f));
        canvas.drawPath(percentContainer, paint);
        paint.setColor(centerTextColor);
        canvas.drawText(percent, percentTextPoint.x, percentTextPoint.y, paint);
        paint.setTextSize(percentTextSize);
        Rect weekdayTextBounds = new Rect();
        paint.getTextBounds(weekDay, 0, weekDay.length(), weekdayTextBounds);
        canvas.drawText(weekDay, percentTextLeftRightPadding, percentTextLeftRightPadding + weekdayTextBounds.height(), paint);

        Rect tempBounds = new Rect();
        paint.setTextSize(percentTextSize);
        paint.getTextBounds(percent, 0, percent.length(), tempBounds);

        float marginWidth = tempBounds.width() + 2*percentTextLeftRightPadding;
        Rect dayTextBounds = new Rect();
        paint.setTextSize(dayTextSize);
        paint.setColor(centerTextColor);
        String dayText = Integer.toString(day);
        paint.getTextBounds(dayText, 0, dayText.length(), dayTextBounds);
        canvas.drawText(dayText, (getWidth()-dayTextBounds.width())/2, (getHeight() + dayTextBounds.height())/2, paint);

    }

    private int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void animateDayView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                elapsed = 0;
                while (elapsed < total ) {
                    try {
                        Thread.sleep(interim);
                        elapsed += frame;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        postInvalidate();
                    }
                }
            }
        }).start();
    }

}
