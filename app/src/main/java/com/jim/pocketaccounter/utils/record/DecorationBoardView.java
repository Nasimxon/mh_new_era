package com.jim.pocketaccounter.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class DecorationBoardView extends BaseBoardView {
    private int table;
    private boolean sleep = true;
    private Context context;
    private RectF workscpace;
    private float beginMargin, shadowSide, betweenButtonsMargin, whiteSide, gradientSide, oneDp;
    private float buttonSideRatio = 0.225f, betweenButtonsRatio = 0.0335f;
    private Bitmap bitmap;
    private BitmapFactory.Options options;
    protected List<ABoardButton> buttons;
    private final int NOT_DRAWN = 0, DRAWING_PROCESS = 1, DRAWN = 2;
    private int drawState = NOT_DRAWN;
    private int alpha = 1000;
    private long relay = 100L, elapsed = 0L;
    public DecorationBoardView(Context context, int table) {
        super(context);
        this.table = table;
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int table) {
        super(context, attrs);
        this.table = table;
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int defStyleAttr, int table) {
        super(context, attrs, defStyleAttr);
        this.table = table;
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int table) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.table = table;
        this.context = context;
        init();
    }


    public void initCache(int cacheId, int bitmapResourceId, int side) {
        if (dataCache.getElements().get(cacheId) == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapResourceId, options);
            bitmap = Bitmap.createScaledBitmap(bitmap, side, side, false);
            dataCache.getElements().put(cacheId, bitmap);
        }

    }


    private void init() {
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        beginMargin = getResources().getDimension(R.dimen.ten_dp);
        int width = getResources().getDisplayMetrics().widthPixels;
        workscpace = new RectF(beginMargin, beginMargin, width - beginMargin, width - beginMargin);
        betweenButtonsMargin = workscpace.width()*betweenButtonsRatio;
        whiteSide = workscpace.width()*buttonSideRatio;
        shadowSide = whiteSide/0.7f;
        oneDp = getResources().getDimension(R.dimen.one_dp);
        gradientSide = whiteSide - oneDp;
        loadDrawingElements();
        initButtons();
    }

    public void loadDrawingElements() {
        initCache(LEFT_TOP_SHAPE_SHADOW, R.drawable.shadow_left_top, (int) shadowSide);
        initCache(LEFT_TOP_SHAPE_WHITE, R.drawable.left_top_shape_white, (int) whiteSide);
        initCache(LEFT_TOP_SHAPE_GRADIENT, R.drawable.left_top_shape_gradient, (int) gradientSide);
        initCache(RIGHT_TOP_SHAPE_SHADOW, R.drawable.shadow_right_top, (int) shadowSide);
        initCache(RIGHT_TOP_SHAPE_WHITE, R.drawable.right_top_shape_white, (int) whiteSide);
        initCache(RIGHT_TOP_SHAPE_GRADIENT, R.drawable.right_top_shape_gradient, (int) gradientSide);
        initCache(LEFT_BOTTOM_SHAPE_SHADOW, R.drawable.shadow_left_bottom, (int) shadowSide);
        initCache(LEFT_BOTTOM_SHAPE_WHITE, R.drawable.left_bottom_shape_white, (int) whiteSide);
        initCache(LEFT_BOTTOM_SHAPE_GRADIENT, R.drawable.left_bottom_shape_gradient, (int) gradientSide);
        initCache(RIGHT_BOTTOM_SHAPE_SHADOW, R.drawable.shadow_right_bottom, (int) shadowSide);
        initCache(RIGHT_BOTTOM_SHAPE_WHITE, R.drawable.right_bottom_shape_white, (int) whiteSide);
        initCache(RIGHT_BOTTOM_SHAPE_GRADIENT, R.drawable.right_bottom_shape_gradient, (int) gradientSide);
        initCache(SIMPLE_SHADOW, R.drawable.shadow_circular, (int) shadowSide);
        initCache(SIMPLE_WHITE, R.drawable.circle_white, (int) whiteSide);
        initCache(SIMPLE_GRADIENT, R.drawable.circle_gradient, (int) gradientSide);
    }

    private void initButtons() {
        buttons = new ArrayList<>();
        List<BoardButton> allButtons = daoSession
                                            .getBoardButtonDao()
                                            .queryBuilder()
                                            .where(BoardButtonDao.Properties.Table.eq(table))
                                            .build()
                                            .list();
        if (table == PocketAccounterGeneral.EXPENSE) {
            float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
            for (int pos = 0; pos < EXPENSE_BUTTONS_COUNT_PER_PAGE; pos++) {
                if (pos%4 == 0) {
                    left = workscpace.left;
                    top = workscpace.top + (whiteSide + betweenButtonsMargin)*pos/4;
                    right = left + whiteSide;
                    bottom = top + whiteSide;
                } else {
                    left = workscpace.left + (whiteSide+betweenButtonsMargin)*(pos%4);
                    right = left + whiteSide;
                }
                RectF container = new RectF(left, top, right, bottom);
                Long categoryId = null;
                for (BoardButton boardButton : allButtons) {
                    if (boardButton.getPos() == currentPage*EXPENSE_BUTTONS_COUNT_PER_PAGE + pos) {
                        categoryId = boardButton.getId();
                        break;
                    }
                }
                ABoardButton aBoardButton = new ABoardButton().builder()
                                                                    .container(container)
                                                                    .category(categoryId)
                                                                    .table(table)
                                                                    .position(pos)
                                                                    .build();
                buttons.add(aBoardButton);
            }
        }
        else {
            float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
            for (int pos = 0; pos < INCOME_BUTTONS_COUNT_PER_PAGE; pos++) {
                if (pos%4 == 0) {
                    left = workscpace.left;
                    top = workscpace.top + (whiteSide + betweenButtonsMargin)*pos/4;
                    right = left + whiteSide;
                    bottom = top + whiteSide;
                } else {
                    left = workscpace.left + (whiteSide+betweenButtonsMargin)*(pos%4);
                    right = left + whiteSide;
                }
                RectF container = new RectF(left, top, right, bottom);
                Long categoryId = null;
                for (BoardButton boardButton : allButtons) {
                    if (boardButton.getPos() == currentPage*EXPENSE_BUTTONS_COUNT_PER_PAGE + pos) {
                        categoryId = boardButton.getId();
                        break;
                    }
                }
                ABoardButton aBoardButton = new ABoardButton().builder()
                        .container(container)
                        .category(categoryId)
                        .table(table)
                        .position(pos)
                        .build();
                buttons.add(aBoardButton);
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawButtons(canvas);
    }

    private void drawButtons(Canvas canvas) {
        Paint shadowsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowsPaint.setAlpha(alpha);
        Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap shadow = null, white = null, gradient = null, icon = null;
        for (ABoardButton button : buttons) {
            if (table == PocketAccounterGeneral.EXPENSE) {
                switch (button.getPosition()) {
                    case 0:
                        shadow = dataCache.getElements().get(LEFT_TOP_SHAPE_SHADOW);
                        white = dataCache.getElements().get(LEFT_TOP_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(LEFT_TOP_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 1:
                    case 2:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 13:
                    case 14:
                        shadow = dataCache.getElements().get(SIMPLE_SHADOW);
                        white = dataCache.getElements().get(SIMPLE_WHITE);
                        gradient = dataCache.getElements().get(SIMPLE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 3:
                        shadow = dataCache.getElements().get(RIGHT_TOP_SHAPE_SHADOW);
                        white = dataCache.getElements().get(RIGHT_TOP_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(RIGHT_TOP_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 12:
                        shadow = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_SHADOW);
                        white = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 15:
                        shadow = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_SHADOW);
                        white = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                }
                canvas.drawBitmap(shadow, button.getContainer().centerX() - shadow.getWidth()/2,
                        button.getContainer().centerY() - shadow.getHeight()/2, shadowsPaint);
                canvas.drawBitmap(white, button.getContainer().left, button.getContainer().top, whitePaint);
                canvas.drawBitmap(gradient, button.getContainer().centerX() - gradient.getWidth()/2,
                        button.getContainer().centerY() - gradient.getHeight()/2, shadowsPaint);
                canvas.drawBitmap(icon, button.getContainer().centerX() - icon.getWidth()/2,
                        button.getContainer().centerY() - icon.getHeight()/2, shadowsPaint);
            }
            else {

            }
        }
    }


    public void sleep() {
        sleep = true;
        invalidate();
    }
    public void wakeUp() {
        sleep = false;
        invalidate();
    }
    class ABoardButton {
        private RectF container;
        private Long buttonId;
        private int table, position;
        private float iconSize = 0.35f*whiteSide;
        private Bitmap icon;
        public ABoardButton builder() {
            return this;
        }
        public ABoardButton container(RectF container) {
            this.container = container;
            iconSize = container.width()/3;
            return this;
        }
        public ABoardButton category(Long buttonId) {
            this.buttonId = buttonId;
            String categoryId = null;
            BoardButton boardButton = null;
            List<BoardButton> buttons = daoSession
                    .getBoardButtonDao()
                    .queryBuilder()
                    .where(BoardButtonDao.Properties.Id.eq(buttonId))
                    .list();
            if (!buttons.isEmpty()) {
                boardButton = buttons.get(0);
                categoryId = boardButton.getCategoryId();
            }
            if (categoryId == null) {
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
            } else {
                String iconPath = null;
                switch (boardButton.getType()) {
                    case PocketAccounterGeneral.CATEGORY:
                        iconPath = daoSession
                                    .getRootCategoryDao()
                                    .queryBuilder()
                                    .where(RootCategoryDao.Properties.Id.eq(categoryId))
                                    .list()
                                    .get(0)
                                    .getIcon();
                        break;
                    case PocketAccounterGeneral.CREDIT:
                        iconPath = daoSession
                                    .getCreditDetialsDao()
                                    .queryBuilder()
                                    .where(CreditDetialsDao.Properties.MyCredit_id.eq(categoryId))
                                    .list()
                                    .get(0)
                                    .getIcon_ID();
                        break;
                    case PocketAccounterGeneral.DEBT_BORROW:
                        iconPath = daoSession
                                    .getDebtBorrowDao()
                                    .queryBuilder()
                                    .where(DebtBorrowDao.Properties.Id.eq(categoryId))
                                    .list()
                                    .get(0)
                                    .getPerson()
                                    .getPhoto();
                        break;
                    case PocketAccounterGeneral.FUNCTION:
                        String[] operationIds = getResources().getStringArray(R.array.operation_ids);
                        String[] operationIcons = getResources().getStringArray(R.array.operation_icons);
                        for (int i = 0; i < operationIds.length; i++) {
                            if (operationIds[i].equals(categoryId)) {
                                iconPath = operationIcons[i];
                                break;
                            }
                        }
                        break;
                    case PocketAccounterGeneral.PAGE:
                        String[] pageIds = getResources().getStringArray(R.array.page_ids);
                        String[] pageIcons = getResources().getStringArray(R.array.page_icons);
                        for (int i = 0; i < pageIds.length; i++) {
                            if (pageIds[i].equals(categoryId)) {
                                iconPath = pageIcons[i];
                                break;
                            }
                        }
                        break;
                }
                int resId = getResources().getIdentifier(iconPath, "drawable", getContext().getPackageName());
                icon = BitmapFactory.decodeResource(getResources(), resId, options);
                icon = Bitmap.createScaledBitmap(icon, (int) iconSize, (int) iconSize, false);
                dataCache.getBoardBitmapsCache().put(buttonId, icon);
            }
            return this;
        }
        public ABoardButton table(int table) {
            this.table = table;
            return this;
        }
        public ABoardButton position(int position) {
            this.position = position;
            return this;
        }
        public ABoardButton build() {
            return this;
        }
        public int getTable() {
            return this.table;
        }
        public int getPosition() {
            return position;
        }
        public Long getButtonId() {
            return buttonId;
        }
        public RectF getContainer() {
            return container;
        }
    }
}
