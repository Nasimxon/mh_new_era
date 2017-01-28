package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

@SuppressLint("ValidFragment")
public class RecordDetailFragment extends Fragment {
    private Calendar date;
    private RecyclerView rvRecordDetail;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private ArrayList<FinanceRecord> records;
    private boolean[] selections;
    private boolean onNoPressed = false;
    Context context;
    @Inject
    DaoSession daoSession;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    LogicManager logicManager;
    @Inject
    DataCache dataCache;



    private ViewPager vpRecord;
    @SuppressLint("ValidFragment")
    public RecordDetailFragment(Calendar date) {
        this.date = (Calendar) date.clone();
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.finance_record_detail_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        vpRecord = (ViewPager) rootView.findViewById(R.id.vpRecord);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FinanceRecordsFragment(Calendar.getInstance()));
        fragments.add(new SmsParseMainFragment());
        fragments.add(new ReportFragment());
        fragments.add(new CurrencyFragment());
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayoutRecords);
        MyAdapter adapter = new MyAdapter(fragments, paFragmentManager.getFragmentManager());
        vpRecord.setAdapter(adapter);
        tabLayout.setupWithViewPager(vpRecord);
        return rootView;
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd,LLL yyyy");
            toolbarManager.setSubtitle(dateFormat.format(date.getTime()));
        }
    }
    private class MyAdapter extends FragmentStatePagerAdapter {
        List<Fragment> list;

        public MyAdapter(List<Fragment> list, FragmentManager fm) {
            super(fm);
            this.list = list;
        }

        public Fragment getItem(int position) {
            return list.get(position);
        }

        public int getCount() {
            return list.size();
        }

        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getResources().getString(R.string.records);
            } else
            if (position == 1) {
                return getResources().getString(R.string.sms_parse);
            }
            if (position == 2) {
                return getResources().getString(R.string.debts);
            }
            return getResources().getString(R.string.credit);
        }
    }
}