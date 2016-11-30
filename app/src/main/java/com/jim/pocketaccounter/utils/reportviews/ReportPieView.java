package com.jim.pocketaccounter.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.jim.pocketaccounter.R;

import java.util.ArrayList;
import java.util.List;

public class ReportPieView extends View {
    private List<PieData> datas;
    private boolean hasDatasToShow = false;
    private Bitmap bitmap;
    private List<ArcShapeWithRotationAngle> arcs;
    static final int BABY = 0, NORMAL = 1, ALONE = 2;
    private Long total = 200L,
                    elapsed = 200L,
                    interim = 10L,
                    frameDuration = 2L;
    private float tenDp = 0.0f;
    private float thickness = 0.0f;
    private float bitmapMargin = 0.0f;
    private float noDataTextSize = 0.0f;
    private float babyAngle = 2.0f;
    private int bgColor = Color.WHITE;
    private boolean disabled = true;
    private String noDataText = "No data to show";
    public ReportPieView(Context context) {
        super(context);
        init();
    }
    public ReportPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ReportPieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    @SuppressLint("NewApi")
    public ReportPieView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_category);
        tenDp = getResources().getDimension(R.dimen.ten_dp);
        thickness = getResources().getDimension(R.dimen.ten_dp);
        bitmapMargin = getResources().getDimension(R.dimen.fifteen_dp);
        noDataTextSize = getResources().getDimension(R.dimen.eleven_dp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datas != null && !datas.isEmpty() && arcs != null && !arcs.isEmpty()) {
            drawPie(canvas);
        } else {
            drawWhenNoDatas(canvas);
        }
    }

    private void drawWhenNoDatas(Canvas canvas) {
        float side = getWidth() >= getHeight() ? getHeight() : getWidth();
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (side/2), (int) (side/2), false);
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, getWidth()/2 - bitmap.getWidth()/2,
                getHeight()/2 - bitmap.getHeight()/2, bitmapPaint);
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#414141"));
        Rect textBounds = new Rect();
        textPaint.setTextSize(noDataTextSize);
        textPaint.getTextBounds(noDataText, 0, noDataText.length(), textBounds);
        canvas.drawText(noDataText, getWidth()/2 - textBounds.width()/2,
                getHeight()/2 + bitmap.getHeight()/2 + 2*tenDp, textPaint);
    }

    public void setCenterBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public void setNoDataTextSize(float noDataTextSize) {
        this.noDataTextSize = noDataTextSize;
        invalidate();
    }

    public void setNoDataText(String noDataText) {
        this.noDataText = noDataText;
        invalidate();
    }

    private void drawPie(Canvas canvas) {
        float side = getWidth() >= getHeight() ? getHeight() : getWidth();
        float timeRatio = (float) elapsed/ (float) total;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float angle = timeRatio * 360.0f;
        for (int i = 0; i < arcs.size(); i++) {
            paint.setColor(datas.get(i).getColor());
            ArcShapeWithRotationAngle arc = arcs.get(i);
            float left, top, right, bottom;
            if (getWidth() > getHeight()) {
                left = (getWidth() - side) / 2;
                right = left + side;
                top = 0.0f;
                bottom = top + side;
            } else {
                left = 0.0f;
                right = left + side;
                top = (getHeight() - side) / 2;
                bottom = top + side;
            }
            RectF rectF = new RectF(left, top, right, bottom);
            if (!disabled) {
                switch (arc.getType()) {
                    case ALONE:
                        canvas.drawArc(rectF, arc.getStartAngle(), angle, true, paint);
                        break;
                    case BABY:
                        if (angle > arc.getStartAngle() + babyAngle)
                            canvas.drawArc(rectF, arc.getStartAngle(), babyAngle, true, paint);
                        break;
                    case NORMAL:
                        float sweep = 0.0f;
                        if (angle > arc.getStartAngle() && angle <= arc.getStartAngle() + arc.getSweepAngle())
                            sweep = angle - arc.getStartAngle();
                        else if (angle >= arc.getStartAngle() + arc.getSweepAngle())
                            sweep = arc.getSweepAngle();
                        canvas.drawArc(rectF, arc.getStartAngle(), sweep, true, paint);
                        break;
                }
            }
        }
        paint.setColor(bgColor);
        float left, top, right, bottom;
        if (getWidth() >= getHeight()) {
            left = (getWidth() - side) / 2 + thickness;
            top = thickness;
            right = left + side - 2*thickness;
            bottom = top + side - 2*thickness;
        } else {
            left = thickness;
            top = (getHeight() - side) / 2 + thickness;
            right = left + side - 2*thickness;
            bottom = top + side - 2*thickness;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawArc(rectF, 0, 360.0f*timeRatio, true, paint);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (side/2), (int) (side/2), false);
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, getWidth()/2 - bitmap.getWidth()/2, getHeight()/2 - bitmap.getHeight()/2, bitmapPaint);
    }

    public void setDisabled(boolean disabled, boolean invalidate) {
        this.disabled = disabled;
        if (invalidate)
            invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        this.bgColor = color;
    }

    public void setDatas(List<PieData> datas) {
        this.datas = datas;
        calculatePercents();
        invalidate();
    }

    private void calculatePercents() {
        if (arcs == null) arcs = new ArrayList<>(); // creating new list
        else arcs.clear(); // clear the list
        hasDatasToShow = !datas.isEmpty(); // defining existing of datas
        if (hasDatasToShow) {
            double total = 0.0d;
            for (PieData data : datas)
                total += data.getAmount();
            List<Integer> types = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++) {
                float percent = (float) (100 * datas.get(i).getAmount() / total);
                float angle = 3.6f * percent;
                if (datas.size() != 1) {
                    if (angle <= 1.0f)
                        types.add(BABY);
                    else
                        types.add(NORMAL);

                }
                else {
                    types.add(ALONE);
                }
            }
            for (int i = 0; i < types.size(); i++) {
                float percent = (float) (100 * datas.get(i).getAmount() / total);
                switch (types.get(i)) {
                    case ALONE:
                        ArcShapeWithRotationAngle arcShapeWithRotationAngle = new ArcShapeWithRotationAngle();
                        arcShapeWithRotationAngle.setPercent(percent);
                        arcShapeWithRotationAngle.setType(ALONE);
                        arcShapeWithRotationAngle.setStartAngle(0.0f);
                        arcShapeWithRotationAngle.setSweepAngle(360.0f);
                        arcs.add(arcShapeWithRotationAngle);
                        break;
                    case BABY:
                            arcShapeWithRotationAngle = new ArcShapeWithRotationAngle();
                            arcShapeWithRotationAngle.setType(BABY);
                        if (i == 0) {
                            arcShapeWithRotationAngle.setPercent(percent);
                            arcShapeWithRotationAngle.setStartAngle(0.0f);
                        } else {
                            arcShapeWithRotationAngle.setPercent(percent);
                            float startAngle = 0.0f;
                            switch (arcs.get(i - 1).getType()) {
                                case BABY:
                                    startAngle = arcs.get(i - 1).getStartAngle() + babyAngle + 1.0f;
                                    break;
                                case NORMAL:
                                    startAngle = arcs.get(i - 1).getStartAngle() + arcs.get(i - 1).getSweepAngle() + 1.0f;
                                    break;
                            }
                            arcShapeWithRotationAngle.setStartAngle(startAngle);
                        }
                        arcShapeWithRotationAngle.setSweepAngle(babyAngle);
                        arcs.add(arcShapeWithRotationAngle);
                        break;
                    case NORMAL:
                        arcShapeWithRotationAngle = new ArcShapeWithRotationAngle();
                        arcShapeWithRotationAngle.setType(NORMAL);
                        arcShapeWithRotationAngle.setPercent(percent);
                        int countOfBabies = 0;
                        int next = i + 1;
                        while (next < types.size() && types.get(next) == BABY) {
                            countOfBabies++;
                            next++;
                        }
                        float babiesDifference = countOfBabies * (babyAngle + 1.0f);
                        if (i == 0) {
                            arcShapeWithRotationAngle.setStartAngle(0.0f);
                        }
                        else {
                            arcShapeWithRotationAngle.setStartAngle(arcs.get(i - 1).getStartAngle() + arcs.get(i - 1).getSweepAngle() + 1.0f);
                        }
                        if (i == types.size() - 1)
                            if (types.get(0) == BABY)
                                arcShapeWithRotationAngle.setSweepAngle(3.6f * percent - babyAngle);
                            else
                                arcShapeWithRotationAngle.setSweepAngle(359.0f - arcShapeWithRotationAngle.getStartAngle());
                        else
                            arcShapeWithRotationAngle.setSweepAngle(3.6f * percent - babiesDifference - 1.0f);
                        arcs.add(arcShapeWithRotationAngle);
                        break;
                }
            }
        }
    }

    public void setBabyAngle(float babyAngle) {
        this.babyAngle = babyAngle;
        invalidate();
    }

    public void defineRatioBetweenArcs(ArcShapeWithRotationAngle arcShapeWithRotationAngle) {
        if (datas.size() == 1) {
            arcShapeWithRotationAngle.setType(ALONE);
            return;
        }
        if (arcShapeWithRotationAngle.getSweepAngle() <= 1.0f)
            arcShapeWithRotationAngle.setType(BABY);
        else
            arcShapeWithRotationAngle.setType(NORMAL);
    }

    public void animatePie() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                elapsed = 0L;
                while (elapsed <= total) {
                    try {
                        Thread.sleep(interim);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        postInvalidate();
                        elapsed += frameDuration;
                    }
                }
                elapsed = total;
            }
        }).start();
    }
}
