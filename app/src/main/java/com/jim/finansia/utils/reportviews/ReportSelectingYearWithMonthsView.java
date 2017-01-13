package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class ReportSelectingYearWithMonthsView extends LinearLayout {
    private ViewPager vpReportSelectingYearWithMonths;
    private Calendar calendar;
    private int mode = PocketAccounterGeneral.EXPENSE;
    private SelectingYearWithMonthAdapter adapter;
    private int position = -1;
    private int lastPosition = -2;
    private SelectingYearWithMonthsListener listener;
    private List<Fragment> fragments;
    @Inject ReportManager reportManager;
    @Inject DaoSession daoSession;
    @Inject PAFragmentManager paFragmentManager;
    public ReportSelectingYearWithMonthsView(Context context) {
        super(context);
        init(context);
    }

    public ReportSelectingYearWithMonthsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReportSelectingYearWithMonthsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public ReportSelectingYearWithMonthsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.report_selecting_year_with_months_view, this, true);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        calendar = Calendar.getInstance();
        vpReportSelectingYearWithMonths = (ViewPager) findViewById(R.id.vpReportSelectingYearWithMonths);
        initYears();
    }

    public void setListener(SelectingYearWithMonthsListener listener) {
        this.listener = listener;
        if (this.listener != null)
            this.listener.OnSelectingYearWithMonths(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    private void initYears() {
        int minYear = calendar.get(Calendar.YEAR), maxYear = calendar.get(Calendar.YEAR);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<Account> accounts = daoSession.getAccountDao().loadAll();
        for (Account account : accounts) {
            if (calendar.compareTo(account.getCalendar()) >= 0)
                minYear = account.getCalendar().get(Calendar.YEAR);
            else
                maxYear = account.getCalendar().get(Calendar.YEAR);

        }
        List<FinanceRecord> records = daoSession.getFinanceRecordDao().loadAll();
        for (FinanceRecord financeRecord : records) {
            if (calendar.compareTo(financeRecord.getDate()) >= 0)
                minYear = financeRecord.getDate().get(Calendar.YEAR);
            else
                maxYear = financeRecord.getDate().get(Calendar.YEAR);
        }
        List<CreditDetials> creditDetialses = daoSession.getCreditDetialsDao().loadAll();
        for (CreditDetials creditDetials : creditDetialses) {
            for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                if (calendar.compareTo(reckingCredit.getPayDate()) >= 0)
                    minYear = reckingCredit.getPayDate().get(Calendar.YEAR);
                else
                    maxYear = reckingCredit.getPayDate().get(Calendar.YEAR);
            }
        }
        List<DebtBorrow> debtBorrows = daoSession.getDebtBorrowDao().loadAll();
        for (DebtBorrow debtBorrow : debtBorrows) {
            if (calendar.compareTo(debtBorrow.getTakenDate()) >= 0)
                minYear = debtBorrow.getTakenDate().get(Calendar.YEAR);
            else
                maxYear = debtBorrow.getTakenDate().get(Calendar.YEAR);
            for (Recking recking : debtBorrow.getReckings()) {
                if (calendar.compareTo(recking.getPayDate()) >= 0)
                    minYear = recking.getPayDate().get(Calendar.YEAR);
                else
                    maxYear = recking.getPayDate().get(Calendar.YEAR);
            }
        }
        List<SmsParseSuccess> smsParseSuccesses = daoSession.getSmsParseSuccessDao().loadAll();
        for (SmsParseSuccess smsParseSuccess : smsParseSuccesses) {
            if (calendar.compareTo(smsParseSuccess.getDate()) >= 0)
                minYear = smsParseSuccess.getDate().get(Calendar.YEAR);
            else
                maxYear = smsParseSuccess.getDate().get(Calendar.YEAR);

        }
        fragments = new ArrayList<>();
        for (int i = minYear; i <= maxYear; i++) {
            OneYearWithMonthsFragment fragment = new OneYearWithMonthsFragment(i);
            fragment.setMode(mode);
            fragment.setActive(false);
            fragment.setListener(new SelectingYearWithMonthsListener() {
                @Override
                public void OnSelectingYearWithMonths(int month, int year) {
                    if (listener != null)
                        listener.OnSelectingYearWithMonths(month, year);
                }
            });
            fragments.add(fragment);

        }
        adapter = new SelectingYearWithMonthAdapter(paFragmentManager.getFragmentManager(), fragments);
        vpReportSelectingYearWithMonths.setAdapter(adapter);
        int selection = 0;
        for (int i = 0; i < fragments.size(); i++) {
            OneYearWithMonthsFragment fragment = (OneYearWithMonthsFragment) fragments.get(i);
            if (fragment != null && fragment.getYear() == currentYear && fragment.getMonth() == currentMonth) {
                selection = i;
                fragment.setActive(true);
                break;
            }
        }
        position = selection;
        lastPosition = selection;
        vpReportSelectingYearWithMonths.setCurrentItem(selection);
        calendar.set(Calendar.YEAR, ((OneYearWithMonthsFragment)fragments.get(fragments.size()-1)).getYear());
        calendar.set(Calendar.MONTH, ((OneYearWithMonthsFragment)fragments.get(fragments.size()-1)).getMonth());
        vpReportSelectingYearWithMonths.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("sss", "onPageSelected: " + position);
                if (ReportSelectingYearWithMonthsView.this.position == -1) {
                    OneYearWithMonthsFragment fragment = (OneYearWithMonthsFragment) adapter.getItem(position);
                    if (fragment != null) {
                        fragment.animateSelection();
                        if (listener != null)
                            listener.OnSelectingYearWithMonths(fragment.getMonth(), fragment.getYear());
                    }
                }
                ReportSelectingYearWithMonthsView.this.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && position != lastPosition) {
                    OneYearWithMonthsFragment fragment = (OneYearWithMonthsFragment) adapter.getItem(position);
                    if (fragment != null) {
                        fragment.animateSelection();
                        if (listener != null)
                            listener.OnSelectingYearWithMonths(fragment.getMonth(), fragment.getYear());
                    }
                    for (int i = 0; i < fragments.size(); i++) {
                        OneYearWithMonthsFragment temp = (OneYearWithMonthsFragment) adapter.getItem(i);
                        if (temp != null)
                            temp.setActive(i == position);
                    }
                    lastPosition = position;
                }
            }
        });
    }

    public void setMode(int mode) {
        if (adapter != null && fragments != null) {
            for (int i = 0; i < fragments.size(); i++) {
                OneYearWithMonthsFragment fragment = (OneYearWithMonthsFragment) adapter.getItem(i);
                if (fragment != null) {
                    fragment.setMode(mode);
                }
            }
        }
    }

    class SelectingYearWithMonthAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;
        public SelectingYearWithMonthAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Fragment getItem(int position) {
            return position < 0 || position >= fragments.size() ?
                                                            null :
                                                            fragments.get(position);
        }
    }

    public interface SelectingYearWithMonthsListener {
        public void OnSelectingYearWithMonths(int month, int year);
    }
}
