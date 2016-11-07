package com.jim.pocketaccounter.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.AccountOperation;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.Purpose;
import com.jim.pocketaccounter.finance.TransferAccountAdapter;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by DEV on 06.09.2016.
 */

public class TransferDialog extends Dialog {
    @Inject
    LogicManager logicManager;
    @Inject
    DaoSession daoSession;
    @Inject
    DatePicker datePicker;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    ReportManager reportManager;
    private View dialogView;
    private EditText etAccountEditName;
    private ImageView spTransferFirst, spTransferSecond;
    private TransferAccountAdapter firstAdapter, secondAdapter;
    private Spinner spAccManDialog;
    private List<Currency> currencies;
    private ImageView  ivAccountManClose;
    private TextView ivYes;
    private TextView date;
    private Calendar calendar;
    private OnTransferDialogSaveListener onTransferDialogSaveListener;
    private List<String> first, second;
    private AccountOperation accountOperation;
    public TransferDialog(Context context) {
        super(context);
        if (!context.getClass().getName().equals(PocketAccounter.class.getName()))
            ((PocketAccounter) ((ContextThemeWrapper) context).getBaseContext()).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        else
            ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        dialogView = getLayoutInflater().inflate(R.layout.account_transfer_dialog, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        etAccountEditName = (EditText) dialogView.findViewById(R.id.etAccountEditName);
        spTransferFirst = (ImageView) dialogView.findViewById(R.id.spTransferFirst);
        spTransferSecond = (ImageView) dialogView.findViewById(R.id.spTransferSecond);
        spAccManDialog = (Spinner) dialogView.findViewById(R.id.spAccManDialog);
        date = (TextView) dialogView.findViewById(R.id.tvAccountDialogDate);
        currencies = daoSession.getCurrencyDao().loadAll();
        String[] currs = new String[currencies.size()];
        for (int i = 0; i < currencies.size(); i++)
            currs[i] = currencies.get(i).getAbbr();
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, currs);
        spAccManDialog.setAdapter(currencyAdapter);
        ivYes = (TextView) dialogView.findViewById(R.id.ivAccountManSave);
        ivYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (etAccountEditName.getText().toString().isEmpty()) {
                    etAccountEditName.setError(getContext().getResources().getString(R.string.enter_amount_error));
                    return;
                }
//                String firstId = first.get(spTransferFirst.getSelectedItemPosition());
//                String secondId = second.get(spTransferSecond.getSelectedItemPosition());
//                if (firstId.equals(secondId)) {
//                    Toast.makeText(getContext(), R.string.choose_different_accounts, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                List<Account> accounts = daoSession.getAccountDao().queryBuilder().where(AccountDao.Properties.Id.eq(firstId)).list();
//                if (!accounts.isEmpty()) {
//                    Account account = accounts.get(0);
//                    if (account.getIsLimited()) {
//                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
//                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
//                        if (limitAccess - amount < -account.getLimite()) {
//                            Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
//                    if (account.getNoneMinusAccount()) {
//                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
//                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
//                        if (limitAccess + amount < 0) {
//                            Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
//                }
                if (accountOperation == null)
                    accountOperation = new AccountOperation();
                accountOperation.setAmount(Double.parseDouble(etAccountEditName.getText().toString()));
                accountOperation.setCurrency(currencies.get(spAccManDialog.getSelectedItemPosition()));
                accountOperation.setDate(calendar);
//                accountOperation.setSourceId(first.get(spTransferFirst.getSelectedItemPosition()));
//                accountOperation.setTargetId(second.get(spTransferSecond.getSelectedItemPosition()));
                accountOperation.__setDaoSession(daoSession);
                logicManager.insertAccountOperation(accountOperation);
                dismiss();
            }
        });
        ivAccountManClose = (ImageView) dialogView.findViewById(R.id.ivAccountManClose);
        ivAccountManClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        calendar = (Calendar) Calendar.getInstance().clone();
        date.setText(dateFormat.format(calendar.getTime()));
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
                datePicker.setOnDatePickListener(new OnDatePickListener() {
                    @Override
                    public void OnDatePick(Calendar pickedDate) {
                        calendar = (Calendar) pickedDate.clone();
                        date.setText(dateFormat.format(calendar.getTime()));
                    }
                });
            }
        });
    }

    public TransferDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected TransferDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setAccountOperation(AccountOperation accountOperation) {
        this.accountOperation = accountOperation;
        first = new ArrayList<>();
        for (Account account : daoSession.getAccountDao().loadAll())
            first.add(account.getId());
        for (Purpose purpose : daoSession.getPurposeDao().loadAll())
            first.add(purpose.getId());
        second = new ArrayList<>();
        for (String id : first) {
            if (!id.matches(accountOperation.getSourceId())) {
                second.add(id);
            }
        }
        firstAdapter = new TransferAccountAdapter(getContext(), first);
//        spTransferFirst.setAdapter(firstAdapter);
//        spTransferFirst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long pos) {
//                String firstId = first.get(i);
//                second.clear();
//                for (String id : first) {
//                    if (!id.matches(firstId))
//                        second.add(id);
//                }
//                secondAdapter = new TransferAccountAdapter(getContext(), second);
//                spTransferSecond.setAdapter(secondAdapter);
//            }
//
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });
        int firstPos = 0;
        for (int i = 0; i < first.size(); i++) {
            if (first.get(i).equals(accountOperation.getSourceId())) {
                firstPos = i;
                break;
            }
        }
//        spTransferFirst.setSelection(firstPos);
        int secondPos = 0;
        for (int i = 0; i < second.size(); i++) {
            if (second.get(i).equals(accountOperation.getTargetId())) {
                secondPos = i;
                break;
            }
        }
//        spTransferSecond.setSelection(secondPos);
        etAccountEditName.setText(Double.toString(accountOperation.getAmount()));
        int currencyPos = 0;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getId().equals(accountOperation.getCurrencyId())) {
                currencyPos = i;
                break;
            }
        }
        spAccManDialog.setSelection(currencyPos);
    }

    public void setAccountOrPurpose(String id, boolean type) {
        if (id != null) {
            List<String> allTemp = new ArrayList<>();
            first = new ArrayList<>();
            second = new ArrayList<>();
            int selectedPos = 0;

            List<Account> accounts = daoSession.getAccountDao().loadAll();
            for (Account account : accounts) {
                allTemp.add(account.getId());
            }
            List<Purpose> purposes = daoSession.getPurposeDao().loadAll();
            for (Purpose purpose : purposes) {
                allTemp.add(purpose.getId());
            }

            for (int i = 0; i < allTemp.size(); i++) {
                if (allTemp.get(i).matches(id)) {
                    selectedPos = i;
                    break;
                }
            }

            first.addAll(allTemp);
            second.addAll(allTemp);

//            secondAdapter = new TransferAccountAdapter(getContext(), second);
//            spTransferSecond.setAdapter(secondAdapter);
//            firstAdapter = new TransferAccountAdapter(getContext(), first);
//            spTransferFirst.setAdapter(firstAdapter);
//            if (type) {
//                spTransferFirst.setSelection(selectedPos);
//                spTransferSecond.setSelection(selectedPos == 0
//                        ? ((allTemp.size() == 1) ? 0 : 1) : 0);
//            } else {
//                spTransferSecond.setSelection(selectedPos);
//                spTransferFirst.setSelection(selectedPos == 0
//                        ? ((allTemp.size() == 1) ? 0 : 1) : 0);
//            }
        }
    }

    public void setOnTransferDialogSaveListener(OnTransferDialogSaveListener onTransferDialogSaveListener) {
        this.onTransferDialogSaveListener = onTransferDialogSaveListener;
        ivYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (etAccountEditName.getText().toString().isEmpty()) {
                    etAccountEditName.setError(getContext().getResources().getString(R.string.enter_amount_error));
                    return;
                }
//                String firstId = first.get(spTransferFirst.getSelectedItemPosition());
//                String secondId = second.get(spTransferSecond.getSelectedItemPosition());
//                if (firstId.equals(secondId)) {
//                    Toast.makeText(getContext(), R.string.choose_different_accounts, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                List<Account> accounts = daoSession.getAccountDao().queryBuilder().where(AccountDao.Properties.Id.eq(firstId)).list();
//                if (!accounts.isEmpty()) {
//                    Account account = accounts.get(0);
//                    if (account.getIsLimited() || account.getNoneMinusAccount()) {
//                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
//                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
//                        if (limitAccess - amount < -account.getLimite()) {
//                            Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
//                    if (account.getNoneMinusAccount()) {
//                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
//                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
//                        if (limitAccess + amount < 0) {
//                            Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
//                }
                if (accountOperation == null)
                    accountOperation = new AccountOperation();
                accountOperation.setAmount(Double.parseDouble(etAccountEditName.getText().toString()));
                accountOperation.setCurrency(currencies.get(spAccManDialog.getSelectedItemPosition()));
                accountOperation.setDate(calendar);
//                accountOperation.setSourceId(first.get(spTransferFirst.getSelectedItemPosition()));
//                accountOperation.setTargetId(second.get(spTransferSecond.getSelectedItemPosition()));
                logicManager.insertAccountOperation(accountOperation);
                TransferDialog.this.onTransferDialogSaveListener.OnTransferDialogSave();
                accountOperation = null;
                dismiss();

            }
        });
    }

    public interface OnTransferDialogSaveListener {
        void OnTransferDialogSave();
    }
}