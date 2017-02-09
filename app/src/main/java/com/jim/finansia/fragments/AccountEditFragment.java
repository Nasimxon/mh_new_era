package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.utils.CurrencySpinnerAdapter;
import com.jim.finansia.utils.IconChooseDialog;
import com.jim.finansia.utils.OnIconPickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@SuppressLint("InflateParams")
public class AccountEditFragment extends PABaseInfoFragment implements OnClickListener {
    private Account account;
    private EditText etAccountEditName;
    private ImageView fabAccountIcon;
    private SwitchCompat chbAccountStartSumEnabled;
    private RelativeLayout rlStartSumContainer;
    private RelativeLayout rlStartLimitContainer;
    private EditText etStartMoney;
    private EditText etStartLimit;
    private Spinner spStartMoneyCurrency;
    private SwitchCompat chbAccountNoneZero;
    private SwitchCompat chbAccountEnabledLimit;
    private Spinner spStartLimit;
    private String choosenIcon = "add_icon";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.account_edit_layout, container, false);
        if (toolbarManager != null) {
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new AccountFragment());
                }
            });
            toolbarManager.setTitle(getResources().getString(R.string.addedit));
            toolbarManager.setSubtitle("");
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
            toolbarManager.setOnSecondImageClickListener(this);
        }
        if (getArguments() != null) {
            String accountId = getArguments().getString(AccountFragment.ACCOUNT_ID);
            if (accountId != null) {
                account = daoSession.load(Account.class, accountId);
            }
        }
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            }
        }, 100);
        List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
//        String[] items = new String[currencies.size()];
        ArrayList curName = new ArrayList();
        ArrayList cur = new ArrayList();
        int mainCurrencyPos = 0;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getMain())
                mainCurrencyPos = i;
//            items[i] = currencies.get(i).getAbbr();
            cur.add(currencies.get(i).getAbbr());
            curName.add(currencies.get(i).getName());
        }
//        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, items);
        etAccountEditName = (EditText) rootView.findViewById(R.id.etAccountEditName); // account name
        fabAccountIcon = (ImageView) rootView.findViewById(R.id.fabAccountIcon); // icon chooser
        int resId = getResources().getIdentifier(choosenIcon, "drawable", getContext().getPackageName());
        fabAccountIcon.setImageResource(resId);
        fabAccountIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final IconChooseDialog iconChooseDialog = new IconChooseDialog(getContext());
                if (account != null) iconChooseDialog.setSelectedIcon(account.getIcon());
                iconChooseDialog.setOnIconPickListener(new OnIconPickListener() {
                    @Override
                    public void OnIconPick(String icon) {
                        choosenIcon = icon;
                        int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());

                        fabAccountIcon.setImageResource(resId);
                        iconChooseDialog.setSelectedIcon(icon);
                        iconChooseDialog.dismiss();
                    }
                });
                iconChooseDialog.show();
            }
        });
        chbAccountStartSumEnabled = (SwitchCompat) rootView.findViewById(R.id.chbAccountStartSumEnabled); // start sum
        rlStartLimitContainer = (RelativeLayout) rootView.findViewById(R.id.rlStartLimitContainer);
        chbAccountStartSumEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // for enabling and disabling start sum
                if (isChecked)
                    rlStartSumContainer.setVisibility(View.VISIBLE);
                else
                    rlStartSumContainer.setVisibility(View.GONE);
            }
        });
        chbAccountEnabledLimit = (SwitchCompat) rootView.findViewById(R.id.chbAccountEnabledLimit); // for enabling and disabling account limit
        chbAccountEnabledLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rlStartLimitContainer.setVisibility(View.VISIBLE);
                else
                    rlStartLimitContainer.setVisibility(View.GONE);
            }
        });
        rlStartSumContainer = (RelativeLayout) rootView.findViewById(R.id.rlStartSumContainer);
        rlStartSumContainer.setVisibility(View.GONE);
        etStartMoney = (EditText) rootView.findViewById(R.id.etStartMoney); // start money amount
        spStartMoneyCurrency = (Spinner) rootView.findViewById(R.id.spStartMoneyCurrency); //start money currency
        spStartMoneyCurrency.setAdapter(new CurrencySpinnerAdapter(getContext(), cur,curName ));
        spStartMoneyCurrency.setSelection(mainCurrencyPos);
        etStartLimit = (EditText) rootView.findViewById(R.id.etStartLimit); //limit amount
        spStartLimit = (Spinner) rootView.findViewById(R.id.spStartLimitCurrency); //limit currency
        spStartLimit.setAdapter(new CurrencySpinnerAdapter(getContext(), cur,curName ));
        spStartLimit.setSelection(mainCurrencyPos);
        rootView.findViewById(R.id.checkBoxSum).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chbAccountStartSumEnabled.toggle();
            }
        });


        rootView.findViewById(R.id.turnOnLimit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chbAccountEnabledLimit.toggle();
            }
        });
        chbAccountNoneZero = (SwitchCompat) rootView.findViewById(R.id.noneZeroAccount); // none minus account's checkbox
        rootView.findViewById(R.id.rlLimitContainer).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chbAccountNoneZero.toggle();
            }
        });
        if (account != null) { // fill, if account is editing
            etAccountEditName.setText(account.getName());
            resId = getResources().getIdentifier(account.getIcon(), "drawable", getContext().getPackageName());

            choosenIcon = account.getIcon();
            fabAccountIcon.setImageResource(resId);
            chbAccountNoneZero.setChecked(account.getNoneMinusAccount());
            if (account.getAmount() != 0) {
                chbAccountStartSumEnabled.setChecked(true);
                rlStartSumContainer.setVisibility(View.VISIBLE);
                etStartMoney.setText(Double.toString(account.getAmount()));
                for (int i = 0; i < currencies.size(); i++)
                    if (currencies.get(i).getId().matches(account.getStartMoneyCurrency().getId())) {
                        spStartMoneyCurrency.setSelection(i);
                        break;
                    }
            }
            if (account.getIsLimited()) {
                chbAccountEnabledLimit.setChecked(true);
                etStartLimit.setText("" + account.getLimite());
                for (int i = 0; i < currencies.size(); i++) {
                    if (currencies.get(i).getId().equals(account.getLimitCurId())) {
                        spStartLimit.setSelection(i);
                        break;
                    }
                }
            }
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivToolbarMostRight:
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (etAccountEditName.getText().toString().matches("")) {
                    etAccountEditName.setError(getString(R.string.enter_name_error));
                    return;
                }
                Account account;
                if (this.account == null) {
                    account = new Account();
                    account.setId(UUID.randomUUID().toString());
                    account.setCalendar(Calendar.getInstance());
                } else
                    account = this.account;
                account.setName(etAccountEditName.getText().toString());

                if (!etStartMoney.getText().toString().matches("")) {
                    try {
                        Double.parseDouble(etStartMoney.getText().toString());
                        etStartMoney.setError(null);
                        account.setAmount(Double.parseDouble(etStartMoney.getText().toString()));
                    } catch (Exception e) {
                        etStartMoney.setError(getString(R.string.wrong_input_type));
                        return;
                    }

                }else
                    account.setAmount(0);

                if (chbAccountEnabledLimit.isChecked()) {
                    if (etStartLimit.getText().toString().equals("")) {
                        etStartLimit.setError(getResources().getString(R.string.enter_amount_error));
                        return;
                    }
                    try {
                        Double.parseDouble(etStartLimit.getText().toString());
                    } catch (Exception e) {
                        etStartLimit.setError(getString(R.string.wrong_input_type));
                        return;
                    }
                    if (this.account != null) {
                        double limit = logicManager.isLimitAccess(this.account, Calendar.getInstance());
                        double limitSum = Double.parseDouble(etStartLimit.getText().toString());
                        if (this.account.getLimitCurId() != null) {
                            Currency limitCurrency = daoSession.getCurrencyDao().queryBuilder()
                                    .where(CurrencyDao.Properties.Id.eq(this.account.getLimitCurId())).list().get(0);
                            if (limit + commonOperations.getCost(Calendar.getInstance(), this.account.getStartMoneyCurrency(), this.account.getAmount()) <
                                    -(commonOperations.getCost(Calendar.getInstance(), limitCurrency, limitSum))) {
                                Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        account.setIsLimited(true);
                        account.setLimite(Double.parseDouble(etStartLimit.getText().toString()));
                    }
                    List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
                    account.setLimitCurId(currencies.get(spStartLimit.getSelectedItemPosition()).getId());
                } else {
                    account.setIsLimited(false);
                }
                account.setStartMoneyCurrency(daoSession.getCurrencyDao().loadAll()
                        .get(spStartMoneyCurrency.getSelectedItemPosition()));
                account.setIcon(choosenIcon);
                if (account != null && chbAccountNoneZero.isChecked()) {
                    double limit = logicManager.isLimitAccess(account, Calendar.getInstance());
                    if (limit < 0) {
                        Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                account.setNoneMinusAccount(chbAccountNoneZero.isChecked());
                if (this.account != null) {
                    daoSession.getAccountDao().insertOrReplace(account);
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new AccountFragment());
                } else {
                    if (logicManager.insertAccount(account) == LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS) {
                        etAccountEditName.setError(getString(R.string.such_account_name_exists_error));
                        return;
                    } else {
                        paFragmentManager.getFragmentManager().popBackStack();
                        paFragmentManager.displayFragment(new AccountFragment());
                    }
                }
                reportManager.clearCache();
                reportManager.refreshDatas();
                dataCache.updateAllPercents();
                paFragmentManager.updateAllFragmentsPageChanges();
                paFragmentManager.updateTemplatesInVoiceRecognitionFragment();
                break;
        }
    }
    @Override
    void refreshList() {

    }
}
