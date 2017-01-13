package com.jim.finansia.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.billing.PurchaseImplementation;
import com.jim.finansia.utils.cache.DataCache;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by vosit on 27.10.16.
 */
@SuppressLint("NewApi")
public abstract class BaseBoardView extends View {
    @Inject PAFragmentManager paFragmentManager;
    @Inject DaoSession daoSession;
    @Inject DataCache dataCache;
    @Inject CommonOperations commonOperations;
    @Inject LogicManager logicManager;
    @Inject ReportManager reportManager;
    @Inject SharedPreferences sharedPreferences;
    @Inject @Named(value = "begin") Calendar begin;
    @Inject @Named(value = "end") Calendar end;
    @Inject @Named(value = "common_formatter") SimpleDateFormat simpleDateFormat;
    @Inject PurchaseImplementation purchaseImplementation;
    protected int INCOME_BUTTONS_COUNT_PER_PAGE = 4;
    protected int EXPENSE_BUTTONS_COUNT_PER_PAGE = 16;
    protected int setCount = 4;
    protected int currentPage = 0;
    protected int table;
    protected final int
            LEFT_TOP_SHAPE_SHADOW = 0, LEFT_TOP_SHAPE_WHITE = 1, LEFT_TOP_SHAPE_GRADIENT = 2,
            RIGHT_TOP_SHAPE_SHADOW = 3, RIGHT_TOP_SHAPE_WHITE = 4, RIGHT_TOP_SHAPE_GRADIENT = 5,
            LEFT_BOTTOM_SHAPE_SHADOW = 6, LEFT_BOTTOM_SHAPE_WHITE = 7, LEFT_BOTTOM_SHAPE_GRADIENT = 8,
            RIGHT_BOTTOM_SHAPE_SHADOW = 9, RIGHT_BOTTOM_SHAPE_WHITE = 10, RIGHT_BOTTOM_SHAPE_GRADIENT = 11,
            SIMPLE_SHADOW = 12, SIMPLE_WHITE = 13, SIMPLE_GRADIENT = 14;

    public BaseBoardView(Context context, int table) {
        super(context);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.table = table;
        refreshPagesCount();
    }

    public BaseBoardView(Context context, AttributeSet attrs, int table) {
        super(context, attrs);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.table = table;
        refreshPagesCount();
    }

    public BaseBoardView(Context context, AttributeSet attrs, int defStyleAttr, int table) {
        super(context, attrs, defStyleAttr);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.table = table;
        refreshPagesCount();
    }

    public BaseBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int table) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.table = table;
        refreshPagesCount();
    }

    public void refreshPagesCount() {
        setCount = sharedPreferences.getInt("key_for_window_top", 4);
        if (table == PocketAccounterGeneral.EXPENSE)
            currentPage = sharedPreferences.getInt("expense_current_page", 0);
        else
            currentPage = sharedPreferences.getInt("income_current_page", 0);
    }


}
