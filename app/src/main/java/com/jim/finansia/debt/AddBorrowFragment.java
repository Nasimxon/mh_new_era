package com.jim.finansia.debt;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
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

public class AddBorrowFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    @Inject
    CommonOperations commonOperations;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    LogicManager logicManager;
    @Inject
    DaoSession daoSession;
    @Inject
    DataCache dataCache;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    ReportManager reportManager;
    private DebtBorrowDao debtBorrowDao;
    private AccountDao accountDao;
    private CurrencyDao currencyDao;

    private FrameLayout contactBtn;
    private CircleImageView imageView;
    private EditText PersonName;
    private EditText PersonNumber;
    private EditText PersonDataGet;
    private EditText PersonDataRepeat;
    private EditText PersonSumm;
    private RecyclerView rvNorify;
    private Spinner PersonValyuta;
    private Spinner PersonAccount;
    private String photoPath = "";
    private Calendar getDate;
    private Calendar returnDate;
    private SwitchCompat calculate;
    private RelativeLayout isCalcRelativeLayout;
    private int TYPE = 0;
    private static final int REQUEST_SELECT_CONTACT = 2;
    public static int RESULT_LOAD_IMAGE = 1;
    private ImageView ivToolbarMostRight;
    private final int PERMISSION_REQUEST_CONTACT = 5;
    private int PICK_CONTACT = 10;
    String sequence2 = "";
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private final int PERMISSION_READ_STORAGE = 6;
    private DebtBorrow currentDebtBorrow;
    RecyclerView.LayoutManager layoutManager;
    private Spinner spNotifMode;
    private AddBorrowFragment.DaysAdapter daysAdapter;
    private ArrayList<String> adapter;
    private String mode = PocketAccounterGeneral.EVERY_DAY, sequence = "";
    private BoardButton boardButton;
    public static AddBorrowFragment getInstance(int type, DebtBorrow debtBorrow) {
        AddBorrowFragment fragment = new AddBorrowFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        if (debtBorrow != null) {
            bundle.putString("key", debtBorrow.getId());
            mainView = true;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    private static boolean mainView = false;
    private int posMain = -1;

    public AddBorrowFragment setMainView(BoardButton boardButton) {
        this.boardButton = boardButton;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        debtBorrowDao = daoSession.getDebtBorrowDao();
        try {
            this.currentDebtBorrow = debtBorrowDao.load(getArguments().getString("key"));
            TYPE = getArguments().getInt("type", 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        debtBorrowDao = daoSession.getDebtBorrowDao();
        accountDao = daoSession.getAccountDao();
        currencyDao = daoSession.getCurrencyDao();
        adapter = new ArrayList<>();
    }

    private DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            getDate.set(year, monthOfYear, dayOfMonth);
            if (returnDate != null && getDate.compareTo(returnDate) > 0) {
                returnDate = getDate;
                PersonDataRepeat.setText(sDateFormat.format(returnDate.getTime()));
            }
            PersonDataGet.setText(sDateFormat.format(getDate.getTime()));
        }
    };
    private DatePickerDialog.OnDateSetListener returnDatesetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            returnDate = (Calendar) getDate.clone();
            returnDate.set(year, monthOfYear, dayOfMonth);
            if (returnDate.compareTo(getDate) < 0) {
                returnDate = getDate;
            }
            PersonDataRepeat.setText(sDateFormat.format(returnDate.getTime()));
            adapter.clear();
            int countOfDays = (int) Math.ceil((returnDate.getTimeInMillis() - getDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
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
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), R.layout.spiner_gravity_left, adapter);
            spNotifMode.setAdapter(adapter1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.add_borrow_fragment_layout_mod, container, false);
        spNotifMode = (Spinner) view.findViewById(R.id.spNotifMode);

        adapter.add(getResources().getString(R.string.notif_everyday));
        adapter.add(getResources().getString(R.string.notif_weekly));
        adapter.add(getResources().getString(R.string.notif_monthly));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), R.layout.spiner_gravity_left, adapter);
        spNotifMode.setAdapter(adapter1);
        spNotifMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        rvNorify.setVisibility(View.GONE);
                        break;
                    case 1:
                        rvNorify.setVisibility(View.VISIBLE);
                        daysAdapter = new AddBorrowFragment.DaysAdapter(0);
                        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                        rvNorify.setLayoutManager(layoutManager);
                        rvNorify.setAdapter(daysAdapter);

                        break;

                    case 2:

                        rvNorify.setVisibility(View.VISIBLE);
                        daysAdapter = new AddBorrowFragment.DaysAdapter(1);
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
        imageView = (CircleImageView) view.findViewById(R.id.ivBorrowAddPopup);
        PersonName = (EditText) view.findViewById(R.id.etBorrowAddPopupName);
        PersonNumber = (EditText) view.findViewById(R.id.etBorrowAddPopupNumber);
        PersonDataGet = (EditText) view.findViewById(R.id.etBorrowAddPopupDataGet);
        PersonDataRepeat = (EditText) view.findViewById(R.id.etBorrowAddPopupDataRepeat);
        PersonSumm = (EditText) view.findViewById(R.id.etBorrowAddPopupSumm);
        PersonValyuta = (Spinner) view.findViewById(R.id.spBorrowAddPopupValyuta);
        PersonAccount = (Spinner) view.findViewById(R.id.spInfoDebtBorrowAccount);
        calculate = (SwitchCompat) view.findViewById(R.id.chbAddDebtBorrowCalculate);
        rvNorify = (RecyclerView) view.findViewById(R.id.rvAddAutoMarketPerItems);
        isCalcRelativeLayout = (RelativeLayout) view.findViewById(R.id.is_calc);
        getDate = paFragmentManager.isMainReturn() ? dataCache.getEndDate() : Calendar.getInstance();
        if (TYPE == DebtBorrow.DEBT) {
            PersonSumm.setHint(getResources().getString(R.string.enter_borrow_amoount));
            ((TextView) view.findViewById(R.id.summ_zayma)).setText(R.string.amount_borrow);
        }
        view.findViewById(R.id.checkInclude).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate.toggle();
            }
        });

        PersonAccount.setOnItemSelectedListener(this);
        PersonValyuta.setOnItemSelectedListener(this);
        String[] accaounts = new String[accountDao.queryBuilder().list().size()];
        for (int i = 0; i < accaounts.length; i++) {
            accaounts[i] = accountDao.queryBuilder().list().get(i).getName();
        }
        String[] valyuts = new String[currencyDao.queryBuilder().list().size()];
        for (int i = 0; i < valyuts.length; i++) {
            valyuts[i] = currencyDao.queryBuilder().list().get(i).getAbbr();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(), R.layout.spiner_gravity_left, accaounts);

        ArrayAdapter<String> arrayValyuAdapter = new ArrayAdapter<>(
                getContext(), R.layout.spiner_gravity_right, valyuts);

        arrayAdapter.setDropDownViewResource(
                R.layout.spiner_gravity_left);
        PersonAccount.setAdapter(arrayAdapter);

        arrayValyuAdapter.setDropDownViewResource(
                R.layout.spinner_single_item);
        PersonValyuta.setAdapter(arrayValyuAdapter);
        int posMain = 0;
        for (int i = 0; i < valyuts.length; i++) {
            if (valyuts[i].equals(commonOperations.getMainCurrency().getAbbr())) {
                posMain = i;
            }
        }
        PersonValyuta.setSelection(posMain);
        PersonDataGet.setText(sDateFormat.format(getDate.getTime()));
        PersonDataGet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar calender = paFragmentManager.isMainReturn() ? dataCache.getEndDate() : Calendar.getInstance();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            getDatesetListener, calender.get(Calendar.YEAR),
                            calender.get(Calendar.MONTH), calender
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
                }
                return true;
            }
        });

        PersonDataRepeat.setOnTouchListener(new View.OnTouchListener() {
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

        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);

        calculate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (calculate.isChecked()) {
                    isCalcRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    isCalcRelativeLayout.setVisibility(View.GONE);
                }
            }
        });

        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                saveAndExit();
            }
        });

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForContactPermission();
            }
        });

        imageView.setImageResource(R.drawable.no_photo);

        imageView.setOnClickListener(new View.OnClickListener() {
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
            PersonName.setText(currentDebtBorrow.getPerson().getName());
            PersonNumber.setText(currentDebtBorrow.getPerson().getPhoneNumber());
            for (int i = 0; i < valyuts.length; i++) {
                if (valyuts[i].matches(currentDebtBorrow.getCurrency().getAbbr())) {
                    PersonValyuta.setSelection(i);
                    break;
                }
            }
            if (currentDebtBorrow.getCalculate())
                for (int i = 0; i < accaounts.length; i++) {
                    if (accaounts[i].matches(currentDebtBorrow.getAccount().getName())) {
                        PersonAccount.setSelection(i);
                        break;
                    }
                }
            calculate.setChecked(currentDebtBorrow.getCalculate());
            PersonSumm.setText(String.valueOf(currentDebtBorrow.getAmount()));
            PersonDataGet.setText(sDateFormat.format(currentDebtBorrow.getTakenDate().getTime()));
            getDate = (Calendar) currentDebtBorrow.getTakenDate().clone();
            if (currentDebtBorrow.getReturnDate() != null) {
                returnDate = (Calendar) currentDebtBorrow.getReturnDate().clone();
                PersonDataRepeat.setText(sDateFormat.format(currentDebtBorrow.getReturnDate().getTime()));
            }
            if (!currentDebtBorrow.getPerson().getPhoto().isEmpty()) {
                try {
                    imageView.setImageBitmap(queryContactImage(Integer.parseInt(currentDebtBorrow.getPerson().getPhoto())));
                } catch (Exception e) {
                    imageView.setImageBitmap(decodeFile(new File(currentDebtBorrow.getPerson().getPhoto())));
                }
                photoPath = currentDebtBorrow.getPerson().getPhoto();
            }

        }
        return view;
    }

    private void saveAndExit() {
        try {
            Double.parseDouble(PersonSumm.getText().toString());
            PersonSumm.setError(null);
        } catch (Exception e) {
            PersonSumm.setError(getString(R.string.invalide_format));
            return;
        }
        if (PersonName.getText().toString().equals("")) {
            PersonName.setError(getString(R.string.enter_name_error));
        } else {
            if (PersonSumm.getText().toString().equals("")) {
                PersonSumm.setError(getString(R.string.enter_amount_error));
            } else {
                if (PersonDataGet.getText().toString().matches("")) {
                    PersonDataGet.setError(getString(R.string.enter_takendate_error));
                } else {
                    ArrayList<DebtBorrow> list = (ArrayList<DebtBorrow>) debtBorrowDao.queryBuilder().list();
                    Currency currency = currencyDao.queryBuilder().list().get(PersonValyuta.getSelectedItemPosition());
                    ArrayList<Recking> reckings = new ArrayList<>();
                    Account account = accountDao.queryBuilder().list().get(PersonAccount.getSelectedItemPosition());
                    File file = null;
                    if (!photoPath.matches("")) {
                        try {
                            Integer.parseInt(photoPath);
                        } catch (Exception e) {
                            Bitmap bitmap = decodeFile(new File(photoPath));
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
                                file = new File(getContext().getFilesDir(), Uri.parse(photoPath).getLastPathSegment());
                                FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
                                C.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    DebtBorrowFragment fragment = new DebtBorrowFragment();
                    if (currentDebtBorrow != null) {
                        if (calculate.isChecked())
                            currentDebtBorrow.setAccount(account);
                        currentDebtBorrow.getPerson().setName(PersonName.getText().toString());
                        currentDebtBorrow.getPerson().setPhoneNumber(PersonNumber.getText().toString());
                        currentDebtBorrow.getPerson().setPhoto(file != null ? file.getAbsolutePath() : photoPath == "" ? "" : photoPath);

                        currentDebtBorrow.setAmount(Double.parseDouble(PersonSumm.getText().toString()));
                        currentDebtBorrow.setCurrency(currency);
                        currentDebtBorrow.setCalculate(calculate.isChecked());
                        currentDebtBorrow.setInfo(mode + ":" + sequence);
                        currentDebtBorrow.setReturnDate(returnDate);
                        currentDebtBorrow.setTakenDate(getDate);
                        currentDebtBorrow.__setDaoSession(daoSession);
                        if (!isMumkin(currentDebtBorrow)) {
                            return;
                        } else {
                            logicManager.insertPerson(currentDebtBorrow.getPerson());
                            logicManager.insertDebtBorrow(currentDebtBorrow);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putInt("pos", currentDebtBorrow.getType());
                        fragment.setArguments(bundle);
                    } else {
                        Person person = new Person(PersonName.getText().toString(),
                                PersonNumber.getText().toString(), file != null ? file.getAbsolutePath() : photoPath == "" ? "" : photoPath);
                        logicManager.insertPerson(person);
                        currentDebtBorrow = new DebtBorrow(person,
                                getDate,
                                returnDate,
                                "borrow_" + UUID.randomUUID().toString(),
                                account,
                                currency,
                                Double.parseDouble(PersonSumm.getText().toString()),
                                TYPE, calculate.isChecked()
                        );
                        if (!isMumkin(currentDebtBorrow)) {
                            return;
                        } else {
                            logicManager.insertDebtBorrow(currentDebtBorrow);
                        }

                        currentDebtBorrow.setInfo(mode + ":" + sequence);
                        list.add(0, currentDebtBorrow);
                        Bundle bundle = new Bundle();
                        fragment.setArguments(bundle);
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

                    if (boardButton != null) {
                        paFragmentManager.getFragmentManager().popBackStack();
                        logicManager.changeBoardButton(boardButton.getTable(), boardButton.getPos(), currentDebtBorrow.getId());
                        if (!mainView) {
                            List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
                                    .where(BoardButtonDao.Properties.Table.eq(boardButton.getTable()),
                                            BoardButtonDao.Properties.Pos.eq(boardButton.getPos())).list();
                            for (BoardButton boardButton : buttons) {
                                dataCache.getBoardBitmapsCache().put(boardButton.getId(), temp);
                            }
                        } else {
                            List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
                                    .where(BoardButtonDao.Properties.CategoryId.eq(currentDebtBorrow.getId()))
                                    .list();
                            for (BoardButton button : buttons) {
                                dataCache.getBoardBitmapsCache().put(button.getId(), temp);
                            }
                        }
                        mainView = false;
                        reportManager.refreshDatas();
                        paFragmentManager.displayMainWindow();
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        paFragmentManager.setMainReturn(false);
                    }
                    else {
                        List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
                                .where(BoardButtonDao.Properties.CategoryId.eq(currentDebtBorrow.getId()))
                                .list();
                        if (!buttons.isEmpty()) {
                            for (BoardButton button : buttons) {
                                dataCache.getBoardBitmapsCache().put(button.getId(), temp);
                            }
                            dataCache.updateAllPercents();
                            paFragmentManager.updateAllFragmentsOnViewPager();
                        }
                        reportManager.refreshDatas();
                        paFragmentManager.displayFragment(fragment);
                    }

//                    if (!paFragmentManager.isMainReturn()) {
//                        List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
//                                .where(BoardButtonDao.Properties.CategoryId.eq(currentDebtBorrow.getId()))
//                                .list();
//                        if (!buttons.isEmpty()) {
//                            for (BoardButton button : buttons) {
//                                dataCache.getBoardBitmapsCache().put(button.getId(), temp);
//                            }
//                            dataCache.updateAllPercents();
//                            paFragmentManager.updateAllFragmentsOnViewPager();
//                        }
//                        paFragmentManager.displayFragment(fragment);
//                    } else {
//                        paFragmentManager.getFragmentManager().popBackStack();
//                        logicManager.changeBoardButton(MAINTYPE, posMain, currentDebtBorrow.getId());
//                        if (!mainView) {
//                            List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
//                                    .where(BoardButtonDao.Properties.Table.eq(MAINTYPE),
//                                            BoardButtonDao.Properties.Pos.eq(posMain)).list();
//                            for (BoardButton boardButton : buttons) {
//                                dataCache.getBoardBitmapsCache().put(boardButton.getId(), temp);
//                            }
//                        } else {
//                            List<BoardButton> buttons = daoSession.getBoardButtonDao().queryBuilder()
//                                    .where(BoardButtonDao.Properties.CategoryId.eq(currentDebtBorrow.getId()))
//                                    .list();
//                            for (BoardButton button : buttons) {
//                                dataCache.getBoardBitmapsCache().put(button.getId(), temp);
//                            }
//                        }
//                        mainView = false;
//                        paFragmentManager.displayMainWindow();
//                        dataCache.updateAllPercents();
//                        paFragmentManager.updateAllFragmentsOnViewPager();
//                        paFragmentManager.setMainReturn(false);
//                    }
                }
            }
        }
    }

    private boolean isMumkin(DebtBorrow debt) {
        Account account = accountDao.queryBuilder().list().get(PersonAccount.getSelectedItemPosition());
        if (account != null && (account.getIsLimited() || account.getNoneMinusAccount())) {
            double limit = account.getLimite();
            double accounted = logicManager.isLimitAccess(account, getDate);

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

                                          dialog.dismiss();
                                      }
                                  }
        );
        ImageView btnNo = (ImageView) dialogView.findViewById(R.id.ivAccountClose);
        btnNo.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         dialog.dismiss();
                                     }
                                 }

        );
        dialog.show();
    }

    private Bitmap decodeFile(File f) {
        try {
//            Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
//            The new size we want to scale to
            final int REQUIRED_SIZE = 128;
//            Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            //Decode with inSampleSize
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
//             Get the URI and query the content provider for the phone number
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
                photoPath = String.valueOf(cursor.getInt(photoIndex));
                if (queryContactImage(cursor.getInt(photoIndex)) != null)
                    imageView.setImageBitmap(queryContactImage(cursor.getInt(photoIndex)));
                else
                    imageView.setImageResource(R.drawable.no_photo);
                PersonName.setText(name);
                PersonNumber.setText(number);
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
            photoPath = picturePath;
            imageView.setImageBitmap(decodeFile(new File(photoPath)));
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
//            if (currentCredit != null) {
//                String [] dates = currentCredit.get().split(",");
//                for (int i = 0; i < days.length; i++) {
//                    for (String date : dates) {
//                        if (days[i].matches(date)) {
//                            tek[i] = true;
//                            break;
//                        }
//                    }
//                }
//            }
        }

        public void getResult() {
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    sequence2 = sequence2 + days[i] + ",";
                }
            }
        }

        public String posDays() {
            String posDay = "";
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    posDay +=i + ",";
                }
            }
            return posDay;
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
}