package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.database.PurposeDao;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.ReportManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by DEV on 06.09.2016.
 */

public class TransferDialog extends Dialog implements View.OnClickListener {
    @Inject LogicManager logicManager;
    @Inject DaoSession daoSession;
    @Inject DatePicker datePicker;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject ReportManager reportManager;
    private View dialogView;
    private EditText etAccountEditName;
    private ImageView spTransferFirst, spTransferSecond;
    private TransferAccountAdapter firstAdapter, secondAdapter;
    private Spinner spAccManDialog;
    private List<Currency> currencies;
    private ImageView ivAccountManClose;
    private TextView ivYes;
    private TextView date;
    private Calendar calendar;
    private OnTransferDialogSaveListener onTransferDialogSaveListener;
    private List<String> first, second;
    private AccountOperation accountOperation;
    private TextView fromAccount;
    private TextView toAccount;

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
        fromAccount = (TextView) dialogView.findViewById(R.id.tvAccountTransferDialogFrom);
        toAccount = (TextView) dialogView.findViewById(R.id.tvAccountTransferDialogTo);
        date = (TextView) dialogView.findViewById(R.id.tvAccountDialogDate);
        currencies = daoSession.getCurrencyDao().loadAll();
        String[] currs = new String[currencies.size()];
        for (int i = 0; i < currencies.size(); i++)
            currs[i] = currencies.get(i).getAbbr();
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getContext(), R.layout.spiner_gravity_right_transfer, currs);
        spAccManDialog.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.grey_ochrang), PorterDuff.Mode.SRC_ATOP);
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
                logicManager.insertAccountOperation(accountOperation);
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
                    daoSession.getAccountDao().load(chooseAccountFirstId).getIcon(), "draweble", getContext().getPackageName()));
            fromAccount.setText(daoSession.getAccountDao().load(chooseAccountFirstId).getName());
        } else {
            spTransferFirst.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getPurposeDao().load(chooseAccountFirstId).getIcon(), "draweble", getContext().getPackageName()));
            fromAccount.setText(daoSession.getPurposeDao().load(chooseAccountFirstId).getDescription());
        }
        if (daoSession.getAccountDao().load(chooseAccountSecondId) != null) {
            spTransferSecond.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getAccountDao().load(chooseAccountSecondId).getIcon(), "draweble", getContext().getPackageName()));
            toAccount.setText(daoSession.getAccountDao().load(chooseAccountSecondId).getName());
        } else {
            spTransferSecond.setImageResource(getContext().getResources().getIdentifier(
                    daoSession.getPurposeDao().load(chooseAccountSecondId).getIcon(), "draweble", getContext().getPackageName()));
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
            if (!type && daoSession.getPurposeDao().load(id) != null) {
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
        dialogChoose.setContentView(dialogView);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.rvAccountItemDialog);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DialogAdapter dialogAdapter;
        if (v.getId() == spTransferFirst.getId())
            dialogAdapter = new DialogAdapter(true);
        else dialogAdapter = new DialogAdapter(false);

        recyclerView.setAdapter(dialogAdapter);
        dialogChoose.getWindow().setLayout(6 * getContext().getResources().getDisplayMetrics().widthPixels / 10, RelativeLayout.LayoutParams.WRAP_CONTENT);
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