package com.jim.finansia.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.reportviews.LinearChartWithAverageView;
import com.jim.finansia.utils.reportviews.ReportSelectingYearWithMonthsView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class ReportByIncomeExpenseMonthlyFragment extends Fragment {
    private ReportSelectingYearWithMonthsView rsywmvReportByIncomeExpenseMonthly;
    private TextView tvJanuary, tvFebruary, tvMarch, tvApril, tvMay, tvJune, tvJuly, tvAugust, tvSeptember, tvOctober, tvNovember, tvDecember;
    private int activeColor = Color.parseColor("#414141"), notActiveColor = Color.parseColor("#9c9c9c");
    private int mode = PocketAccounterGeneral.INCOME;
    private RelativeLayout llIncomeButton, llExpenseButton;
    private ImageView ivReportExpense, ivReportIncome;
    private TextView tvReportIncome, tvReportExpense;
    private TextView tvReportMonthlyMaximumAmount,
                     tvReportMaximumTotal,
                     tvReportMonthlyMiniumAmount,
                     tvReportMinimum,
                     tvReportMonthlyAverageAmount,
                     tvReportMonthlyAverage,
                     tvReportMonthlyTotalAmount,
                     tvReportMonthlyTotal,
                     tvReportMonthlyTop;

    private LinearChartWithAverageView lchwavReportMonthly;
    private int year, month;
    private SimpleDateFormat format = new SimpleDateFormat("LLLL, yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject DecimalFormat formetter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.report_by_income_expense_monthly, container, false);
        toolbarManager.setOnTitleClickListener(null);
        tvJanuary = (TextView) rootView.findViewById(R.id.tvJanuary);
        tvFebruary = (TextView) rootView.findViewById(R.id.tvFebruary);
        tvMarch = (TextView) rootView.findViewById(R.id.tvMarch);
        tvApril = (TextView) rootView.findViewById(R.id.tvApril);
        tvMay = (TextView) rootView.findViewById(R.id.tvMay);
        tvJune = (TextView) rootView.findViewById(R.id.tvJune);
        tvJuly = (TextView) rootView.findViewById(R.id.tvJuly);
        tvAugust = (TextView) rootView.findViewById(R.id.tvAugust);
        tvSeptember = (TextView) rootView.findViewById(R.id.tvSeptember);
        tvOctober = (TextView) rootView.findViewById(R.id.tvOctober);
        tvNovember = (TextView) rootView.findViewById(R.id.tvNovember);
        tvDecember = (TextView) rootView.findViewById(R.id.tvDecember);
        tvReportMonthlyMaximumAmount = (TextView) rootView.findViewById(R.id.tvReportMonthlyMaximumAmount);
        tvReportMaximumTotal = (TextView) rootView.findViewById(R.id.tvReportMaximumTotal);
        tvReportMonthlyMiniumAmount = (TextView) rootView.findViewById(R.id.tvReportMonthlyMiniumAmount);
        tvReportMinimum = (TextView) rootView.findViewById(R.id.tvReportMinimum);
        tvReportMonthlyAverageAmount = (TextView) rootView.findViewById(R.id.tvReportMonthlyAverageAmount);
        tvReportMonthlyAverage = (TextView) rootView.findViewById(R.id.tvReportMonthlyAverage);
        tvReportMonthlyTotalAmount = (TextView) rootView.findViewById(R.id.tvReportMonthlyTotalAmount);
        tvReportMonthlyTotal = (TextView) rootView.findViewById(R.id.tvReportMonthlyTotal);
        tvReportMonthlyTop = (TextView) rootView.findViewById(R.id.tvReportMonthlyTop);
        llIncomeButton = (RelativeLayout) rootView.findViewById(R.id.llIncomeButton);
        llIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMode(PocketAccounterGeneral.INCOME);
            }
        });
        llExpenseButton = (RelativeLayout) rootView.findViewById(R.id.llExpenseButton);
        llExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMode(PocketAccounterGeneral.EXPENSE);
            }
        });
        ivReportExpense = (ImageView) rootView.findViewById(R.id.ivReportExpense);
        ivReportIncome = (ImageView) rootView.findViewById(R.id.ivReportIncome);
        tvReportIncome = (TextView) rootView.findViewById(R.id.tvReportIncome);
        tvReportExpense = (TextView) rootView.findViewById(R.id.tvReportExpense);
        lchwavReportMonthly = (LinearChartWithAverageView) rootView.findViewById(R.id.lchwavReportMonthly);
        rsywmvReportByIncomeExpenseMonthly = (ReportSelectingYearWithMonthsView) rootView.findViewById(R.id.rsywmvReportByIncomeExpenseMonthly);
        rsywmvReportByIncomeExpenseMonthly.setListener(new ReportSelectingYearWithMonthsView.SelectingYearWithMonthsListener() {
            @Override
            public void OnSelectingYearWithMonths(int month, int year) {
                Log.d("sss", "month: " + month + " year: " + year);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                tvReportMonthlyTop.setText(format.format(calendar.getTime()));
                resetAll();
                ReportByIncomeExpenseMonthlyFragment.this.year = year;
                ReportByIncomeExpenseMonthlyFragment.this.month = month;
                switch (month) {
                    case Calendar.JANUARY:
                        tvJanuary.setTextColor(activeColor);
                        break;
                    case Calendar.FEBRUARY:
                        tvFebruary.setTextColor(activeColor);
                        break;
                    case Calendar.MARCH:
                        tvMarch.setTextColor(activeColor);
                        break;
                    case Calendar.APRIL:
                        tvApril.setTextColor(activeColor);
                        break;
                    case Calendar.MAY:
                        tvMay.setTextColor(activeColor);
                        break;
                    case Calendar.JUNE:
                        tvJune.setTextColor(activeColor);
                        break;
                    case Calendar.JULY:
                        tvJuly.setTextColor(activeColor);
                        break;
                    case Calendar.AUGUST:
                        tvAugust.setTextColor(activeColor);
                        break;
                    case Calendar.SEPTEMBER:
                        tvSeptember.setTextColor(activeColor);
                        break;
                    case Calendar.OCTOBER:
                        tvOctober.setTextColor(activeColor);
                        break;
                    case Calendar.NOVEMBER:
                        tvNovember.setTextColor(activeColor);
                        break;
                    case Calendar.DECEMBER:
                        tvDecember.setTextColor(activeColor);
                        break;
                }
                updateDatas(mode);
            }
        });
        setMode(mode);
        return rootView;
    }

    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setTitle(getResources().getString(R.string.monthly_state_report));
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
        }
    }

    private void setMode(int mode) {
        switch (mode) {
            case PocketAccounterGeneral.INCOME:
                tvReportIncome.setTextColor(activeColor);
                tvReportExpense.setTextColor(notActiveColor);
                ivReportIncome.setRotation(180.0f);
                ivReportExpense.setRotation(0.0f);
                llIncomeButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
                llExpenseButton.setBackgroundColor(Color.parseColor("#00000000"));
                break;
            case PocketAccounterGeneral.EXPENSE:
                tvReportIncome.setTextColor(notActiveColor);
                tvReportExpense.setTextColor(activeColor);
                ivReportIncome.setRotation(0.0f);
                ivReportExpense.setRotation(180.0f);
                llExpenseButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
                llIncomeButton.setBackgroundColor(Color.parseColor("#00000000"));
                break;
        }
        rsywmvReportByIncomeExpenseMonthly.setMode(mode);
        updateDatas(mode);
        this.mode = mode;
    }

    private void updateDatas(int mode) {
        Calendar begin = Calendar.getInstance();
        begin.set(Calendar.YEAR, year);
        begin.set(Calendar.MONTH, month);
        begin.set(Calendar.DAY_OF_MONTH, 1);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) begin.clone();
        int maxDaysCount = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
        end.set(Calendar.DAY_OF_MONTH, maxDaysCount);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        List<ReportObject> objects = reportManager.getReportObjects(true, begin, end, FinanceRecord.class,
                                                                                        DebtBorrow.class,
                                                                                        CreditDetials.class,
                                                                                        SmsParseSuccess.class);
        switch (mode) {
            case PocketAccounterGeneral.INCOME:
                Calendar temp = (Calendar) begin.clone();
                Calendar b = Calendar.getInstance();
                Calendar e = Calendar.getInstance();
                List<Double> datas = new ArrayList<>();
                while (temp.compareTo(end) <= 0) {
                    b.set(Calendar.YEAR, temp.get(Calendar.YEAR));
                    b.set(Calendar.MONTH, temp.get(Calendar.MONTH));
                    b.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
                    b.set(Calendar.HOUR_OF_DAY, 0);
                    b.set(Calendar.MINUTE, 0);
                    b.set(Calendar.SECOND, 0);
                    b.set(Calendar.MILLISECOND, 0);
                    e.set(Calendar.YEAR, temp.get(Calendar.YEAR));
                    e.set(Calendar.MONTH, temp.get(Calendar.MONTH));
                    e.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
                    e.set(Calendar.HOUR_OF_DAY, 23);
                    e.set(Calendar.MINUTE, 59);
                    e.set(Calendar.SECOND, 59);
                    e.set(Calendar.MILLISECOND, 59);
                    double dayAmount = 0.0d;
                    for (ReportObject object : objects) {
                        if (object.getType() == PocketAccounterGeneral.INCOME &&
                                object.getDate().compareTo(b) >= 0 &&
                                object.getDate().compareTo(e) <= 0) {
                            dayAmount += object.getAmount();
                        }
                    }
                    datas.add(dayAmount);
                    temp.add(Calendar.DAY_OF_MONTH, 1);
                }
                lchwavReportMonthly.setData(datas);
                lchwavReportMonthly.setLineThickness(getResources().getDimension(R.dimen.four_dp));
                lchwavReportMonthly.setLineColor(Color.parseColor("#99C71A"));
                lchwavReportMonthly.setAverageLineColor(Color.parseColor("#EE8B2C"));
                lchwavReportMonthly.setAverageLineThickness(getResources().getDimension(R.dimen.two_dp));
                lchwavReportMonthly.invalidate();
                double totalIncome = 0.0d;
                List<ReportObject> onlyIncomes = new ArrayList<>();
                for (ReportObject object : objects) {
                    if (object.getType() == PocketAccounterGeneral.INCOME) {
                        totalIncome += object.getAmount();
                        onlyIncomes.add(object);
                    }

                }
                double average = totalIncome/(double) maxDaysCount;
                double maxIncome = 0.0d, minIncome = Double.MAX_VALUE;
                if (!onlyIncomes.isEmpty()) {
                    for (ReportObject object : onlyIncomes) {
                        maxIncome = maxIncome < object.getAmount() ? object.getAmount() : maxIncome;
                        minIncome = minIncome > object.getAmount() ? object.getAmount() : minIncome;
                    }
                } else {
                    maxIncome = 0.0d;
                    minIncome = 0.0d;
                }
                tvReportMonthlyTotal.setText(getResources().getString(R.string.report_income_expanse_total_income));
                tvReportMonthlyTotalAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                DecimalFormat format = new DecimalFormat("0.##");
                tvReportMonthlyTotalAmount.setText(formetter.format(totalIncome) + commonOperations.getMainCurrency().getAbbr());
                tvReportMonthlyAverage.setText(R.string.average_income);
                tvReportMonthlyAverageAmount.setText(formetter.format(average) + commonOperations.getMainCurrency().getAbbr());
                tvReportMaximumTotal.setText(R.string.max_income_in_the_month);
                tvReportMonthlyMaximumAmount.setText(formetter.format(maxIncome) + commonOperations.getMainCurrency().getAbbr());
                tvReportMinimum.setText(R.string.min_income_in_the_month);
                tvReportMonthlyMiniumAmount.setText(formetter.format(minIncome) + commonOperations.getMainCurrency().getAbbr());
                break;
            case PocketAccounterGeneral.EXPENSE:
                temp = (Calendar) begin.clone();
                datas = new ArrayList<>();
                b = (Calendar) temp.clone();
                e = (Calendar) temp.clone();
                while (temp.compareTo(end) <= 0) {
                    b.set(Calendar.YEAR, temp.get(Calendar.YEAR));
                    b.set(Calendar.MONTH, temp.get(Calendar.MONTH));
                    b.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
                    b.set(Calendar.HOUR_OF_DAY, 0);
                    b.set(Calendar.MINUTE, 0);
                    b.set(Calendar.SECOND, 0);
                    b.set(Calendar.MILLISECOND, 0);
                    e.set(Calendar.YEAR, temp.get(Calendar.YEAR));
                    e.set(Calendar.MONTH, temp.get(Calendar.MONTH));
                    e.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
                    e.set(Calendar.HOUR_OF_DAY, 23);
                    e.set(Calendar.MINUTE, 59);
                    e.set(Calendar.SECOND, 59);
                    e.set(Calendar.MILLISECOND, 59);

                    double dayAmount = 0.0d;
                    for (ReportObject object : objects) {
                        if (object.getType() == PocketAccounterGeneral.EXPENSE &&
                                object.getDate().compareTo(b) >= 0 &&
                                object.getDate().compareTo(e) <= 0) {
                            dayAmount += object.getAmount();
                        }
                    }
                    datas.add(dayAmount);
                    temp.add(Calendar.DAY_OF_MONTH, 1);
                }
                lchwavReportMonthly.setData(datas);
                lchwavReportMonthly.setLineThickness(getResources().getDimension(R.dimen.four_dp));
                lchwavReportMonthly.setLineColor(Color.parseColor("#99C71A"));
                lchwavReportMonthly.setAverageLineColor(Color.parseColor("#EE8B2C"));
                lchwavReportMonthly.setAverageLineThickness(getResources().getDimension(R.dimen.two_dp));
                lchwavReportMonthly.invalidate();
                double totalExpense = 0.0d;
                List<ReportObject> onlyExpenses = new ArrayList<>();
                for (ReportObject object : objects) {
                    if (object.getType() == PocketAccounterGeneral.EXPENSE) {
                        totalExpense += object.getAmount();
                        onlyExpenses.add(object);
                    }

                }
                double averageExpense = totalExpense/(double) maxDaysCount;
                double maxExpense = 0.0d, minExpense = Double.MAX_VALUE;
                if (!onlyExpenses.isEmpty()) {
                    for (ReportObject object : onlyExpenses) {
                        maxExpense = maxExpense < object.getAmount() ? object.getAmount() : maxExpense;
                        minExpense = minExpense > object.getAmount() ? object.getAmount() : minExpense;
                    }
                } else {
                    maxExpense = 0.0d;
                    minExpense = 0.0d;
                }
                tvReportMonthlyTotal.setText(getResources().getString(R.string.report_income_expanse_total_expanse));
                tvReportMonthlyTotalAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                format = new DecimalFormat("0.##");
                tvReportMonthlyTotalAmount.setText(formetter.format(totalExpense) + commonOperations.getMainCurrency().getAbbr());
                tvReportMonthlyAverage.setText(R.string.average_expense);
                tvReportMonthlyAverageAmount.setText(formetter.format(averageExpense) + commonOperations.getMainCurrency().getAbbr());
                tvReportMaximumTotal.setText(R.string.max_expense_in_the_month);
                tvReportMonthlyMaximumAmount.setText(formetter.format(maxExpense) + commonOperations.getMainCurrency().getAbbr());
                tvReportMinimum.setText(R.string.min_expense_in_the_month);
                tvReportMonthlyMiniumAmount.setText(formetter.format(minExpense) + commonOperations.getMainCurrency().getAbbr());
                break;
        }
    }

    private void resetAll() {
        tvJanuary.setTextColor(notActiveColor);
        tvFebruary.setTextColor(notActiveColor);
        tvMarch.setTextColor(notActiveColor);
        tvApril.setTextColor(notActiveColor);
        tvMay.setTextColor(notActiveColor);
        tvJune.setTextColor(notActiveColor);
        tvJuly.setTextColor(notActiveColor);
        tvAugust.setTextColor(notActiveColor);
        tvSeptember.setTextColor(notActiveColor);
        tvOctober.setTextColor(notActiveColor);
        tvNovember.setTextColor(notActiveColor);
        tvDecember.setTextColor(notActiveColor);
    }
}
