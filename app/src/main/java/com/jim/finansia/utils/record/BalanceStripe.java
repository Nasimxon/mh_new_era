package com.jim.finansia.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("NewApi")
public class BalanceStripe extends LinearLayout {
    private Calendar begin;
    private Calendar end;
    private SharedPreferences preferences;
    private ReportManager reportManager;
    private DataCache dataCache;
    private CommonOperations commonOperations;
    private TextView tvBalanceIncome, tvBalanceExpense, tvBalanceBalance;
    private Calendar day;
    private LinearLayout llNotImportant;
    private LinearLayout llBalanceStripeBackground;
    private FrameLayout paddingIskustvenno;
    public BalanceStripe(Context context, Calendar day, Calendar begin, Calendar end, SharedPreferences preferences, ReportManager reportManager, DataCache dataCache, CommonOperations commonOperations) {
        super(context);
        this.day = day;
        init(begin, end, preferences, reportManager, dataCache, commonOperations);
    }
    public BalanceStripe(Context context, AttributeSet attrs, Calendar day, Calendar begin, Calendar end, SharedPreferences preferences, ReportManager reportManager, DataCache dataCache, CommonOperations commonOperations) {
        super(context, attrs);
        this.day = day;
        init(begin, end, preferences, reportManager, dataCache, commonOperations);
    }
    public BalanceStripe(Context context, AttributeSet attrs, int defStyleAttr, Calendar day, Calendar begin, Calendar end, SharedPreferences preferences, ReportManager reportManager, DataCache dataCache, CommonOperations commonOperations) {
        super(context, attrs, defStyleAttr);
        this.day = day;
        init(begin, end, preferences, reportManager, dataCache, commonOperations);
    }
    public BalanceStripe(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Calendar day, Calendar begin, Calendar end, SharedPreferences preferences, ReportManager reportManager, DataCache dataCache, CommonOperations commonOperations) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.day = day;
        init(begin, end, preferences, reportManager, dataCache, commonOperations);
    }
    private void init(Calendar begin, Calendar end, SharedPreferences preferences, ReportManager reportManager, DataCache dataCache, CommonOperations commonOperations) {
        inflate(getContext(), R.layout.balance_stripe, this);
        this.begin = (Calendar) begin.clone();
        this.end = (Calendar) end.clone();
        this.preferences = preferences;
        this.reportManager = reportManager;
        this.dataCache = dataCache;
        this.commonOperations = commonOperations;
        tvBalanceIncome = (TextView) findViewById(R.id.tvBalanceIncome);
        tvBalanceExpense = (TextView) findViewById(R.id.tvBalanceExpense);
        tvBalanceBalance = (TextView) findViewById(R.id.tvBalanceBalance);
        llNotImportant = (LinearLayout) findViewById(R.id.llNotImportant);
        paddingIskustvenno = (FrameLayout) findViewById(R.id.paddingIskustvenniy);
        llBalanceStripeBackground = (LinearLayout) findViewById(R.id.llBalanceStripeBackground);
    }
    public void hideNotImportantPart() {
        llNotImportant.setVisibility(GONE);
        paddingIskustvenno.setVisibility(VISIBLE);
    }
    public void showNotImportantPart() { llNotImportant.setVisibility(VISIBLE); paddingIskustvenno.setVisibility(GONE); }
    public void refreshCurrencies() {
        commonOperations.refreshCurrency();
    }
    public void calculateBalance() {
        String mode = preferences.getString("balance_solve", "0");
        end.setTimeInMillis(day.getTimeInMillis());
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        if (mode.equals("0")) {
            begin.setTimeInMillis(dataCache.getBeginDate().getTimeInMillis());
        }
        else {
            begin.setTimeInMillis(day.getTimeInMillis());
            begin.set(Calendar.HOUR_OF_DAY, 0);
            begin.set(Calendar.MINUTE, 0);
            begin.set(Calendar.SECOND, 0);
            begin.set(Calendar.MILLISECOND, 0);
        }
        Map<String, Double> balance = reportManager.calculateBalance(begin, end);
        DecimalFormat decFormat = new DecimalFormat("0.##");
        String abbr = commonOperations.getMainCurrency().getAbbr();
        for (String key : balance.keySet()) {
            switch (key) {
                case PocketAccounterGeneral.INCOMES:
                    tvBalanceIncome.setText(decFormat.format(balance.get(key)) + abbr);
                    break;
                case PocketAccounterGeneral.EXPENSES:
                    tvBalanceExpense.setText(decFormat.format(balance.get(key)) + abbr);
                    break;
                case PocketAccounterGeneral.BALANCE:
                    tvBalanceBalance.setText(decFormat.format(balance.get(key)) + abbr);
                    break;
            }
        }
    }
}
