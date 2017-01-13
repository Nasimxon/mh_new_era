package com.jim.finansia.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.AccountCurrencyPair;
import com.jim.finansia.report.FilterSelectable;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.report.TableView;
import com.jim.finansia.utils.FilterDialog;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReportByAccountFragment extends Fragment implements View.OnClickListener {
    @Inject PAFragmentManager paFragmentManager;
    @Inject DaoSession daoSession;
    @Inject FilterDialog filterDialog;
    @Inject ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    @Inject SharedPreferences preferences;
    private String[] titles;
    private int pos_account = 0, pos_currency = 0;
    private Calendar begin, end;
    private DecimalFormat decimalFormat;
    private Account account;
    private Currency currency;
    private TableView tbReportByAccount;
    private List<ReportObject> sortReportByAccount;
    private LinearLayout linLayReportByAccountInfo;
    private TextView tvReportByAccountNoDatas;
    private TextView tvReportbyAccountTotalIncome, tvReportbyAccountTotalExpanse, tvReportbyAccountTotalProfit, tvReportbyAccountAverageProfit;
    private final int PERMISSION_READ_STORAGE = 0;
    private List<AccountCurrencyPair> pairs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        final View rootView = inflater.inflate(R.layout.report_by_account, container, false);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PocketAccounter.keyboardVisible) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        }, 100);
        initDates();
        linLayReportByAccountInfo = (LinearLayout) rootView.findViewById(R.id.linLayReportByAccountInfo);
        tvReportbyAccountTotalIncome = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalIncome);
        tvReportbyAccountTotalExpanse = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalExpanse);
        tvReportbyAccountTotalProfit = (TextView) rootView.findViewById(R.id.tvReportbyAccountTotalProfit);
        tvReportbyAccountAverageProfit = (TextView) rootView.findViewById(R.id.tvReportbyAccountAverageProfit);
        toolbarManager.setTitle("");
        toolbarManager.setSubtitle("");
        toolbarManager.setSpinnerVisibility(View.VISIBLE);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
        toolbarManager.setImageToFirstImage(R.drawable.ic_excel);
        toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
        toolbarManager.setOnFirstImageClickListener(this);
        toolbarManager.setOnSecondImageClickListener(this);
        final List<String> result = new ArrayList<>();
        List<ReportObject> reportObjects = reportManager.getReportObjects(false, begin, end,
                Account.class, DebtBorrow.class, CreditDetials.class, SmsParseSuccess.class,
                FinanceRecord.class);
        List<Currency> allCurrencies = daoSession.getCurrencyDao().loadAll();
        List<Account> allAccounts = daoSession.getAccountDao().loadAll();
        pairs = new ArrayList<>();
        for (Account account : allAccounts) {
            for (Currency currency : allCurrencies) {
                for (ReportObject reportObject : reportObjects) {
                    if (reportObject.getAccount().getId().equals(account.getId()) &&
                            reportObject.getCurrency().getId().equals(currency.getId())) {
                        boolean found = false;
                        for (AccountCurrencyPair accountCurrencyPair : pairs) {
                            if (accountCurrencyPair.getAccount().getId().equals(account.getId()) &&
                                    accountCurrencyPair.getCurrency().getId().equals(currency.getId())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            AccountCurrencyPair accountCurrencyPair = new AccountCurrencyPair();
                            accountCurrencyPair.setAccount(account);
                            accountCurrencyPair.setCurrency(currency);
                            pairs.add(accountCurrencyPair);
                        }
                    }
                }
            }
        }
        for (int i=0; i<pairs.size(); i++)
            result.add(pairs.get(i).getAccount().getName() + ", "+pairs.get(i).getCurrency().getAbbr());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spiner_gravity_right2,
                result);
        toolbarManager.getSpinner().setAdapter(arrayAdapter);
        if (pairs.isEmpty()){
            toolbarManager.setTitle(getString(R.string.report_by_account_title));
        } else {
            toolbarManager.setTitle("");
        }
        toolbarManager.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                account = pairs.get(position).getAccount();
                currency = pairs.get(position).getCurrency();
                onCreateReportbyAccount(begin, end, account, currency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        tbReportByAccount = (TableView) rootView.findViewById(R.id.tbReportByAccount);
        tvReportByAccountNoDatas = (TextView) rootView.findViewById(R.id.tvReportByAccountNoDatas);
        titles = rootView.getResources().getStringArray(R.array.report_by_account_titles);
        String tekwir = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("report_shp", "");
        if (!tekwir.matches("")) {
            int position = 0;
            for (String temp : result) {
                if (temp.equals(tekwir)) {
                    try {
                        toolbarManager.getSpinner().setSelection(position);
                    } catch (Exception o) {
                        o.printStackTrace();
                    }
                    break;
                }
                position++;
            }
        }
        tbReportByAccount.setTitle(titles, true);
        return rootView;
    }

    private void initDates() {
        int pos = preferences.getInt("filter_pos", 0);
        begin = Calendar.getInstance();
        end = Calendar.getInstance();
        switch (pos) {
            case 0:
                begin.set(Calendar.DAY_OF_MONTH, 1);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 59);
                break;
            case 1:
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 59);
                begin.add(Calendar.DAY_OF_MONTH, -2);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                break;
            case 2:
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 59);
                begin.add(Calendar.DAY_OF_MONTH, -6);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                break;
            case 3:
            case 4:
            case 5:
                Long begTime = preferences.getLong("filter_begin_time", 0L);
                Long endTime = preferences.getLong("filter_end_time", 0L);
                begin.setTimeInMillis(begTime);
                end.setTimeInMillis(endTime);
                break;
        }
        begin = Calendar.getInstance();
        end = Calendar.getInstance();
        begin.set(Calendar.DAY_OF_YEAR, end.get(Calendar.DAY_OF_YEAR) - 2);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivToolbarMostRight:
                filterDialog.show();
                filterDialog.setOnDateSelectedListener(new FilterSelectable() {
                    @Override
                    public void onDateSelected(Calendar begin, Calendar end) {
                        ReportByAccountFragment.this.begin = (Calendar) begin.clone();
                        ReportByAccountFragment.this.end = (Calendar) end.clone();
                        final List<String> result = new ArrayList<>();
                        List<ReportObject> reportObjects = reportManager.getReportObjects(false, begin, end,
                                Account.class, DebtBorrow.class, CreditDetials.class, SmsParseSuccess.class,
                                FinanceRecord.class);
                        List<Currency> allCurrencies = daoSession.getCurrencyDao().loadAll();
                        List<Account> allAccounts = daoSession.getAccountDao().loadAll();
                        pairs = new ArrayList<>();
                        for (Account account : allAccounts) {
                            for (Currency currency : allCurrencies) {
                                for (ReportObject reportObject : reportObjects) {
                                    if (reportObject.getAccount().getId().equals(account.getId()) &&
                                            reportObject.getCurrency().getId().equals(currency.getId())) {
                                        boolean found = false;
                                        for (AccountCurrencyPair accountCurrencyPair : pairs) {
                                            if (accountCurrencyPair.getAccount().getId().equals(account.getId()) &&
                                                    accountCurrencyPair.getCurrency().getId().equals(currency.getId())) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            AccountCurrencyPair accountCurrencyPair = new AccountCurrencyPair();
                                            accountCurrencyPair.setAccount(account);
                                            accountCurrencyPair.setCurrency(currency);
                                            pairs.add(accountCurrencyPair);
                                        }
                                    }
                                }
                            }
                        }
                        for (int i=0; i<pairs.size(); i++)
                            result.add(pairs.get(i).getAccount().getName() + ", "+pairs.get(i).getCurrency().getAbbr());
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                getContext(),
                                R.layout.spiner_gravity_right2,
                                result);
                        toolbarManager.getSpinner().setAdapter(arrayAdapter);
                        if (pairs.isEmpty()){
                            toolbarManager.setTitle(getString(R.string.report_by_account_title));
                        } else {
                            toolbarManager.setTitle("");
                        }
                    }
                });
                break;
            case R.id.ivToolbarExcel:
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
                break;
        }
    }
    void onCreateReportbyAccount(Calendar begin, Calendar end, Account account, Currency currency) {
        List<ReportObject> objects = reportManager.getReportObjects(false, begin, end,
                Account.class, DebtBorrow.class, SmsParseSuccess.class,
                CreditDetials.class, FinanceRecord.class);;
        if (sortReportByAccount == null)
             sortReportByAccount = new ArrayList<>();
        else
            sortReportByAccount.clear();
        long betweenDays = commonOperations.betweenDays(begin, end);
        for (ReportObject reportObject : objects) {
            if (reportObject.getAccount().getId().equals(account.getId()) &&
                    reportObject.getCurrency().getId().equals(currency.getId())) {
                sortReportByAccount.add(reportObject);
            }
        }
        if (sortReportByAccount.isEmpty()) {
            tbReportByAccount.setVisibility(View.GONE);
            linLayReportByAccountInfo.setVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            tvReportByAccountNoDatas.setVisibility(View.VISIBLE);
            return;
        } else {
            tbReportByAccount.setVisibility(View.VISIBLE);
            linLayReportByAccountInfo.setVisibility(View.VISIBLE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
            tvReportByAccountNoDatas.setVisibility(View.GONE);
        }
        decimalFormat = new DecimalFormat("0.00##");
        double totalIncome = 0.0, totalExpanse = 0.0, totalProfit = 0.0, averageProfit = 0.0;
        String abbr = "";
        for (int i = 0; i < sortReportByAccount.size(); i++) {
            abbr = sortReportByAccount.get(i).getCurrency().getAbbr();
            if (sortReportByAccount.get(i).getType() == 0)
                totalIncome = totalIncome + sortReportByAccount.get(i).getAmount();
            else
                totalExpanse = totalExpanse + sortReportByAccount.get(i).getAmount();
        }
        tbReportByAccount.setDatas((ArrayList<? extends Object>) sortReportByAccount);
        totalProfit = totalIncome - totalExpanse;
        averageProfit = totalProfit / betweenDays;
        tvReportbyAccountTotalIncome.setText(getResources().getString(R.string.report_income_expanse_total_income) + decimalFormat.format(totalIncome) + abbr);
        tvReportbyAccountTotalExpanse.setText(getResources().getString(R.string.report_income_expanse_total_expanse) + decimalFormat.format(totalExpanse) + abbr);
        tvReportbyAccountTotalProfit.setText(getResources().getString(R.string.report_income_expanse_total_profit) + decimalFormat.format(totalProfit) + abbr);
        tvReportbyAccountAverageProfit.setText(getResources().getString(R.string.report_income_expanse_aver_profit) + decimalFormat.format(averageProfit) + abbr);
    }
    class MyComparator implements Comparator<ReportObject> {
        @Override
        public int compare(ReportObject o1, ReportObject o2) {
            return o1.getDate().compareTo(o2.getDate());
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
                File sd = Environment.getExternalStorageDirectory();
                String fname = sd.getAbsolutePath() + "/" +
                        "Pocket Accounter/" +
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
                    String[] labels = getResources().getStringArray(R.array.report_by_account_titles);

                    for (int i = 0; i < labels.length; i++) {
                        Label label = new Label(i, 0, labels[i]);
                        writableSheet.addCell(label);
                    }
                    for (int i = 0; i < sortReportByAccount.size(); i++) {
                        Number type = new Number(0, i + 1, sortReportByAccount.get(i).getType());
                        Label date = new Label(1, i + 1, simpleDateFormat.format(sortReportByAccount.get(i).getDate().getTime()));
                        Number amount = new Number(2, i + 1, sortReportByAccount.get(i).getAmount());
                        Label category = new Label(3, i + 1, sortReportByAccount.get(i).getDescription());
                        writableSheet.addCell(type);
                        writableSheet.addCell(date);
                        writableSheet.addCell(amount);
                        writableSheet.addCell(category);
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