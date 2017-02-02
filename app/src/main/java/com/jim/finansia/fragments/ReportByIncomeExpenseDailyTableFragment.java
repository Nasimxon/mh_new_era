package com.jim.finansia.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.reportfilter.IntervalPickDialog;
import com.jim.finansia.utils.reportfilter.IntervalPickerView;
import com.jim.finansia.utils.reportviews.TableView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReportByIncomeExpenseDailyTableFragment extends Fragment {
    private TableView tvReportDailyTable;
    private IntervalPickDialog dialog;
    private int sortingType = TableView.BY_DATE;
    private Calendar b, e;
    private boolean orderAsc = true;
    private List<TableView.TableViewData> datas;
    private FloatingActionButton fabToExcelFile;
    private boolean show = false;
    private final int PERMISSION_READ_STORAGE = 0;
    @Inject ToolbarManager toolbarManager;
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    @Inject DecimalFormat formatter;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication)getContext().getApplicationContext()).inject(this);
        final View rootView = inflater.inflate(R.layout.report_by_income_expense_daily_table_fragment, container, false);

        tvReportDailyTable = (TableView) rootView.findViewById(R.id.tvReportDailyTable);
        RecyclerView recyclerView = tvReportDailyTable.getRvTableView();
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    try {
                        onScrolledList(dy > 0);
                    } catch (NullPointerException e) {}
                }
            });
        }
        dialog = new IntervalPickDialog(getContext());
        dialog.setListener(new IntervalPickerView.IntervalPickListener() {
            @Override
            public void onIntervalPick(Calendar begin, Calendar end) {
                b = (Calendar) begin.clone();
                e = (Calendar) end.clone();
                initDatas(begin, end);
                toolbarManager.setSubtitle(getString(R.string.c_interval)+": "+sDateFormat.format(begin.getTime())+" > "+getString(R.string.do_interval)+": "+sDateFormat.format(end.getTime()));
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
        fabToExcelFile = (FloatingActionButton) rootView.findViewById(R.id.fabToExcelFile);
        fabToExcelFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(((PocketAccounter) getContext()),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                .setTitle("Permission required");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_READ_STORAGE);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                } else {
                    saveExcel();
                }
            }
        });
        return  rootView;
    }
    private void initDatas(Calendar begin, Calendar end) {
        List<ReportObject> objects = reportManager.getReportObjects(true, begin, end,
                                                            FinanceRecord.class,
                                                            DebtBorrow.class,
                                                            CreditDetials.class,
                                                            SmsParseSuccess.class);
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
        datas = new ArrayList<>();
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
        data.setIncome(formatter.format(totalIncome) + mainCurrencyAbbr);
        data.setExpense(formatter.format(totalExpense) + mainCurrencyAbbr);
        datas.add(data);
        for (int i = 0; i < datas.size(); i++) {
            if (i != 0 && i != datas.size() - 1) {
                datas.get(i).setIncome(formatter.format(Double.parseDouble(datas.get(i).getIncome())) + mainCurrencyAbbr);
                datas.get(i).setExpense(formatter.format(Double.parseDouble(datas.get(i).getExpense())) + mainCurrencyAbbr);
            }
        }
        tvReportDailyTable.setDatas(datas);
    }

    private void saveExcel() {
        File direct = new File(Environment.getExternalStorageDirectory() + "/Finansia");
        if (!direct.exists()) {
            if (direct.mkdir()) {
                exportToExcelFile();
            }
        } else {
            exportToExcelFile();
        }
    }

    private void exportToExcelFile() {
        final WarningDialog dialog = new WarningDialog(getContext());
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd LLL, yyyy");
        dialog.setText(getResources().getString(R.string.save_to_excel));
        dialog.setOnYesButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sd = Environment.getExternalStorageDirectory();
                String fname = sd.getAbsolutePath() + "/" +
                        "Finansia/" +
                        "ra_" + simpleDateFormat.format(Calendar.getInstance().getTime());
                File temp = new File(fname + ".xlsx");
                while (temp.exists()) {
                    fname = fname + "_copy";
                    temp = new File(fname);
                }
                fname = fname + ".xlsx";
                try {
                    File exlFile = new File(fname);
                    WritableWorkbook writableWorkbook = Workbook.createWorkbook(exlFile);
                    WritableSheet writableSheet = writableWorkbook.createSheet(getContext().getResources().getString(R.string.app_name), 0);
                    String[] labels = getResources().getStringArray(R.array.excel_headers);
                    for (int i = 0; i < labels.length; i++) {
                        Label label = new Label(i, 0, labels[i]);
                        writableSheet.addCell(label);
                    }
                    for (int i = 0; i < datas.size(); i++) {
                        TableView.TableViewData data = datas.get(i);
                        if (i == datas.size()-1) {
                            Label total = new Label(0, i, getResources().getString(R.string.total));
                            Label income = new Label(1, i, data.getIncome());
                            Label expense = new Label(2, i, data.getExpense());
                            writableSheet.addCell(total);
                            writableSheet.addCell(income);
                            writableSheet.addCell(expense);
                        }
                        else if (data != null){
                            Label date = new Label(0, i, data.getDate());
                            Label income = new Label(1, i, data.getIncome());
                            Label expense = new Label(2, i, data.getExpense());
                            writableSheet.addCell(date);
                            writableSheet.addCell(income);
                            writableSheet.addCell(expense);
                        }
                    }
                    writableWorkbook.write();
                    writableWorkbook.close();
                    Toast.makeText(getContext(), fname + ": saved...", Toast.LENGTH_SHORT).show();
                } catch (IOException | WriteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.setOnNoButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void onScrolledList(boolean k) {
        if (k) {
            if (!show)
                fabToExcelFile.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_down));
            show = true;
        } else {
            if (show)
                fabToExcelFile.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_up));
            show = false;
        }
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setSubtitle(null);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
            toolbarManager.setTitle(getString(R.string.financial_transactions_report));
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }


}
