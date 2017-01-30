package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.debt.BorrowFragment;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.photocalc.PhotoAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SpaceTabLayout;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class RecordDetailFragment extends Fragment {
    public static String DATE = "date";
    public static String PARENT = "parent";
    public static String CATEGORY_ID = "category_id";
    public static String RECORD_ID = "record_id";
    private String date = "";
    @Inject DaoSession daoSession;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject DataCache dataCache;
    private SpaceTabLayout tabLayout;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private ViewPager vpRecord;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.finance_record_detail_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if (getArguments() != null)
            date = getArguments().getString(DATE);
        //add the fragments you want to display in a List
        Bundle bundle = new Bundle();
        bundle.putString(DATE, date);
        List<Fragment> fragmentList = new ArrayList<>();
        DetailedCreditsFragment credits = new DetailedCreditsFragment();
        credits.setArguments(bundle);
        DetailedDebtBorrowsFragment debtBorrows = new DetailedDebtBorrowsFragment();
        debtBorrows.setArguments(bundle);
        FinanceRecordsFragment financeRecords = new FinanceRecordsFragment();
        financeRecords.setArguments(bundle);
        DetailedSmsSuccessesFragment smsSuccesses = new DetailedSmsSuccessesFragment();
        smsSuccesses.setArguments(bundle);
        fragmentList.add(financeRecords);
        fragmentList.add(smsSuccesses);
        fragmentList.add(debtBorrows);
        fragmentList.add(credits);
        vpRecord = (ViewPager) rootView.findViewById(R.id.vpRecord);
        vpRecord.setOffscreenPageLimit(0);
        vpRecord.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                else
                    toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        tabLayout = (SpaceTabLayout) rootView.findViewById(R.id.spaceTabLayout);
        //we need the savedInstanceState to get the position
        tabLayout.initialize(vpRecord, paFragmentManager.getFragmentManager(),
                fragmentList, savedInstanceState);
        return rootView;
    }

    //we need the outState to save the position
    @Override
    public void onSaveInstanceState(Bundle outState) {
        tabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setImageToSecondImage(R.drawable.pencil);
            toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int size = 0;
                    size = paFragmentManager.getFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < size; i++) {
                        ((PocketAccounter)getContext()).getSupportFragmentManager().popBackStack();
                    }
                    paFragmentManager.displayMainWindow();
                }
            });
            toolbarManager.setTitle(getResources().getString(R.string.records));
            toolbarManager.setSubtitle(date);
        }
    }
}