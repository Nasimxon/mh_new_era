package com.jim.finansia.debt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.Person;
import com.jim.finansia.database.Recking;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.CurrencySpinnerAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SpinnerAdapter;
import com.jim.finansia.utils.cache.DataCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by user on 6/4/2016.
 */
@SuppressLint("ValidFragment")
public class AddBorrowFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject DaoSession daoSession;
    @Inject DataCache dataCache;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject ReportManager reportManager;
    @Inject SharedPreferences preferences;
    @Inject FinansiaFirebaseAnalytics analytics;
    private int mode = PocketAccounterGeneral.NO_MODE;
    private FrameLayout contactBtn;
    private CircleImageView civImage;
    private EditText etPersonName, etPersonNumber;
    private String personPhotoPath = "";
    private EditText etTakenDate;
    private EditText etReturnDate;
    private EditText etDebtSum;
    private Spinner spDebtBorrowCurrency;
    private Spinner spDebtBorrowAccount;
    private Calendar takenDate;
    private Calendar returnDate;
    private SwitchCompat scDebtBorrowCalculation;
    private String notifMode = PocketAccounterGeneral.EVERY_DAY, sequence = "";
    private RecyclerView rvNorify;
    private Spinner spNotifMode;
    private int type = 0;
    private LinearLayout llAccountHolder;
    private int position = 0;

    private static final int REQUEST_SELECT_CONTACT = 2;
    public static int RESULT_LOAD_IMAGE = 1;
    private final int PERMISSION_REQUEST_CONTACT = 5;
    private int PICK_CONTACT = 10;
    String sequence2 = "";
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private final int PERMISSION_READ_STORAGE = 6;
    private DebtBorrow currentDebtBorrow;
    RecyclerView.LayoutManager layoutManager;
    private AddBorrowFragment.DaysAdapter daysAdapter;
    private ArrayList<String> adapter;
    private int localAppereance = DebtBorrowFragment.FROM_MAIN;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.add_borrow_fragment_layout_mod, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        if (getArguments() != null) {
            String debtBorrowId = getArguments().getString(DebtBorrowFragment.DEBT_BORROW_ID);
            if (debtBorrowId != null)
                currentDebtBorrow = daoSession.load(DebtBorrow.class, debtBorrowId);
            mode = getArguments().getInt(DebtBorrowFragment.MODE);
            position = getArguments().getInt(DebtBorrowFragment.POSITION);
            type = getArguments().getInt(DebtBorrowFragment.TYPE);
            localAppereance = getArguments().getInt(DebtBorrowFragment.LOCAL_APPEREANCE);
        }
        spNotifMode = (Spinner) view.findViewById(R.id.spNotifMode);
        adapter = new ArrayList<>();
        adapter.add(getResources().getString(R.string.notif_everyday));
        adapter.add(getResources().getString(R.string.notif_weekly));
        adapter.add(getResources().getString(R.string.notif_monthly));
        spNotifMode.setAdapter(new SpinnerAdapter(getContext(), adapter));
        spNotifMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        rvNorify.setVisibility(View.GONE);
                        break;
                    case 1:
                        rvNorify.setVisibility(View.VISIBLE);
                        daysAdapter = new AddBorrowFragment.DaysAdapter(notifMode);
                        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                        rvNorify.setLayoutManager(layoutManager);
                        rvNorify.setAdapter(daysAdapter);
                        break;
                    case 2:
                        rvNorify.setVisibility(View.VISIBLE);
                        daysAdapter = new AddBorrowFragment.DaysAdapter(notifMode);
                        layoutManager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);
                        rvNorify.setLayoutManager(layoutManager);
                        rvNorify.setAdapter(daysAdapter);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        contactBtn = (FrameLayout) view.findViewById(R.id.btBorrowAddPopupContact);
        civImage = (CircleImageView) view.findViewById(R.id.ivBorrowAddPopup);
        etPersonName = (EditText) view.findViewById(R.id.etBorrowAddPopupName);
        etPersonNumber = (EditText) view.findViewById(R.id.etBorrowAddPopupNumber);
        etTakenDate = (EditText) view.findViewById(R.id.etBorrowAddPopupDataGet);
        etReturnDate = (EditText) view.findViewById(R.id.etBorrowAddPopupDataRepeat);
        etDebtSum = (EditText) view.findViewById(R.id.etBorrowAddPopupSumm);
        spDebtBorrowCurrency = (Spinner) view.findViewById(R.id.spBorrowAddPopupValyuta);
        spDebtBorrowAccount = (Spinner) view.findViewById(R.id.spDebtBorrowAccount);
        scDebtBorrowCalculation = (SwitchCompat) view.findViewById(R.id.chbAddDebtBorrowCalculate);
        rvNorify = (RecyclerView) view.findViewById(R.id.rvAddAutoMarketPerItems);
        takenDate = mode == PocketAccounterGeneral.NO_MODE ? Calendar.getInstance() : dataCache.getEndDate();
        llAccountHolder = (LinearLayout) view.findViewById(R.id.llAccountHolder);
        if (type == DebtBorrow.DEBT) {
            etDebtSum.setHint(getResources().getString(R.string.enter_borrow_amoount));
            ((TextView) view.findViewById(R.id.summ_zayma)).setText(R.string.amount);
        }
        view.findViewById(R.id.checkInclude).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scDebtBorrowCalculation.toggle();
            }
        });
        spDebtBorrowAccount.setOnItemSelectedListener(this);
        spDebtBorrowCurrency.setOnItemSelectedListener(this);
        final List<Account> allAccounts = daoSession.loadAll(Account.class);
        final ArrayList accounts = new ArrayList();
        for (int i = 0; i < allAccounts.size(); i++) {
            accounts.add(allAccounts.get(i).getId());
        }
        List<Currency> allCurrencies = daoSession.loadAll(Currency.class);
        ArrayList currencies = new ArrayList();
        ArrayList currenciesName = new ArrayList();
        for (int i = 0; i < allCurrencies.size(); i++) {
            currencies.add(allCurrencies.get(i).getAbbr());
            currenciesName.add(allCurrencies.get(i).getName());
        }
        spDebtBorrowAccount.setAdapter(new TransferAccountAdapter(getContext(), accounts));
        spDebtBorrowCurrency.setAdapter(new CurrencySpinnerAdapter(getContext(), currencies, currenciesName));
        String lastAccountId = preferences.getString("CHOSEN_ACCOUNT_ID",  "");
        if (lastAccountId != null && !lastAccountId.isEmpty()) {
            int position = 0;
            for (int i = 0; i < allAccounts.size(); i++) {
                if (allAccounts.get(i).getId().equals(lastAccountId)) {
                    position = i;
                    break;
                }
            }
            spDebtBorrowAccount.setSelection(position);
        }
        spDebtBorrowAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                preferences.edit().putString("CHOSEN_ACCOUNT_ID", allAccounts.get(i).getId()).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        int posMain = 0;
        for (int i = 0; i < allCurrencies.size(); i++) {
            if (allCurrencies.get(i).getId().equals(commonOperations.getMainCurrency().getId())) {
                posMain = i;
                break;
            }
        }
        spDebtBorrowCurrency.setSelection(posMain);
        etTakenDate.setText(sDateFormat.format(takenDate.getTime()));
        etTakenDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar calender = (Calendar) takenDate.clone();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            getDatesetListener, calender.get(Calendar.YEAR),
                            calender.get(Calendar.MONTH), calender
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
                }
                return true;
            }
        });

        etReturnDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar calender = Calendar.getInstance();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            returnDatesetListener, calender.get(Calendar.YEAR),
                            calender.get(Calendar.MONTH), calender
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
                    return true;
                }
                return false;
            }
        });
        scDebtBorrowCalculation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llAccountHolder.setVisibility(View.VISIBLE);
                } else {
                    llAccountHolder.setVisibility(View.GONE);
                }
            }
        });
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForContactPermission();
            }
        });
        civImage.setImageResource(R.drawable.no_photo);
        civImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(((PocketAccounter) getContext()),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                .setTitle("Permission required");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_READ_STORAGE);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                } else {
                    getPhoto();
                }
            }
        });

        if (currentDebtBorrow != null) {
            etPersonName.setText(currentDebtBorrow.getPerson().getName());
            etPersonNumber.setText(currentDebtBorrow.getPerson().getPhoneNumber());
            for (int i = 0; i < allCurrencies.size(); i++) {
                if (allCurrencies.get(i).getId().equals(currentDebtBorrow.getCurrency().getId())) {
                    spDebtBorrowCurrency.setSelection(i);
                    break;
                }
            }
            if (currentDebtBorrow.getCalculate())
                for (int i = 0; i < allAccounts.size(); i++) {
                    if (allAccounts.get(i).equals(currentDebtBorrow.getAccount().getId())) {
                        spDebtBorrowAccount.setSelection(i);
                        break;
                    }
                }
            scDebtBorrowCalculation.setChecked(currentDebtBorrow.getCalculate());
            etDebtSum.setText(String.valueOf(currentDebtBorrow.getAmount()));
            etTakenDate.setText(sDateFormat.format(currentDebtBorrow.getTakenDate().getTime()));
            takenDate = (Calendar) currentDebtBorrow.getTakenDate().clone();
            if (currentDebtBorrow.getReturnDate() != null) {
                returnDate = (Calendar) currentDebtBorrow.getReturnDate().clone();
                etReturnDate.setText(sDateFormat.format(currentDebtBorrow.getReturnDate().getTime()));
            }
            if (!currentDebtBorrow.getPerson().getPhoto().isEmpty() && !currentDebtBorrow.getPerson().getPhoto().equals("0")) {
                try {
                    civImage.setImageBitmap(queryContactImage(Integer.parseInt(currentDebtBorrow.getPerson().getPhoto())));
                } catch (Exception e) {
                    civImage.setImageBitmap(decodeFile(new File(currentDebtBorrow.getPerson().getPhoto())));
                }
                personPhotoPath = currentDebtBorrow.getPerson().getPhoto();
            } else {
                civImage.setImageResource(R.drawable.no_photo);
            }

        }
        return view;
    }

    private DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            takenDate.set(year, monthOfYear, dayOfMonth);
            if (returnDate != null && takenDate.compareTo(returnDate) > 0) {
                returnDate = takenDate;
                etReturnDate.setText(sDateFormat.format(returnDate.getTime()));
            }
            etTakenDate.setText(sDateFormat.format(takenDate.getTime()));
        }
    };
    private DatePickerDialog.OnDateSetListener returnDatesetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            returnDate = (Calendar) takenDate.clone();
            returnDate.set(year, monthOfYear, dayOfMonth);
            if (returnDate.compareTo(takenDate) < 0) {
                returnDate = takenDate;
            }
            etReturnDate.setText(sDateFormat.format(returnDate.getTime()));
            adapter.clear();
            int countOfDays = (int) Math.ceil((returnDate.getTimeInMillis() - takenDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
            if (countOfDays < 7) {
                adapter.add(getResources().getString(R.string.notif_everyday));
            } else if (countOfDays <= 30) {
                adapter.add(getResources().getString(R.string.notif_everyday));
                adapter.add(getResources().getString(R.string.notif_weekly));
            } else if (countOfDays > 30) {
                adapter.add(getResources().getString(R.string.notif_everyday));
                adapter.add(getResources().getString(R.string.notif_weekly));
                adapter.add(getResources().getString(R.string.notif_monthly));
            }
            spNotifMode.setAdapter(new SpinnerAdapter(getContext(), adapter));
        }
    };


    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setTitle(getResources().getString(R.string.addedit));
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    saveAndExit();
                }
            });
        }
    }
    private void saveAndExit() {
        if (etPersonName.getText().toString().isEmpty()) {
            etPersonName.setError(getString(R.string.enter_name_error));
            return;
        }
        String sum = etDebtSum.getText().toString();
        sum.replace(",", ".");
        if (sum == null || sum.isEmpty()) {
            etDebtSum.setError(getResources().getString(R.string.enter_amount_error));
            return;
        }
        preferences.edit().putBoolean(PocketAccounterGeneral.FIRST_DEBT_BORROW, false).commit();
        List<DebtBorrow> list = daoSession.loadAll(DebtBorrow.class);
        List<Currency> allCurrencies = daoSession.loadAll(Currency.class);
        List<Account> allAccounts = daoSession.loadAll(Account.class);
        Currency currency = allCurrencies.get(spDebtBorrowCurrency.getSelectedItemPosition());
        Account account = allAccounts.get(spDebtBorrowAccount.getSelectedItemPosition());
        if (account != null && (account.getIsLimited() || account.getNoneMinusAccount())) {
            double limit = account.getLimite();
            double accounted = logicManager.isLimitAccess(account, takenDate);
            if (type == DebtBorrow.DEBT) {
                accounted = accounted + commonOperations.getCost(Calendar.getInstance(), currency, account.getCurrency(), Double.parseDouble(etDebtSum.getText().toString()));
            } else {
                accounted = accounted - commonOperations.getCost(Calendar.getInstance(), currency, account.getCurrency(), Double.parseDouble(etDebtSum.getText().toString()));
            }
            if (account.getNoneMinusAccount()) {
                if (accounted < 0) {
                    Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (-limit > accounted) {
                    Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        File file = null;
        if (personPhotoPath != null && !personPhotoPath.equals("")) {
            try {
                Integer.parseInt(personPhotoPath);
            } catch (Exception e) {
                Bitmap bitmap = decodeFile(new File(personPhotoPath));
                Bitmap C;
                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    C = Bitmap.createBitmap(
                            bitmap,
                            bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                            0,
                            bitmap.getHeight(),
                            bitmap.getHeight()
                    );
                } else {
                    C = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                            bitmap.getWidth(),
                            bitmap.getWidth()
                    );
                }
                try {
                    file = new File(getContext().getFilesDir(), Uri.parse(personPhotoPath).getLastPathSegment());
                    FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
                    C.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (currentDebtBorrow != null) {
            if (scDebtBorrowCalculation.isChecked()) {
                currentDebtBorrow.setCalculate(true);
                currentDebtBorrow.setAccount(account);
            }
            else {
                currentDebtBorrow.setCalculate(false);
                currentDebtBorrow.setAccount(null);
            }
            currentDebtBorrow.getPerson().setName(etPersonName.getText().toString());
            currentDebtBorrow.getPerson().setPhoneNumber(etPersonNumber.getText().toString());
            currentDebtBorrow.getPerson().setPhoto(file != null ? file.getAbsolutePath() : personPhotoPath.equals("") ? "" : personPhotoPath);
            currentDebtBorrow.setAmount(Double.parseDouble(etDebtSum.getText().toString()));
            currentDebtBorrow.setCurrency(currency);
            currentDebtBorrow.setInfo(mode + ":" + sequence);
            currentDebtBorrow.setReturnDate(returnDate);
            currentDebtBorrow.setTakenDate(takenDate);
            currentDebtBorrow.__setDaoSession(daoSession);
            if (scDebtBorrowCalculation.isChecked() && !isLimiteAccess()) {
                Toast.makeText(getContext(), "Сумма превышает лимит счета", Toast.LENGTH_SHORT).show();
                return;
            } else {
                logicManager.insertPerson(currentDebtBorrow.getPerson());
                logicManager.insertDebtBorrow(currentDebtBorrow);
            }
        } else {
            Person person = new Person(etPersonName.getText().toString(),
                    etPersonNumber.getText().toString(), file != null ? file.getAbsolutePath() : personPhotoPath == "" ? "" : personPhotoPath);
            logicManager.insertPerson(person);
            currentDebtBorrow = new DebtBorrow(person,
                    takenDate,
                    returnDate,
                    "borrow_" + UUID.randomUUID().toString(),
                    account,
                    currency,
                    Double.parseDouble(etDebtSum.getText().toString()),
                    type,
                    scDebtBorrowCalculation.isChecked()
            );
            if (scDebtBorrowCalculation.isChecked() && !isLimiteAccess()) {
                Toast.makeText(getContext(), "Сумма превышает лимит счета", Toast.LENGTH_SHORT).show();
                return;
            } else {
                logicManager.insertDebtBorrow(currentDebtBorrow);
            }
            currentDebtBorrow.setInfo(mode + ":" + sequence);
            list.add(0, currentDebtBorrow);
        }
        logicManager.insertDebtBorrow(currentDebtBorrow);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap temp = null;
        if (!currentDebtBorrow.getPerson().getPhoto().isEmpty()) {
            try {
                temp = queryContactImage(Integer.parseInt(currentDebtBorrow.getPerson().getPhoto()));
            } catch (NumberFormatException e) {
                temp = BitmapFactory.decodeFile(currentDebtBorrow.getPerson().getPhoto());
            }
        }
        if (temp == null)
            temp = BitmapFactory.decodeResource(getResources(), R.drawable.no_photo, options);
        temp = Bitmap.createScaledBitmap(temp, (int) getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), false);

        if (mode == PocketAccounterGeneral.MAIN || mode == PocketAccounterGeneral.EXPANSE_MODE || mode == PocketAccounterGeneral.INCOME_MODE) {
            paFragmentManager.getFragmentManager().popBackStack();
            int table = mode == PocketAccounterGeneral.INCOME_MODE ? PocketAccounterGeneral.INCOME : PocketAccounterGeneral.EXPENSE;
            logicManager.changeBoardButton(table, position, currentDebtBorrow.getId());
            List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
                    .where(BoardButtonDao.Properties.Table.eq(table),
                            BoardButtonDao.Properties.Pos.eq(position)).list();
            if (!buttons.isEmpty()) {
                dataCache.getBoardBitmapsCache().put(buttons.get(0).getId(), temp);
            }
            reportManager.clearCache();
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsPageChanges();
            paFragmentManager.displayMainWindow();
        }
        else {
            List<BoardButton> buttons = daoSession
                                        .getBoardButtonDao()
                                        .queryBuilder()
                                        .where(BoardButtonDao.Properties.CategoryId.eq(currentDebtBorrow.getId()))
                                        .list();
            if (!buttons.isEmpty()) {
                for (BoardButton button : buttons) {
                    dataCache.getBoardBitmapsCache().put(button.getId(), temp);
                }
            }
            reportManager.clearCache();
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsPageChanges();
            paFragmentManager.updateVoiceRecognizePageCurrencyChanges();
            for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                if (fragment == null) continue;
                if (fragment instanceof  BorrowFragment) {
                    BorrowFragment borrowFragment = (BorrowFragment) fragment;
                    if (borrowFragment != null)
                        borrowFragment.refreshList();
                }
            }
            paFragmentManager.getFragmentManager().popBackStack();
            if (localAppereance == DebtBorrowFragment.FROM_INFO) {
                boolean found = false;
                for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                    if (fragment == null) continue;
                    if (fragment instanceof  BorrowFragment) {
                        BorrowFragment borrowFragment = (BorrowFragment) fragment;
                        if (borrowFragment != null) {
                            borrowFragment.refreshList();
                            found = true;
                        }
                    }
                    if (fragment instanceof DebtBorrowFragment) {
                        if (fragment == null) continue;
                        DebtBorrowFragment borrowFragment = (DebtBorrowFragment) fragment;
                        if (borrowFragment != null) {
                            borrowFragment.updateToolbar();
                            found = true;
                        }
                    }
                }
                paFragmentManager.getFragmentManager().popBackStack();
                if (!found) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new DebtBorrowFragment());
                }
            }
            if (localAppereance == DebtBorrowFragment.FROM_MAIN)
            {
                boolean found = false;
                for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                    if (fragment == null) continue;
                    if (fragment instanceof  BorrowFragment) {
                        BorrowFragment borrowFragment = (BorrowFragment) fragment;
                        if (borrowFragment != null) {
                            borrowFragment.refreshList();
                            found = true;
                        }
                    }
                    if (fragment instanceof DebtBorrowFragment) {
                        DebtBorrowFragment borrowFragment = (DebtBorrowFragment) fragment;
                        if (borrowFragment != null) {
                            borrowFragment.updateToolbar();
                            found = true;
                        }
                    }
                }
                if (!found) {
                    paFragmentManager.displayFragment(new DebtBorrowFragment());
                }
            }
        }
    }

    public int getLocalAppereance() {
        return localAppereance;
    }

    private boolean isLimiteAccess() {
        Account account = daoSession.loadAll(Account.class).get(spDebtBorrowAccount.getSelectedItemPosition());
        if (account != null && (account.getIsLimited() || account.getNoneMinusAccount())) {
            double limit = account.getLimite();
            double accounted = logicManager.isLimitAccess(account, takenDate);
            if (account.getNoneMinusAccount()) {
                if (accounted < 0) {
                    Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                if (-limit > accounted) {
                    Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    private Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            final int REQUIRED_SIZE = 128;
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_ID
            };
            Cursor cursor = getContext().getContentResolver().query(contactUri, projection,
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                personPhotoPath = String.valueOf(cursor.getInt(photoIndex));
                if (queryContactImage(cursor.getInt(photoIndex)) != null)
                    civImage.setImageBitmap(queryContactImage(cursor.getInt(photoIndex)));
                else
                    civImage.setImageResource(R.drawable.no_photo);
                etPersonName.setText(name);
                etPersonNumber.setText(number);
            }
        }
        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            personPhotoPath = picturePath;
            civImage.setImageBitmap(decodeFile(new File(personPhotoPath)));
        }
    }

    private Bitmap queryContactImage(int imageDataRow) {
        Cursor c = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                ContactsContract.CommonDataKinds.Photo.PHOTO
        }, ContactsContract.Data._ID + "=?", new String[]{
                Integer.toString(imageDataRow)
        }, null);
        byte[] imageBytes = null;
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0);
            }
            c.close();
        }
        if (imageBytes != null) {
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }

    public void askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.contact_access_needed);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(R.string.please_confirm_contact_access);//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);
                }
            } else {
                getContact();
            }
        } else {
            getContact();
        }
    }

    private void getContact() {
        PocketAccounter.openActivity = true;
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void getPhoto() {
        PocketAccounter.openActivity = true;
        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContact();
                } else {
                }
                return;
            }
            case PERMISSION_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhoto();
                }
                break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
    private class DaysAdapter extends RecyclerView.Adapter<AddBorrowFragment.ViewHolderDialog> {
        private String[] days;
        private boolean selection[];
        public DaysAdapter(String mode) {
            sequence2 = "";
            if (mode.equals(PocketAccounterGeneral.EVERY_DAY)) {
                days = getResources().getStringArray(R.array.week_day_auto);
            } else {
                days = new String[31];
                for (int i = 0; i < days.length; i++) {
                    days[i] = i < 9 ? "" + (i + 1) : "" + (i + 1);
                }
            }
            selection = new boolean[days.length];
        }
        @Override
        public int getItemCount() {
            return days.length;
        }
        public void onBindViewHolder(final AddBorrowFragment.ViewHolderDialog view, final int position) {
            if (position % 7 == 0) {
                view.frameLayout.setVisibility(View.GONE);
            }
            view.day.setText(days[position]);
            if (selection[position])
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
                    if (!selection[position]) {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                        view.day.setTypeface(null, Typeface.BOLD);

                    } else {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.black_for_secondary_text));
                        view.day.setTypeface(null, Typeface.NORMAL);

                    }
                    selection[position] = !selection[position];
                }
            });
        }
        public AddBorrowFragment.ViewHolderDialog onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_month_layout, parent, false);
            return new AddBorrowFragment.ViewHolderDialog(view);
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

    public int getMode() {
        return mode;
    }
}