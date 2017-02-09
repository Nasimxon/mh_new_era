package com.jim.finansia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.UserEnteredCalendars;
import com.jim.finansia.finance.CurrencyChooseAdapter;
import com.jim.finansia.utils.WarningDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CurrencyChooseFragment extends PABaseInfoFragment {
    private RecyclerView gvCurrencyChoose;
    private ArrayList<Currency> currencies;
    private boolean[] chbs;
    private WarningDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.currency_choose_fragment, container, false);
        if (toolbarManager != null){
            toolbarManager.setTitle(getResources().getString(R.string.choose_currencies)); // toolbar settings
            toolbarManager.setSubtitle("");
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                        if (frag == null) continue;
                        if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                            CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                            if (currencyFragment != null) currencyFragment.updateToolbar();
                            break;
                        }
                    }
                }
            });
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        }
        dialog = new WarningDialog(getContext());
        gvCurrencyChoose = (RecyclerView) view.findViewById(R.id.gvCurrencyChoose); // gridview for representing currencies
        final String[] baseCurrencies = getResources().getStringArray(R.array.base_currencies); // getting data from resources to creating default currency list
        final String[] baseAbbrs = getResources().getStringArray(R.array.base_abbrs);
        final String[] currIds = getResources().getStringArray(R.array.currency_ids);
        final String[] costs = getResources().getStringArray(R.array.currency_costs);
        chbs = new boolean[baseCurrencies.length];
        List<Currency> allCurrenciesFromDb = daoSession.getCurrencyDao().loadAll();
        for (int i = 0; i < currIds.length; i++) {
            boolean found = false;
            for (int j = 0; j < allCurrenciesFromDb.size(); j++) {
                if (currIds[i].matches(allCurrenciesFromDb.get(j).getId())) {
                    found = true;
                    break;
                }
            }
            chbs[i] = found;
        }
        currencies = new ArrayList<>();
        for (int i = 0; i < baseCurrencies.length; i++) {
            Currency currency = new Currency();
            currency.setAbbr(baseAbbrs[i]);
            currency.setName(baseCurrencies[i]);
            currency.setId(currIds[i]);
            currencies.add(currency);
        }
        CurrencyChooseAdapter adapter = new CurrencyChooseAdapter(getActivity(), currencies, chbs);
        gvCurrencyChoose.setLayoutManager(new GridLayoutManager(getContext(), 3));
        gvCurrencyChoose.setAdapter(adapter);
        toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = false; // must be at least on cell is checked, else show warning toast
                for (int i = 0; i < chbs.length; i++) {
                    if (chbs[i]) {
                        checked = true;
                        break;
                    }
                }
                if (!checked) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.curr_not_choosen), Toast.LENGTH_SHORT).show(); // toast for denying
                    return;
                }
                final List<Currency> checkedCurrencies = new ArrayList<>(); // accumulating all checked cell from gridView
                for (int i=0; i < chbs.length; i++) {
                    if (chbs[i]) {
                        checkedCurrencies.add(currencies.get(i));
                    }
                }

                boolean isCurrencyListChanged = false; // checking for the some of an old currency is not checked
                final List<Currency> dbCurrencies = daoSession.getCurrencyDao().loadAll();
                for (Currency currency : dbCurrencies) {
                    boolean found = false;
                    for (Currency curr : checkedCurrencies) {
                        if (curr.getId().equals(currency.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        isCurrencyListChanged = true;
                        break;
                    }
                }
                String temp = Locale.getDefault().getCountry() + " ";
                for (Currency currency : checkedCurrencies) {
                    temp += currency.getAbbr() + ":";
                }
                analytics.sendText(temp);
                if (isCurrencyListChanged) { // if has not checked some of an old currencies
                    dialog.setText(getResources().getString(R.string.currency_exchange_warning));
                    dialog.setOnYesButtonListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<Currency> dbCurrs = daoSession.getCurrencyDao().loadAll();
                            for (Currency currency : checkedCurrencies) {
                                boolean found = false;
                                for (Currency curr : dbCurrs) {
                                    if (currency.getId().equals(curr.getId())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    int pos = 0;
                                    for (int i=0; i<currencies.size(); i++) {
                                        if (currency.getId().equals(currencies.get(i).getId())) {
                                            pos = i;
                                            break;
                                        }
                                    }
                                    UserEnteredCalendars userEnteredCalendars = new UserEnteredCalendars();
                                    userEnteredCalendars.setCurrencyId(currency.getId());
                                    userEnteredCalendars.setCalendar(Calendar.getInstance());
                                    daoSession.getUserEnteredCalendarsDao().insertOrReplace(userEnteredCalendars);
                                    daoSession.getCurrencyDao().insertOrReplace(currency);
                                    logicManager.generateCurrencyCosts(Calendar.getInstance(), Double.parseDouble(costs[pos]), currency);
                                }
                            }
                            daoSession.getCurrencyDao().detachAll();
                            List<Currency> currencies = new ArrayList<>();
                            for (Currency currency : dbCurrencies) {
                                boolean found = false;
                                for (Currency curr : checkedCurrencies) {
                                    if (curr.getId().equals(currency.getId())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found)
                                    currencies.add(currency);
                            }
                            logicManager.deleteCurrency(currencies);
                            dialog.dismiss();
                            for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                                if (frag == null) continue;
                                if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                                    CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                                    if (currencyFragment != null) {
                                        currencyFragment.updateToolbar();
                                        currencyFragment.refreshList();
                                    }
                                    break;
                                }
                            }
                            paFragmentManager.getFragmentManager().popBackStack();
                        }
                    });
                    dialog.setOnNoButtonClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else { // all old currencies are present
                    for (Currency currency : checkedCurrencies) {
                        boolean found = false;
                        List<Currency> dbCurrs = daoSession.getCurrencyDao().loadAll();
                        for (Currency curr : dbCurrs) {
                            if (currency.getId().matches(curr.getId())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            int pos = 0;
                            for (int i=0; i<currencies.size(); i++) {
                                if (currency.getId().equals(currencies.get(i).getId())) {
                                    pos = i;
                                    break;
                                }
                            }
                            UserEnteredCalendars userEnteredCalendars = new UserEnteredCalendars();
                            userEnteredCalendars.setCurrencyId(currency.getId());
                            userEnteredCalendars.setCalendar(Calendar.getInstance());
                            daoSession.getUserEnteredCalendarsDao().insertOrReplace(userEnteredCalendars);
                            daoSession.getCurrencyDao().insertOrReplace(currency);
                            logicManager.generateCurrencyCosts(Calendar.getInstance(), Double.parseDouble(costs[pos]), currency);
                        }
                    }
                    for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                        if (frag == null) continue;
                        if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                            CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                            if (currencyFragment != null) {
                                currencyFragment.updateToolbar();
                                currencyFragment.refreshList();
                            }
                            break;
                        }
                    }
                    paFragmentManager.getFragmentManager().popBackStack();
            }
            }
        });
        return view;
    }

    @Override
    void refreshList() {

    }
}