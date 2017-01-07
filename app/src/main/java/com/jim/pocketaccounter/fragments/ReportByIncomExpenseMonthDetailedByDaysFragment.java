package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.CircleImageView;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import org.apache.commons.lang3.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

@SuppressLint("ValidFragment")
public class ReportByIncomExpenseMonthDetailedByDaysFragment extends Fragment {
    private LineChartView lchvReportByIncomeExpenseMonthDetailedByDays;
    private int month, year;
    private Calendar calendar;
    private int pointsRadius = 0, lineThickness = 0;
    private static final int INCOME = 0, EXPENSE = 1, BALANCE = 2;
    private SimpleDateFormat format = new SimpleDateFormat("MMM, yyyy");
    private TextView tvReportByIncomeExpenseDate;
    private int incomeColor,
                expenseColor,
                profitColor;
    @Inject ReportManager reportManager;
    public ReportByIncomExpenseMonthDetailedByDaysFragment(int month, int year) {
        this.month = month;
        this.year = year;
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounterApplication) getContext().getApplicationContext()).component().inject(this);
        View rootView = inflater.inflate(R.layout.report_by_income_expense_month_detail_by_days_fragment, container, false);
        incomeColor = ContextCompat.getColor(getContext(),R.color.diagram_green);
        expenseColor = ContextCompat.getColor(getContext(),R.color.diagram_red);
        profitColor = ContextCompat.getColor(getContext(),R.color.diagram_yellow);
        pointsRadius = (int) getResources().getDimension(R.dimen.one_dp);
        lineThickness = (int) getResources().getDimension(R.dimen.one_dp);
        tvReportByIncomeExpenseDate = (TextView) rootView.findViewById(R.id.tvReportByIncomeExpenseDate);
        tvReportByIncomeExpenseDate.setText(format.format(calendar.getTime()));
        lchvReportByIncomeExpenseMonthDetailedByDays = (LineChartView) rootView.findViewById(R.id.lchvReportByIncomeExpenseMonthDetailedByDays);
        lchvReportByIncomeExpenseMonthDetailedByDays.setLineChartData(generateLineData());
        lchvReportByIncomeExpenseMonthDetailedByDays.setZoomEnabled(false);
        lchvReportByIncomeExpenseMonthDetailedByDays.setViewportCalculationEnabled(true);
        lchvReportByIncomeExpenseMonthDetailedByDays.setValueSelectionEnabled(true);

        setViewport();
        return rootView;
    }

    private void setViewport() {
        float min = 0.0f, max = 0.0f;
        LineChartData data = (LineChartData) lchvReportByIncomeExpenseMonthDetailedByDays.getChartData();
        List<Line> lines = data.getLines();
        for (int i = 0; i < lines.size(); i++) {
            List<PointValue> values = lines.get(i).getValues();
            for (int j = 0; j < values.size(); j++) {
                max = max < values.get(j).getY() ? values.get(j).getY() : max;
                min = min > values.get(j).getY() ? values.get(j).getY() : min;
            }
        }
        final Viewport v = new Viewport(lchvReportByIncomeExpenseMonthDetailedByDays.getMaximumViewport());
        v.bottom = min - 20;
        v.top = max + 20;
        v.left = 4;

        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        v.right = maxDay + 1;
        // You have to set max and current viewports separately.
        lchvReportByIncomeExpenseMonthDetailedByDays.setMaximumViewport(v);
        // I changing current viewport with animation in this case.
        lchvReportByIncomeExpenseMonthDetailedByDays.setCurrentViewportWithAnimation(v);

    }

    private LineChartData generateLineData() {
        Calendar begin = (Calendar) calendar.clone();
        begin.set(Calendar.DAY_OF_MONTH, 1);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) calendar.clone();
        end.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        List<ReportObject> objects = reportManager.getReportObjects(true, begin, end, FinanceRecord.class, DebtBorrow.class, CreditDetials.class);

        List<Line> lines = new ArrayList<>();
        List<PointValue> incomeValues = new ArrayList<>();
        List<PointValue> expenseValues = new ArrayList<>();
        List<PointValue> balanceValues = new ArrayList<>();

        Calendar tempBegin = (Calendar) begin.clone();
        Calendar tempEnd = (Calendar) begin.clone();

        for (int i = 0; i < 3; ++i) {
            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            double income = 0.0d, expense = 0.0d, balance;
            for (int j = 1; j <= max; j++) {
                tempBegin.set(Calendar.DAY_OF_MONTH, j);
                tempBegin.set(Calendar.HOUR_OF_DAY, 0);
                tempBegin.set(Calendar.MINUTE, 0);
                tempBegin.set(Calendar.SECOND, 0);
                tempBegin.set(Calendar.MILLISECOND, 0);
                tempEnd.set(Calendar.DAY_OF_MONTH, j);
                tempEnd.set(Calendar.HOUR_OF_DAY, 23);
                tempEnd.set(Calendar.MINUTE, 59);
                tempEnd.set(Calendar.SECOND, 59);
                tempEnd.set(Calendar.MILLISECOND, 59);
                switch (i) {
                    case INCOME:
                        for (ReportObject object : objects) {
                            if (object.getType() == PocketAccounterGeneral.INCOME &&
                                    tempBegin.compareTo(object.getDate()) <= 0 &&
                                    tempEnd.compareTo(object.getDate()) >= 0) {
                                income += object.getAmount();
                            }
                        }
                        if (j%5 == 0 || j == max - 1) {
                            incomeValues.add(new PointValue(j, (float) income));
                            income = 0.0d;
                        }
                        break;
                    case EXPENSE:
                        for (ReportObject object : objects) {
                            if (object.getType() == PocketAccounterGeneral.EXPENSE &&
                                    tempBegin.compareTo(object.getDate()) <= 0 &&
                                    tempEnd.compareTo(object.getDate()) >= 0) {
                                expense += object.getAmount();
                            }
                        }
                        if (j%5 == 0 || j == max - 1) {
                            expenseValues.add(new PointValue(j, (float) expense));
                            expense = 0.0d;
                        }
                        break;
                    case  BALANCE:
                        for (ReportObject object : objects) {
                            if (object.getType() == PocketAccounterGeneral.INCOME &&
                                    tempBegin.compareTo(object.getDate()) <= 0 &&
                                    tempEnd.compareTo(object.getDate()) >= 0) {
                                income += object.getAmount();
                            }
                        }
                        for (ReportObject object : objects) {
                            if (object.getType() == PocketAccounterGeneral.EXPENSE &&
                                    tempBegin.compareTo(object.getDate()) <= 0 &&
                                    tempEnd.compareTo(object.getDate()) >= 0) {
                                expense += object.getAmount();
                            }
                        }
                        if (j%5 == 0 || j == max - 1) {
                            balance = income - expense;
                            balanceValues.add(new PointValue(j, (float) balance));
                            income = 0.0d;
                            expense = 0.0d;
                        }
                        break;

                }
            }


        }
        Line incomeLine = new Line(incomeValues);
        incomeLine.setColor(incomeColor);
        incomeLine.setCubic(true);
        incomeLine.setHasLabels(false);
        incomeLine.setHasLines(true);
        incomeLine.setHasPoints(false);
        incomeLine.setPointRadius(pointsRadius);
        incomeLine.setStrokeWidth(lineThickness);

        lines.add(incomeLine);
        Line expenseLine = new Line(expenseValues);
        expenseLine.setColor(expenseColor);
        expenseLine.setCubic(true);
        expenseLine.setHasLabels(false);
        expenseLine.setHasLines(true);
        expenseLine.setHasPoints(false);
        expenseLine.setPointRadius(pointsRadius);
        expenseLine.setStrokeWidth(lineThickness);
        lines.add(expenseLine);
//        Line balanceLine = new Line(balanceValues);
//        balanceLine.setColor(profitColor);
//        balanceLine.setCubic(true);
//        balanceLine.setHasLabels(false);
//        balanceLine.setHasLines(true);
//        balanceLine.setHasPoints(false);
//        balanceLine.setPointRadius(pointsRadius);
//        balanceLine.setStrokeWidth(lineThickness);
//
//        lines.add(balanceLine);
        LineChartData lineChartData = new LineChartData(lines);
        return lineChartData;
    }
    public int getMonth() {
        return month;
    }
    public int getYear() {
        return year;
    }
}
