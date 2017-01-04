package com.jim.pocketaccounter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.managers.PAFragmentManager;

import javax.inject.Inject;

public class ReportFragment extends Fragment {
    @Inject PAFragmentManager paFragmentManager;
    private LinearLayout btnReportByCategory, btnReportByIncomeAndExpenseTable,
            btnReportByIncomeAndExpenseGraphic, btnReportByIncomeAndExpenseMonthly;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        btnReportByCategory = (LinearLayout) rootView.findViewById(R.id.btnReportByCategory);
        btnReportByCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paFragmentManager.displayFragment(new ReportByCategoryFragment());
            }
        });
        btnReportByIncomeAndExpenseTable = (LinearLayout) rootView.findViewById(R.id.btnReportByIncomeAndExpenseTable);
        btnReportByIncomeAndExpenseTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paFragmentManager.displayFragment(new ReportByIncomeExpenseDailyTableFragment());
            }
        });
        btnReportByIncomeAndExpenseGraphic = (LinearLayout) rootView.findViewById(R.id.btnReportByIncomeAndExpenseGraphic);
        btnReportByIncomeAndExpenseGraphic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paFragmentManager.displayFragment(new ReportByIncomeExpenseDaily());
            }
        });
        btnReportByIncomeAndExpenseMonthly = (LinearLayout) rootView.findViewById(R.id.btnReportByIncomeAndExpenseMonthly);
        btnReportByIncomeAndExpenseMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paFragmentManager.displayFragment(new ReportByIncomeExpenseMonthlyFragment());
            }
        });
        return rootView;
    }
}

