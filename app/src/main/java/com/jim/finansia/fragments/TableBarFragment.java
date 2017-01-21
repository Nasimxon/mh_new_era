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

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.FilterSelectable;
import com.jim.finansia.utils.FilterDialog;

import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

public class TableBarFragment extends Fragment {
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    FilterDialog filterDialog;
    ReportByIncomeExpanseTableFragment tableFragment;
    ReportByIncomeExpanseBarFragment barFragment;
    @Override
    public void onStart() {
        super.onStart();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        final View view = inflater.inflate(R.layout.table_bar_fragment_layout, container, false);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
        toolbarManager.setImageToFirstImage(R.drawable.ic_excel);
        toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
        toolbarManager.setTitle(getResources().getString(R.string.income_expance_title));
        toolbarManager.setSubtitle("");
        toolbarManager.setOnFirstImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableFragment.setPermissionForExcelFile();
            }
        });
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.show();
            }
        });
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PocketAccounter.keyboardVisible) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 100);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.vpTableBarFragment);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tlTableBarFragment);
        ArrayList<Fragment> list = new ArrayList<>();
        tableFragment = new ReportByIncomeExpanseTableFragment();
        barFragment = new ReportByIncomeExpanseBarFragment();
        list.add(tableFragment);
        list.add(barFragment);
        viewPager.setAdapter(new TableBarAdapter(((PocketAccounter) getContext()).getSupportFragmentManager(), list));
        tabLayout.setupWithViewPager(viewPager);
        filterDialog.setOnDateSelectedListener(new FilterSelectable() {
            @Override
            public void onDateSelected(Calendar begin, Calendar end) {
                tableFragment.invalidate(begin, end);
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                switch (position) {
                    case 0:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
                        break;
                    case 1:
                        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                        break;
                }
                filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                    @Override
                    public void onDateSelected(Calendar begin, Calendar end) {
                        if (position == 0) {
                            tableFragment.invalidate(begin, end);
                        } else if (position == 1) {
                            barFragment.invalidate(begin, end);
                        }
                    }
                });
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return view;
    }
    private class TableBarAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Fragment> list;
        public TableBarAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }
        public Fragment getItem(int position) {
            return list.get(position);
        }
        public int getCount() {
            return 2;
        }
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getResources().getString(R.string.table);
            }
            return getString(R.string.bars);
        }
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}
