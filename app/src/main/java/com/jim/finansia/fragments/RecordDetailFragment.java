package com.jim.finansia.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.SpaceTabLayout;
import com.jim.finansia.utils.cache.DataCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Inject SharedPreferences preferences;
    private SpaceTabLayout tabLayout;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private ViewPager vpRecord;
    public static final String SELECTED_POSITION = "selected_position";
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.finance_record_detail_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if (toolbarManager != null)
        {

            toolbarManager.setTitle(getResources().getString(R.string.records));
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
            toolbarManager.setSubtitle("");
        }
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                        toolbarManager.setOnTitleClickListener(null);
                        toolbarManager.setTitle(getResources().getString(R.string.records));
                        toolbarManager.setSubtitle("");
                        break;

                    case 1:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
                        toolbarManager.setOnTitleClickListener(null);
                        toolbarManager.setTitle(getResources().getString(R.string.sms_parse));
                        toolbarManager.setSubtitle("");
                        break;
                    case 2:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
                        toolbarManager.setOnTitleClickListener(null);
                        toolbarManager.setTitle(getResources().getString(R.string.debts_title));
                        toolbarManager.setSubtitle("");
                        break;
                    case 3:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
                        toolbarManager.setTitle(getResources().getString(R.string.credit));
                        toolbarManager.setOnTitleClickListener(null);
                        toolbarManager.setSubtitle("");
                        break;
                    default:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
                        toolbarManager.setTitle("");
                        toolbarManager.setSubtitle("");
                        toolbarManager.setOnTitleClickListener(null);
                        break;

                }
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

    public void updateFragments() {
        toolbarManager.setTitle(getResources().getString(R.string.records));
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
        toolbarManager.setSubtitle(date);
        for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
            if (fragment == null) continue;
            if (fragment.getClass().getName().equals(FinanceRecordsFragment.class.getName())) {
                FinanceRecordsFragment f = (FinanceRecordsFragment) fragment;
                if (f != null)
                    f.refreshList();
            }
            if (fragment.getClass().getName().equals(DetailedSmsSuccessesFragment.class.getName())) {
                DetailedSmsSuccessesFragment f = (DetailedSmsSuccessesFragment) fragment;
                if (f != null)
                    f.refreshList();
            }
            if (fragment.getClass().getName().equals(DetailedDebtBorrowsFragment.class.getName())) {
                DetailedDebtBorrowsFragment f = (DetailedDebtBorrowsFragment) fragment;
                if (f != null)
                    f.refreshList();
            }
            if (fragment.getClass().getName().equals(DetailedCreditsFragment.class.getName())) {
                DetailedCreditsFragment f = (DetailedCreditsFragment) fragment;
                if (f != null)
                    f.refreshList();
            }
        }
    }

    //we need the outState to save the position
    @Override
    public void onSaveInstanceState(Bundle outState) {
        tabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}