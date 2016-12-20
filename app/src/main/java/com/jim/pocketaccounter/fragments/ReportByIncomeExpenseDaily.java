package com.jim.pocketaccounter.fragments;

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
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.CircleImageView;
import com.jim.pocketaccounter.utils.GetterAttributColors;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.reportviews.MonthPickSliderView;
import com.jim.pocketaccounter.utils.reportviews.ReportByIncomeExpenseOneDayView;

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
    private CircleImageView ivDailyIncomeRect, ivDailyExpenseRect, ivDailyProfitRect;

    private List<DayData> adapterList;
    @Inject DaoSession daoSession;
    @Inject ReportManager reportManager;
    @Inject ToolbarManager toolbarManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.report_by_income_and_expense_daily, container, false);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        toolbarManager.setSpinnerVisibility(View.GONE);
        rvReportByIncomeExpenseDays = (RecyclerView) rootView.findViewById(R.id.rvReportByIncomeExpenseDays);
        rvReportByIncomeExpenseDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvReportByIncomeExpenseDetail = (RecyclerView) rootView.findViewById(R.id.rvReportByIncomeExpenseDetail);
        rvReportByIncomeExpenseDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        mpReportByIncomeExpense = (MonthPickSliderView) rootView.findViewById(R.id.mpReportByIncomeExpense);
        ivDailyIncomeRect = (CircleImageView) rootView.findViewById(R.id.ivDailyIncomeRect);
        ivDailyIncomeRect.setImageResource(R.color.diagram_green);
        ivDailyExpenseRect = (CircleImageView) rootView.findViewById(R.id.ivDailyExpenseRect);
        ivDailyExpenseRect.setImageResource(R.color.diagram_red);
        ivDailyProfitRect = (CircleImageView) rootView.findViewById(R.id.ivDailyProfitRect);
        ivDailyProfitRect.setImageResource(R.color.diagram_yellow);
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
                DebtBorrow.class
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
            dayData.setSelected(i == 2);
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
        double minValue = 0.0d, maxValue = 0.0;
        for (DayData dayData : result) {
            if (dayData.getLeftExpense() <= minValue) minValue = dayData.getLeftExpense();
            if (dayData.getLeftExpense() >= maxValue) maxValue = dayData.getLeftExpense();
            if (dayData.getLeftIncome() <= minValue) minValue = dayData.getLeftIncome();
            if (dayData.getLeftIncome() >= maxValue) maxValue = dayData.getLeftIncome();
            if (dayData.getLeftProfit() <= minValue) minValue = dayData.getLeftProfit();
            if (dayData.getLeftProfit() >= maxValue) maxValue = dayData.getLeftProfit();
            if (dayData.getRightExpense() <= minValue) minValue = dayData.getRightExpense();
            if (dayData.getRightExpense() >= maxValue) maxValue = dayData.getRightExpense();
            if (dayData.getRightIncome() <= minValue) minValue = dayData.getRightIncome();
            if (dayData.getRightIncome() >= maxValue) maxValue = dayData.getRightIncome();
            if (dayData.getRightProfit() <= minValue) minValue = dayData.getRightProfit();
            if (dayData.getRightProfit() >= maxValue) maxValue = dayData.getRightProfit();
        }
        for (DayData dayData : result) {
            dayData.setMinValue(minValue);
            dayData.setMaxValue(maxValue);
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
        List<ReportObject> reportObjects = reportManager.getReportObjects(false, b, e, FinanceRecord.class, CreditDetials.class, DebtBorrow.class);
        rvReportByIncomeExpenseDetail.setAdapter(new DetailAdapter(reportObjects));
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
            view.tvReportIncomeExpenseDetailName.setText(result.get(position).getDescription());
            String sign = "";
            if (result.get(position).getType() == PocketAccounterGeneral.INCOME) {
                view.tvReportIncomeExpenseDetailAmount.setTextColor(Color.GREEN);
                sign = "+";
            }
            else {
                view.tvReportIncomeExpenseDetailAmount.setTextColor(Color.RED);
                sign = "-";
            }
            view.tvReportIncomeExpenseDetailAmount.setText(sign + result.get(position).getAmount() + result.get(position).getCurrency().getAbbr());
        }
        public ReportByIncomeExpenseDaily.DetailViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_income_expense_daily_detail_list_item, parent, false);
            return new ReportByIncomeExpenseDaily.DetailViewHolder(view);
        }
    }

    public class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportIncomeExpenseDetailName, tvReportIncomeExpenseDetailAmount;
        public DetailViewHolder(View view) {
            super(view);
            tvReportIncomeExpenseDetailName = (TextView) view.findViewById(R.id.tvReportIncomeExpenseDetailName);
            tvReportIncomeExpenseDetailAmount = (TextView) view.findViewById(R.id.tvReportIncomeExpenseDetailAmount);
        }
    }

    private class DaysAdapter extends RecyclerView.Adapter<ReportByIncomeExpenseDaily.ViewHolder> {
        private List<DayData> result;
        public DaysAdapter(List<DayData> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByIncomeExpenseDaily.ViewHolder view, final int position) {
            view.rbieodvReportByIncomeExpenseDayItem.setDay(result.get(position).getDay());
            view.rbieodvReportByIncomeExpenseDayItem.setMinValue(result.get(position).getMinValue());
            view.rbieodvReportByIncomeExpenseDayItem.setMaxValue(result.get(position).getMaxValue());
            view.rbieodvReportByIncomeExpenseDayItem.setDayOfWeek(result.get(position).getDayOfWeek());
            switch (mode) {
                case EXPENSE:
                    view.rbieodvReportByIncomeExpenseDayItem.setLeftValue(result.get(position).getLeftExpense());
                    view.rbieodvReportByIncomeExpenseDayItem.setRightValue(result.get(position).getRightExpense());
                    view.rbieodvReportByIncomeExpenseDayItem.setIncreasePercent((int) result.get(position).getExpensePercent());
                    view.rbieodvReportByIncomeExpenseDayItem.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.diagram_red_mutniy));
                    view.rbieodvReportByIncomeExpenseDayItem.setTrapezeColor(ContextCompat.getColor(getContext(),R.color.diagram_red));

                    break;
                case INCOME:
                    view.rbieodvReportByIncomeExpenseDayItem.setLeftValue(result.get(position).getLeftIncome());
                    view.rbieodvReportByIncomeExpenseDayItem.setRightValue(result.get(position).getRightIncome());
                    view.rbieodvReportByIncomeExpenseDayItem.setIncreasePercent((int) result.get(position).getIncomePercent());
                    view.rbieodvReportByIncomeExpenseDayItem.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.diagram_green_mutniy));
                    view.rbieodvReportByIncomeExpenseDayItem.setTrapezeColor(ContextCompat.getColor(getContext(),R.color.diagram_green));
                    break;
                case BALANCE:
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
                    generateDataForDetailList(position + 1);
                    notifyDataSetChanged();
                }
            });
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
