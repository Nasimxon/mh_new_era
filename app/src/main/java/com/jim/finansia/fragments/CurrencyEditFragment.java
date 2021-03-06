package com.jim.finansia.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout.LayoutParams;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyCost;
import com.jim.finansia.database.UserEnteredCalendars;
import com.jim.finansia.finance.CurrencyExchangeAdapter;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.utils.GetterAttributColors;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CurrencyEditFragment extends PABaseInfoFragment implements OnClickListener, OnItemClickListener {
    private LinearLayout ivExCurrencyAdd;
    private RecyclerView lvCurrencyEditExchange;
    private Currency currency;
    private TextView tvAbrValyuti, tvNameValyute, tvCurrentCurrencyRate;
    private ImageView chbCurrencyEditMainCurrency;
    private TextView textColorMain;
    private LinearLayout linearLayoutCheck;
    private Calendar day = Calendar.getInstance();
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private boolean[] selected;
    private boolean isCheckedMain = false;

    public static CurrencyEditFragment newInstance(Currency currrency) {
        Bundle bundle = new Bundle();
        if (currrency != null)
            bundle.putString(CurrencyFragment.CURRENCY_ID, currrency.getId());
        CurrencyEditFragment fragment = new CurrencyEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        analytics.sendText("User enters " + getClass().getName());
        if (getArguments() != null) {
            String currencyId = getArguments().getString(CurrencyFragment.CURRENCY_ID);
            if (currencyId != null)
                currency = daoSession.load(Currency.class, currencyId);
        }
        View rootView = inflater.inflate(R.layout.currency_edit_modern, container, false);
        if (toolbarManager != null) {
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                        if (frag == null) continue;
                        if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                            CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                            if (currencyFragment != null) {
                                currencyFragment.updateToolbar();
                                paFragmentManager.getFragmentManager().popBackStack();
                            }
                            break;
                        }
                    }
                }
            });
            toolbarManager.setTitle(currency.getName());
            toolbarManager.setSubtitle(getResources().getString(R.string.edit));
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setOnSecondImageClickListener(this);
        }
        ivExCurrencyAdd = (LinearLayout) rootView.findViewById(R.id.ivExCurrencyAdd);
        ivExCurrencyAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonOperations.buttonClickCustomAnimation(ivExCurrencyAdd, new CommonOperations.AfterAnimationEnd() {
                    @Override
                    public void onAnimoationEnd() {
                        exchangeEditDialog(null);
                    }
                });
            }
        });
        lvCurrencyEditExchange = (RecyclerView) rootView.findViewById(R.id.lvCurrencyEditExchange);
        tvAbrValyuti = (TextView) rootView.findViewById(R.id.tvAbrValyuti);
        textColorMain = (TextView) rootView.findViewById(R.id.textColorMain);
        tvNameValyute = (TextView) rootView.findViewById(R.id.tvNameValyute);
        tvCurrentCurrencyRate = (TextView) rootView.findViewById(R.id.tvCurrentCurrencyRate);
        chbCurrencyEditMainCurrency = (ImageView) rootView.findViewById(R.id.chbCurrencyEditMainCurrency);
        chbCurrencyEditMainCurrency.setImageResource((currency.getMain() == true) ? R.drawable.blue_background_checked : R.drawable.blue_background_unchecked);
        isCheckedMain = currency.getMain();
        tvAbrValyuti.setText(currency.getAbbr());
        tvNameValyute.setText(currency.getName());
        DecimalFormat decFormat = new DecimalFormat("0.####");
        tvCurrentCurrencyRate.setText("1" + currency.getAbbr() +
                " = " + decFormat.format(currency.getCosts().get(currency.getCosts().size() - 1).getCost()) + commonOperations.getMainCurrency().getAbbr());
        linearLayoutCheck = (LinearLayout) rootView.findViewById(R.id.checkerForClick);
        linearLayoutCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonOperations.buttonClickCustomAnimation(linearLayoutCheck, new CommonOperations.AfterAnimationEnd() {
                    @Override
                    public void onAnimoationEnd() {
                        if (isCheckedMain) {
                            chbCurrencyEditMainCurrency.setImageResource(R.drawable.blue_background_unchecked);
                            chbCurrencyEditMainCurrency.setColorFilter(Color.parseColor("#d1cfd0"));
                            textColorMain.setTextColor(Color.parseColor("#d1cfd0"));
                            isCheckedMain = false;
                        } else {
                            chbCurrencyEditMainCurrency.setImageResource(R.drawable.blue_background_checked);
                            chbCurrencyEditMainCurrency.setColorFilter(GetterAttributColors.fetchHeadAccedentColor(getContext()));
                            textColorMain.setTextColor(GetterAttributColors.fetchHeadAccedentColor(getContext()));
                            isCheckedMain = true;

                        }
                    }
                });

            }
        });
        refreshList();
        return rootView;
    }

    @Override
    void refreshList() {
        currency.resetUserEnteredCalendarses();
        currency.refreshCosts();
        CurrencyExchangeAdapter adapter = new CurrencyExchangeAdapter(new OpenDialog() {
            @Override
            public void openDialogForDate(CurrencyCost currCost) {
                exchangeEditDialog(currCost);
            }
        }, getActivity(),
                (ArrayList<CurrencyCost>) currency.getCosts(), selected, mode, currency.getAbbr());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        lvCurrencyEditExchange.setLayoutManager(layoutManager);
        lvCurrencyEditExchange.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mode == PocketAccounterGeneral.NORMAL_MODE)
            exchangeEditDialog(currency.getCosts().get(position));
        else {
            if (view != null) {
                CheckBox chbCurrencyExchangeListItem = (CheckBox) view.findViewById(R.id.chbCurrencyExchangeListItem);
                chbCurrencyExchangeListItem.setChecked(!chbCurrencyExchangeListItem.isChecked());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivToolbarMostRight:
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (isCheckedMain) {
                    logicManager.setMainCurrency(currency);
                }
                reportManager.clearCache();
                commonOperations.refreshCurrency();
                dataCache.updateAllPercents();
                paFragmentManager.updateAllFragmentsPageChanges();
                paFragmentManager.updateVoiceRecognizePageCurrencyChanges();
                boolean found = false;
                for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                    if (frag == null) continue;
                    if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                        CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                        if (currencyFragment != null) {
                            currencyFragment.updateToolbar();
                            currencyFragment.refreshList();
                            found = true;
                        }
                        break;
                    }
                }
                paFragmentManager.getFragmentManager().popBackStack();
                if (!found) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new CurrencyFragment());
                }
                break;
        }
    }

    private void exchangeEditDialog(final CurrencyCost currCost) {
        final Dialog dialog = new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.exchange_edit_modern, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        final LinearLayout tvExchangeEditDateClick = (LinearLayout) dialogView.findViewById(R.id.tvExchangeEditDate);
        final EditText tvExchangeEditDate = (EditText) dialogView.findViewById(R.id.etDateExchangeRate);
        final EditText etHowMuch = (EditText) dialogView.findViewById(R.id.etHowMuch);
        etHowMuch.setText("" + 1);
        ((TextView) dialogView.findViewById(R.id.currentCurrencyidic)).setText(1 + currency.getAbbr() + " = ");
        ((TextView) dialogView.findViewById(R.id.tvCurrentCurrencyAbr)).setText(commonOperations.getMainCurrency().getAbbr());
        dialog.findViewById(R.id.etDateExchangeRate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.date_picker, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                final DatePicker dp = (DatePicker) dialogView.findViewById(R.id.dp);
                TextView ivDatePickOk = (TextView) dialogView.findViewById(R.id.ivDatePickOk);
                ivDatePickOk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        day.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        tvExchangeEditDate.setText(format.format(day.getTime()));
                        dialog.dismiss();
                    }
                });
                TextView ivDatePickCancel = (TextView) dialogView.findViewById(R.id.ivDatePickCancel);
                ivDatePickCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        tvExchangeEditDateClick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.date_picker, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                final DatePicker dp = (DatePicker) dialogView.findViewById(R.id.dp);
                TextView ivDatePickOk = (TextView) dialogView.findViewById(R.id.ivDatePickOk);
                ivDatePickOk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        day.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        tvExchangeEditDate.setText(format.format(day.getTime()));
                        dialog.dismiss();
                    }
                });
                TextView ivDatePickCancel = (TextView) dialogView.findViewById(R.id.ivDatePickCancel);
                ivDatePickCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        final EditText etExchange = (EditText) dialogView.findViewById(R.id.etExchange);
        final TextView glava = (TextView) dialogView.findViewById(R.id.glava);
        final TextView currentAbr = (TextView) dialogView.findViewById(R.id.glava);
        glava.setText(currency.getAbbr());
        currentAbr.setText(currency.getAbbr());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        final DecimalFormat decFormat = new DecimalFormat("0.####", otherSymbols);
        dialog.findViewById(R.id.deleteButton).setVisibility(View.GONE);

        etExchange.setText(decFormat.format(0.0));
        double cost = 1.0;
        if (currCost != null) {
            dialog.findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
            dialog.findViewById(R.id.deleteButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currency.getUserEnteredCalendarses().size() == 1) {
                        Toast.makeText(getContext(), R.string.must_be_one_currency, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < currency.getUserEnteredCalendarses().size(); i++) {
                        if (dateFormat.format(currCost.getDay().getTime()).equals(dateFormat.format(currency.getUserEnteredCalendarses().get(i).getCalendar().getTime()))) {
                            daoSession.delete(currency.getUserEnteredCalendarses().get(i));
                            daoSession.getCurrencyDao().detachAll();
                            refreshList();
                            dialog.dismiss();

                        }
                    }
                }
            });
            tvExchangeEditDate.setText(dateFormat.format(currCost.getDay().getTime()));
            day = (Calendar) currCost.getDay().clone();
            cost = currCost.getCost();
            ((TextView) dialogView.findViewById(R.id.currentRateBig)).setText(decFormat.format(currCost.getCost()) + commonOperations.getMainCurrency().getAbbr());

        } else if (currency.getCosts().size() != 0) {
            cost = currency.getCosts().get(currency.getCosts().size() - 1).getCost();
            ((TextView) dialogView.findViewById(R.id.currentRateBig)).setText(decFormat.format(cost) + commonOperations.getMainCurrency().getAbbr());
        } else {

            ((TextView) dialogView.findViewById(R.id.currentRateBig)).setText(decFormat.format(1.0) + commonOperations.getMainCurrency().getAbbr());

        }
        tvExchangeEditDate.setText(dateFormat.format(day.getTime()));
        etExchange.setText(decFormat.format(cost));
        TextView ivCurrencyEditDialogOk = (TextView) dialogView.findViewById(R.id.ivCurrencyEditDialogOk);
        ImageView ivCurrencyEditDialogCancel = (ImageView) dialogView.findViewById(R.id.ivCurrencyEditDialogCancel);
        ivCurrencyEditDialogOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double.parseDouble(etExchange.getText().toString().replace(',','.'));
                    etExchange.setError(null);
                } catch (Exception e) {
                    etExchange.setError(getString(R.string.wrong_input_type));
                    return;
                }
                if (etExchange.getText().toString().matches("")) {
                    etExchange.setError(getString(R.string.incorrect_value));
                    return;
                }
                try {
                    Double.parseDouble(etHowMuch.getText().toString().replace(',','.'));
                    etHowMuch.setError(null);
                } catch (Exception e) {
                    etHowMuch.setError(getString(R.string.wrong_input_type));
                    return;
                }
                if (etHowMuch.getText().toString().matches("")) {
                    etHowMuch.setError(getString(R.string.incorrect_value));
                    return;
                }
                if (logicManager.insertUserEnteredCalendars(currency, (Calendar) day.clone()) == LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS) {
                    logicManager.generateCurrencyCosts((Calendar) day.clone(), Double.parseDouble(etExchange.getText().toString().replace(',','.')) / Double.parseDouble(etHowMuch.getText().toString().replace(',','.')), currency);
                } else {
                    logicManager.generateCurrencyCosts((Calendar) day.clone(), Double.parseDouble(etExchange.getText().toString().replace(',','.')) / Double.parseDouble(etHowMuch.getText().toString().replace(',','.')), currency);
                }
                reportManager.clearCache();
                commonOperations.refreshCurrency();
                dataCache.updateAllPercents();
                paFragmentManager.updateAllFragmentsPageChanges();
                paFragmentManager.updateVoiceRecognizePageCurrencyChanges();
                refreshList();
                currency.refreshCosts();
                tvCurrentCurrencyRate.setText("1" + currency.getAbbr() +
                        " = " + decFormat.format(currency.getCosts().get(currency.getCosts().size() - 1).getCost()) + commonOperations.getMainCurrency().getAbbr());

                dialog.dismiss();
            }
        });
        ivCurrencyEditDialogCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(9 * width / 10, LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public interface OpenDialog {
        void openDialogForDate(CurrencyCost currCost);
    }

}