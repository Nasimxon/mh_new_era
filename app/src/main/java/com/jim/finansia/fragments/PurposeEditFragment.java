package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.CurrencySpinnerAdapter;
import com.jim.finansia.utils.DatePicker;
import com.jim.finansia.utils.IconChooseDialog;
import com.jim.finansia.utils.OnIconPickListener;
import com.jim.finansia.utils.SpinnerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("InflateParams")
public class PurposeEditFragment extends Fragment implements OnClickListener, OnItemClickListener {
    @Inject LogicManager logicManager;
    @Inject ToolbarManager toolbarManager;
    @Inject DaoSession daoSession;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject IconChooseDialog iconChooseDialog;
    @Inject DatePicker datePicker;
    @Inject CommonOperations commonOperations;
    @Inject FinansiaFirebaseAnalytics analytics;

    private String choosenIcon = "add_icon";
    private Purpose purpose;
    private EditText purposeName;
    private ImageView iconPurpose;
    private EditText amountPurpose;
    private Spinner curPurpose;
    private Spinner periodPurpose;
    private EditText beginDate;
    private EditText endDate;
    private LinearLayout linearLayoutForGone;
    private Calendar begCalendar;
    private Calendar endCalendar;
    private TextView etPeriodCount;
    private SimpleDateFormat simpleDateFormat;
    private boolean forCustomPeriod = false;



    boolean keyb = true;
    DatePickerDialog.OnDateSetListener getDatesetListener2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.purpose_edit_layout
                , container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        begCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        if (toolbarManager != null)
        {
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
        if (getArguments() != null) {
            String purposeId = getArguments().getString(PurposeFragment.PURPOSE_ID);
            if (purposeId != null)
                purpose = daoSession.load(Purpose.class, purposeId);
        }
        purposeName = (EditText) rootView.findViewById(R.id.etPurposeEditName);
        iconPurpose = (ImageView) rootView.findViewById(R.id.fabPurposeIcon);
        amountPurpose = (EditText) rootView.findViewById(R.id.etPurposeTotal);
        curPurpose = (Spinner) rootView.findViewById(R.id.spPurposeCurrency);
        periodPurpose = (Spinner) rootView.findViewById(R.id.spPurposePeriod);
        beginDate = (EditText) rootView.findViewById(R.id.tvPurposeBeginDate);
        endDate = (EditText) rootView.findViewById(R.id.tvPurposeEndDate);
            linearLayoutForGone = (LinearLayout) rootView.findViewById(R.id.linTextForGOne);
        etPeriodCount = (EditText) rootView.findViewById(R.id.for_period_credit);
        CurrencyDao currencyDao = daoSession.getCurrencyDao();
        final List<String> curList = new ArrayList<>();
        final List<String> curName = new ArrayList<>();
        for (Currency c : currencyDao.queryBuilder().list()) {
            curList.add(c.getAbbr());
            curName.add(c.getName());
        }
        if (purpose != null)
            choosenIcon = purpose.getIcon();
        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));
        // ------------ Toolbar setting ----------
        toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountPurpose.getText().toString().isEmpty()) {
                    amountPurpose.setError(getString(R.string.wrong_input_type));
                    return;
                }
                try {
                    Double.parseDouble(amountPurpose.getText().toString());
                } catch (Exception e) {
                    amountPurpose.setError(getString(R.string.wrong_input_type));
                    return;
                }
                if (amountPurpose.getText().toString().isEmpty()) {
                    amountPurpose.setError(getResources().getString(R.string.enter_amount));
                } else if (purposeName.getText().toString().isEmpty()) {
                    amountPurpose.setError(null);
                    purposeName.setError(getResources().getString(R.string.enter_name_error));
                    return;
                }
                if(choosenIcon.equals("add_icon")){
                    Toast.makeText(getContext(),getString(R.string.choise_photo),Toast.LENGTH_SHORT).show();
                    return;
                }

                else {
                    if (purpose == null) {
                        purpose = new Purpose();
                    }
                    purpose.setDescription(purposeName.getText().toString());
                    purpose.setIcon(choosenIcon);
                    purpose.setPeriodPos(periodPurpose.getSelectedItemPosition());
                    purpose.setPurpose(Double.parseDouble(amountPurpose.getText().toString()));
                    purpose.setBegin(begCalendar);
                    purpose.setEnd(endCalendar);
                    if (!etPeriodCount.getText().toString().equals(""))
                        purpose.setPeriodSize(Integer.parseInt(etPeriodCount.getText().toString()));
                    Currency currencyy = null;
                    List<Currency> curListTemp = daoSession.getCurrencyDao().loadAll();
                    for (Currency temp : curListTemp) {
                        if (curList.get(curPurpose.getSelectedItemPosition()).equals(temp.getAbbr())) {
                            currencyy = temp;
                            break;
                        }
                    }
                    if (currencyy == null)
                        return;
                    purpose.setCurrency(currencyy);
                    switch (logicManager.insertPurpose(purpose)) {
                        case LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS: {
                            Toast.makeText(getContext(), R.string.such_name_exists, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case LogicManagerConstants.SAVED_SUCCESSFULL: {
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayFragment(new PurposeFragment());
                            break;
                        }
                    }
                }
            }
        });
        // ------------ end toolbar setting ------
        // ------------ icon set ----------
        int resId = getResources().getIdentifier(purpose != null ? purpose.getIcon() : choosenIcon, "drawable", getContext().getPackageName());
        iconPurpose.setImageResource(resId);
        iconPurpose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iconChooseDialog.setOnIconPickListener(new OnIconPickListener() {
                    @Override
                    public void OnIconPick(String icon) {
                        choosenIcon = icon;
                        int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                        iconPurpose.setImageResource(resId);
                        iconChooseDialog.setSelectedIcon(icon);
                        iconChooseDialog.dismiss();
                    }
                });
                iconChooseDialog.show();
            }
        });
        // ------------ end icon set ---------
        // ------------ spinner currency --------
        curPurpose.setAdapter(new CurrencySpinnerAdapter(getContext(), (ArrayList) curList,(ArrayList) curName));
        int posMain = 0;
        for (int i = 0; i < curList.size(); i++) {
            if (curList.get(i).equals(commonOperations.getMainCurrency().getAbbr())) {
                posMain = i;
            }
        }
        curPurpose.setSelection(posMain);
        // ------------ end spinner currency -------
        // ------------ period purpose spinner ------
        linearLayoutForGone.setVisibility(View.GONE);
        ArrayList<String> periodList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.period_purpose)));
        periodPurpose.setAdapter(new SpinnerAdapter(getContext(), periodList));
        periodPurpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.purpose_dialog_layout, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                if (begCalendar != null && !keyb) {
                    forDateSyncFirst();
                } else if (endCalendar != null && !keyb) {
                    forDateSyncLast();
                }

                switch (position) {
                    case 0: {
                        linearLayoutForGone.setVisibility(View.GONE);
                        etPeriodCount.setVisibility(View.GONE);
                        begCalendar = null;
                        endCalendar = null;
                        endDate.setText("");
                        begCalendar = Calendar.getInstance();
                        beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));
                        keyb = true;
                        forCustomPeriod = false;
                        break;
                    }
                    case 1: {
                        linearLayoutForGone.setVisibility(View.VISIBLE);
                        etPeriodCount.setVisibility(View.VISIBLE);
                        keyb = false;
                        forCustomPeriod = false;
                        endDate.setOnClickListener(null);
                        break;
                    }
                    case 2: {
                        linearLayoutForGone.setVisibility(View.VISIBLE);
                        etPeriodCount.setVisibility(View.VISIBLE);
                        keyb = false;
                        forCustomPeriod = false;
                        endDate.setOnClickListener(null);
                        break;
                    }
                    case 3: {
                        linearLayoutForGone.setVisibility(View.VISIBLE);
                        etPeriodCount.setVisibility(View.VISIBLE);
                        keyb = false;
                        forCustomPeriod = false;
                        endDate.setOnClickListener(null);
                        break;
                    }
                    case 4: {
                        linearLayoutForGone.setVisibility(View.VISIBLE);
                        etPeriodCount.setVisibility(View.GONE);
                        keyb = false;
                        forCustomPeriod = true;
                        endDate.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                beginDate.setError(null);
                                endDate.setError(null);
                                Calendar calendar = Calendar.getInstance();
                                Dialog mDialog = new DatePickerDialog(getContext(),
                                        getDatesetListener2, calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH), calendar
                                        .get(Calendar.DAY_OF_MONTH));
                                mDialog.show();
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //----- Calendar Data picker -------

        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(android.widget.DatePicker arg0, int arg1, int arg2, int arg3) {
                begCalendar = new GregorianCalendar(arg1, arg2, arg3);
                beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));
                if (!forCustomPeriod) {
                    endCalendar = (Calendar) begCalendar.clone();
                    int period_long = 1;
                    if (!etPeriodCount.getText().toString().matches("")) {
                        period_long = Integer.parseInt(etPeriodCount.getText().toString());

                        switch (periodPurpose.getSelectedItemPosition()) {
                            case 1:
                                //week
                                endCalendar.add(Calendar.WEEK_OF_YEAR, period_long);


                                break;
                            case 2:
                                //year
                                endCalendar.add(Calendar.MONTH, period_long);

                                break;
                            case 3:
                                //year
                                endCalendar.add(Calendar.YEAR, period_long);
                                break;
                            case 4:
                                return;
                            default:
                                return;
                        }

                        // forCompute+=period_long;

                        endDate.setText(simpleDateFormat.format(endCalendar.getTime()));

                    } else {
                        etPeriodCount.setError(getString(R.string.purpose_term));
                    }
                }
            }
        };
        getDatesetListener2 = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(android.widget.DatePicker arg0, int arg1, int arg2, int arg3) {
                endCalendar = new GregorianCalendar(arg1, arg2, arg3);
                endDate.setText(simpleDateFormat.format(endCalendar.getTime()));
                if (!forCustomPeriod) {
                    begCalendar = (Calendar) endCalendar.clone();
                    int period_long = 1;
                    if (!etPeriodCount.getText().toString().matches("")) {
                        period_long = Integer.parseInt(etPeriodCount.getText().toString());

                        switch (periodPurpose.getSelectedItemPosition()) {
                            case 1:
                                //week
                                begCalendar.add(Calendar.WEEK_OF_YEAR, -period_long);
                                break;
                            case 2:
                                //year
                                begCalendar.add(Calendar.MONTH, -period_long);
                                break;
                            case 3:
                                //year
                                begCalendar.add(Calendar.YEAR, -period_long);
                                break;
                            case 4:
                                return;
                            default:
                                return;
                        }

                        beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));

                    } else {
                        etPeriodCount.setError(getString(R.string.first_enter_period));
                    }
                }

            }
        };


        beginDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                beginDate.setError(null);
                endDate.setError(null);
                Calendar calendar = Calendar.getInstance();
                Dialog mDialog = new DatePickerDialog(getContext(),
                        getDatesetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH));
                mDialog.show();
            }
        });
        if (periodPurpose.getSelectedItemPosition() != 4) {
            endDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    beginDate.setError(null);
                    endDate.setError(null);
                    Calendar calendar = Calendar.getInstance();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            getDatesetListener2, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
                }
            });
        } else endDate.setOnClickListener(null);

        etPeriodCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (begCalendar != null && !s.equals("")) {
                    forDateSyncFirst();
                } else if (endCalendar != null && !s.equals("")) {
                    forDateSyncLast();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // ------------ end period spinner ---------
        if (purpose != null) {
            purposeName.setText(purpose.getDescription());
            amountPurpose.setText("" + purpose.getPurpose());
            begCalendar = purpose.getBegin();
            endCalendar = purpose.getEnd();
            periodPurpose.setSelection(purpose.getPeriodPos());
            beginDate.setText(simpleDateFormat.format(purpose.getBegin().getTime()));
            if (purpose.getEnd() != null){
            endDate.setText(simpleDateFormat.format(purpose.getEnd().getTime()));}
            etPeriodCount.setText("" + purpose.getPeriodSize());
        }
        return rootView;
    }
    public void forDateSyncFirst() {
        if (!forCustomPeriod) {
            beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));
            endCalendar = (Calendar) begCalendar.clone();
            int period_long = 1;
            if (!etPeriodCount.getText().toString().matches("")) {
                period_long = Integer.parseInt(etPeriodCount.getText().toString());

                switch (periodPurpose.getSelectedItemPosition()) {
                    case 1:
                        //week
                        endCalendar.add(Calendar.WEEK_OF_YEAR, period_long);


                        break;
                    case 2:
                        //year
                        endCalendar.add(Calendar.MONTH, period_long);

                        break;
                    case 3:
                        //year
                        endCalendar.add(Calendar.YEAR, period_long);
                        break;
                    case 4:
                        return;
                    default:
                        return;
                }


                // forCompute+=period_long;

                endDate.setText(simpleDateFormat.format(endCalendar.getTime()));

            } else {
                etPeriodCount.setError(getString(R.string.first_enter_period));
            }
        }
    }

    public void forDateSyncLast() {
        if (!forCustomPeriod) {
            endDate.setText(simpleDateFormat.format(endCalendar.getTime()));
            begCalendar = (Calendar) endCalendar.clone();
            int period_long = 1;
            if (!etPeriodCount.getText().toString().matches("")) {
                period_long = Integer.parseInt(etPeriodCount.getText().toString());

                switch (periodPurpose.getSelectedItemPosition()) {
                    case 1:
                        //week
                        begCalendar.add(Calendar.WEEK_OF_YEAR, -period_long);
                        break;
                    case 2:
                        //year
                        begCalendar.add(Calendar.MONTH, -period_long);

                        break;
                    case 3:
                        //year
                        begCalendar.add(Calendar.YEAR, -period_long);
                        break;
                    case 4:
                        return;
                    default:
                        return;
                }

                beginDate.setText(simpleDateFormat.format(begCalendar.getTime()));

            } else {
                etPeriodCount.setError(getString(R.string.first_enter_period));
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        paFragmentManager.getFragmentManager().popBackStack();
    }

    @Override
    public void onClick(View v) {

    }

}

