package com.jim.finansia.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.CurrencySpinnerAdapter;
import com.jim.finansia.utils.IconChooseDialog;
import com.jim.finansia.utils.OnIconPickListener;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SpinnerAdapter;
import com.jim.finansia.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;


public class AddCreditFragment extends Fragment {
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    LogicManager logicManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    IconChooseDialog iconChooseDialog;
    @Inject
    DataCache dataCache;
    @Inject
    SharedPreferences preferences;
    CreditDetialsDao creditDetialsDao;
    CurrencyDao currencyDao;
    AccountDao accountDao;
    FinanceRecordDao financeRecordDao;
    DebtBorrowDao debtBorrowDao;
    RelativeLayout checkContribution;
    TextView tvWhatAboutType;
    boolean onSucsessed = false;
    Spinner spiner_forValut, spiner_procent, spinner_peiod, spTypeLoan ,spBankFee;
    ImageView icona;
    TextView firsContributionCurrency;
    RelativeLayout is_calc;
    String[] valyutes;
    String[] accs;
    Spinner accountSp;
    String selectedIcon;
    DecimalFormat formater = new DecimalFormat("0.##");
    EditText etMonthFee;
    EditText nameCred, valueCred, procentCred, periodCred, firstCred, lastCred, transactionCred;
    Context context;
    int argFirst[] = new int[3];
    int argLast[] = new int[3];
    long forMoth = 1000L * 60L * 60L * 24L * 30L;
    long forYear = 1000L * 60L * 60L * 24L * 365L;
    List<Currency> currencies;
    SwitchCompat isHaveFirstPay;
    public static final String OPENED_TAG = "Addcredit";
    public static boolean to_open_dialog = false;
    CreditDetials currentCredit;
    private String mode = PocketAccounterGeneral.EVERY_DAY, sequence = "";
    private Spinner spNotifMode;
    private ArrayList<String> adapter;
    SwitchCompat keyForBalance;
    boolean fromMainWindow = false;
    int modeFromMain = PocketAccounterGeneral.NO_MODE;
    RecyclerView.LayoutManager layoutManager;
    int posFromMain ;
    String sequence2 = "";
    RelativeLayout relativeLayoutStart;
    private AddCreditFragment.DaysAdapter daysAdapter;
    private RecyclerView rvDays;
    ArrayList<Account> accaunt_AC;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");

    final static String CREDIT_ID = "CREDID";
    final static String ICON_ID = "ICON_ID";
    final static String CREDIT_NAME = "CREDNAME";
    final static String TAKE_TIME = "TAKETIME";
    final static String PROCENT = "PROCENT";
    final static String PROCENT_INTERVAL = "PROCENT_INTERVAL";
    final static String PERIOD_TIME = "PERIOD_TIME";
    final static String VALUE_OF_CREDIT = "VALUE_OF_CREDIT";
    final static String CURRENCY_ID = "CURRENCY_ID";
    final static String VALUE_OF_CREDIT_WITH_PROCENT = "VALUE_OF_CREDIT_WITH_PROCENT";
    final static String PERIOD_TIME_TIP = "PERIOD_TIME_TIP";
    final static String KEY_FOR_INCLUDE = "KEY_FOR_INCLUDE";
    final static String ACCOUNT_ID = "ACCOUNT_ID";
    final static String FROM_EDIT = "FROM_EDIT";
    final static String MONTHLY_FEE = "MONTHLY_FEE";
    final static String TYPE_LOAN = "TYPE_LOAN";
    final static String MONTHLY_FEE_TYPE = "MONTHLY_FEE_TYPE";
    final static String PERVONACALNIY = "PERVONACALNIY";




    public boolean isEdit() {
        return currentCredit != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);

        creditDetialsDao = daoSession.getCreditDetialsDao();
        accountDao = daoSession.getAccountDao();
        currencyDao = daoSession.getCurrencyDao();
        financeRecordDao = daoSession.getFinanceRecordDao();
        debtBorrowDao = daoSession.getDebtBorrowDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_add_credit, container, false);
        if(getArguments()!=null){
            Long creditId = getArguments().getLong(CreditTabLay.CREDIT_ID);
            if(creditId!=null){
                currentCredit = daoSession.load(CreditDetials.class,creditId);
            }
            modeFromMain = getArguments().getInt(CreditTabLay.MODE);
            posFromMain = getArguments().getInt(CreditTabLay.POSITION);


        }
        context = getActivity();

        spiner_forValut = (Spinner) V.findViewById(R.id.spinner);
        spiner_procent = (Spinner) V.findViewById(R.id.spinner_procent);
        spinner_peiod = (Spinner) V.findViewById(R.id.spinner_period);
        spBankFee = (Spinner) V.findViewById(R.id.spBankFee);
        spTypeLoan = (Spinner) V.findViewById(R.id.spTypeLoan);
        accountSp = (Spinner) V.findViewById(R.id.spInfoDebtBorrowAccount);
        keyForBalance = (SwitchCompat) V.findViewById(R.id.key_for_balance);
//        isOpkey = (SwitchCompat) V.findViewById(R.id.key_for_balance);
        isHaveFirstPay = (SwitchCompat) V.findViewById(R.id.chbAccountStartSumEnabled);
        tvWhatAboutType = (TextView) V.findViewById(R.id.tvWhatAboutType);
        firsContributionCurrency = (TextView) V.findViewById(R.id.firsContributionCurrency);
        is_calc = (RelativeLayout) V.findViewById(R.id.is_calc);
        to_open_dialog = false;

        nameCred = (EditText) V.findViewById(R.id.editText);
        etMonthFee = (EditText) V.findViewById(R.id.etMonthFee);
        valueCred = (EditText) V.findViewById(R.id.value_credit);
        procentCred = (EditText) V.findViewById(R.id.procent_credit);
        periodCred = (EditText) V.findViewById(R.id.for_period_credit);
        firstCred = (EditText) V.findViewById(R.id.date_pick_edit);
        lastCred = (EditText) V.findViewById(R.id.date_ends_edit);
        transactionCred = (EditText) V.findViewById(R.id.for_trasaction_credit);
        relativeLayoutStart = (RelativeLayout) V.findViewById(R.id.rlStartSumContainer);
        rvDays = (RecyclerView) V.findViewById(R.id.rvAddAutoMarketPerItems);
//        checkInclude = (RelativeLayout) V.findViewById(R.id.checkInclude);
        checkContribution = (RelativeLayout) V.findViewById(R.id.checkContribution);

        spNotifMode = (Spinner) V.findViewById(R.id.spNotifModeCredit);
        toolbarManager.setOnTitleClickListener(null);
//        btnDetalization = (FrameLayout) V.findViewById(R.id.btnDetalizationCredit);
//        btnDetalization.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openNotifSettingDialog();
//            }
//        });
        adapter = new ArrayList<>();
        adapter.add(getResources().getString(R.string.notif_everyday));
        adapter.add(getResources().getString(R.string.notif_weekly));
        adapter.add(getResources().getString(R.string.notif_monthly));

        ArrayList adapter2 = new ArrayList<>();
        adapter2.add(getResources().getString(R.string.even_princ));
        adapter2.add(getResources().getString(R.string.even_total));

        ArrayList adapter22 = new ArrayList<>();
        adapter22.add(getString(R.string.from_loan_amount));
        adapter22.add(getResources().getString(R.string.from_balance));

        spBankFee.setAdapter(new SpinnerAdapter(getContext(), adapter22));
        spTypeLoan.setAdapter(new SpinnerAdapter(getContext(), adapter2));
        spTypeLoan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        tvWhatAboutType.setText(getString(R.string.with_the_even_principal));
                        break;
                    case 1:
                        tvWhatAboutType.setText(R.string.even_about_total);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spNotifMode.setAdapter(new SpinnerAdapter(getContext(), adapter));
        accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
        ArrayList accounts = new ArrayList();
        for (int i = 0; i < accaunt_AC.size(); i++) {
            accounts.add(accaunt_AC.get(i).getName());
        }
        accountSp.setAdapter(new SpinnerAdapter(getContext(), accounts));
        String lastAccountId = preferences.getString("CHOSEN_ACCOUNT_ID",  "");
        if (lastAccountId != null && !lastAccountId.isEmpty()) {
            int position = 0;
            for (int i = 0; i < accaunt_AC.size(); i++) {
                if (accaunt_AC.get(i).getId().equals(lastAccountId)) {
                    position = i;
                    break;
                }
            }
            accountSp.setSelection(position);
        }
        accountSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                preferences.edit().putString("CHOSEN_ACCOUNT_ID", accaunt_AC.get(i).getId()).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        V.findViewById(R.id.checkInclude).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyForBalance.toggle();
            }
        });
        keyForBalance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(keyForBalance.isChecked()){
                    is_calc.setVisibility(View.VISIBLE);
                }
                else{
                    is_calc.setVisibility(View.GONE);
                }
            }
        });
        spNotifMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        rvDays.setVisibility(View.GONE);
                        break;
                    case 1:
                        rvDays.setVisibility(View.VISIBLE);
                        daysAdapter = new AddCreditFragment.DaysAdapter(0);
                        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                        rvDays.setLayoutManager(layoutManager);
                        rvDays.setAdapter(daysAdapter);

                        break;

                    case 2:

                        rvDays.setVisibility(View.VISIBLE);
                        daysAdapter = new AddCreditFragment.DaysAdapter(1);
                        layoutManager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);
                        rvDays.setLayoutManager(layoutManager);
                        rvDays.setAdapter(daysAdapter);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nameCred.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (nameCred.getText().toString().matches("")) {
                    to_open_dialog = false;
                } else {
                    to_open_dialog = true;
                }
            }
        });


        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMojno = true;
                if (nameCred.getText().toString().equals("")) {
                    nameCred.setError(getString(R.string.should_not_empty));
                    isMojno = false;
                } else
                    nameCred.setHintTextColor(ContextCompat.getColor(context, R.color.black_for_secondary_text));
                if (valueCred.getText().toString().equals("")) {
                    valueCred.setError(getString(R.string.value_shoud_not_empty));
                    isMojno = false;
                } else {
                    try {
                        if (!(Double.parseDouble(valueCred.getText().toString()) > 0)) {
                            valueCred.setError(getString(R.string.incorrect_value));
                            isMojno = false;
                        }
                    } catch (Exception o) {
                    }
                    try {
                        Double.parseDouble(valueCred.getText().toString());
                    } catch (Exception e) {
                        valueCred.setError(getString(R.string.wrong_input_type));
                        return;
                    }
                }
                if (procentCred.getText().toString().equals("")) {
                    procentCred.setError(getString(R.string.procent_should_not_empty));
                    isMojno = false;
                }

                if (periodCred.getText().toString().equals("")) {
                    periodCred.setError(getString(R.string.period_should_not_empty));
                    isMojno = false;
                } else {
                    try {
                        if (!(Integer.parseInt(periodCred.getText().toString()) > 0)) {
                            periodCred.setError(getString(R.string.incorrect_value));
                            isMojno = false;
                        }
                    } catch (Exception o) {
                    }
                }

                if (firstCred.getText().toString().equals("")) {
                    firstCred.setError(getString(R.string.after_per_choise));
                    isMojno = false;
                }

                //TODO first transaction
                if (isMojno) {
                    creditBuildAndSend();
//                    openDialog();
                }
            }
        });

        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                Date AAa = (new Date());
                argFirst[0] = arg1;
                argFirst[1] = arg2;
                argFirst[2] = arg3;
                Calendar calend = new GregorianCalendar(arg1, arg2, arg3);
                AAa.setTime(calend.getTimeInMillis());

                firstCred.setText(sDateFormat.format(AAa));

                int period_long = 1;
                if (!periodCred.getText().toString().matches("")) {
                    period_long = Integer.parseInt(periodCred.getText().toString());

                    switch (spinner_peiod.getSelectedItemPosition()) {
                        case 1:
                            //moth
                            calend.add(Calendar.MONTH, period_long);

                            break;
                        case 0:
                            //year
                            calend.add(Calendar.YEAR, period_long);
                            break;
                        case 2:
                            //week
                            calend.add(Calendar.WEEK_OF_YEAR, period_long);
                            break;
                        case 3:
                            //day
                            calend.add(Calendar.DAY_OF_YEAR, period_long);

                            break;
                        default:
                            break;
                    }


                    long forCompute = calend.getTimeInMillis();
                    // forCompute+=period_long;

                    AAa.setTime(forCompute);
                    lastCred.setText(sDateFormat.format(AAa));

                } else {
                    periodCred.setError(getString(R.string.first_enter_period));
                }
            }
        };
        final DatePickerDialog.OnDateSetListener getDatesetListener2 = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                argLast[0] = arg1;
                argLast[1] = arg2;
                argLast[2] = arg3;

                Date AAa = (new Date());
                Calendar calend = new GregorianCalendar(arg1, arg2, arg3);
                AAa.setTime(calend.getTimeInMillis());
                lastCred.setText(sDateFormat.format(AAa));
                int period_long = 1;
                if (!periodCred.getText().toString().matches("")) {
                    period_long = Integer.parseInt(periodCred.getText().toString());
                    switch (spinner_peiod.getSelectedItemPosition()) {
                        case 1:
                            //moth
                            calend.add(Calendar.MONTH, -period_long);
                            break;
                        case 0:
                            //year
                            calend.add(Calendar.YEAR, -period_long);
                            break;
                        case 2:
                            //week
                            calend.add(Calendar.WEEK_OF_YEAR, -period_long);
                            break;
                        case 3:
                            //day
                            calend.add(Calendar.DAY_OF_YEAR, -period_long);

                            break;
                        default:
                            break;
                    }
                    long forCompute = calend.getTimeInMillis();
                    AAa.setTime(forCompute);
                    firstCred.setText(sDateFormat.format(AAa));
                } else {
                    periodCred.setError(getString(R.string.first_enter_period));
                }
            }
        };

        //        lastCred.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                firstCred.setError(null);
//                lastCred.setError(null);
//                Calendar calendar = Calendar.getInstance();
//                Dialog mDialog = new DatePickerDialog(getContext(),
//                        getDatesetListener2, calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH), calendar
//                        .get(Calendar.DAY_OF_MONTH));
//                mDialog.show();
//            }
//        });
        firstCred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstCred.setError(null);
                lastCred.setError(null);
                Calendar calendar = Calendar.getInstance();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            getDatesetListener, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
            }
        });
//        checkInclude.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isOpkey.toggle();
//            }});
        checkContribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHaveFirstPay.toggle();
            }
        });
        isHaveFirstPay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isHaveFirstPay.isChecked()) {
                    relativeLayoutStart.setVisibility(View.VISIBLE);
                } else  {
                    relativeLayoutStart.setVisibility(View.GONE);
                    transactionCred.setText("");
                }
            }
        });
//        isOpkey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isOpkey.isChecked()) {
//                    spiner_trasnact.setVisibility(View.VISIBLE);
//                } else spiner_trasnact.setVisibility(View.GONE);
//            }
//        });
        procentCred.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String A = procentCred.getText().toString();
                    if (!A.equals("")) {
                        if (A.contains("%")) {
                            StringBuilder sb = new StringBuilder(A);
                            sb.deleteCharAt(A.indexOf("%"));
                            procentCred.setText(sb.toString() + "%");
                        } else {
                            procentCred.setText(A + "%");
                        }
                    }
                }
            }
        });
        etMonthFee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String A = etMonthFee.getText().toString();
                    if (!A.equals("")) {
                        if (A.contains("%")) {
                            StringBuilder sb = new StringBuilder(A);
                            sb.deleteCharAt(A.indexOf("%"));
                            etMonthFee.setText(sb.toString() + "%");
                        } else {
                            etMonthFee.setText(A + "%");
                        }
                    }
                }
            }
        });
        spinner_peiod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (argFirst[0] != 0) {
                    forDateSyncFirst();
                } else if (argLast[0] != 0) {
                    forDateSyncLast();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        periodCred.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (argFirst[0] != 0) {
                    forDateSyncFirst();
                } else if (argLast[0] != 0) {
                    forDateSyncLast();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        icona = (ImageView) V.findViewById(R.id.imageForIcon);
        String[] tempIcons = getResources().getStringArray(R.array.icons);
        selectedIcon = tempIcons[4];

        icona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO ICON DIALOG
                iconChooseDialog.setSelectedIcon(selectedIcon);
                iconChooseDialog.setOnIconPickListener(new OnIconPickListener() {
                    @Override
                    public void OnIconPick(String icon) {
                        int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                        icona.setImageResource(resId);
                        selectedIcon = icon;
                        iconChooseDialog.dismiss();
                    }
                });
                iconChooseDialog.show();
            }
        });

        V.findViewById(R.id.pustoyy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        currencies = currencyDao.loadAll();
        valyutes = new String[currencies.size()];
        ArrayList cur = new ArrayList();
        ArrayList curName = new ArrayList();
        for (int i = 0; i < valyutes.length; i++) {
            valyutes[i] = currencies.get(i).getAbbr();
            cur.add(valyutes[i]);
            curName.add(currencies.get(i).getName());
        }

        ArrayList percent = new ArrayList<>();
        percent.add(getString(R.string.per_year));
        percent.add(getResources().getString(R.string.per_month));
        ArrayList term = new ArrayList<>();
        term.add(getString(R.string.yearr));
        term.add(getResources().getString(R.string.mont));
        spiner_forValut.setAdapter(new CurrencySpinnerAdapter(getContext(), cur, curName));
        int posMain = 0;
        for (int i = 0; i < valyutes.length; i++) {
            if (valyutes[i].equals(commonOperations.getMainCurrency().getAbbr())) {
                posMain = i;
            }
        }
        spiner_forValut.setSelection(posMain);
        spiner_procent.setAdapter(new SpinnerAdapter(getContext(), percent));
        spinner_peiod.setAdapter(new SpinnerAdapter(getContext(), term));

        spiner_forValut.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                firsContributionCurrency.setText(valyutes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (isEdit()) {
            int resId = getResources().getIdentifier(currentCredit.getIcon_ID(), "drawable", getContext().getPackageName());
            selectedIcon=currentCredit.getIcon_ID();
            icona.setImageResource(resId);
            nameCred.setText(currentCredit.getCredit_name());
            valueCred.setText(formater.format(currentCredit.getValue_of_credit()+currentCredit.getPervonacalniy()));
            procentCred.setText(formater.format(currentCredit.getProcent()) + "%");
            etMonthFee.setText(formater.format(currentCredit.getMonthly_fee()) + "%");
            spTypeLoan.setSelection(currentCredit.getType_loan());
            spiner_forValut.setSelection(getIndex(spiner_forValut, currentCredit.getValyute_currency().getAbbr()));

            if (currentCredit.getProcent_interval() == forMoth) {
                spiner_procent.setSelection(1);
            } else if (currentCredit.getProcent_interval() == forYear) {
                spiner_procent.setSelection(0);}

            if(currentCredit.getPervonacalniy()!=0){
                isHaveFirstPay.toggle();
                transactionCred.setText(formater.format(currentCredit.getPervonacalniy()));
            }
            if(currentCredit.getKey_for_include()){
                Account account = accountDao.load(currentCredit.getAccountID());
                if(account!=null){
                    for (int i=0;i<accaunt_AC.size();i++){
                        if(accaunt_AC.get(i).getId().equals(account.getId())){
                            keyForBalance.toggle();
                            accountSp.setSelection(i);
                            break;
                        }
                    }
                }
            }

            periodCred.setText(Long.toString(currentCredit.getPeriod_time() / currentCredit.getPeriod_time_tip()));

            if (currentCredit.getPeriod_time_tip() == forMoth) {
                spinner_peiod.setSelection(1);
            } else if (currentCredit.getPeriod_time_tip() == forYear) {
                spinner_peiod.setSelection(0);
            }


            firstCred.setText(sDateFormat.format(currentCredit.getTake_time().getTime()));

            Calendar calc = (Calendar) currentCredit.getTake_time().clone();

            if (currentCredit.getProcent_interval() == forMoth) {
                calc.add(Calendar.MONTH, (int) (currentCredit.getPeriod_time() / currentCredit.getPeriod_time_tip()));
            } else if (currentCredit.getProcent_interval() == forYear) {
                calc.add(Calendar.YEAR, (int) (currentCredit.getPeriod_time() / currentCredit.getPeriod_time_tip()));
            }

            lastCred.setText(dateFormat.format(calc.getTime()));
            argFirst[0] = currentCredit.getTake_time().get(Calendar.YEAR);
            argFirst[1] = currentCredit.getTake_time().get(Calendar.MONTH);
            argFirst[2] = currentCredit.getTake_time().get(Calendar.DAY_OF_MONTH);
            argLast[0] = calc.get(Calendar.YEAR);
            argLast[1] = calc.get(Calendar.MONTH);
            argLast[2] = calc.get(Calendar.DAY_OF_MONTH);
        }
        return V;
    }

    public void toolbarBackupMethod() {
        if (toolbarManager == null) return;
        toolbarManager.setOnTitleClickListener(null);
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMojno = true;
                if (nameCred.getText().toString().equals("")) {
                    nameCred.setError(getString(R.string.should_not_empty));
                    isMojno = false;
                } else
                    nameCred.setHintTextColor(ContextCompat.getColor(context, R.color.black_for_secondary_text));
                if (valueCred.getText().toString().equals("")) {
                    valueCred.setError(getString(R.string.value_shoud_not_empty));
                    isMojno = false;
                } else {
                    try {
                        if (!(Double.parseDouble(valueCred.getText().toString()) > 0)) {
                            valueCred.setError(getString(R.string.incorrect_value));
                            isMojno = false;
                        }
                    } catch (Exception o) {
                    }
                    try {
                        Double.parseDouble(valueCred.getText().toString());
                    } catch (Exception e) {
                        valueCred.setError(getString(R.string.wrong_input_type));
                        return;
                    }
                }
                if (procentCred.getText().toString().equals("")) {
                    procentCred.setError(getString(R.string.procent_should_not_empty));
                    isMojno = false;
                }

                if (periodCred.getText().toString().equals("")) {
                    periodCred.setError(getString(R.string.period_should_not_empty));
                    isMojno = false;
                } else {
                    try {
                        if (!(Integer.parseInt(periodCred.getText().toString()) > 0)) {
                            periodCred.setError(getString(R.string.incorrect_value));
                            isMojno = false;
                        }
                    } catch (Exception o) {
                    }
                }

                if (firstCred.getText().toString().equals("")) {
                    firstCred.setError(getString(R.string.after_per_choise));
                    isMojno = false;
                }

                //TODO first transaction
                if (isMojno) {
                    creditBuildAndSend();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbarManager != null) {
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setTitle(getResources().getString(R.string.credit));
        }
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    private void openNotifSettingDialog() {
        final Dialog dialog = new Dialog(getActivity());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.notif_settings, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        LinearLayout llNotifSettingBody = (LinearLayout) dialogView.findViewById(R.id.llNotifSettingBody);
        llNotifSettingBody.removeAllViews();
        final Spinner sp = new Spinner(getContext());
        final ArrayList<CheckBox> chbs = new ArrayList<>();
        switch (mode) {
            case PocketAccounterGeneral.EVERY_WEEK:
                String[] weekdays = getResources().getStringArray(R.array.week_days);
                for (int i = 0; i < weekdays.length; i++) {
                    CheckBox chb = new CheckBox(getContext());
                    if (i == 0) chb.setChecked(true);
                    chb.setText(weekdays[i]);
                    chb.setTextSize(getResources().getDimension(R.dimen.five_dp));
                    chb.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color));
                    chb.setPadding(0, 0, (int) getResources().getDimension(R.dimen.ten_sp), 0);
                    chbs.add(chb);
                    llNotifSettingBody.addView(chb);
                }
                break;
            case PocketAccounterGeneral.EVERY_MONTH:
                LinearLayout ll = new LinearLayout(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setLayoutParams(lp);

                String[] days = new String[31];
                for (int i = 0; i < 31; i++) {
                    days[i] = Integer.toString(i + 1);
                }
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, days);
                sp.setAdapter(adapter1);
                TextView tv = new TextView(getContext());
                tv.setText(getResources().getString(R.string.choose_date) + ": ");
                tv.setTextSize(getResources().getDimension(R.dimen.five_dp));
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color));
                LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvlp.setMargins(0, 0, (int) getResources().getDimension(R.dimen.ten_sp), 0);
                tv.setLayoutParams(tvlp);
                ll.addView(tv);
                ll.addView(sp);
                llNotifSettingBody.addView(ll);
                break;
        }
        ImageView btnYes = (ImageView) dialogView.findViewById(R.id.ivAccountSave);
        btnYes.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          String text = "";
                                          if (mode.matches(PocketAccounterGeneral.EVERY_WEEK)) {
                                              for (int i = 0; i < chbs.size(); i++) {
                                                  if (chbs.get(i).isChecked()) {
                                                      text = text + i + ",";
                                                  }
                                              }
                                          }
                                          if (mode.matches(PocketAccounterGeneral.EVERY_MONTH)) {
                                              text = sp.getSelectedItem() + "";
                                          }
                                          if (!text.matches("") && text.endsWith(","))
                                              sequence = text.substring(0, text.length() - 1);
                                          else
                                              sequence = text;
                                          dialog.dismiss();
                                      }
                                  }
        );
        ImageView btnNo = (ImageView) dialogView.findViewById(R.id.ivAccountClose);
        btnNo.setOnClickListener(new View.OnClickListener()

                                 {
                                     @Override
                                     public void onClick(View v) {
                                         dialog.dismiss();
                                     }
                                 }
        );
        dialog.show();
    }

    public void forDateSyncFirst() {
        Date AAa = (new Date());
        Calendar calend = new GregorianCalendar(argFirst[0], argFirst[1], argFirst[2]);
        AAa.setTime(calend.getTimeInMillis());

        firstCred.setText(sDateFormat.format(AAa));

        int period_long = 1;
        if (!periodCred.getText().toString().matches("")) {
            period_long = Integer.parseInt(periodCred.getText().toString());
            switch (spinner_peiod.getSelectedItemPosition()) {
                case 1:
                    //moth
                    calend.add(Calendar.MONTH, period_long);

                    break;
                case 0:
                    //year
                    calend.add(Calendar.YEAR, period_long);
                    break;
                case 2:
                    //week
                    calend.add(Calendar.WEEK_OF_YEAR, period_long);
                    break;
                case 3:
                    //day
                    calend.add(Calendar.DAY_OF_YEAR, period_long);

                    break;
                default:
                    break;
            }


            long forCompute = calend.getTimeInMillis();
            // forCompute+=period_long;

            AAa.setTime(forCompute);
            lastCred.setText(sDateFormat.format(AAa));
        } else {
            periodCred.setError(getString(R.string.first_enter_period));
        }
    }

    public void forDateSyncLast() {
        Date AAa = (new Date());
        Calendar calend = new GregorianCalendar(argLast[0], argLast[1], argLast[2]);
        AAa.setTime(calend.getTimeInMillis());
        lastCred.setText(sDateFormat.format(AAa));

        int period_long = 1;
        if (!periodCred.getText().toString().matches("")) {
            period_long = Integer.parseInt(periodCred.getText().toString());
            switch (spinner_peiod.getSelectedItemPosition()) {
                case 1:
                    //moth
                    calend.add(Calendar.MONTH, -period_long);
                    break;
                case 0:
                    //year
                    calend.add(Calendar.YEAR, -period_long);
                    break;
                case 2:
                    //week
                    calend.add(Calendar.WEEK_OF_YEAR, -period_long);
                    break;
                case 3:
                    //day
                    calend.add(Calendar.DAY_OF_YEAR, -period_long);

                    break;
                default:
                    break;
            }
            long forCompute = calend.getTimeInMillis();
            // forCompute+=period_long;

            AAa.setTime(forCompute);
            firstCred.setText(sDateFormat.format(AAa));
        } else {
            periodCred.setError(getString(R.string.first_enter_period));

        }
    }

    StringBuilder sb;
    StringBuilder sbMonthlyFee;


    private void creditBuildAndSend(){
        sb = new StringBuilder(procentCred.getText().toString());
        Log.d("sbb", sb.toString());
        int a = sb.toString().indexOf('%');
        if (a != -1)
            sb.deleteCharAt(a);
        sbMonthlyFee = new StringBuilder(etMonthFee.getText().toString());
        int ss = sbMonthlyFee.toString().indexOf('%');
        if (ss != -1)
            sbMonthlyFee.deleteCharAt(ss);


        long procent_inter = 1;
        switch (spiner_procent.getSelectedItemPosition()) {
            case 0:
                procent_inter *= forYear;
                break;
            case 1:
                procent_inter *= forMoth;
                break;
        }
        long period_inter = Long.parseLong(periodCred.getText().toString());
        long period_tip = 0;
        switch (spinner_peiod.getSelectedItemPosition()) {
            case 0:
                    period_inter *= forYear;
                    period_tip = forYear;
                    break;
            case 1:
                    period_inter *= forMoth;
                    period_tip = forMoth;
                    break;
            }
        boolean key = keyForBalance.isChecked();
//        key = isOpkey.isChecked();
        CreditDetials A1;
        Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
        // check limit account
        if (account.getIsLimited() && key) {
            double limit = account.getLimite();
            double accounted = logicManager.isLimitAccess(account, new GregorianCalendar(argFirst[0], argFirst[1], argFirst[2]));
            if (isEdit() && currentCredit.getKey_for_include()) {
                for (ReckingCredit reckingCredit : currentCredit.getReckings()) {
                    if (currentCredit.getTake_time().getTimeInMillis() == reckingCredit.getPayDate().getTimeInMillis())
                        accounted=+commonOperations.getCost(reckingCredit.getPayDate(), currentCredit.getValyute_currency(), reckingCredit.getAmount());
                }
            }
            accounted = accounted - commonOperations.getCost((new GregorianCalendar(argFirst[0], argFirst[1], argFirst[2])), currencies.get(spiner_forValut.getSelectedItemPosition()), account.getCurrency(), Double.parseDouble(transactionCred.getText().toString()));
            if (-limit > accounted) {
                Toast.makeText(context, R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isEdit()) {
            Log.d("sbb",Double.parseDouble(sb.toString())+"" );
            A1 = new CreditDetials(selectedIcon, nameCred.getText().toString(), new GregorianCalendar(argFirst[0], argFirst[1], argFirst[2]),
                    Double.parseDouble(sb.toString()), procent_inter, period_inter, period_tip, key, Double.parseDouble(valueCred.getText().toString()),
                    currencies.get(spiner_forValut.getSelectedItemPosition()), -1, currentCredit.getMyCredit_id(),(keyForBalance.isChecked())?accaunt_AC.get(accountSp.getSelectedItemPosition()).getId():"");

        } else {
            A1 = new CreditDetials(selectedIcon, nameCred.getText().toString(), new GregorianCalendar(argFirst[0], argFirst[1], argFirst[2]),
                    Double.parseDouble(sb.toString()), procent_inter, period_inter, period_tip, key, Double.parseDouble(valueCred.getText().toString()),
                    currencies.get(spiner_forValut.getSelectedItemPosition()), -1, System.currentTimeMillis(),(keyForBalance.isChecked())?accaunt_AC.get(accountSp.getSelectedItemPosition()).getId():"");
        }

        if (isHaveFirstPay.isChecked()&&!transactionCred.getText().toString().equals("")) {
            double firsPay =0;
            try{
                firsPay = Double.parseDouble(transactionCred.getText().toString());

            }
            catch (Exception o){
                transactionCred.setError(getString(R.string.invalide_format));
                return;
            }
            A1.setPervonacalniy(firsPay);
           A1.setValue_of_credit(A1.getValue_of_credit()-firsPay);
        }

        A1.setType_loan(spTypeLoan.getSelectedItemPosition());
        A1.setMonthly_fee(Double.parseDouble(sbMonthlyFee.toString().replace(",", ".")));
        A1.setMonthly_fee_type(spBankFee.getSelectedItemPosition());
        A1.__setDaoSession(daoSession);

        A1.setInfo(mode + ":" + sequence);


        onSucsessed = true;

        ScheduleCreditFragment scheduleCreditFragment = new ScheduleCreditFragment();
        Bundle bundle = new Bundle();

        bundle.putBoolean(ScheduleCreditFragment.FROM_ADDING,true);
        bundle.putLong(CREDIT_ID,A1.getMyCredit_id());
        bundle.putString(ICON_ID,A1.getIcon_ID());
        bundle.putString(CREDIT_NAME,A1.getCredit_name());
        bundle.putLong(TAKE_TIME,A1.getTake_time().getTimeInMillis());
        bundle.putDouble(PROCENT,A1.getProcent());
        bundle.putDouble(PROCENT_INTERVAL,A1.getProcent_interval());
        bundle.putLong(PERIOD_TIME,A1.getPeriod_time());
        bundle.putDouble(VALUE_OF_CREDIT,A1.getValue_of_credit());
        bundle.putString(CURRENCY_ID,A1.getCurrencyId());
        bundle.putDouble(VALUE_OF_CREDIT_WITH_PROCENT,A1.getValue_of_credit_with_procent());
        bundle.putLong(PERIOD_TIME_TIP,A1.getPeriod_time_tip());
        bundle.putBoolean(KEY_FOR_INCLUDE,A1.getKey_for_include());
        bundle.putString(ACCOUNT_ID,A1.getAccountID());
        bundle.putDouble(MONTHLY_FEE,A1.getMonthly_fee());
        bundle.putInt(TYPE_LOAN,A1.getType_loan());
        bundle.putInt(MONTHLY_FEE_TYPE,A1.getMonthly_fee_type());
        bundle.putInt(CreditTabLay.LOCAL_APPEREANCE, CreditTabLay.LOCAL_EDIT);
        bundle.putDouble(PERVONACALNIY,A1.getPervonacalniy());



        bundle.putInt(CreditTabLay.MODE,modeFromMain);
        bundle.putInt(CreditTabLay.POSITION,posFromMain);

        bundle.putBoolean(FROM_EDIT, isEdit());
        scheduleCreditFragment.setArguments(bundle);
        paFragmentManager.displayFragment(scheduleCreditFragment);

    }








    private class DaysAdapter extends RecyclerView.Adapter<AddCreditFragment.ViewHolderDialog> {
        private String[] days;
        private boolean tek[];

        public DaysAdapter(int type) {
            sequence2 = "";
            if (type == 0) {
                days = getResources().getStringArray(R.array.week_day_auto);
            } else {
                days = new String[31];
                for (int i = 0; i < days.length; i++) {
                    days[i] = i < 9 ? "" + (i + 1) : "" + (i + 1);
                }
            }
            tek = new boolean[days.length];
        }





        @Override
        public int getItemCount() {
            return days.length;
        }

        public void onBindViewHolder(final AddCreditFragment.ViewHolderDialog view, final int position) {
            if (position % 7 == 0) {
                view.frameLayout.setVisibility(View.GONE);
            }
            view.day.setText(days[position]);
            if (tek[position])
            {
                view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                view.day.setTypeface(null, Typeface.BOLD);

            }
            else {
                view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.black_for_secondary_text));
                view.day.setTypeface(null, Typeface.NORMAL);

            }
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tek[position]) {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                        view.day.setTypeface(null, Typeface.BOLD);

                    } else {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.black_for_secondary_text));
                        view.day.setTypeface(null, Typeface.NORMAL);

                    }
                    tek[position] = !tek[position];
                }
            });
        }

        public AddCreditFragment.ViewHolderDialog onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_month_layout, parent, false);
            return new AddCreditFragment.ViewHolderDialog(view);
        }


    }

    public class ViewHolderDialog extends RecyclerView.ViewHolder {
        public TextView day;
        public FrameLayout frameLayout;
        public View itemView;
        public ViewHolderDialog(View view) {
            super(view);
            itemView  = view;
            day = (TextView) view.findViewById(R.id.tvItemDay);
            frameLayout = (FrameLayout) view.findViewById(R.id.flItemDay);
        }
    }

}
