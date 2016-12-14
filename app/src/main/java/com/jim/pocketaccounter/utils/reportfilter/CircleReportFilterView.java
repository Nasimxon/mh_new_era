package com.jim.pocketaccounter.utils.reportfilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.utils.GetterAttributColors;

import java.util.Calendar;

import javax.inject.Inject;

public class CircleReportFilterView extends View {

    //active mode options
    private final int BEGIN_ACTIVE = 0, END_ACTIVE = 1, NONE = 2;

    //options adding day
    public static final int BEGIN = 0, END = 1;

    //active mode
    private int mode = NONE;

    //preference keys
    private String BEGIN_YEAR = "BEGIN_YEAR";
    private String BEGIN_MONTH = "BEGIN_MONTH";
    private String BEGIN_DAY = "BEGIN_DAY";
    private String END_YEAR = "END_YEAR";
    private String END_MONTH = "END_MONTH";
    private String END_DAY = "END_DAY";

    //begin and end points
    private Calendar begin, end;

    //drawing area rectangle
    private RectF container;

    //main circle radius
    private float radius;

    //private main circle thickness
    private float thickness = 0.0f;

    //accedent color
    private int accedentColor = 0;

    //month texts
    private String[] monthTexts = null;
    private float textSize = 0.0f;

    //step agree and full circle degree
    private float step = 0.0f, fullDegree = 360.f;

    //last values
    private float beginLastValue = 0.0f, endLastValue = 0.0f;

    //touching angle margin
    private float angleMargin = 15.0f;

    //all cycles count
    private int betweenCyclesCount = 0;

    //begin and end degrees
    private float beginDegree = 0.0f, endDegree = 0.0f;
    private boolean beginCCW = true, endCCW = true;

    private String fromText = "", toText = "";

    private IntervlCircleTickListener listener;

    @Inject SharedPreferences preferences;
    public CircleReportFilterView(Context context) {
        super(context);
        init();
    }

    public CircleReportFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleReportFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public CircleReportFilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        ((PocketAccounterApplication) getContext().getApplicationContext()).component().inject(this);
        //init month texts
        monthTexts = getResources().getStringArray(R.array.month_abbrs);
        //init step
        step = fullDegree/monthTexts.length;
        //init begin point
        begin = Calendar.getInstance();
        begin.set(Calendar.YEAR, preferences.getInt(BEGIN_YEAR, begin.get(Calendar.YEAR)));
        begin.set(Calendar.MONTH, preferences.getInt(BEGIN_MONTH, begin.get(Calendar.MONTH)));
        begin.set(Calendar.DAY_OF_MONTH, preferences.getInt(BEGIN_DAY, 10));
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        //init end point
        end = Calendar.getInstance();
        end.set(Calendar.YEAR, preferences.getInt(END_YEAR, end.get(Calendar.YEAR)));
        end.add(Calendar.MONTH, 3);
        end.set(Calendar.MONTH, preferences.getInt(END_MONTH, end.get(Calendar.MONTH)));
        end.set(Calendar.DAY_OF_MONTH, preferences.getInt(END_DAY, 17));
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        float beginMaxDays = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
        beginDegree = begin.get(Calendar.YEAR) * 360 + step * (begin.get(Calendar.MONTH) + (begin.get(Calendar.DAY_OF_MONTH)-1)/beginMaxDays);
        beginLastValue = beginDegree;
        float endMaxDays = end.getActualMaximum(Calendar.DAY_OF_MONTH);
        endDegree = end.get(Calendar.YEAR) * 360 + step * (end.get(Calendar.MONTH) + (end.get(Calendar.DAY_OF_MONTH) - 1)/endMaxDays);
        endLastValue = endDegree;
        betweenCyclesCount = (int) ((endDegree - beginDegree)/360);
        accedentColor = GetterAttributColors.fetchHeadAccedentColor(getContext());
        thickness = getResources().getDimension(R.dimen.five_dp);
        textSize = getResources().getDimension(R.dimen.ten_dp);
        fromText = getResources().getString(R.string.from);
        fromText = fromText.substring(0, fromText.length() - 1);
        toText = getResources().getString(R.string.to);
        toText = toText.substring(0, toText.length() - 1);
        if (listener != null)
            listener.onIntervalCircleTick(begin, end, false);
        setClickable(true);
    }

    public Calendar getBegin() {
        return begin;
    }

    public Calendar getEnd() {
        return end;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMainCircle(canvas);
        drawValues(canvas);
        drawFromToTexts(canvas);
        drawBetweenArc(canvas);
    }

    private void drawBetweenArc(Canvas canvas) {

    }

    private void drawMainCircle(Canvas canvas) {
        if (container == null) {
            float side = getWidth() >= getHeight() ? getHeight() : getWidth();
            container = new RectF((getWidth() - side)/2.0f,
                                  (getHeight() - side)/2.0f,
                                  (getWidth() + side)/2.0f,
                                  (getHeight() + side)/2.0f);
        }
        radius = 0.85f * container.width()/2;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(accedentColor);
        canvas.drawCircle(container.centerX(), container.centerY(), radius, paint);
        float innerRadius = radius - thickness;
        paint.setColor(Color.WHITE);
        canvas.drawCircle(container.centerX(), container.centerY(), innerRadius, paint);
        paint.setColor(Color.parseColor("#414141"));
        paint.setTextSize(textSize);
        float textRadius = radius * 0.85f;
        Rect textBounds = new Rect();
        for (int i = 0; i < monthTexts.length; i++) {
            paint.getTextBounds(monthTexts[i], 0, monthTexts[i].length(), textBounds);
            float x = (float) (container.centerX() - textRadius * Math.sin(Math.toRadians(i * step - 180.0f)) - textBounds.width()/2);
            float y = (float) (container.centerY() + textRadius * Math.cos(Math.toRadians(i * step - 180.0f)) + textBounds.height()/2);
            canvas.drawText(monthTexts[i], x, y, paint);
        }
    }

    private void drawValues(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(adjustAlpha(accedentColor, 0.1f));
        paint.setStrokeWidth(2.0f);
        for (int i = 0; i < betweenCyclesCount; i++)
            canvas.drawCircle(container.centerX(), container.centerY(), radius, paint);
        paint.setColor(accedentColor);
        //draw begin line
        canvas.drawLine(container.centerX(), container.centerY(),
                (float) (container.centerX() - radius * Math.sin(Math.toRadians(beginDegree - 180.0f))),
                (float) (container.centerY() + radius * Math.cos(Math.toRadians(beginDegree - 180.0f))),
                paint);
        //draw end line
        canvas.drawLine(container.centerX(), container.centerY(),
                (float) (container.centerX() - radius * Math.sin(Math.toRadians(endDegree - 180.0f))),
                (float) (container.centerY() + radius * Math.cos(Math.toRadians(endDegree - 180.0f))),
                paint);
        paint.setColor(adjustAlpha(accedentColor, 0.1f));
        RectF arcRect = new RectF(container.centerX() - radius,
                                  container.centerY() - radius,
                                  container.centerX() + radius,
                                  container.centerY() + radius);
        //consider modes
        if (beginDegree%360 > endDegree%360)
            canvas.drawArc(arcRect, beginDegree%360 - 90.0f, fullDegree - beginDegree%360 + endDegree%360, true, paint);
        else
            canvas.drawArc(arcRect, beginDegree%360 - 90.0f, endDegree%360 - beginDegree%360, true, paint);

    }

    private void drawFromToTexts(Canvas canvas) {
        float fromToTextRadius = radius * 1.10f;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#414141"));
        paint.setTextSize(getResources().getDimension(R.dimen.eight_dp));
        Rect fromBounds = new Rect();
        paint.getTextBounds(fromText, 0, fromText.length(), fromBounds);
        Rect toBounds = new Rect();
        paint.getTextBounds(toText, 0, toText.length(), toBounds);
        float fromX = (float) (container.centerX() - fromToTextRadius * Math.sin(Math.toRadians(beginDegree - 180.0f))) - fromBounds.width()/2;
        float fromY = (float) (container.centerY() + fromToTextRadius * Math.cos(Math.toRadians(beginDegree - 180.0f))) + fromBounds.height()/2;
        canvas.drawText(fromText, fromX, fromY, paint);
        float toX = (float) (container.centerX() - fromToTextRadius * Math.sin(Math.toRadians(endDegree - 180.0f))) - toBounds.width()/2;
        float toY = (float) (container.centerY() + fromToTextRadius * Math.cos(Math.toRadians(endDegree - 180.0f))) + toBounds.height()/2;
        canvas.drawText(toText, toX, toY, paint);
    }

    public void setListener(IntervlCircleTickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchedX = event.getX();
        float touchedY = event.getY();
        float tx = touchedX - container.centerX();
        float ty = touchedY - container.centerY();
        float touchedDistance = (float) Math.sqrt(tx*tx + ty*ty);
        if (touchedDistance > radius)
            return super.onTouchEvent(event);
        float angle = 180.0f + (float) Math.toDegrees(Math.atan2(-tx, ty));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: //detect touched line
                if ((endDegree % 360) - angleMargin <= 0) {
                    if (((360.0f + ((endDegree%360) - angleMargin) <= angle && angle <= 360.0f) || (angle >= 0.0f && angle <= endDegree%360)) ||
                            (endDegree%360 <= angle && endDegree%360 + angleMargin >= angle)) {
                        mode = END_ACTIVE;
                        float residue = endDegree % step;
                        if (residue != 0) {
                            if (endDegree - residue < beginDegree)
                                endDegree = endDegree - residue + 360;
                            else
                                endDegree -= residue;
                            int year = (int) (endDegree/360);
                            end.set(Calendar.YEAR, year);
                            float res = endDegree % 360;
                            int month = (int) (res/step);
                            end.set(Calendar.MONTH, month);
                            end.set(Calendar.DAY_OF_MONTH, 1);
                            end.set(Calendar.HOUR_OF_DAY, 0);
                            end.set(Calendar.MINUTE, 0);
                            end.set(Calendar.SECOND, 0);
                            end.set(Calendar.MILLISECOND, 0);
                            if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            invalidate();
                        }
                    }
                } else {
                    if ((endDegree % 360) - angleMargin <= angle && (endDegree%360) + angleMargin >= angle) {
                        mode = END_ACTIVE;
                        float residue = endDegree % step;
                        if (residue != 0) {
                            if (endDegree - residue < beginDegree)
                                endDegree = endDegree - residue + 360;
                            else
                                endDegree -= residue;
                            int year = (int) (endDegree/360);
                            end.set(Calendar.YEAR, year);
                            float res = endDegree % 360;
                            int month = (int) (res/step);
                            end.set(Calendar.MONTH, month);
                            end.set(Calendar.DAY_OF_MONTH, 1);
                            end.set(Calendar.HOUR_OF_DAY, 0);
                            end.set(Calendar.MINUTE, 0);
                            end.set(Calendar.SECOND, 0);
                            end.set(Calendar.MILLISECOND, 0);
                            if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            invalidate();
                        }
                    }
                }
                if ((beginDegree%360) - angleMargin <= 0 && mode == NONE) {
                    if ((360.0f + ((beginDegree%360) - angleMargin) >= angle || (angle >= 0.0f && angle <= beginDegree%360)) ||
                            (beginDegree%360 <= angle && (beginDegree%360) + angleMargin >= angle)) {
                        mode = BEGIN_ACTIVE;
                        float residue = step - beginDegree % step;
                        if (residue%step != 0) {
                            if (beginDegree + residue > endDegree)
                                beginDegree = beginDegree + residue - 360;
                            else
                                beginDegree += residue;
                            int year = (int) (beginDegree/360);
                            begin.set(Calendar.YEAR, year);
                            float res = beginDegree % 360;
                            int month = (int) (res/step);
                            begin.set(Calendar.MONTH, month);
                            begin.set(Calendar.DAY_OF_MONTH, 1);
                            begin.set(Calendar.HOUR_OF_DAY, 0);
                            begin.set(Calendar.MINUTE, 0);
                            begin.set(Calendar.SECOND, 0);
                            begin.set(Calendar.MILLISECOND, 0);
                            if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            invalidate();
                        }
                    }
                } else if (mode == NONE){
                    if ((beginDegree%360) - angleMargin <= angle && (beginDegree%360) + angleMargin >= angle) {
                        mode = BEGIN_ACTIVE;
                        float residue = step - beginDegree % step;
                        if (residue%step != 0) {
                            if (beginDegree + residue > endDegree)
                                beginDegree = beginDegree + residue - 360;
                            else
                                beginDegree += residue;
                            int year = (int) (beginDegree/360);
                            begin.set(Calendar.YEAR, year);
                            float res = beginDegree % 360;
                            int month = (int) (res/step);
                            begin.set(Calendar.MONTH, month);
                            begin.set(Calendar.DAY_OF_MONTH, 1);
                            begin.set(Calendar.HOUR_OF_DAY, 0);
                            begin.set(Calendar.MINUTE, 0);
                            begin.set(Calendar.SECOND, 0);
                            begin.set(Calendar.MILLISECOND, 0);
                            if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: //line move
                int section = (int) Math.floor(angle/step);
                if (mode != NONE) {
                    switch (mode) {
                        case BEGIN_ACTIVE:
                            if ((beginLastValue%360 == 330 && section*step == 0 && step*section != beginLastValue%360 ) ||
                                    (step * section - beginLastValue%360 == 30)) {
                                if (beginDegree + step <= endDegree)
                                    beginDegree += step;
                                else
                                    beginDegree = beginDegree - 360 + step;
                                int year = (int) (beginDegree/360);
                                begin.set(Calendar.YEAR, year);
                                float res = beginDegree % 360;
                                int month = (int) (res/step);
                                begin.set(Calendar.MONTH, month);
                                begin.set(Calendar.DAY_OF_MONTH, 1);
                                begin.set(Calendar.HOUR_OF_DAY, 0);
                                begin.set(Calendar.MINUTE, 0);
                                begin.set(Calendar.SECOND, 0);
                                begin.set(Calendar.MILLISECOND, 0);
                                if (listener != null) listener.onIntervalCircleTick(begin, end, false);

                            }
                            else if ((beginLastValue%360 == 0 && section*step == 330 && step*section != beginLastValue%360 ) ||
                                    (beginLastValue%360 - step * section == 30 )) {
                                    beginDegree -= step;
                                int year = (int) (beginDegree/360);
                                begin.set(Calendar.YEAR, year);
                                float res = beginDegree % 360;
                                int month = (int) (res/step);
                                begin.set(Calendar.MONTH, month);
                                begin.set(Calendar.DAY_OF_MONTH, 1);
                                begin.set(Calendar.HOUR_OF_DAY, 0);
                                begin.set(Calendar.MINUTE, 0);
                                begin.set(Calendar.SECOND, 0);
                                begin.set(Calendar.MILLISECOND, 0);
                                if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            }
                            betweenCyclesCount = (int) (endDegree - beginDegree)/360;
                            beginLastValue = beginDegree;
                            invalidate();
                            break;
                        case END_ACTIVE:
                            if ((endLastValue%360 == 330 && section*step == 0 && step*section != endLastValue%360 ) ||
                                (step * section - endLastValue%360 == 30)) {
                                endDegree += step;
                                int year = (int) (endDegree/360);
                                end.set(Calendar.YEAR, year);
                                float res = endDegree % 360;
                                int month = (int) (res/step);
                                end.set(Calendar.MONTH, month);
                                end.set(Calendar.DAY_OF_MONTH, 1);
                                end.set(Calendar.HOUR_OF_DAY, 0);
                                end.set(Calendar.MINUTE, 0);
                                end.set(Calendar.SECOND, 0);
                                end.set(Calendar.MILLISECOND, 0);
                                if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            }
                            else if ((endLastValue%360 == 0 && section*step == 330 && step*section != endLastValue%360 ) ||
                                     (endLastValue%360 - step * section == 30 )) {
                                if (endDegree - step >= beginDegree)
                                    endDegree -= step;
                                else
                                    endDegree = endDegree + 360 - step;
                                int year = (int) (endDegree/360);
                                end.set(Calendar.YEAR, year);
                                float res = endDegree % 360;
                                int month = (int) (res/step);
                                end.set(Calendar.MONTH, month);
                                end.set(Calendar.DAY_OF_MONTH, 1);
                                end.set(Calendar.HOUR_OF_DAY, 0);
                                end.set(Calendar.MINUTE, 0);
                                end.set(Calendar.SECOND, 0);
                                end.set(Calendar.MILLISECOND, 0);
                                if (listener != null) listener.onIntervalCircleTick(begin, end, false);
                            }
                            betweenCyclesCount = (int) (endDegree - beginDegree)/360;
                            endLastValue = endDegree;
                            invalidate();
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setDay(int day, int mode) {
        float maxDays = 0.0f, degree = 0.0f;
        switch (mode) {
            case BEGIN:
                begin.set(Calendar.YEAR, begin.get(Calendar.YEAR));
                begin.set(Calendar.MONTH, begin.get(Calendar.MONTH));
                begin.set(Calendar.DAY_OF_MONTH, day+1);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                maxDays = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
                degree = step * (float)day/maxDays;
                beginDegree = begin.get(Calendar.YEAR) * 360 + begin.get(Calendar.MONTH) * step + degree;
                break;
            case END:
                end.set(Calendar.YEAR, end.get(Calendar.YEAR));
                end.set(Calendar.MONTH, end.get(Calendar.MONTH));
                end.set(Calendar.DAY_OF_MONTH, day+1);
                end.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.MINUTE, 0);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);
                maxDays = end.getActualMaximum(Calendar.DAY_OF_MONTH);
                degree = step * (float)day/maxDays;
                endDegree = end.get(Calendar.YEAR) * 360 + end.get(Calendar.MONTH) * step + degree;
                break;
        }
        invalidate();
        if (listener != null)
            listener.onIntervalCircleTick(begin, end, true);
    }

    public void saveState() {
        preferences
                .edit()
                .putInt(BEGIN_YEAR, begin.get(Calendar.YEAR))
                .commit();
        preferences
                .edit()
                .putInt(BEGIN_MONTH, begin.get(Calendar.MONTH))
                .commit();
        preferences
                .edit()
                .putInt(BEGIN_DAY, begin.get(Calendar.DAY_OF_MONTH))
                .commit();
        preferences
                .edit()
                .putInt(END_YEAR, end.get(Calendar.YEAR))
                .commit();
        preferences
                .edit()
                .putInt(BEGIN_MONTH, end.get(Calendar.MONTH))
                .commit();
        preferences
                .edit()
                .putInt(END_DAY, end.get(Calendar.DAY_OF_MONTH))
                .commit();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public interface IntervlCircleTickListener {
        void onIntervalCircleTick(Calendar begin, Calendar end, boolean dayPickMode);
    }
}
