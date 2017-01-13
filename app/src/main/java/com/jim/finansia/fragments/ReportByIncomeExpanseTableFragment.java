package com.jim.finansia.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.IncomeExpanseDataRow;
import com.jim.finansia.report.TableView;
import com.jim.finansia.utils.FilterDialog;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReportByIncomeExpanseTableFragment extends Fragment {
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    FilterDialog filterDialog;
    @Inject
    ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject
    ReportManager reportManager;
    @Inject
    CommonOperations commonOperations;
    @Inject SharedPreferences sharedPreferences;
    private TableView tvReportIncomeExpance;
    private List<IncomeExpanseDataRow> sortReportIncomeExpance;
    private Calendar begin, end;
    private TextView tvTotalIncome, tvAverageIncome, tvTotalExpanse, tvAverageExpanse, tvTotalProfit, tvAverageProfit, tvReportIncomeExpanseNoDatas;
    private final int PERMISSION_READ_STORAGE = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        begin = Calendar.getInstance();
        begin.set(Calendar.DAY_OF_MONTH,1);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end = Calendar.getInstance();
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        final View rootView = inflater.inflate(R.layout.report_by_income_expanse, container, false);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PocketAccounter.keyboardVisible) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        }, 100);
        String[] titles = getResources().getStringArray(R.array.report_by_income_expanse_table_titles);
        tvReportIncomeExpance = (TableView) rootView.findViewById(R.id.tvReportByIncomeExpanse);
        tvTotalIncome = (TextView) rootView.findViewById(R.id.tvTotalIncome);
        tvAverageIncome = (TextView) rootView.findViewById(R.id.tvAverageIncome);
        tvTotalExpanse = (TextView) rootView.findViewById(R.id.tvTotalExpanse);
        tvAverageExpanse = (TextView) rootView.findViewById(R.id.tvAverageExpanse);
        tvTotalProfit = (TextView) rootView.findViewById(R.id.tvTotalProfit);
        tvAverageProfit = (TextView) rootView.findViewById(R.id.tvAverageProfit);
        tvReportIncomeExpanseNoDatas = (TextView) rootView.findViewById(R.id.tvReportIncomeExpanseNoDatas);
        init();
        sortReportIncomeExpance = reportManager.getIncomeExpanceReport(begin,end);
        for (int i = 0; i < sortReportIncomeExpance.size(); i++) {
            if (sortReportIncomeExpance.get(i).getTotalIncome() == 0.0d &&
                    sortReportIncomeExpance.get(i).getTotalExpanse() == 0.0) {
                sortReportIncomeExpance.remove(i);
                i--;
            }
        }
        if(sortReportIncomeExpance.isEmpty()) {
            tvReportIncomeExpanseNoDatas.setVisibility(View.VISIBLE);
            tvReportIncomeExpance.setVisibility(View.GONE);
        }
        else {
            tvReportIncomeExpanseNoDatas.setVisibility(View.GONE);
            tvReportIncomeExpance.setVisibility(View.VISIBLE);
        }
        tvReportIncomeExpance.setTitle(titles, false);
        tvReportIncomeExpance.setDatas(sortReportIncomeExpance);
        calculateDatas();
        return rootView;
    }
    public void invalidate(Calendar begin, Calendar end) {
        this.begin = (Calendar) begin.clone();
        this.end = (Calendar) end.clone();
        sortReportIncomeExpance = reportManager.getIncomeExpanceReport(begin,end);
        for (int i = 0; i < sortReportIncomeExpance.size(); i++) {
            if (sortReportIncomeExpance.get(i).getTotalIncome() == 0.0d &&
                    sortReportIncomeExpance.get(i).getTotalExpanse() == 0.0) {
                sortReportIncomeExpance.remove(i);
                i--;
            }
        }
        if(sortReportIncomeExpance.isEmpty()) {
            tvReportIncomeExpanseNoDatas.setVisibility(View.VISIBLE);
            tvReportIncomeExpance.setVisibility(View.GONE);
        }
        else {
            tvReportIncomeExpanseNoDatas.setVisibility(View.GONE);
            tvReportIncomeExpance.setVisibility(View.VISIBLE);
        }
        tvReportIncomeExpance.setDatas(sortReportIncomeExpance);
        calculateDatas();
    }
    private void calculateDatas() {
        int countOfDays = 0;
        Calendar beg = (Calendar) begin.clone();
        while (beg.compareTo(end) <= 0) {
            countOfDays++;
            beg.add(Calendar.DAY_OF_MONTH, 1);
        }
        DecimalFormat format = new DecimalFormat("0.00##");
        String abbr = commonOperations.getMainCurrency().getAbbr();
        double totalIncome = 0.0, totalExpanse = 0.0, totalProfit = 0.0,
                averageIncome = 0.0, averageExpanse = 0.0, averageProfit = 0.0;
        totalProfit = totalIncome - totalExpanse;
        if (sortReportIncomeExpance != null) {
            for (int i = 0; i < sortReportIncomeExpance.size(); i++) {
                totalIncome = totalIncome + sortReportIncomeExpance.get(i).getTotalIncome();
                totalExpanse = totalExpanse + sortReportIncomeExpance.get(i).getTotalExpanse();
                totalProfit = totalProfit + sortReportIncomeExpance.get(i).getTotalProfit();
            }
        }
        averageIncome = totalIncome / countOfDays;
        averageExpanse = totalExpanse / countOfDays;
        averageProfit = totalProfit / countOfDays;
        tvTotalIncome.setText(getString(R.string.report_income_expanse_total_income) + format.format(totalIncome) + abbr);
        tvAverageIncome.setText(getString(R.string.report_income_expanse_aver_income) + format.format(averageIncome) + abbr);
        tvTotalExpanse.setText(getString(R.string.report_income_expanse_total_expanse) + format.format(totalExpanse) + abbr);
        tvAverageExpanse.setText(getString(R.string.report_income_expanse_aver_expanse) + format.format(averageExpanse) + abbr);
        tvTotalProfit.setText(getString(R.string.report_income_expanse_total_profit) + format.format(totalProfit) + abbr);
        tvAverageProfit.setText(getString(R.string.report_income_expanse_aver_profit) + format.format(averageProfit) + abbr);
    }
    private void init() {
        String setting = sharedPreferences.getString("report_filter", "0");
        begin = Calendar.getInstance();
        end = Calendar.getInstance();
        switch (setting) {
            case "0":
                begin.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "1":
                begin.add(Calendar.DAY_OF_MONTH, -2);
                break;
            case "2":
                begin.add(Calendar.DAY_OF_MONTH, -6);
                break;
        }
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveExcel();
                }
                break;
            }
        }
    }
    public void setPermissionForExcelFile() {
        int permission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(((PocketAccounter) getContext()),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.").
                        setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_READ_STORAGE);
            }
        } else {
            saveExcel();
        }
    }
    private void saveExcel() {
        File direct = new File(Environment.getExternalStorageDirectory() + "/Pocket Accounter");
        if (!direct.exists()) {
            if (direct.mkdir()) {
                exportToExcelFile();
            }
        } else {
            exportToExcelFile();
        }
    }
    public void exportToExcelFile() {
        final Dialog dialog = new Dialog(getContext());
        View dialogView = ((PocketAccounter) getContext()).getLayoutInflater().inflate(R.layout.warning_dialog, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        TextView tvWarningText = (TextView) dialogView.findViewById(R.id.tvWarningText);
        tvWarningText.setText(R.string.save_excel);
        Button ok = (Button) dialogView.findViewById(R.id.btnWarningYes);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy");
                File sd = Environment.getExternalStorageDirectory();
                String fname = sd.getAbsolutePath() + "/Pocket Accounter/pa_" + format.format(Calendar.getInstance().getTime());
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
                    String[] labels = new String[]{getContext().getResources().getString(R.string.date),
                            getContext().getResources().getString(R.string.income),
                            getContext().getResources().getString(R.string.expanse),
                            getContext().getResources().getString(R.string.profit)};
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    for (int i = 0; i < labels.length; i++) {
                        Label label = new Label(i, 0, labels[i]);
                        writableSheet.addCell(label);
                    }
                    for (int i = 0; i < sortReportIncomeExpance.size(); i++) {
                        Label date = new Label(0, i + 1, dateFormat.format(sortReportIncomeExpance.get(i).getDate().getTime()));
                        Number income = new Number(1, i + 1, sortReportIncomeExpance.get(i).getTotalIncome());
                        Number expance = new Number(2, i + 1, sortReportIncomeExpance.get(i).getTotalExpanse());
                        Number profit = new Number(3, i + 1, sortReportIncomeExpance.get(i).getTotalProfit());
                        writableSheet.addCell(date);
                        writableSheet.addCell(income);
                        writableSheet.addCell(expance);
                        writableSheet.addCell(profit);
                    }
                    writableWorkbook.write();
                    writableWorkbook.close();
                    Toast.makeText(getContext(), fname + ": saved...", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.btnWarningNo);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
