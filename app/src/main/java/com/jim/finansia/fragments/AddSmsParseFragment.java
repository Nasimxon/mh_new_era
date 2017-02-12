package com.jim.finansia.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.SmsParseObject;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.database.TemplateSms;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.utils.SpinnerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 9/29/16.
 */

public class AddSmsParseFragment extends PABaseFragment{
    private EditText etNumber;
    private TextView ivSms;
    private RadioGroup rgSortSms;
    private RecyclerView rvSmsList;
    private TextView tvSmsCount;
    private EditText etIncome;
    private EditText etExpance;
    private EditText etAmount;
    private Spinner spAccount;
    private Spinner spCurrency;

    private Dialog dialog;
    private MyAdapter myAdapter;
    private TextView tvIncome, tvExpense;
    private int posIncExp = -1;
    private int posAmount = -1;
    private SmsParseObject oldObject;
    private List<TemplateSms> templateSmsList;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    int txSize;
    private final String SMS_URI_INBOX = "content://sms/inbox";
    private final String SMS_URI_ALL = "content://sms/";
    private List<Sms> forChoose, all, choosenSms;
    private List<String> incomeKeys, expenseKeys, amountKeys;
    private RadioButton rbnSmsParseAddAll, rbnSmsParseAddIncome, rbnSmsParseAddExpance;
    private List<TemplateSms> templates;
    private List<TextView> tvList;
    private List<String> splittedBody;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        incomeKeys = new ArrayList<>();
        expenseKeys = new ArrayList<>();
        amountKeys = new ArrayList<>();
        templates = new ArrayList<>();
        choosenSms = new ArrayList<>();
        if (getArguments() != null) {
            String id = getArguments().getString(SmsParseMainFragment.SMS_PARSE_OBJECT_ID);
            if (id != null)
                oldObject = daoSession.load(SmsParseObject.class, id);
        }
        View rootView = inflater.inflate(R.layout.add_sms_sender, container, false);
        txSize = (int) ((int) (getResources().getDimension(R.dimen.fourteen_dp)) / getResources().getDisplayMetrics().density);
        etNumber = (EditText) rootView.findViewById(R.id.etSmsParseAddNumber);
        ivSms = (TextView) rootView.findViewById(R.id.ivSmsParseGet);
        rgSortSms = (RadioGroup) rootView.findViewById(R.id.rgSmsParseAddSort);
        rbnSmsParseAddAll = (RadioButton) rgSortSms.findViewById(R.id.rbnSmsParseAddAll);
        rbnSmsParseAddIncome = (RadioButton) rgSortSms.findViewById(R.id.rbnSmsParseAddIncome);
        rbnSmsParseAddExpance = (RadioButton) rgSortSms.findViewById(R.id.rbnSmsParseAddExpance);
        rvSmsList = (RecyclerView) rootView.findViewById(R.id.rvSmsParseAdd);
        etIncome = (EditText) rootView.findViewById(R.id.etSmsParseAddIncome);
        etExpance = (EditText) rootView.findViewById(R.id.etSmsParseAddExpance);
        etAmount = (EditText) rootView.findViewById(R.id.etSmsParseAddAmount);
        spAccount = (Spinner) rootView.findViewById(R.id.spSmsParseAddAccount);
        spCurrency = (Spinner) rootView.findViewById(R.id.spSmsParseAddCurrency);
        tvSmsCount = (TextView) rootView.findViewById(R.id.tvAddSmsParseCount);
        tvSmsCount.setText("0");
        tvIncome = (TextView) rootView.findViewById(R.id.forIncome);
        tvExpense = (TextView) rootView.findViewById(R.id.smsParsForExpense);
        templates = new ArrayList<>();
        final List<String> accStrings = new ArrayList<>();
        for (Account ac : daoSession.getAccountDao().loadAll()) {
            accStrings.add(ac.getId());
        }
        final TransferAccountAdapter transferAccountAdapter = new TransferAccountAdapter(getContext(), accStrings);
        spAccount.setAdapter(transferAccountAdapter);
        final List<Account> allAccounts = daoSession.loadAll(Account.class);
        String lastAccountId = preferences.getString("CHOSEN_ACCOUNT_ID",  "");
        if (lastAccountId != null && !lastAccountId.isEmpty()) {
            int position = 0;
            for (int i = 0; i < allAccounts.size(); i++) {
                if (allAccounts.get(i).getId().equals(lastAccountId)) {
                    position = i;
                    break;
                }
            }
            spAccount.setSelection(position);
        }
        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                preferences.edit().putString("CHOSEN_ACCOUNT_ID", allAccounts.get(i).getId()).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final List<String> cursStrings = new ArrayList<>();
        List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
        int main_currency_index = -1;
        for (Currency cr : currencies) {
            cursStrings.add(cr.getAbbr()+" ("+cr.getName()+")");
            if(cr.getIsMain()){
                main_currency_index= currencies.indexOf(cr);
            }
        }
        spCurrency.setAdapter(new SpinnerAdapter(getContext(), (ArrayList) cursStrings));
        if(main_currency_index!=-1)
        spCurrency.setSelection(main_currency_index);
        myAdapter = new MyAdapter(null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setOnTitleClickListener(null);
        toolbarManager.setTitle(getResources().getString(R.string.sms_parse));
        toolbarManager.setSubtitle("");
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldObject != null) {
                    if (templateSmsList != null) {
                        for (TemplateSms templateSms : templateSmsList) {
                            templateSms.setParseObjectId(oldObject.getId());
                            for (SmsParseSuccess smsParseSuccess : oldObject.getSuccessList()) {
                                if (!smsParseSuccess.getIsSuccess() && smsParseSuccess.getBody().matches(templateSms.getRegex())) {
                                    smsParseSuccess.setIsSuccess(true);
                                }
                            }
                        }
                    }
                    oldObject.setCurrency(daoSession.getCurrencyDao().queryBuilder()
                            .where(CurrencyDao.Properties.Abbr.eq("" + spCurrency.getSelectedItem())).list().get(0));
                    oldObject.setAccount(daoSession.getAccountDao().queryBuilder()
                            .where(AccountDao.Properties.Id.eq(accStrings.get(spAccount.getSelectedItemPosition()))).list().get(0));
                    daoSession.getTemplateSmsDao().insertOrReplaceInTx(templateSmsList);
                    daoSession.getSmsParseObjectDao().insertOrReplace(oldObject);
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new SmsParseMainFragment());
                } else {
                    if (etNumber.getText().toString().isEmpty()) {
                        etNumber.setError(getString(R.string.enter_contact_error));
                        return;
                    }
                    if (rbnSmsParseAddAll.isChecked() ) {
                        if (etExpance.getText().toString().isEmpty()) {
                            etExpance.setError(getString(R.string.expense_keyword_error));
                            return;
                        }
                        else if (etIncome.getText().toString().isEmpty()) {
                            etIncome.setError(getString(R.string.income_keyword_error));
                            return;
                        }
                    }
                    else if (rbnSmsParseAddIncome.isChecked() && etIncome.getText().toString().isEmpty()) {
                        etIncome.setError(getString(R.string.income_keyword_error));
                        return;
                    }
                    else if (rbnSmsParseAddExpance.isChecked() && etExpance.getText().toString().isEmpty()) {
                        etExpance.setError(getString(R.string.expense_keyword_error));
                        return;
                    }
                    if (etAmount.getText().toString().isEmpty()) {
                        etNumber.setError(getString(R.string.amount_keyword_error));
                        return;
                    }
                    String[] incomes = etIncome.getText().toString().split(",");
                    String[] expanses = etExpance.getText().toString().split(",");
                    String[] amounts = etAmount.getText().toString().split(",");
                    incomeKeys = incomeKeys == null ? new ArrayList<String>() : incomeKeys;
                    expenseKeys = expenseKeys == null ? new ArrayList<String>() : expenseKeys;
                    amountKeys = amountKeys == null ? new ArrayList<String>() : amountKeys;
                    templateSmsList = templateSmsList == null ? new ArrayList<TemplateSms>() : templateSmsList;
                    List<String> addingIncomes = new ArrayList<>();
                    addingIncomes.addAll(incomeKeys);
                    List<String> addingExpenses = new ArrayList<>();
                    addingExpenses.addAll(expenseKeys);
                    List<String> addingAmounts = new ArrayList<>();
                    addingAmounts.addAll(amountKeys);
                    for (String income : incomes) {
                        boolean found = false;
                        for (String adapter : incomeKeys) {
                            if (income.equals(adapter)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            addingIncomes.add(income);
                        }
                    }

                    for (String expense : expanses) {
                        boolean found = false;
                        for (String adapter : expenseKeys) {
                            if (expense.equals(adapter)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            addingExpenses.add(expense);
                        }
                    }

                    for (String amount : amounts) {
                        boolean found = false;
                        for (String adapter : amountKeys) {
                            if (amount.equals(adapter)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            addingAmounts.add(amount);
                            break;
                        }
                    }
                    if (!incomeKeys.isEmpty() && !expenseKeys.isEmpty() && !addingAmounts.isEmpty())
                        templateSmsList.addAll(commonOperations.generateSmsTemplateList(null, 0, 0, addingIncomes,
                                addingExpenses, addingAmounts));
                    SmsParseObject smsParseObject = new SmsParseObject();
                    smsParseObject.setCurrency(daoSession.loadAll(Currency.class).get(spCurrency.getSelectedItemPosition()));
                    smsParseObject.setAccount(daoSession.getAccountDao().queryBuilder()
                            .where(AccountDao.Properties.Id.eq(accStrings.get(spAccount.getSelectedItemPosition()))).list().get(0));
                    smsParseObject.setNumber(etNumber.getText().toString());
                    daoSession.getSmsParseObjectDao().insertOrReplace(smsParseObject);
                    for (TemplateSms templateSms : templateSmsList)
                        templateSms.setParseObjectId(smsParseObject.getId());
                    daoSession.getTemplateSmsDao().insertInTx(templateSmsList);
                    smsParseObject.resetTemplates();
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new SmsParseMainFragment());

                }
            }
        });
        rvSmsList.setLayoutManager(layoutManager);
        rgSortSms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbnSmsParseAddExpance) {
                    etIncome.setVisibility(View.GONE);
                    tvIncome.setVisibility(View.GONE);
                    etExpance.setVisibility(View.VISIBLE);
                    tvExpense.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rbnSmsParseAddIncome) {
                    etIncome.setVisibility(View.VISIBLE);
                    tvIncome.setVisibility(View.VISIBLE);
                    etExpance.setVisibility(View.GONE);
                    tvExpense.setVisibility(View.GONE);
                } else {
                    etIncome.setVisibility(View.VISIBLE);
                    etExpance.setVisibility(View.VISIBLE);
                    tvIncome.setVisibility(View.VISIBLE);
                    tvExpense.setVisibility(View.VISIBLE);
                }
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        });
        etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_SMS},
                            REQUEST_CODE_ASK_PERMISSIONS);
                } else {
                    List<Sms> smsList = new ArrayList<>();
                    initSms();
                    for (Sms sms : lstSms) {
                        if (sms != null && sms.getNumber().equals(s.toString())) {
                            smsList.add(sms);
                        }
                    }
                    myAdapter = new MyAdapter(smsList);
                    rvSmsList.setAdapter(myAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ivSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_sms_parse_numbers, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.rvSmsParseDialog);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_SMS},
                            REQUEST_CODE_ASK_PERMISSIONS);
                } else {
                    if (all == null || forChoose == null) {
                        initSms();
                    }
                    MyNumberAdapter myAdapter = new MyNumberAdapter(forChoose);
                    recyclerView.setAdapter(myAdapter);
                    int width = getResources().getDisplayMetrics().widthPixels;
                    dialog.getWindow().setLayout(8 * width / 9, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                    dialog.show();
                }
            }
        });
        if (oldObject != null) {
            etNumber.setText(oldObject.getNumber());
            etNumber.setEnabled(false);
            myAdapter.refreshList();
            for (int i = 0; i < cursStrings.size(); i++) {
                if (cursStrings.get(i).equals(oldObject.getCurrency().getAbbr())) {
                    spCurrency.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < accStrings.size(); i++) {
                if (accStrings.get(i).equals(oldObject.getAccountId())) {
                    spAccount.setSelection(i);
                    break;
                }
            }
        }
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSms();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    public void initSms() {
        forChoose = new ArrayList<>();
        all = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
            Cursor cur = getContext().getContentResolver().query(uri, projection, null /*"address='123456789'"*/, null, "date desc");
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");
                do {
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int int_Type = cur.getInt(index_Type);

                    Sms sms = new Sms();
                    sms.setNumber(strAddress);
                    calendar.setTimeInMillis(longDate);
                    sms.setDate(format.format(calendar.getTime()));
                    sms.setId(UUID.randomUUID().toString());
                    sms.setBody(strbody);

                    boolean found = false;
                    for (Sms s : forChoose) {
                        if (s.getNumber().equals(strAddress)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        forChoose.add(sms);
                    all.add(sms);
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } // end if
        }
        catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    private static final int URL_LOADER = 0;
    private Uri message = Uri.parse("content://sms/");
    private static final String[] mProjection = new String[]{"_id", "address", "body", "date", "type"};
    private List<Sms> lstSms = new ArrayList<>();

    class Sms {
        private String body;
        private String number;
        private String id;
        private String date;
        public String getBody() {
            return body;
        }
        public void setBody(String body) {
            this.body = body;
        }
        public String getDate() {
            return date;
        }
        public void setDate(String date) {
            this.date = date;
        }
        public String getNumber() {
            return number;
        }
        public void setNumber(String number) {
            this.number = number;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
    }

    private List<String> smsBodyParse(String body) {
        String anyWordWithoutNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;])";
        String anyNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)";
        String numberWordNumberWord = "([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)";
        String wordNumberWordNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`:;]*)([0-9]+[.,]?[0-9]*)";
        String[] strings = body.split(" ");
        List<String> temp = Arrays.asList(strings);
        for (String s : temp) s.replace("\n", "");
        List<String> words = new ArrayList<>();
        for (int i = temp.size() - 1; i >= 0; i--) {
            Pattern pattern = Pattern.compile(anyWordWithoutNumber);
            Matcher matcher = pattern.matcher(temp.get(i));
            if (matcher.matches()) {
                words.add(matcher.group(1));
                continue;
            }

            pattern = Pattern.compile(anyNumber);
            matcher = pattern.matcher(temp.get(i));
            if (matcher.matches()) {
                words.add(matcher.group(3));
                words.add(matcher.group(2));
                words.add(matcher.group(1));
                continue;
            }
            pattern = Pattern.compile(numberWordNumberWord);
            matcher = pattern.matcher(temp.get(i));
            if (matcher.matches()) {
                words.add(matcher.group(4));
                words.add(matcher.group(3));
                words.add(matcher.group(2));
                words.add(matcher.group(1));
                continue;
            }
            pattern = Pattern.compile(wordNumberWordNumber);
            matcher = pattern.matcher(temp.get(i));
            if (matcher.matches()) {
                words.add(matcher.group(4));
                words.add(matcher.group(3));
                words.add(matcher.group(2));
                words.add(matcher.group(1));
                continue;
            }
            words.add(temp.get(i));
        }
        Collections.reverse(words);
        return words;
    }

    private class MyAdapter extends RecyclerView.Adapter<AddSmsParseFragment.ViewHolder> implements View.OnClickListener {
        private List<Sms> result;
        public MyAdapter(List<Sms> objects) {
            this.result = objects;
            String[] incomeK = etIncome.getText().toString().split(",");
            for (String key : incomeK)
                incomeKeys.add(key);
            String[] expenseK = etExpance.getText().toString().split(",");
            for (String key : expenseK)
                expenseKeys.add(key);
            String[] amountK = etAmount.getText().toString().split(",");
            for (String key : amountK)
                amountKeys.add(key);
        }

        public void refreshList() {
            this.result = choosenSms;
            notifyDataSetChanged();
        }

        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final AddSmsParseFragment.ViewHolder view, final int position) {
            view.body.setText(result.get(position).getBody());
            if (rbnSmsParseAddAll.isChecked()) {
                view.income.setVisibility(View.VISIBLE);
                view.expance.setVisibility(View.VISIBLE);
            } else if (rbnSmsParseAddIncome.isChecked()) {
                view.income.setVisibility(View.VISIBLE);
                view.expance.setVisibility(View.GONE);
            } else {
                view.income.setVisibility(View.GONE);
                view.expance.setVisibility(View.VISIBLE);
            }
            view.income.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogSms(true, position);
                }
            });
            view.expance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogSms(false, position);
                }
            });
        }
        private int measureListText(List<String> list) {
            Paint paint = new Paint();
            Rect rect = new Rect();
            paint.setTextSize(txSize);
            int length = 0;
            for (String s : list) {
                paint.getTextBounds(s, 0, s.length(), rect);
                length += rect.width();
            }
            return length;
        }

        public AddSmsParseFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adding_sms_item, parent, false);
            return new ViewHolder(view);
        }

        TextView amountkey;
        TextView parsingkey;

        private void dialogSms(final boolean type, final int position) {
            posIncExp = -1;
            posAmount = -1;
            dialog = new Dialog(getActivity());
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_parsin_sms_select_word, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            final ImageView close = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
            final ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
            final TextView tvSmsDialogTypeTitle = (TextView) dialogView.findViewById(R.id.tvSmsDialogTypeTitle);
            if (type) {
                tvSmsDialogTypeTitle.setText(getResources().getString(R.string.income_decide_with_static_word));
            } else {
                tvSmsDialogTypeTitle.setText(R.string.expense_decide_with_static_word);
            }
            amountkey = (TextView) dialogView.findViewById(R.id.amountKey);
            parsingkey = (TextView) dialogView.findViewById(R.id.parsingKey);
            final LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.llDialogSmsParseAdd);
            int eni = (int) ((8 * getResources().getDisplayMetrics().widthPixels/10
                    - 2 * commonOperations.convertDpToPixel(40))/getResources().getDisplayMetrics().density);
            splittedBody = smsBodyParse(choosenSms.get(position).getBody());
            tvList = new ArrayList<>();
            Map<Integer, List<String>> map = new TreeMap<>();
            for (int i = splittedBody.size() - 1; i >= 0; i--) {
                if (splittedBody.get(i) == null || splittedBody.get(i).isEmpty()) {
                    splittedBody.remove(i);
                } else
                    splittedBody.set(i, splittedBody.get(i) + " ");
            }
            List<String> tempList = new ArrayList<>();
            int length;
            int row = 1;
            for (int i = 0; i < splittedBody.size(); i++) {
                List<String> temp = new ArrayList<>();
                temp.addAll(tempList);
                temp.add(splittedBody.get(i));
                length = measureListText(temp);
                if (eni > length) {
                    tempList.add(splittedBody.get(i));
                } else {
                    map.put(row++, tempList);
                    tempList = new ArrayList<>();
                    tempList.add(splittedBody.get(i));
                }
                if (i == splittedBody.size() - 1 && !tempList.isEmpty()) {
                    map.put(row++, tempList);
                }
            }
            row = 0;
            for (Integer integer : map.keySet()) {
                List<String> lt = map.get(integer);
                LinearLayout linearLayout1 = new LinearLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout1.setLayoutParams(layoutParams);
                linearLayout.addView(linearLayout1);
                for (int i = 0; i < lt.size(); i++) {
                    TextView textView = new TextView(getContext());
                    textView.setTag(row++);
                    textView.setTextSize(txSize);
                    textView.setBackgroundResource(R.drawable.select_grey);
                    textView.setText(lt.get(i));
                    tvList.add(textView);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins((int) getResources().getDimension(R.dimen.five_dp), 0, 0, 0);
                    textView.setLayoutParams(lp);
                    textView.setOnClickListener(MyAdapter.this);
                    linearLayout1.addView(textView);
                }
            }
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posAmount == -1) {
                        Toast.makeText(getContext(), "Choose amount", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (posIncExp == -1) {
                        Toast.makeText(getContext(), "Choose " + (type ? "income " : "expance " + "key"), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        for (int i = 0; i < splittedBody.size(); i++) {
                            splittedBody.set(i, splittedBody.get(i).trim());
                        }
                        if (type) {
                            boolean found = false;
                            for (String key : incomeKeys) {
                                if (key.equals(splittedBody.get(posIncExp))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                incomeKeys.add(splittedBody.get(posIncExp));
                        }
                        else {
                            boolean found = false;
                            for (String key : expenseKeys) {
                                if (key.equals(splittedBody.get(posIncExp))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                expenseKeys.add(splittedBody.get(posIncExp));
                        }
                        boolean amountKeyDefined = false;
                        int amountKeyPos;
                        String dateRegex = "[0-9]+[.,|/^*~&%@!+()$#-\\/'\"\\{`\\];\\[:][0-9]+[.,|/^*~&%@!+()$#-\\/'\"\\{`\\];\\[:]?[0-9]*";
                        if (posAmount == 0) {
                            amountKeyPos = posAmount+1;
                            while (!amountKeyDefined) {
                                if (amountKeyPos >= splittedBody.size()) break;
                                else {
                                    if (!splittedBody.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else {
                                        amountKeyPos++;
                                    }
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount + 1;
                            }
                        }
                        else if (posAmount > 0 && posAmount < splittedBody.size()-1) {
                            amountKeyPos = posAmount-1;
                            boolean forward = false;
                            while (!amountKeyDefined) {
                                if (amountKeyPos < 0) {
                                    forward = true;
                                    amountKeyPos = posAmount+1;
                                }
                                else if (amountKeyPos >= splittedBody.size()) break;
                                else if (!forward) {
                                    if (!splittedBody.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos--;
                                } else if (forward) {
                                    if (!splittedBody.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos++;
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount - 1;
                            }
                        }
                        else {
                            amountKeyPos = posAmount - 1;
                            while (!amountKeyDefined) {
                                if (amountKeyPos >= splittedBody.size()) break;
                                else {
                                    if (!splittedBody.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos++;
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount-1;
                            }
                        }
                        amountKeys.add(splittedBody.get(amountKeyPos));
                        for (int i = 0; i < incomeKeys.size(); i++) {
                            if (incomeKeys.get(i) == null || incomeKeys.get(i).isEmpty()) {
                                incomeKeys.remove(i);
                                i--;
                            }
                        }
                        for (int i = 0; i < expenseKeys.size(); i++) {
                            if (expenseKeys.get(i) == null || expenseKeys.get(i).isEmpty()) {
                                expenseKeys.remove(i);
                                i--;
                            }
                        }
                        templateSmsList = templateSmsList == null ? new ArrayList<TemplateSms>() : templateSmsList;
                        templateSmsList.addAll(commonOperations.generateSmsTemplateList(splittedBody, posIncExp, posAmount, incomeKeys, expenseKeys, new ArrayList<String>()));
                        for (int i = choosenSms.size() - 1; i >= 0; i--) { //TODO whole checking
                            for (TemplateSms templateSms : templateSmsList) {
                                if (choosenSms.get(i).getBody().matches(templateSms.getRegex())) {
                                    choosenSms.remove(i);
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < incomeKeys.size(); i++) {
                            if (incomeKeys.get(i) == null || incomeKeys.get(i).isEmpty()) {
                                incomeKeys.remove(i);
                                i--;
                            }
                        }
                        for (int i = 0; i < expenseKeys.size(); i++) {
                            if (expenseKeys.get(i) == null || expenseKeys.get(i).isEmpty()) {
                                expenseKeys.remove(i);
                                i--;
                            }
                        }
                        for (int i = 0; i < amountKeys.size(); i++) {
                            if (amountKeys.get(i) == null || amountKeys.get(i).isEmpty()) {
                                amountKeys.remove(i);
                                i--;
                            }
                        }
                        adapter.refreshList();
                        String incs = "";
                        for (String s : incomeKeys) {
                            String divider = incomeKeys.indexOf(s) == incomeKeys.size()-1 ? "" : ", ";
                            incs += s + divider;
                        }
                        String exps = "";
                        for (String expanceKey : expenseKeys) {
                            String divider = expenseKeys.indexOf(expanceKey) == expenseKeys.size()-1 ? "" : ", ";
                            exps += expanceKey + divider;
                        }
                        String ams = "";
                        for (String amountKey : amountKeys) {
                            String divider = amountKeys.indexOf(amountKey) == amountKeys.size()-1 ? "" : ", ";
                            ams += amountKey + divider;
                        }
                        if (!incomeKeys.isEmpty())
                            etIncome.setText(incs.substring(0, incs.length()));
                        if (!expenseKeys.isEmpty())
                            etExpance.setText(exps.substring(0, exps.length()));
                        etAmount.setText(ams.substring(0, ams.length()));
                        tvSmsCount.setText("" + choosenSms.size());
                    }
                    dialog.dismiss();
                }
            });
            int width = getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout(9 * width / 10, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            dialog.show();
        }

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                String regex = "([0-9]+[.,]?[0-9]*\\s*)+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(splittedBody.get((Integer) v.getTag()));
                if (!matcher.matches()) {
                    if (posIncExp != -1) {
                        parsingkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posIncExp).setBackgroundResource(R.drawable.select_grey);
                    }
                    posIncExp = (int) v.getTag();
                    v.setBackgroundResource(R.drawable.select_green);
                    parsingkey.setText(((TextView) v).getText().toString());
                } else {
                    if (posAmount != -1) {
                        amountkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posAmount).setBackgroundResource(R.drawable.select_grey);
                    }
                    posAmount = (int) v.getTag();
                    amountkey.setText(((TextView) v).getText().toString());
                    v.setBackgroundResource(R.drawable.select_yellow);
                }
            }
        }
    }


    private MyAdapter adapter;
    private void refreshProcessingSmsList(String number) {
        if (all == null)
            initSms();
        choosenSms = new ArrayList<>();
        for (Sms sms : all) {
            if (sms.getNumber().equals(number))
                choosenSms.add(sms);
        }
        adapter = new MyAdapter(choosenSms);
        rvSmsList.setAdapter(adapter);
        tvSmsCount.setText(Integer.toString(choosenSms.size()));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView body;
        public TextView income;
        public TextView expance;
        public ViewHolder(View view) {
            super(view);
            body = (TextView) view.findViewById(R.id.tvAddSmsParseItemBody);
            income = (TextView) view.findViewById(R.id.tvAddSmsParseItemIncome);
            expance = (TextView) view.findViewById(R.id.tvAddSmsParseItemExpance);
        }
    }

    private class MyNumberAdapter extends RecyclerView.Adapter<AddSmsParseFragment.ViewHolderNumber> {
        private List<Sms> result;
        public MyNumberAdapter(List<Sms> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ViewHolderNumber view, final int position) {
            view.number.setText(result.get(position).getNumber());
            view.date.setText(result.get(position).getDate());
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etNumber.setText(result.get(position).getNumber());
                    refreshProcessingSmsList(result.get(position).getNumber());
                    dialog.dismiss();
                }
            });
        }
        public AddSmsParseFragment.ViewHolderNumber onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_sms_parsing, parent, false);
            return new AddSmsParseFragment.ViewHolderNumber(view);
        }
    }

    public class ViewHolderNumber extends RecyclerView.ViewHolder {
        private TextView number;
        private TextView date;

        public ViewHolderNumber(View view) {
            super(view);
            number = (TextView) view.findViewById(R.id.tvSmsParseDialogItemNumber);
            date = (TextView) view.findViewById(R.id.tvSmsParseDialogItemDate);
        }
    }
}