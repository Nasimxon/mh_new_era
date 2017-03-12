package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.fragments.ReportByIncomeExpenseMonthlyFragment;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.report.ReportByIncomeExpenseMonthlyView;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@SuppressLint("ValidFragment")
public class OneYearWithMonthsFragment extends Fragment {
    private int month, year;
    private Calendar begin, end;
    private ReportByIncomeExpenseMonthlyView rbiemvMonhtly;
    private int mode = PocketAccounterGeneral.EXPENSE;
    private ReportSelectingYearWithMonthsView.SelectingYearWithMonthsListener listener;
    private boolean active = true;
    @Inject ReportManager reportManager;
    @Inject PAFragmentManager paFragmentManager;
    public static String YEAR ="year";
    public static String MODE ="mode";
    public static String ACTIVE ="active";


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.one_year_with_months_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if(getArguments()!=null){
            this.year = getArguments().getInt(YEAR);
            setActive(getArguments().getBoolean(ACTIVE));
            setMode(getArguments().getInt(MODE));
            begin = Calendar.getInstance();
            end = Calendar.getInstance();
            month = year == begin.get(Calendar.YEAR) ? begin.get(Calendar.MONTH) : 0;
        }
        rbiemvMonhtly = (ReportByIncomeExpenseMonthlyView) rootView.findViewById(R.id.rbiemvMonthly);
        rbiemvMonhtly.active(active);
        rbiemvMonhtly.setListener(new ReportByIncomeExpenseMonthlyView.OnMonthlyItemSelectedListener() {
            @Override
            public void onMonthlyItemSelected(int position) {
                month = position;
                for(Fragment fragment:paFragmentManager.getFragmentManager().getFragments()){
                    if(fragment instanceof ReportByIncomeExpenseMonthlyFragment){
                        ((ReportByIncomeExpenseMonthlyFragment) fragment).setYearMonth(month,year);
                    }
                }
//                if (listener != null)
//                    listener.OnSelectingYearWithMonths(month, year);
            }
        });
        initDatas();
        return rootView;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (rbiemvMonhtly != null)
            rbiemvMonhtly.active(active);
    }

    public void setListener(ReportSelectingYearWithMonthsView.SelectingYearWithMonthsListener listener) {
        this.listener = listener;
    }

    private void initDatas() {
        if (reportManager == null) return;
        Map<Integer, Float> datas = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            begin = Calendar.getInstance();
            end = Calendar.getInstance();
            begin.set(Calendar.YEAR, year);
            begin.set(Calendar.MONTH, i);
            begin.set(Calendar.DAY_OF_MONTH, 1);
            begin.set(Calendar.HOUR_OF_DAY, 0);
            begin.set(Calendar.MINUTE, 0);
            begin.set(Calendar.SECOND, 0);
            begin.set(Calendar.MILLISECOND, 0);
            end.set(Calendar.YEAR, year);
            end.set(Calendar.MONTH, i);
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 59);
            List<ReportObject> objects = reportManager.getReportObjects(true, begin, end,
                    FinanceRecord.class,
                    CreditDetials.class,
                    DebtBorrow.class);
            float amount = 0.0f;
            for (ReportObject object : objects) {
                if (object.getType() == mode)
                    amount += object.getAmount();
            }
            datas.put(i, amount);
        }
        if (rbiemvMonhtly != null) {
            rbiemvMonhtly.setDatas(datas);
            rbiemvMonhtly.animateShapes();
            rbiemvMonhtly.animateClick(month);
        }
    }

    public void animateSelection() {
        initDatas();
    }
    public void setYear(int year) {
        this.year = year;
    }
    public void setMonth(int month) {
        this.month = month;
        if (rbiemvMonhtly != null)
            rbiemvMonhtly.animateClick(month);
    }

    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public void setMode(int mode) {
        this.mode = mode;
        if (reportManager == null) return;
        Map<Integer, Float> datas = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            begin = Calendar.getInstance();
            end = Calendar.getInstance();
            begin.set(Calendar.YEAR, year);
            begin.set(Calendar.MONTH, i);
            begin.set(Calendar.DAY_OF_MONTH, 1);
            begin.set(Calendar.HOUR_OF_DAY, 0);
            begin.set(Calendar.MINUTE, 0);
            begin.set(Calendar.SECOND, 0);
            begin.set(Calendar.MILLISECOND, 0);
            end.set(Calendar.YEAR, year);
            end.set(Calendar.MONTH, i);
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 59);
            List<ReportObject> objects = reportManager.getReportObjects(true, begin, end,
                    FinanceRecord.class,
                    CreditDetials.class,
                    DebtBorrow.class);
            float amount = 0.0f;
            for (ReportObject object : objects) {
                if (object.getType() == mode)
                    amount += object.getAmount();
            }
            datas.put(i, amount);
        }
        if (rbiemvMonhtly != null) {
            rbiemvMonhtly.setDatas(datas);
            rbiemvMonhtly.animateShapes();
        }
    }
}
