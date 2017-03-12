package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.fragments.ReportByIncomExpenseMonthDetailedByDaysFragment;
import com.jim.finansia.managers.PAFragmentManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class MonthPickSliderView extends RelativeLayout {
    private ViewPager vpMonthPickSlider;
    private List<Fragment> fragments;
    private int position;
    private MonthCurveLinedChartAdapter adapter;
    private MonthDetailedByDaysSelectedListener listener;
    @Inject DaoSession daoSession;
    @Inject PAFragmentManager paFragmentManager;
    public MonthPickSliderView(Context context) {
        super(context);
        init(context);
    }

    public MonthPickSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MonthPickSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public MonthPickSliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.month_pick_slider_view, this, true);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        vpMonthPickSlider = (ViewPager) findViewById(R.id.vpMonthPickSlider);
        fragments = new ArrayList<>();
        List<FinanceRecord> records = daoSession.loadAll(FinanceRecord.class);
        List<CreditDetials> credits = daoSession.loadAll(CreditDetials.class);
        List<DebtBorrow> debtBorrows  = daoSession.loadAll(DebtBorrow.class);
        Calendar calendar = Calendar.getInstance();
        if (records.isEmpty() && credits.isEmpty() && debtBorrows.isEmpty()) {
            ReportByIncomExpenseMonthDetailedByDaysFragment fragment = new ReportByIncomExpenseMonthDetailedByDaysFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(ReportByIncomExpenseMonthDetailedByDaysFragment.MONTH,calendar.get(Calendar.MONTH));
            bundle.putInt(ReportByIncomExpenseMonthDetailedByDaysFragment.YEAR,calendar.get(Calendar.YEAR));
            fragment.setArguments(bundle);
            fragments.add(fragment);
        } else {
            for (FinanceRecord financeRecord : records) {
                if (financeRecord.getDate().compareTo(calendar) <= 0) {
                    calendar = (Calendar) financeRecord.getDate().clone();
                }
            }
            for (CreditDetials credit : credits) {
                for (ReckingCredit recking : credit.getReckings()) {
                    if (recking.getPayDate().compareTo(calendar) <= 0) {
                        calendar = (Calendar) recking.getPayDate().clone();
                    }
                }
            }
            for (DebtBorrow debtBorrow : debtBorrows) {
                if (debtBorrow.getTakenDate().compareTo(calendar) <= 0)
                    calendar = (Calendar) debtBorrow.getTakenDate().clone();
                for (Recking recking : debtBorrow.getReckings()) {
                    if (recking.getPayDate().compareTo(calendar) <= 0)
                        calendar = (Calendar) recking.getPayDate().clone();
                }
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Calendar now = Calendar.getInstance();
            while (calendar.compareTo(now) <= 0) {
                ReportByIncomExpenseMonthDetailedByDaysFragment fragment = new ReportByIncomExpenseMonthDetailedByDaysFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(ReportByIncomExpenseMonthDetailedByDaysFragment.MONTH,calendar.get(Calendar.MONTH));
                bundle.putInt(ReportByIncomExpenseMonthDetailedByDaysFragment.YEAR,calendar.get(Calendar.YEAR));
                fragment.setArguments(bundle);
                fragments.add(fragment);
                calendar.add(Calendar.MONTH, 1);
            }
        }
        adapter = new MonthCurveLinedChartAdapter(paFragmentManager.getFragmentManager(), fragments);
        vpMonthPickSlider.setAdapter(adapter);
        vpMonthPickSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MonthPickSliderView.this.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    ReportByIncomExpenseMonthDetailedByDaysFragment fragment = (ReportByIncomExpenseMonthDetailedByDaysFragment)adapter.getItem(position);
                    if (fragment != null) {
                        listener.onMonthSelected(fragment.getMonth(), fragment.getYear());
                    }
                }
            }
        });
    }

    public int getItemsSize() {
        return fragments.size();
    }

    public void setCurrentItem(int position) {
        vpMonthPickSlider.setCurrentItem(position);
    }

    public void setListener(MonthDetailedByDaysSelectedListener listener) {
        this.listener = listener;
    }

    class MonthCurveLinedChartAdapter extends FragmentStatePagerAdapter{
        private List<Fragment> fragments;
        public MonthCurveLinedChartAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public interface MonthDetailedByDaysSelectedListener {
        public void onMonthSelected(int month, int year);
    }

}
