package com.jim.pocketaccounter.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("NewApi")
public class TextDrawingBoardView extends DecorationBoardView {
    private boolean drawing = false, drawn = false;
    private int relay = 50, elapsed = 0;
    private int interim = 4;
    private ArrayList<TextElement> elements;
    protected Calendar day;
    private float execFrame;
    private int color, red, green;
    private float delay;
    public TextDrawingBoardView(Context context, int table, Calendar day) {
        super(context, table);
        this.day = day;
        init();
    }

    public TextDrawingBoardView(Context context, AttributeSet attrs, int table, Calendar day) {
        super(context, attrs, table);
        this.day = day;
        init();
    }

    public TextDrawingBoardView(Context context, AttributeSet attrs, int defStyleAttr, int table, Calendar day) {
        super(context, attrs, defStyleAttr, table);
        this.day = day;
        init();
    }

    public TextDrawingBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int table, Calendar day) {
        super(context, attrs, defStyleAttr, defStyleRes, table);
        this.day = day;
        init();
    }

    protected void init() {
        super.initButtons();
        color = ContextCompat.getColor(getContext(), R.color.toolbar_text_color);
        red = ContextCompat.getColor(getContext(), R.color.red);
        green = ContextCompat.getColor(getContext(), R.color.green_just);
        elements = new ArrayList<>();
        delay = getResources().getDimension(R.dimen.eight_dp);
        for (ABoardButton button : buttons) {
            TextElement textElement = new TextElement(button);
            elements.add(textElement);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawing) {
            for (TextElement textElement : elements) {
                drawWithAnimation(textElement, canvas);
            }
        }
        if (drawn) {
            for (TextElement textElement : elements) {
                drawText(textElement, canvas);
            }
        }
    }
    private void drawWithAnimation(TextElement element, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1f);
        paint.setColor(color);
        paint.setAlpha(0x80);
        paint.setStyle(Paint.Style.STROKE);
        RectF cont = element.getContainer();
        RectF container = new RectF(cont.left + delay, cont.top + delay, cont.right, cont.bottom);
        float iconSide = container.width()/3 - delay;
        PointF beginPoint = new PointF(container.left + iconSide, container.top + iconSide);
        Path path = element.getPath();
        path.moveTo(beginPoint.x, beginPoint.y);
        if (execFrame <= 0.25) {
            double sqrtTwo = Math.sqrt(2.0d);
            canvas.drawLine(beginPoint.x, beginPoint.y, (float)(beginPoint.x - sqrtTwo * execFrame * iconSide), (float) (beginPoint.y - sqrtTwo * execFrame * iconSide), paint);
        }
        else {
            float diff = (execFrame - 0.25f)/0.75f;
            canvas.drawLine(beginPoint.x, beginPoint.y, container.left, container.top, paint);
            canvas.drawLine(container.left, container.top, container.left + container.width()*diff, container.top, paint);
            if (execFrame >= 0.5f) {
                Paint textPaint = new Paint();
                textPaint.setAntiAlias(true);
                textPaint.setColor(color);
                textPaint.setAlpha((int)(0x80*(execFrame-0.5f)/0.5f));
                textPaint.setTextSize(getResources().getDimension(R.dimen.twelve_dp));
                Rect rect = new Rect();
                textPaint.getTextBounds(element.getText(), 0, element.getText().length(), rect);
                canvas.drawText(element.getText(), container.left /*right - rect.width()*/, container.top - rect.height()/3, textPaint);
                String amount = element.getAmount() + commonOperations.getMainCurrency().getAbbr();
                textPaint.getTextBounds(amount, 0, amount.length(), rect);
                if (table == PocketAccounterGeneral.EXPENSE)
                    textPaint.setColor(red);
                else
                    textPaint.setColor(green);
                canvas.drawText(amount, container.right - rect.width(), container.top + 4*rect.height()/3, textPaint);
            }
        }
    }

    private void drawText(TextElement element, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Path path = element.getPath();
        RectF container = element.getContainer();
        path.moveTo(container.left + container.width()/3, container.top + container.height()/3);
        path.moveTo(container.left, container.top);
        path.moveTo(container.right, container.top);
        canvas.drawPath(path, paint);
    }

    public void showText() {
        drawing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (elapsed <= relay) {
                    try {
                        Thread.sleep(interim);
                        execFrame = (float) elapsed/relay;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        postInvalidate();
                        elapsed++;
                    }
                }
                elapsed = 0;
                drawn = true;
                sharedPreferences.edit().putBoolean(PocketAccounterGeneral.INFO_VISIBILITY, true).commit();
            }
        }).start();
    }

    public void hideText() {
        drawing = false;
        drawn = false;
        invalidate();
        sharedPreferences.edit().putBoolean(PocketAccounterGeneral.INFO_VISIBILITY, false).commit();
    }

    class TextElement {
        private String text;
        private Path path;
        private double amount;
        private RectF container;
        TextElement(ABoardButton aBoardButton) {
            path = new Path();
            container = aBoardButton.getContainer();
            BoardButton boardButton = daoSession.getBoardButtonDao().load(aBoardButton.getButtonId());
            if (boardButton != null && boardButton.getCategoryId() != null) {
                String categoryId = daoSession.getBoardButtonDao().load(boardButton.getId()).getCategoryId();
                int type = commonOperations.defineType(categoryId);
                switch (type) {
                    case PocketAccounterGeneral.CATEGORY:
                        text = daoSession.getRootCategoryDao().load(categoryId).getName();
                        break;
                    case PocketAccounterGeneral.CREDIT:
                        text = daoSession.getCreditDetialsDao().load(Long.parseLong(categoryId)).getCredit_name();
                        break;
                    case PocketAccounterGeneral.DEBT_BORROW:
                        text = daoSession.getDebtBorrowDao().load(categoryId).getPerson().getName();
                        break;
                    case PocketAccounterGeneral.FUNCTION:
                        String[] operationIds = getResources().getStringArray(R.array.operation_ids);
                        String[] operationNames = getResources().getStringArray(R.array.operation_names);
                        for (int i = 0; i < operationIds.length; i++) {
                            if (operationIds[i].equals(categoryId)) {
                                text = operationNames[i];
                                break;
                            }
                        }
                        break;
                    case PocketAccounterGeneral.PAGE:
                        String[] pageIds = getResources().getStringArray(R.array.page_ids);
                        String[] pageNames = getResources().getStringArray(R.array.page_names);
                        for (int i = 0; i < pageIds.length; i++) {
                            if (pageIds[i].equals(categoryId)) {
                                text = pageNames[i];
                                break;
                            }
                        }
                        break;
                }
            }
            else {
                text = getResources().getString(R.string.add);
            }
            amount = dataCache.getPercent(table, day, boardButton.getPos());
        }
        public RectF getContainer() {
            return container;
        }
        public Path getPath() {
            return path;
        }
        public String getText() {
            return text;
        }
        public double getAmount() {
            return amount;
        }
    }
}
