package com.jim.finansia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;

import javax.inject.Inject;

public class ReportFragment extends Fragment {
    @Inject PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;
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
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setTitle(getResources().getString(R.string.report_by_account_title));
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        }
    }
}

