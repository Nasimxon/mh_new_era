package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.FilterSelectable;
import com.jim.finansia.utils.FilterDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

public class ReportByCategory extends Fragment {
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    FilterDialog filterDialog;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat simpleDateFormat;
    @Inject
    ReportManager reportManager;

    private ImageView ivToolbarMostRight;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);

        final View rootView = inflater.inflate(R.layout.report_by_category, container, false);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(PocketAccounter.keyboardVisible){
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);}
            }
        },100);

        ViewPager vpReportByCategory = (ViewPager) rootView.findViewById(R.id.vpReportByCategoryPager);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tlReportByCategoryTab);
        final ReportByCategoryIncomesFragment incomesFragment = new ReportByCategoryIncomesFragment();
        final ReportByCategoryExpansesFragment expansesFragment = new ReportByCategoryExpansesFragment();
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
        filterDialog.setOnDateSelectedListener(new FilterSelectable() {
            @Override
            public void onDateSelected(Calendar begin, Calendar end) {
                incomesFragment.invalidate(begin, end);
            }
        });
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.show();
            }
        });
        ArrayList<Fragment> list = new ArrayList<>();
        list.add(expansesFragment);
        list.add(incomesFragment);
        toolbarManager.setTitle(getResources().getString(R.string.report_by_categories));
        toolbarManager.setSubtitle("");
        vpReportByCategory.setAdapter(new MyViewPagerAdapter(paFragmentManager.getFragmentManager(), list));
        tabLayout.setupWithViewPager(vpReportByCategory);
        vpReportByCategory.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                        @Override
                        public void onDateSelected(Calendar begin, Calendar end) {
                            incomesFragment.invalidate(begin, end);
                        }
                    });
                }
                else {
                    filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                        @Override
                        public void onDateSelected(Calendar begin, Calendar end) {
                            expansesFragment.invalidate(begin, end);
                        }
                    });
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return rootView;
    }
    private class MyViewPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Fragment> fragments;
        public MyViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        public int getCount() {
            return 2;
        }
        public CharSequence getPageTitle(int position) {
            if (position == 1)
                return getResources().getString(R.string.income);
            return getResources().getString(R.string.expanse);
        }
    }
}