package com.jim.finansia.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;

import javax.inject.Inject;

public class ReportByIncomeAndExpense extends Fragment {
    @Inject FinansiaFirebaseAnalytics analytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        View rootView = inflater.inflate(R.layout.report_by_income_and_expense_daily, container, false);


        return rootView;
    }

}
