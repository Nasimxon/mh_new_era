package com.jim.pocketaccounter.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.CurrencyDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.SmsParseObject;
import com.jim.pocketaccounter.database.SmsParseSuccess;
import com.jim.pocketaccounter.database.TemplateSms;
import com.jim.pocketaccounter.finance.TransferAccountAdapter;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Created by root on 9/29/16.
 */

@SuppressLint("ValidFragment")
public class AddSmsParseFragment extends PABaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
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

    private final int ALL_SMS = 0;
    private final int INCOME_SMS = 1;
    private final int EXPANCE_SMS = 2;
    private int posIncExp = -1;
    private int posAmount = -1;
    private SmsParseObject oldObject;
    private List<TemplateSms> templateSmsList;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    int txSize;

    public AddSmsParseFragment(SmsParseObject object) {
        this.oldObject = object;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_sms_sender, container, false);
        txSize = (int) ((int) (getResources().getDimension(R.dimen.fourteen_dp)) / getResources().getDisplayMetrics().density);
        etNumber = (EditText) rootView.findViewById(R.id.etSmsParseAddNumber);
        ivSms = (TextView) rootView.findViewById(R.id.ivSmsParseGet);
        rgSortSms = (RadioGroup) rootView.findViewById(R.id.rgSmsParseAddSort);
        rvSmsList = (RecyclerView) rootView.findViewById(R.id.rvSmsParseAdd);
        etIncome = (EditText) rootView.findViewById(R.id.etSmsParseAddIncome);
        etExpance = (EditText) rootView.findViewById(R.id.etSmsParseAddExpance);
        etAmount = (EditText) rootView.findViewById(R.id.etSmsParseAddAmount);
        spAccount = (Spinner) rootView.findViewById(R.id.spSmsParseAddAccount);
        spCurrency = (Spinner) rootView.findViewById(R.id.spSmsParseAddCurrency);
        tvSmsCount = (TextView) rootView.findViewById(R.id.tvAddSmsParseCount);
        tvSmsCount.setText("0");
        final List<String> accStrings = new ArrayList<>();
        for (Account ac : daoSession.getAccountDao().loadAll()) {
            accStrings.add(ac.getId());
        }
        final TransferAccountAdapter transferAccountAdapter = new TransferAccountAdapter(getContext(), accStrings);
        spAccount.setAdapter(transferAccountAdapter);
        List<String> cursStrings = new ArrayList<>();
        for (Currency cr : daoSession.getCurrencyDao().loadAll()) {
            cursStrings.add(cr.getAbbr());
        }
        ArrayAdapter<String> cursAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.spiner_gravity_right, cursStrings);
        spCurrency.setAdapter(cursAdapter);
        int posMain = 0;
        for (int i = 0; i < cursStrings.size(); i++) {
            if (cursStrings.get(i).equals(commonOperations.getMainCurrency().getAbbr())) {
                posMain = i;
            }
        }
        spCurrency.setSelection(posMain);
        myAdapter = new MyAdapter(ALL_SMS, null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
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
                    } else if (etIncome.getText().toString().isEmpty() &&
                            etExpance.getText().toString().isEmpty() || etAmount.getText().toString().isEmpty()) {
                        etIncome.setError(getString(R.string.income_keyword_error));
                        if (etExpance.getText().toString().isEmpty())
                            etExpance.setError(getString(R.string.expense_keyword_error));
                        if (etAmount.getText().toString().isEmpty()) {
                            etExpance.setError(getString(R.string.amount_keyword_error));
                        }
                    } else {
                        String[] incomes = etIncome.getText().toString().split(",");
                        String[] expanses = etExpance.getText().toString().split(",");
                        String[] amounts = etAmount.getText().toString().split(",");
                        boolean change = false;
                        for (String income : incomes) {
                            boolean found = false;
                            for (String adapter : myAdapter.getIncomeKeys()) {
                                if (income.equals(adapter)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                change = true;
                                break;
                            }
                        }
                        if (change) {
                            templateSmsList.addAll(commonOperations.generateSmsTemplateList(null, 0, 0, myAdapter.getIncomeKeys(),
                                    myAdapter.getExpanceKeys(), myAdapter.getAmountKeys()));
                        }
                        change = false;
                        for (String expense : expanses) {
                            boolean found = false;
                            for (String adapter : myAdapter.getExpanceKeys()) {
                                if (expense.equals(adapter)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                change = true;
                                break;
                            }
                        }
                        if (change) {
                            templateSmsList.addAll(commonOperations.generateSmsTemplateList(null, 0, 0, myAdapter.getIncomeKeys(),
                                    myAdapter.getExpanceKeys(), myAdapter.getAmountKeys()));
                        }
                        change = false;
                        for (String amount : amounts) {
                            boolean found = false;
                            for (String adapter : myAdapter.getAmountKeys()) {
                                if (amount.equals(adapter)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                change = true;
                                break;
                            }
                        }
                        if (change) {
                            templateSmsList.addAll(commonOperations.generateSmsTemplateList(null, 0, 0, myAdapter.getIncomeKeys(),
                                    myAdapter.getExpanceKeys(), myAdapter.getAmountKeys()));
                        }

                        SmsParseObject smsParseObject = new SmsParseObject();
                        if (templateSmsList != null) {
                            for (TemplateSms templateSms : templateSmsList)
                                templateSms.setParseObjectId(smsParseObject.getId());
                        }
                        smsParseObject.setCurrency(daoSession.getCurrencyDao().queryBuilder()
                                .where(CurrencyDao.Properties.Abbr.eq("" + spCurrency.getSelectedItem())).list().get(0));
                        smsParseObject.setAccount(daoSession.getAccountDao().queryBuilder()
                                .where(AccountDao.Properties.Id.eq(accStrings.get(spAccount.getSelectedItemPosition()))).list().get(0));
                        smsParseObject.setNumber(etNumber.getText().toString());
                        daoSession.getTemplateSmsDao().insertInTx(templateSmsList);
                        daoSession.getSmsParseObjectDao().insertOrReplace(smsParseObject);
                        paFragmentManager.getFragmentManager().popBackStack();
                        paFragmentManager.displayFragment(new SmsParseMainFragment());
                    }
                }
            }
        });
        rvSmsList.setLayoutManager(layoutManager);
        rgSortSms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (myAdapter.getTypeSort() != (checkedId == R.id.rbnSmsParseAddExpance ? EXPANCE_SMS
                        : checkedId == R.id.rbnSmsParseAddIncome ? INCOME_SMS : ALL_SMS)) {
                    if (checkedId == R.id.rbnSmsParseAddExpance) {
                        myAdapter.setType(EXPANCE_SMS);
                    } else if (checkedId == R.id.rbnSmsParseAddIncome) {
                        myAdapter.setType(INCOME_SMS);
                    } else {
                        myAdapter.setType(ALL_SMS);
                    }
                }
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
                    getAllSms();
                    for (Sms sms : lstSms) {
                        if (sms != null && sms.getNumber().equals(s.toString())) {
                            smsList.add(sms);
                        }
                    }
                    myAdapter = new MyAdapter(myAdapter.getTypeSort(), smsList);
                    rvSmsList.setAdapter(myAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
                    MyNumberAdapter myAdapter = new MyNumberAdapter();
                    recyclerView.setAdapter(myAdapter);
                    int width = getResources().getDisplayMetrics().widthPixels;
                    dialog.getWindow().setLayout(8 * width / 10, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                    dialog.show();
                }
            }
        });
        if (oldObject != null) {
            etNumber.setText(oldObject.getNumber());
            etNumber.setEnabled(false);
            myAdapter.oldTemplateChange();
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
                getAllSms();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void getAllSms() {
        if (lstSms.isEmpty())
            getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private static final int URL_LOADER = 0;
    private Uri message = Uri.parse("content://sms/");
    private static final String[] mProjection = new String[]{"_id", "address", "body", "date", "type"};
    private List<Sms> lstSms = new ArrayList<>();

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        message,        // Table to query
                        mProjection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.isClosed()) {
//            data.
        }
        int count = data.getCount();
        Sms objSms;

        if (data.moveToFirst()) {
            for (int i = 0; i < count; i++) {
                objSms = new Sms();
                objSms.setId(data.getString(data.getColumnIndexOrThrow("_id")));
                objSms.setNumber(data.getString(data.getColumnIndexOrThrow("address")));
                objSms.setBody(data.getString(data.getColumnIndexOrThrow("body")));
                objSms.setDate(data.getString(data.getColumnIndexOrThrow("date")));
                if (data.getString(data.getColumnIndexOrThrow("type")).contains("1")) {
                objSms.setFolderName("sent");
                lstSms.add(objSms);
                }
            }
            data.moveToNext();
        }
        data.close();
        getActivity().getSupportLoaderManager().destroyLoader(URL_LOADER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    class Sms {
        private String body;
        private String number;
        private String id;
        private String folderName;
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

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }
    }

    private List<String> smsBodyParse(String body) {
        List<String> words = new ArrayList<>();
        String[] strings = body.split(" ");

        for (String s : strings) {
            if (s.split(" ").length == 1 && s.split("\n").length == 1) {
                words.add(s);
            } else {
                if (s.split(" ").length == 1) {
                    for (String s1 : s.split("\n")) {
                        words.add(s1);
                    }
                } else {
                    for (String s1 : s.split(" ")) {
                        words.add(s1);
                    }
                }
            }
        }
        for (int i = words.size() - 1; i >= 0; i--) {
            String regex = "[a-zA-Z:;_][0-9]?[0-9][.,@#*]([1][0-2][0]?[0-9])[0-9]{2,4}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(words.get(i));
            if (!matcher.matches()) {
                regex = "(\\s*[^0-9]*)([0-9]+[.,]?[0-9]*)([^0-9]*)";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(words.get(i));
                matcher.matches();
                if (matcher.matches()) {
                    words.remove(i);
                    if (!matcher.group(3).isEmpty())
                        words.add(i, matcher.group(3));
                    if (!matcher.group(2).isEmpty())
                        words.add(i, matcher.group(2));
                    if (!matcher.group(1).isEmpty())
                        words.add(i, matcher.group(1));
                }
            }
        }
        for (int i = words.size() - 1; i > 0; i--) {
            String regex = "([0-9]+[.,]?[0-9]*\\s*)*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(words.get(i));
            if (matcher.matches() && pattern.matcher(words.get(i - 1)).matches()) {
                words.set(i - 1, words.get(i - 1) + " " + words.get(i));
                words.remove(i);
            }
        }
        return words;
    }

    private class MyAdapter extends RecyclerView.Adapter<AddSmsParseFragment.ViewHolder> implements View.OnClickListener {
        private int typeSort;
        private List<Sms> list;
        private List<String> strings;
        private List<TextView> tvList;
        private List<String> incomeKeys;
        private List<String> expanceKeys;
        private List<String> amountKeys;
        private List<String> amountKeyOld;

        public MyAdapter(int typeSort, List<Sms> list) {
            this.typeSort = typeSort;
            this.list = list;
            if (list != null)
                tvSmsCount.setText("" + list.size());
            incomeKeys = new ArrayList<>();
            expanceKeys = new ArrayList<>();
            amountKeys = new ArrayList<>();
            templateSmsList = new ArrayList<>();
            amountKeyOld = new ArrayList<>();
        }

        public void oldTemplateChange() {
            if (oldObject.getTemplates() != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    for (TemplateSms templateSms : oldObject.getTemplates()) {
                        if (list.get(i).getBody().matches(templateSms.getRegex())) {
                            list.remove(i);
                        }
                    }
                }
            }
            notifyDataSetChanged();
            templateSmsList.addAll(oldObject.getTemplates());
            tvSmsCount.setText("" + list.size());
        }

        public int getTypeSort() {
            return typeSort;
        }

        public void setType(int typeSms) {
            this.typeSort = typeSms;
            notifyDataSetChanged();
        }

        public List<String> getIncomeKeys() {
            return incomeKeys;
        }

        public List<String> getExpanceKeys() {
            return expanceKeys;
        }

        public List<String> getAmountKeys() {
            return amountKeys;
        }

        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public void onBindViewHolder(final AddSmsParseFragment.ViewHolder view, final int position) {
            view.body.setText(list.get(position).getBody());
            if (typeSort == ALL_SMS) {
                view.income.setVisibility(View.VISIBLE);
                view.expance.setVisibility(View.VISIBLE);
            } else if (typeSort == INCOME_SMS) {
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

            int eni = (int) ((8 * getResources().getDisplayMetrics().widthPixels / 10
                    - 2 * commonOperations.convertDpToPixel(40)) / getResources().getDisplayMetrics().density);

            strings = smsBodyParse(list.get(position).getBody());
            tvList = new ArrayList<>();

            Map<Integer, List<String>> map = new TreeMap<>();

            for (int i = strings.size() - 1; i >= 0; i--) {
                if (strings.get(i).isEmpty()) {
                    strings.remove(i);
                } else
                    strings.set(i, strings.get(i) + " ");
            }

            List<String> tempList = new ArrayList<>();
            int length;
            int row = 1;
            for (int i = 0; i < strings.size(); i++) {
                List<String> temp = new ArrayList<>();
                temp.addAll(tempList);
                temp.add(strings.get(i));
                length = measureListText(temp);
                if (eni > length) {
                    tempList.add(strings.get(i));
                } else {
                    map.put(row++, tempList);
                    tempList = new ArrayList<>();
                    tempList.add(strings.get(i));
                }
                if (i == strings.size() - 1 && !tempList.isEmpty()) {
                    map.put(row++, tempList);
                }
            }
            row = 1;
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
                    } else if (posIncExp == -1) {
                        Toast.makeText(getContext(), "Choose " + (type ? "income " : "expance " + "key"), Toast.LENGTH_SHORT).show();
                    } else {
                        for (int i = 0; i < strings.size(); i++) {
                            strings.set(i, strings.get(i).trim());
                        }
                        if (type)
                            incomeKeys.add(strings.get(posIncExp));
                        else
                            expanceKeys.add(strings.get(posIncExp));
                        amountKeys.add(strings.get(posAmount));
                        if (posAmount != 0) {
                            amountKeyOld.add(strings.get(posAmount - 1));
                        } else {
                            amountKeyOld.add(strings.get(position + 1));
                        }
                        templateSmsList = commonOperations.generateSmsTemplateList(strings, posIncExp, posAmount, incomeKeys, expanceKeys, new ArrayList<String>());
                        for (int i = list.size() - 1; i >= 0; i--) {
                            for (TemplateSms templateSms : templateSmsList) {
                                if (list.get(i).getBody().matches(templateSms.getRegex())) {
                                    list.remove(i);
                                    break;
                                }
                            }
                        }
                        String incs = "";
                        for (String s : incomeKeys) {
                            incs += s + ", ";
                        }
                        String exps = "";
                        for (String expanceKey : expanceKeys) {
                            exps += expanceKey + ", ";
                        }
                        String ams = "";
                        for (String amountKey : amountKeyOld) {
                            ams += amountKey + ", ";
                        }
                        if (!incomeKeys.isEmpty())
                            etIncome.setText(incs.substring(0, incs.length() - 1));
                        if (!expanceKeys.isEmpty())
                            etExpance.setText(exps.substring(0, exps.length() - 1));
                        etAmount.setText(ams.substring(0, ams.length() - 1));
                        notifyDataSetChanged();
                        tvSmsCount.setText("" + list.size());
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
                Matcher matcher = pattern.matcher(strings.get((Integer) v.getTag() - 1));
                if (!matcher.matches() && !strings.get((int) v.getTag() - 1).matches("\\s?[0-9]+\\s?")) {
                    if (posIncExp != -1) {
                        parsingkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posIncExp).setBackgroundResource(R.drawable.select_grey);
                    }
                    posIncExp = (int) v.getTag() - 1;
                    v.setBackgroundResource(R.drawable.select_green);
                    parsingkey.setText(((TextView) v).getText().toString());
                } else {
                    if (posAmount != -1) {
                        amountkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posAmount).setBackgroundResource(R.drawable.select_grey);
                    }
                    posAmount = (int) v.getTag() - 1;
                    amountkey.setText(((TextView) v).getText().toString());
                    v.setBackgroundResource(R.drawable.select_yellow);
                }
            }
        }
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
        private List<Sms> smsList;

        public MyNumberAdapter() {
            getAllSms();
            smsList = lstSms;
        }

        public int getItemCount() {
            return smsList.size();
        }

        public void onBindViewHolder(final ViewHolderNumber view, final int position) {
            view.number.setText(smsList.get(position).getNumber());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date date = new Date();
            date.setTime(Long.parseLong(smsList.get(position).getDate()));
            view.date.setText(simpleDateFormat.format(date.getTime()));
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etNumber.setText(smsList.get(position).getNumber());
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