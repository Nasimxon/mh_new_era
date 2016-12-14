package com.jim.pocketaccounter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.report.ReportByIncomeExpenseMonthlyData;
import com.jim.pocketaccounter.report.ReportByIncomeExpenseMonthlyView;

import java.util.ArrayList;
import java.util.List;

public class ReportByIncomeExpenseMonthlyFragment extends Fragment {
    private ReportByIncomeExpenseMonthlyView rbiemvMonhtly;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.report_by_income_expense_monthly, container, false);
        rbiemvMonhtly = (ReportByIncomeExpenseMonthlyView) rootView.findViewById(R.id.rbiemvMonhtly);
        List<ReportByIncomeExpenseMonthlyData> datas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            ReportByIncomeExpenseMonthlyData data = new ReportByIncomeExpenseMonthlyData();
            data.setMonth(i);
            data.setYear(2016);
            data.setValue(i+5);
            datas.add(data);
        }
        rbiemvMonhtly.setDatas(datas);
        rbiemvMonhtly.animateShapes();
        rbiemvMonhtly.animateClick(3);
        return rootView;
    }
}
