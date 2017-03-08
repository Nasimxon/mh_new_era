package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.AccountOperation;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyCost;
import com.jim.finansia.database.CurrencyCostState;
import com.jim.finansia.database.CurrencyCostStateDao;
import com.jim.finansia.database.CurrencyWithAmount;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.database.PurposeDao;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.ReportManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class TransferDialog extends Dialog implements View.OnClickListener {
    @Inject LogicManager logicManager;
    @Inject DaoSession daoSession;
    @Inject DatePicker datePicker;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject ReportManager reportManager;
    private View dialogView;
    private EditText etAccountEditName, etAccountTargitAmount, etCost;
    private ImageView spTransferFirst, spTransferSecond;
    private Spinner spAccManDialog, spTargetCurrencyId;
    private List<Currency> currencies;
    private ImageView ivAccountManClose;
    private TextView ivYes;
    private TextView date;
    private Calendar calendar;
    private OnTransferDialogSaveListener onTransferDialogSaveListener;
    private AccountOperation accountOperation;
    private TextView fromAccount;
    private TextView toAccount;
    private TextView currentCurrency, exchangeCurrency;
    private DecimalFormat format = new DecimalFormat("0.######");
    private boolean sourceChanged = false, targetChanged = false, costChanged = false, spinnerChanged = false;
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
        currencies = daoSession.getCurrencyDao().loadAll();
        etAccountEditName = (EditText) dialogView.findViewById(R.id.etAccountEditName);
        currentCurrency = (TextView) dialogView.findViewById(R.id.tvCurrentCurrencyAbr);
        exchangeCurrency = (TextView) dialogView.findViewById(R.id.tvExchangeCurrencyAbr);
        etAccountEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (!spinnerChanged && !targetChanged && !costChanged) {
                        sourceChanged = true;
                        etAccountTargitAmount.setText(format.format(Double.parseDouble(charSequence.toString().replace(",", "."))
                                * Double.parseDouble(etCost.getText().toString().replace(",", "."))));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sourceChanged = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etAccountTargitAmount = (EditText) dialogView.findViewById(R.id.etAccountTargitAmount);
        etAccountTargitAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (!spinnerChanged && !sourceChanged && !costChanged) {
                        targetChanged = true;
                        etAccountEditName.setText(format.format(Double.parseDouble(charSequence.toString().replace(",", "."))
                                / Double.parseDouble(etCost.getText().toString().replace(",", "."))));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    targetChanged = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etCost = (EditText) dialogView.findViewById(R.id.etCost);
        etCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty() || charSequence.toString().equals("0"))
                    etCost.setText("1");
                try {
                    if (!spinnerChanged && !targetChanged && !sourceChanged) {
                        costChanged = true;
                       etAccountTargitAmount.setText(format.format(Double.parseDouble(charSequence.toString().replace(",", "."))
                            * Double.parseDouble(etAccountEditName.getText().toString().replace(",", "."))));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    costChanged = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        spTransferFirst = (ImageView) dialogView.findViewById(R.id.spTransferFirst);
        spTransferSecond = (ImageView) dialogView.findViewById(R.id.spTransferSecond);
        spAccManDialog = (Spinner) dialogView.findViewById(R.id.spAccManDialog);
        spTargetCurrencyId = (Spinner) dialogView.findViewById(R.id.spTargetCurrencyId);
        fromAccount = (TextView) dialogView.findViewById(R.id.tvAccountTransferDialogFrom);
        toAccount = (TextView) dialogView.findViewById(R.id.tvAccountTransferDialogTo);
        date = (TextView) dialogView.findViewById(R.id.tvAccountDialogDate);

        ArrayList currs = new ArrayList();
        ArrayList currsName = new ArrayList();
        int main_currency_index = -1;
        for (int i = 0; i < currencies.size(); i++){
            currs.add(currencies.get(i).getAbbr());
            if(currencies.get(i).getIsMain()){
                main_currency_index = i;
            }
            currsName.add(currencies.get(i).getName());
        }
        currentCurrency.setText(currencies.get(main_currency_index).getAbbr());
        exchangeCurrency.setText(currencies.get(main_currency_index).getAbbr());
        spAccManDialog.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.grey_ochrang), PorterDuff.Mode.SRC_ATOP);
        spAccManDialog.setAdapter(new CurrencySpinnerAdapter(getContext(),currs, currsName));
        spAccManDialog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    spinnerChanged = true;
                    Currency fromCurrency = currencies.get(spAccManDialog.getSelectedItemPosition());
                    Currency toCurrency = currencies.get(spTargetCurrencyId.getSelectedItemPosition());
                    currentCurrency.setText(currencies.get(spAccManDialog.getSelectedItemPosition()).getAbbr());
                    exchangeCurrency.setText(currencies.get(spTargetCurrencyId.getSelectedItemPosition()).getAbbr());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateFormat.parse(date.getText().toString()));
                    double cost = getCost(calendar, fromCurrency, toCurrency);
                    etCost.setText(format.format(cost));
                    if (etAccountEditName.getText().toString().isEmpty())
                        etAccountEditName.setText("0");
                    if (etAccountTargitAmount.getText().toString().isEmpty())
                        etAccountTargitAmount.setText("0");
                    double amount = Double.parseDouble(etCost.getText().toString().replace(",", ".")) * Double.parseDouble(etAccountEditName.getText().toString().replace(",", "."));
                    etAccountTargitAmount.setText(format.format(amount));
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    spinnerChanged = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spTargetCurrencyId.setAdapter(new CurrencySpinnerAdapter(getContext(),currs, currsName));
        spTargetCurrencyId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    spinnerChanged = true;
                    Currency fromCurrency = currencies.get(spAccManDialog.getSelectedItemPosition());
                    Currency toCurrency = currencies.get(spTargetCurrencyId.getSelectedItemPosition());
                    currentCurrency.setText(currencies.get(spAccManDialog.getSelectedItemPosition()).getAbbr());
                    exchangeCurrency.setText(currencies.get(spTargetCurrencyId.getSelectedItemPosition()).getAbbr());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateFormat.parse(date.getText().toString()));
                    double cost = getCost(calendar, fromCurrency, toCurrency);
                    etCost.setText(format.format(cost));
                    if (etAccountEditName.getText().toString().isEmpty())
                        etAccountEditName.setText("0");
                    if (etAccountTargitAmount.getText().toString().isEmpty())
                        etAccountTargitAmount.setText("0");
                    double amount = Double.parseDouble(etCost.getText().toString().replace(",", ".")) * Double.parseDouble(etAccountEditName.getText().toString().replace(",", "."));
                    etAccountTargitAmount.setText(format.format(amount));
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    spinnerChanged = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        if (main_currency_index!=-1){
            spAccManDialog.setSelection(main_currency_index);
            spTargetCurrencyId.setSelection(main_currency_index);
        }
        ivYes = (TextView) dialogView.findViewById(R.id.ivAccountManSave);
        ivYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (etAccountEditName.getText().toString().isEmpty() || etAccountEditName.getText().toString().equals("0")) {
                    etAccountEditName.setError(getContext().getResources().getString(R.string.enter_amount_error));
                    return;
                }
                if (etAccountTargitAmount.getText().toString().isEmpty() || etAccountTargitAmount.getText().toString().equals("0")) {
                    etAccountTargitAmount.setError(getContext().getResources().getString(R.string.enter_amount_error));
                    return;
                }
                if (etCost.getText().toString().isEmpty()) {
                    etCost.setError(getContext().getResources().getString(R.string.enter_amount_error));
                    return;
                }
                if (chooseAccountFirstId.equals(chooseAccountSecondId)) {
                    Toast.makeText(getContext(), R.string.choose_different_accounts, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Account> accounts = daoSession.getAccountDao().queryBuilder().where
                        (AccountDao.Properties.Id.eq(chooseAccountFirstId)).list();
                if (!accounts.isEmpty()) {
                    Account account = accounts.get(0);
                    if (account.getIsLimited()) {
                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
                        if (limitAccess - amount < -account.getLimite()) {
                            Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (account.getNoneMinusAccount()) {
                        Double limitAccess = logicManager.isLimitAccess(account, calendar);
                        Double amount = Double.parseDouble(etAccountEditName.getText().toString());
                        if (limitAccess - amount < 0) {
                            Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if (accountOperation == null)
                    accountOperation = new AccountOperation();
                accountOperation.setAmount(Double.parseDouble(etAccountEditName.getText().toString()));
                accountOperation.setCurrency(currencies.get(spAccManDialog.getSelectedItemPosition()));
                accountOperation.setDate(calendar);
                accountOperation.setSourceId(chooseAccountFirstId);
                accountOperation.setTargetId(chooseAccountSecondId);
                accountOperation.setTargetCurrency(currencies.get(spTargetCurrencyId.getSelectedItemPosition()));
                accountOperation.setTargetAmount(Double.parseDouble(etAccountTargitAmount.getText().toString().replace(",", ".")));
                accountOperation.setCost(Double.parseDouble(etCost.getText().toString().replace(",", ".")));
                logicManager.insertAccountOperation(accountOperation);
                reportManager.refreshDatas();
                if (onTransferDialogSaveListener != null)
                    onTransferDialogSaveListener.OnTransferDialogSave();
                accountOperation = null;
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
        spTransferFirst.setOnClickListener(this);
        spTransferSecond.setOnClickListener(this);
    }

    public TransferDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected TransferDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setEditAccountPurpose (AccountOperation accountOperation) {
        this.accountOperation = accountOperation;
        chooseAccountFirstId = accountOperation.getSourceId();
        chooseAccountSecondId = accountOperation.getTargetId();
        if (daoSession.getAccountDao().load(chooseAccountFirstId) != null) {
            spTransferFirst.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getAccountDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            fromAccount.setText(daoSession.getAccountDao().load(chooseAccountFirstId).getName());
        } else {
            spTransferFirst.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getPurposeDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            fromAccount.setText(daoSession.getPurposeDao().load(chooseAccountFirstId).getDescription());
        }
        if (daoSession.getAccountDao().load(chooseAccountSecondId) != null) {
            spTransferSecond.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getAccountDao().load(chooseAccountSecondId).getIcon(), "drawable", getContext().getPackageName()));
            toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
        } else {
            spTransferSecond.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getPurposeDao().load(chooseAccountSecondId).getIcon(), "drawable", getContext().getPackageName()));
            toAccount.setText(daoSession.getPurposeDao().load(chooseAccountSecondId).getDescription());
        }
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

            //type
            //true - purpose
            //false - account
            //ne logichno ne xuya

            if (!type  && daoSession.getPurposeDao().load(id) != null) {
                chooseAccountSecondId = id;
                chooseAccountFirstId = daoSession.getAccountDao().queryBuilder().
                        where(AccountDao.Properties.Id.notEq(id)).list().get(0).getId();
            } else {
                chooseAccountFirstId = id;
                chooseAccountSecondId = daoSession.getAccountDao().queryBuilder().
                        where(AccountDao.Properties.Id.notEq(id)).list().get(0).getId();
                spTransferSecond.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getAccountDao().load(chooseAccountSecondId).getIcon(), "drawable", getContext().getPackageName()));
            }
            if (!type && daoSession.getPurposeDao().load(chooseAccountFirstId) != null) {
                spTransferFirst.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getAccountDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            } else if (type && daoSession.getPurposeDao().load(chooseAccountFirstId) != null) {
                spTransferFirst.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getPurposeDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            } else if (!type && daoSession.getPurposeDao().load(chooseAccountSecondId) != null) {
                spTransferSecond.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getPurposeDao().load(chooseAccountSecondId).getIcon(), "drawable", getContext().getPackageName()));
                spTransferFirst.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getAccountDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            } else {
                spTransferFirst.setImageResource(getContext().getResources().getIdentifier
                        (daoSession.getAccountDao().load(chooseAccountFirstId).getIcon(), "drawable", getContext().getPackageName()));
            }

            if (!type && daoSession.getAccountDao().load(chooseAccountFirstId) != null) {
                fromAccount.setText(daoSession.getAccountDao().load(chooseAccountFirstId).getName());
                if (daoSession.getPurposeDao().load(chooseAccountSecondId) != null)
                    toAccount.setText(daoSession.getPurposeDao().load(chooseAccountSecondId).getDescription());
                else
                    toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
            } else if (daoSession.getPurposeDao().load(chooseAccountFirstId) != null) {
                fromAccount.setText(daoSession.getPurposeDao().load(chooseAccountFirstId).getDescription());
                toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
            } else {
                fromAccount.setText(daoSession.getAccountDao().load(chooseAccountFirstId).getName());
                toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
            }
        }
    }

    public void setOnTransferDialogSaveListener(OnTransferDialogSaveListener onTransferDialogSaveListener) {
        this.onTransferDialogSaveListener = onTransferDialogSaveListener;
    }

    public interface OnTransferDialogSaveListener {
        void OnTransferDialogSave();
    }

    private Dialog dialogChoose;
    private String chooseAccountFirstId = "";
    private String chooseAccountSecondId = "";

    @Override
    public void onClick(View v) {
        dialogChoose = new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.account_dialog_layout, null);
        dialogChoose.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogChoose.setContentView(dialogView);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.rvAccountItemDialog);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DialogAdapter dialogAdapter;
        if (v.getId() == spTransferFirst.getId())
            dialogAdapter = new DialogAdapter(true);
        else dialogAdapter = new DialogAdapter(false);

        recyclerView.setAdapter(dialogAdapter);
        dialogChoose.getWindow().setLayout(8 * getContext().getResources().getDisplayMetrics().widthPixels / 10, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialogChoose.show();
    }

    private class DialogAdapter extends RecyclerView.Adapter<DialogViewHolder> {
        private List<Object> list;
        private boolean isFirst;

        public DialogAdapter(boolean isFirst) {
            this.isFirst = isFirst;
            list = new ArrayList<>();
            if (isFirst) {
                list.addAll(daoSession.getAccountDao().queryBuilder().where(
                        AccountDao.Properties.Id.notEq(chooseAccountSecondId)).list());
                list.addAll(daoSession.getPurposeDao().queryBuilder().where(
                        PurposeDao.Properties.Id.notEq(chooseAccountSecondId)).list());
            } else {
                list.addAll(daoSession.getAccountDao().queryBuilder().where(
                        AccountDao.Properties.Id.notEq(chooseAccountFirstId)).list());
                list.addAll(daoSession.getPurposeDao().queryBuilder().where(
                        PurposeDao.Properties.Id.notEq(chooseAccountFirstId)).list());
            }
        }

        @Override
        public void onBindViewHolder(DialogViewHolder holder, final int position) {
            if (list.get(position).getClass().getName().equals(Account.class.getName())) {
                final Account account = (Account) list.get(position);
                holder.textView.setText(account.getName());
                holder.imageView.setImageResource(getContext().getResources().getIdentifier(account.getIcon(), "drawable", getContext().getPackageName()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isFirst) {
                            chooseAccountFirstId = account.getId();
                            spTransferFirst.setImageResource(getContext().getResources().
                                    getIdentifier(account.getIcon(), "drawable", getContext().getPackageName()));
                            fromAccount.setText(daoSession.getAccountDao().load(chooseAccountFirstId).getName());
                        } else {
                            chooseAccountSecondId = account.getId();
                            spTransferSecond.setImageResource(getContext().getResources().
                                    getIdentifier(account.getIcon(), "drawable", getContext().getPackageName()));
                            toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
                        }
                        dialogChoose.dismiss();
                    }
                });
            } else {
                final Purpose purpose = (Purpose) list.get(position);
                holder.textView.setText(purpose.getDescription());
                holder.imageView.setImageResource(getContext().getResources().getIdentifier(purpose.getIcon(), "drawable", getContext().getPackageName()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isFirst) {
                            chooseAccountFirstId = purpose.getId();
                            spTransferFirst.setImageResource(getContext().getResources().
                                    getIdentifier(purpose.getIcon(), "drawable", getContext().getPackageName()));
                            fromAccount.setText(purpose.getDescription());
                        } else {
                            chooseAccountSecondId = purpose.getId();
                            spTransferSecond.setImageResource(getContext().getResources().
                                    getIdentifier(purpose.getIcon(), "drawable", getContext().getPackageName()));
                            toAccount.setText(purpose.getDescription());
                        }
                        dialogChoose.dismiss();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public DialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_account_dialog, parent, false);
            return new DialogViewHolder(view);
        }
    }

    private double getCost(Calendar date, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.getId().equals(toCurrency.getId())) return 1.0d;
        List<CurrencyCostState> states = daoSession
                                    .queryBuilder(CurrencyCostState.class)
                                    .where(CurrencyCostStateDao.Properties.MainCurId.eq(fromCurrency.getId()))
                                    .list();
        if (date.compareTo(states.get(states.size() - 1).getDay()) >= 0) {
            for (CurrencyWithAmount amount : states.get(states.size() - 1).getCurrencyWithAmountList()) {
                if (amount.getCurrencyId().equals(toCurrency.getId()))
                    return amount.getAmount();
            }
        }
        else if (date.compareTo(states.get(0).getDay()) >= 0) {
            for (CurrencyWithAmount amount : states.get(0).getCurrencyWithAmountList()) {
                if (amount.getCurrencyId().equals(toCurrency.getId()))
                    return amount.getAmount();
            }
        }
        double result = 1.0d;
        long difference = -1;
        for (CurrencyCostState state : states) {
            if (difference < 0 || date.getTimeInMillis() - state.getDay().getTimeInMillis() < difference) {
                for (CurrencyWithAmount amount : state.getCurrencyWithAmountList()) {
                    if (amount.getCurrencyId().equals(toCurrency.getId()))
                        result =  amount.getAmount();
                }
                difference = date.getTimeInMillis() - state.getDay().getTimeInMillis();
            }
        }
        return result;
    }

    private class DialogViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public DialogViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.ivAccountItemDialog);
            textView = (TextView) itemView.findViewById(R.id.tvAccountItemDialog);
        }
    }
}