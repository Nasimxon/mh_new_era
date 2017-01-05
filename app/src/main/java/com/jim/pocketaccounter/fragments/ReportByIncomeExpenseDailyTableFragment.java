package com.jim.pocketaccounter.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.reportfilter.IntervalPickDialog;
import com.jim.pocketaccounter.utils.reportfilter.IntervalPickerView;
import com.jim.pocketaccounter.utils.reportviews.TableView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class ReportByIncomeExpenseDailyTableFragment extends Fragment {
    private TableView tvReportDailyTable;
    private IntervalPickDialog dialog;
    private int sortingType = TableView.BY_DATE;
    private Calendar b, e;
    private boolean orderAsc = true;
    @Inject ToolbarManager toolbarManager;
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication)getContext().getApplicationContext()).inject(this);
        final View rootView = inflater.inflate(R.layout.report_by_income_expense_daily_table_fragment, container, false);
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        toolbarManager.setSubtitle(null);
        toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
//        toolbarManager.setTitle(getString(R.string.report_by_income_expense_table));
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        tvReportDailyTable = (TableView) rootView.findViewById(R.id.tvReportDailyTable);
        dialog = new IntervalPickDialog(getContext());
        dialog.setListener(new IntervalPickerView.IntervalPickListener() {
            @Override
            public void onIntervalPick(Calendar begin, Calendar end) {
                b = (Calendar) begin.clone();
                e = (Calendar) end.clone();
                initDatas(begin, end);

                dialog.dismiss();
            }

            @Override
            public void onCancelPick() {
                dialog.dismiss();
            }
        });
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.getWindow().setLayout(9 * getContext().getResources().getDisplayMetrics().widthPixels / 10, RelativeLayout.LayoutParams.WRAP_CONTENT);
                dialog.show();
            }
        });
        tvReportDailyTable.setListener(new TableView.OnTableRowClickListener() {
            @Override
            public void onTableHeadClick(int column) {
                switch (column) {
                    case 0:
                        sortingType = TableView.BY_DATE;
                        break;
                    case 1:
                        sortingType = TableView.BY_INCOME;
                        break;
                    case 2:
                        sortingType = TableView.BY_EXPENSE;
                        break;
                }
                tvReportDailyTable.setSortingType(sortingType);
                orderAsc = !orderAsc;
                tvReportDailyTable.setOrderAsc(orderAsc);
                initDatas(b, e);
            }

            @Override
            public void onTableRowClick(int position) {

            }
        });
        return  rootView;
    }
    private void initDatas(Calendar begin, Calendar end) {
        List<ReportObject> objects = reportManager.getReportObjects(true, begin, end,
                                                            FinanceRecord.class,
                                                            DebtBorrow.class,
                                                            CreditDetials.class);



        switch (sortingType) {
            case TableView.BY_DATE:
                for (int i = 0; i < objects.size(); i++) {
                    for (int j = objects.size() - 1; j > i; j--) {
                        if (orderAsc) {
                            if (objects.get(i).getDate().compareTo(objects.get(j).getDate()) > 0) {
                                ReportObject tmp = objects.get(i);
                                objects.set(i, objects.get(j));
                                objects.set(j,tmp);
                            }
                        } else {
                            if (objects.get(i).getDate().compareTo(objects.get(j).getDate()) < 0) {
                                ReportObject tmp = objects.get(i);
                                objects.set(i, objects.get(j));
                                objects.set(j,tmp);
                            }
                        }
                    }
                }
                break;
            case TableView.BY_INCOME:
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i).getType() != PocketAccounterGeneral.INCOME)
                        continue;
                    for (int j = objects.size() - 1; j > i; j--) {
                        if (objects.get(j).getType() == PocketAccounterGeneral.EXPENSE || objects.get(i).getAmount() > objects.get(j).getAmount()) {
                            ReportObject tmp = objects.get(i);
                            objects.set(i, objects.get(j));
                            objects.set(j,tmp);
                        }
                    }
                }
                if (!orderAsc)
                    Collections.reverse(objects);
                break;
            case TableView.BY_EXPENSE:
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i).getType() != PocketAccounterGeneral.EXPENSE)
                        continue;
                    for (int j = objects.size() - 1; j > i; j--) {
                        if (objects.get(j).getType() == PocketAccounterGeneral.INCOME || objects.get(i).getAmount() > objects.get(j).getAmount()) {
                            ReportObject tmp = objects.get(i);
                            objects.set(i, objects.get(j));
                            objects.set(j,tmp);
                        }
                    }
                }
                if (!orderAsc)
                    Collections.reverse(objects);
                break;
        }
        List<TableView.TableViewData> datas = new ArrayList<>();
        final SimpleDateFormat format = new SimpleDateFormat("dd LLL, yyyy");
        String mainCurrencyAbbr = commonOperations.getMainCurrency().getAbbr();
        double totalIncome = 0.0d, totalExpense = 0.0d;
        for (int i = 0; i < objects.size(); i++) {
            boolean found = false;
            for (int j = 0; j < datas.size(); j++) {
                if (datas.get(j) != null && format.format(objects.get(i).getDate().getTime()).equals(datas.get(j).getDate())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (int j = i; j < objects.size(); j++) {
                    if (format.format(objects.get(i).getDate().getTime()).equals(format.format(objects.get(j).getDate().getTime()))) {
                        if (objects.get(j).getType() == PocketAccounterGeneral.INCOME)
                            totalIncome += objects.get(j).getAmount();
                        else
                            totalExpense += objects.get(j).getAmount();
                    }
                }
                TableView.TableViewData data = new TableView.TableViewData();
                data.setDate(format.format(objects.get(i).getDate().getTime()));
                data.setIncome(Double.toString(totalIncome));
                data.setExpense(Double.toString(totalExpense));
                datas.add(data);
                totalExpense = 0.0d;
                totalIncome = 0.0d;
            }
        }
        switch (sortingType) {
            case TableView.BY_DATE:
                Collections.sort(datas, new Comparator<TableView.TableViewData>() {
                    @Override
                    public int compare(TableView.TableViewData tableViewData, TableView.TableViewData t1) {
                        Calendar first = Calendar.getInstance(), second = Calendar.getInstance();
                        try {
                            first.setTimeInMillis(format.parse(tableViewData.getDate()).getTime());
                            second.setTimeInMillis(format.parse(t1.getDate()).getTime());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        return first.compareTo(second);
                    }
                });
                break;
            case TableView.BY_INCOME:
                Collections.sort(datas, new Comparator<TableView.TableViewData>() {
                    @Override
                    public int compare(TableView.TableViewData tableViewData, TableView.TableViewData t1) {
                        double first = Double.parseDouble(tableViewData.getIncome());
                        double second = Double.parseDouble(t1.getIncome());
                        return (int) (first - second);
                    }
                });
                break;
            case TableView.BY_EXPENSE:
                Collections.sort(datas, new Comparator<TableView.TableViewData>() {
                    @Override
                    public int compare(TableView.TableViewData tableViewData, TableView.TableViewData t1) {
                        double first = Double.parseDouble(tableViewData.getExpense());
                        double second = Double.parseDouble(t1.getExpense());
                        return (int) (first - second);
                    }
                });
                break;
        }
        if (!orderAsc)
            Collections.reverse(datas);
        totalIncome = 0.0d;
        totalExpense = 0.0d;
        for (TableView.TableViewData data : datas) {
            totalIncome += Double.parseDouble(data.getIncome());
            totalExpense += Double.parseDouble(data.getExpense());
        }
        datas.add(0, null);
        TableView.TableViewData data = new TableView.TableViewData();
        data.setDate(format.format(Calendar.getInstance().getTime()));
        data.setIncome(Double.toString(totalIncome) + mainCurrencyAbbr);
        data.setExpense(Double.toString(totalExpense) + mainCurrencyAbbr);
        datas.add(data);
        for (int i = 0; i < datas.size(); i++) {
            if (i != 0 && i != datas.size() - 1) {
                datas.get(i).setIncome(datas.get(i).getIncome() + mainCurrencyAbbr);
                datas.get(i).setExpense(datas.get(i).getExpense() + mainCurrencyAbbr);
            }
        }
        tvReportDailyTable.setDatas(datas);
    }
}
