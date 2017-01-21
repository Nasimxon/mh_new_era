package com.jim.finansia.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.reportviews.MonthPickSliderView;
import com.jim.finansia.utils.reportviews.ReportByIncomeExpenseOneDayView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class ReportByIncomeExpenseDaily extends Fragment {
    private MonthPickSliderView mpReportByIncomeExpense;
    private RecyclerView rvReportByIncomeExpenseDays, rvReportByIncomeExpenseDetail;
    private Calendar begin, end;
    private DaysAdapter daysAdapter;
    private final int INCOME = 0, EXPENSE = 1, BALANCE = 2;
    private int mode = EXPENSE;
    private int month, year;
    private int activeColor = Color.parseColor("#414141"), notActiveColor = Color.parseColor("#9c9c9c");
    private ImageView ivReportExpense, ivReportIncome;
    private RelativeLayout expenseButton,incomeButton;
    private List<DayData> adapterList;
    private TextView tvReportIncome, tvReportExpense;
    private Calendar today = Calendar.getInstance();
    private double maxIncome, minIncome, maxExpense, minExpense, maxBalance, minBalance;
    @Inject DaoSession daoSession;
    @Inject ReportManager reportManager;
    @Inject ToolbarManager toolbarManager;
    @Inject DecimalFormat formatter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.report_by_income_and_expense_daily, container, false);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        toolbarManager.setOnTitleClickListener(null);
        ivReportExpense = (ImageView) rootView.findViewById(R.id.ivReportExpense);
        ivReportIncome = (ImageView) rootView.findViewById(R.id.ivReportIncome);

        expenseButton = (RelativeLayout) rootView.findViewById(R.id.expenseButton);
        incomeButton = (RelativeLayout) rootView.findViewById(R.id.incomeButton);
        tvReportIncome = (TextView) rootView.findViewById(R.id.tvReportIncome);
        tvReportExpense = (TextView) rootView.findViewById(R.id.tvReportExpense);
        rvReportByIncomeExpenseDays = (RecyclerView) rootView.findViewById(R.id.rvReportByIncomeExpenseDays);
        rvReportByIncomeExpenseDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvReportByIncomeExpenseDetail = (RecyclerView) rootView.findViewById(R.id.rvReportByIncomeExpenseDetail);
        rvReportByIncomeExpenseDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        mpReportByIncomeExpense = (MonthPickSliderView) rootView.findViewById(R.id.mpReportByIncomeExpense);

        mpReportByIncomeExpense.setCurrentItem(mpReportByIncomeExpense.getItemsSize() - 1);
        mpReportByIncomeExpense.setListener(new MonthPickSliderView.MonthDetailedByDaysSelectedListener() {
            @Override
            public void onMonthSelected(int month, int year) {
                ReportByIncomeExpenseDaily.this.month = month;
                ReportByIncomeExpenseDaily.this.year = year;
                adapterList = generateDatas(month, year);
                daysAdapter = new DaysAdapter(adapterList);
                rvReportByIncomeExpenseDays.setAdapter(daysAdapter);
            }
        });
        Calendar cal = Calendar.getInstance();
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        adapterList = generateDatas(month, year);
        daysAdapter = new DaysAdapter(adapterList);
        rvReportByIncomeExpenseDays.setAdapter(daysAdapter);

        int scrollPosition = 0;
        for (DayData dayData : adapterList) {
            if (dayData.isSelected()) {
                scrollPosition = dayData.getDay() - 1;
                break;
            }
        }
        generateDataForDetailList(scrollPosition + 1);
        if (scrollPosition - 2 >= 0) scrollPosition -= 2;
        rvReportByIncomeExpenseDays.scrollToPosition(scrollPosition);
        tvReportIncome.setTextColor(notActiveColor);
        tvReportExpense.setTextColor(activeColor);
        ivReportIncome.setRotation(0.0f);
        ivReportExpense.setRotation(180.0f);
        expenseButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
        incomeButton.setBackgroundColor(Color.parseColor("#00000000"));
        mode = EXPENSE;
        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvReportIncome.setTextColor(notActiveColor);
                tvReportExpense.setTextColor(activeColor);
                ivReportIncome.setRotation(0.0f);
                ivReportExpense.setRotation(180.0f);
                expenseButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
                incomeButton.setBackgroundColor(Color.parseColor("#00000000"));
                mode = EXPENSE;
                int position = 0;
                if (adapterList != null && !adapterList.isEmpty()) {
                    for (DayData dayData : adapterList) {
                        if (dayData.isSelected()) {
                            position = dayData.getDay() - 1;
                            break;
                        }
                    }
                    generateDataForDetailList(position + 1);
                }
                daysAdapter.notifyDataSetChanged();
            }
        });
        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvReportIncome.setTextColor(activeColor);
                tvReportExpense.setTextColor(notActiveColor);
                ivReportIncome.setRotation(180.0f);
                ivReportExpense.setRotation(0.0f);
                incomeButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
                expenseButton.setBackgroundColor(Color.parseColor("#00000000"));
                mode = INCOME;
                int position = 0;
                if (adapterList != null && !adapterList.isEmpty()) {
                    for (DayData dayData : adapterList) {
                        if (dayData.isSelected()) {
                            position = dayData.getDay() - 1;
                            break;
                        }
                    }
                    generateDataForDetailList(position + 1);
                }
                daysAdapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }

    private List<DayData> generateDatas(int month, int year) {
        List<DayData> result = new ArrayList<>();
        begin = Calendar.getInstance();
        begin.set(Calendar.YEAR, year);
        begin.set(Calendar.MONTH, month);
        begin.set(Calendar.DAY_OF_MONTH, 1);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        begin.add(Calendar.DAY_OF_MONTH, -1);

        end = Calendar.getInstance();
        end.set(Calendar.YEAR, year);
        end.set(Calendar.MONTH, month);
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        List<ReportObject> objects = reportManager.getReportObjects(true, begin, end,
                FinanceRecord.class,
                CreditDetials.class,
                DebtBorrow.class,
                SmsParseSuccess.class
        );
        int beforeMonthLastDay = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
        int beforeMonth = begin.get(Calendar.MONTH);
        int maxDaysInMonth = end.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar temp = Calendar.getInstance();
        temp.set(Calendar.YEAR, year);
        temp.set(Calendar.MONTH, month);
        for (int i = 1; i <= maxDaysInMonth; i++) {
            double leftIncome = 0.0d, leftExpense = 0.0d, leftProfit = 0.0d,
                    rightIncome = 0.0d, rightExpense = 0.0d, rightProfit = 0.0d;
            int lastDay, lastMonth, currentDay = i, currentMonth = end.get(Calendar.MONTH);
            if (i == 1) {
                lastDay = beforeMonthLastDay;
                lastMonth = beforeMonth;
            }
            else {
                lastDay = i - 1;
                lastMonth = end.get(Calendar.MONTH);
            }
            for (ReportObject object : objects) {
                if (object.getDate().get(Calendar.DAY_OF_MONTH) == lastDay &&
                        object.getDate().get(Calendar.MONTH) == lastMonth) {
                    if (object.getType() == PocketAccounterGeneral.INCOME)
                        leftIncome += object.getAmount();
                    else if (object.getType() == PocketAccounterGeneral.EXPENSE)
                        leftExpense += object.getAmount();
                }
                if (object.getDate().get(Calendar.DAY_OF_MONTH) == currentDay &&
                        object.getDate().get(Calendar.MONTH) == currentMonth) {
                    if (object.getType() == PocketAccounterGeneral.INCOME)
                        rightIncome += object.getAmount();
                    else if (object.getType() == PocketAccounterGeneral.EXPENSE)
                        rightExpense += object.getAmount();
                }
            }
            leftProfit = leftIncome - leftExpense;
            rightProfit = rightIncome - rightExpense;
            temp.set(Calendar.DAY_OF_MONTH, currentDay);
            DayData dayData = new DayData();
            dayData.setDay(currentDay);
            dayData.setSelected(today.get(Calendar.YEAR) == year &&
                    today.get(Calendar.MONTH) == month
                    && i == today.get(Calendar.DAY_OF_MONTH));
            dayData.setDayOfWeek(temp.get(Calendar.DAY_OF_WEEK));
            dayData.setLeftIncome(leftIncome);
            dayData.setLeftExpense(leftExpense);
            dayData.setLeftProfit(leftProfit);
            dayData.setRightIncome(rightIncome);
            dayData.setRightExpense(rightExpense);
            dayData.setRightProfit(rightProfit);
            double incomePercent = 0.0d;

            if (leftIncome != 0.0d || rightIncome != 0.0d) {
                if (leftIncome == 0.0d || rightIncome != 0.0d) { incomePercent = 100.0d; }
                if (leftIncome != 0.0d || rightIncome == 0.0d) { incomePercent = -100.0d; }
                if (leftIncome != 0.0d && rightIncome != 0.0d) {
                    if (leftIncome > rightIncome) { incomePercent = 100.0d * (rightIncome/leftIncome - 1); }
                    if (leftIncome < rightIncome) { incomePercent = 100.0d * (1 - leftIncome/rightIncome); }
                    if (leftIncome == rightIncome) { incomePercent = 0.0d; }
                }
            }
            dayData.setIncomePercent(incomePercent);
            double expensePercent = 0.0d;
            if (leftExpense != 0.0d || rightExpense != 0.0d) {
                if (leftExpense == 0.0d || rightExpense != 0.0d) { expensePercent = 100.0d; }
                if (leftExpense != 0.0d || rightExpense == 0.0d) { expensePercent = -100.0d; }
                if (leftExpense != 0.0d && rightExpense != 0.0d) {
                    if (leftExpense > rightExpense) { expensePercent = 100.0d * (rightExpense/leftExpense - 1); }
                    if (leftExpense < rightExpense) { expensePercent = 100.0d * (1 - leftExpense/rightExpense); }
                    if (leftExpense == rightExpense) { expensePercent = 0.0d; }
                }
            }
            dayData.setExpensePercent(expensePercent);
            double profitPercent = 0.0d;
            if (leftProfit != 0.0d || rightProfit != 0.0d) {
                if (leftProfit == 0.0d || rightProfit != 0.0d) { profitPercent = 100.0d; }
                if (leftProfit != 0.0d || rightProfit == 0.0d) { profitPercent = -100.0d; }
                if (leftProfit != 0.0d && rightProfit != 0.0d) {
                    if (leftProfit > rightProfit) { profitPercent = 100.0d * (rightProfit/leftProfit - 1); }
                    if (leftProfit < rightProfit) { profitPercent = 100.0d * (1 - leftProfit/rightProfit); }
                    if (leftProfit == rightProfit) { profitPercent = 0.0d; }
                }
            }
            dayData.setProfitPercent(profitPercent);
            result.add(dayData);
        }
        minIncome = 0.0d;
        maxIncome = 0.0d;
        minExpense = 0.0d;
        maxExpense = 0.0d;
        minBalance = 0.0d;
        maxBalance = 0.0d;
        for (DayData dayData : result) {
            minIncome = dayData.getLeftIncome() <= minIncome ? dayData.getLeftIncome() : minIncome;
            minIncome = dayData.getRightIncome() <= minIncome ? dayData.getRightIncome() : minIncome;
            maxIncome = dayData.getLeftIncome() >= maxIncome ? dayData.getLeftIncome() : maxIncome;
            maxIncome = dayData.getRightIncome() >= maxIncome ? dayData.getRightIncome() : maxIncome;

            minExpense = dayData.getLeftExpense() <= minExpense ? dayData.getLeftExpense() : minExpense;
            minExpense = dayData.getRightExpense() <= minExpense ? dayData.getRightExpense() : minExpense;
            maxExpense = dayData.getLeftExpense() >= maxExpense ? dayData.getLeftExpense() : maxExpense;
            maxExpense = dayData.getRightExpense() >= maxExpense ? dayData.getRightExpense() : maxExpense;

            minBalance = dayData.getLeftProfit() <= minBalance ? dayData.getLeftProfit() : minBalance;
            minBalance = dayData.getRightProfit() <= minBalance ? dayData.getRightProfit() : minBalance;
            maxBalance = dayData.getLeftProfit() >= maxBalance ? dayData.getLeftProfit() : maxBalance;
            maxBalance = dayData.getRightProfit() >= maxBalance ? dayData.getRightProfit() : maxBalance;
        }
        return result;
    }

    private void generateDataForDetailList(int day) {
        Calendar b = Calendar.getInstance();
        b.set(Calendar.YEAR, year);
        b.set(Calendar.MONTH, month);
        b.set(Calendar.DAY_OF_MONTH, day);
        b.set(Calendar.HOUR_OF_DAY, 0);
        b.set(Calendar.MINUTE, 0);
        b.set(Calendar.SECOND, 0);
        b.set(Calendar.MILLISECOND, 0);
        Calendar e = (Calendar) b.clone();
        e.set(Calendar.HOUR_OF_DAY, 23);
        e.set(Calendar.MINUTE, 59);
        e.set(Calendar.SECOND, 59);
        e.set(Calendar.MILLISECOND, 59);
        List<ReportObject> reportObjects = reportManager.getReportObjects(false, b, e, FinanceRecord.class, CreditDetials.class, DebtBorrow.class, SmsParseSuccess.class);
        List<ReportObject> temp = new ArrayList<>();
        for (ReportObject reportObject : reportObjects) {
            if (reportObject.getType() == mode) {
                temp.add(reportObject);
            }
        }
        rvReportByIncomeExpenseDetail.setAdapter(new DetailAdapter(temp));
     }

    private class DetailAdapter extends RecyclerView.Adapter<ReportByIncomeExpenseDaily.DetailViewHolder> {
        private List<ReportObject> result;
        public DetailAdapter(List<ReportObject> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByIncomeExpenseDaily.DetailViewHolder view, final int position) {
            String sign = "";
            DecimalFormat decimalFormat = new DecimalFormat("0.##");;
            switch (mode) {
                case PocketAccounterGeneral.INCOME:
                    if (result.get(position).getType() == PocketAccounterGeneral.INCOME) {
                        view.tvReportIncomeExpenseDetailName.setText(result.get(position).getDescription());
                        view.tvReportIncomeExpenseDetailAmount.setTextColor(Color.parseColor("#8cc156"));
                        sign = "+";
                        view.tvReportIncomeExpenseDetailAmount.setText(sign + decimalFormat.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr());
                    }
                    break;
                case PocketAccounterGeneral.EXPENSE:
                    if (result.get(position).getType() == PocketAccounterGeneral.EXPENSE){
                        view.tvReportIncomeExpenseDetailName.setText(result.get(position).getDescription());
                        view.tvReportIncomeExpenseDetailAmount.setTextColor(Color.parseColor("#dc4849"));
                        sign = "-";
                        view.tvReportIncomeExpenseDetailAmount.setText(sign + decimalFormat.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr());
                    }
                    break;
            }

        }
        public ReportByIncomeExpenseDaily.DetailViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_income_expense_daily_detail_list_item, parent, false);
            return new ReportByIncomeExpenseDaily.DetailViewHolder(view);
        }
    }

    public class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportIncomeExpenseDetailName, tvReportIncomeExpenseDetailAmount;
        LinearLayout forLine;
        public DetailViewHolder(View view) {
            super(view);
            tvReportIncomeExpenseDetailName = (TextView) view.findViewById(R.id.tvReportIncomeExpenseDetailName);
            tvReportIncomeExpenseDetailAmount = (TextView) view.findViewById(R.id.tvReportIncomeExpenseDetailAmount);
            forLine = (LinearLayout) view.findViewById(R.id.forLine);
        }
    }

    private class DaysAdapter extends RecyclerView.Adapter<ReportByIncomeExpenseDaily.ViewHolder> {
        private List<DayData> result;
        boolean keyForAnim =true;
        int typeChange = 0;
        public DaysAdapter(List<DayData> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByIncomeExpenseDaily.ViewHolder view, final int position) {
            view.rbieodvReportByIncomeExpenseDayItem.setDay(result.get(position).getDay());
            view.rbieodvReportByIncomeExpenseDayItem.setDayOfWeek(result.get(position).getDayOfWeek());
            switch (mode) {
                case EXPENSE:
                    if(typeChange==1){
                        keyForAnim = false;
                    }else {
                        keyForAnim = true;
                    }
                    typeChange = 1;
                    view.rbieodvReportByIncomeExpenseDayItem.setMinValue(minExpense);
                    view.rbieodvReportByIncomeExpenseDayItem.setMaxValue(maxExpense);
                    view.rbieodvReportByIncomeExpenseDayItem.setLeftValue(result.get(position).getLeftExpense());
                    view.rbieodvReportByIncomeExpenseDayItem.setRightValue(result.get(position).getRightExpense());
                    view.rbieodvReportByIncomeExpenseDayItem.setIncreasePercent((int) result.get(position).getExpensePercent());
                    view.rbieodvReportByIncomeExpenseDayItem.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.diagram_red_mutniy));
                    view.rbieodvReportByIncomeExpenseDayItem.setTrapezeColor(ContextCompat.getColor(getContext(),R.color.diagram_red));
                    break;
                case INCOME:
                    if(typeChange==2){
                        keyForAnim = false;
                    }else {
                        keyForAnim = true;
                    }
                    typeChange = 2;
                    view.rbieodvReportByIncomeExpenseDayItem.setMinValue(minIncome);
                    view.rbieodvReportByIncomeExpenseDayItem.setMaxValue(maxIncome);
                    view.rbieodvReportByIncomeExpenseDayItem.setLeftValue(result.get(position).getLeftIncome());
                    view.rbieodvReportByIncomeExpenseDayItem.setRightValue(result.get(position).getRightIncome());
                    view.rbieodvReportByIncomeExpenseDayItem.setIncreasePercent((int) result.get(position).getIncomePercent());
                    view.rbieodvReportByIncomeExpenseDayItem.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.diagram_green_mutniy));
                    view.rbieodvReportByIncomeExpenseDayItem.setTrapezeColor(ContextCompat.getColor(getContext(),R.color.diagram_green));
                    break;
                case BALANCE:
                    if(typeChange==3){
                        keyForAnim = false;
                    }else {
                        keyForAnim = true;
                    }
                    typeChange = 3;
                    view.rbieodvReportByIncomeExpenseDayItem.setMinValue(minBalance);
                    view.rbieodvReportByIncomeExpenseDayItem.setMaxValue(maxBalance);
                    view.rbieodvReportByIncomeExpenseDayItem.setLeftValue(result.get(position).getLeftProfit());
                    view.rbieodvReportByIncomeExpenseDayItem.setRightValue(result.get(position).getRightProfit());
                    view.rbieodvReportByIncomeExpenseDayItem.setIncreasePercent((int) result.get(position).getProfitPercent());
                    view.rbieodvReportByIncomeExpenseDayItem.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.diagram_yellow_mutniy));
                    view.rbieodvReportByIncomeExpenseDayItem.setTrapezeColor(ContextCompat.getColor(getContext(),R.color.diagram_yellow));
                    break;
            }

            if (result.get(position).isSelected())
                view.ivDailySelected.setVisibility(View.VISIBLE);
            else
                view.ivDailySelected.setVisibility(View.GONE);
            view.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < result.size(); i++)
                        result.get(i).setSelected(i == position);
                    keyForAnim =false;
                    generateDataForDetailList(position + 1);
                    notifyDataSetChanged();
                }
            });
            if (keyForAnim)
            view.rbieodvReportByIncomeExpenseDayItem.animateDayView();
        }
        public ReportByIncomeExpenseDaily.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_income_expense_days_list_item, parent, false);
            return new ReportByIncomeExpenseDaily.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ReportByIncomeExpenseOneDayView rbieodvReportByIncomeExpenseDayItem;
        ImageView ivDailySelected;
        View view;
        public ViewHolder(View view) {
            super(view);
            rbieodvReportByIncomeExpenseDayItem = (ReportByIncomeExpenseOneDayView) view.findViewById(R.id.rbieodvReportByIncomeExpenseDayItem);
            ivDailySelected = (ImageView) view.findViewById(R.id.ivDailySelected);
            this.view = view;
        }
    }

    class DayData {
        private boolean selected = false;
        private int day = 1, dayOfWeek = Calendar.MONDAY;
        private double leftIncome = 0.0d, leftExpense = 0.0d, leftProfit = 0.0d,
                        rightIncome = 0.0d, rightExpense = 0.0d, rightProfit = 0.0d;
        private double maxValue = 0.0d, minValue = 0.0;
        private double incomePercent = 0.0d, expensePercent = 0.0d, profitPercent = 0.0d;
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        public int getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public double getLeftIncome() { return leftIncome; }
        public void setLeftIncome(double leftIncome) { this.leftIncome = leftIncome; }
        public double getLeftExpense() { return leftExpense; }
        public void setLeftExpense(double leftExpense) { this.leftExpense = leftExpense; }
        public double getLeftProfit() { return leftProfit; }
        public void setLeftProfit(double leftProfit) { this.leftProfit = leftProfit; }
        public double getRightIncome() { return rightIncome; }
        public void setRightIncome(double rightIncome) { this.rightIncome = rightIncome; }
        public double getRightExpense() { return rightExpense; }
        public void setRightExpense(double rightExpense) { this.rightExpense = rightExpense; }
        public double getRightProfit() { return rightProfit; }
        public void setRightProfit(double rightProfit) { this.rightProfit = rightProfit; }
        public double getMaxValue() { return maxValue; }
        public void setMaxValue(double maxValue) { this.maxValue = maxValue; }
        public double getMinValue() { return minValue; }
        public void setMinValue(double minValue) { this.minValue = minValue; }
        public double getIncomePercent() { return incomePercent; }
        public void setIncomePercent(double incomePercent) { this.incomePercent = incomePercent; }
        public double getExpensePercent() { return expensePercent; }
        public void setExpensePercent(double expensePercent) { this.expensePercent = expensePercent; }
        public double getProfitPercent() { return profitPercent; }
        public void setProfitPercent(double profitPercent) { this.profitPercent = profitPercent; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
}
