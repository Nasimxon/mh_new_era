package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.finance.AccountChoiseDialogAdapter;
import com.jim.finansia.finance.ChoiseCategoryDialoogItemAdapter;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.photocalc.PhotoAdapter;
import com.jim.finansia.utils.GetterAttributColors;
import com.jim.finansia.utils.OnSubcategorySavingListener;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SubCatAddEditDialog;
import com.jim.finansia.utils.cache.DataCache;
import com.transitionseverywhere.AutoTransition;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.jim.finansia.debt.AddBorrowFragment.RESULT_LOAD_IMAGE;
import static com.jim.finansia.photocalc.PhotoAdapter.REQUEST_DELETE_PHOTOS;

@SuppressLint("ValidFragment")
public class RecordEditFragment extends Fragment implements OnClickListener {
    private boolean keyforback = false;
    private TextView tvRecordEditDisplay;
    private ImageView ivRecordEditCategory, ivRecordEditSubCategory;
    private Spinner spRecordEdit, spToolbar;
    private RootCategory category, cameCategory;
    private SubCategory subCategory;
    private FinanceRecord record;
    private Currency currency;
    private Account account;
    private Calendar date;
    private ImageView choosePhoto;
    private int parent;
    private int[] numericButtons = {R.id.rlZero, R.id.rlOne, R.id.rlTwo, R.id.rlThree, R.id.rlFour, R.id.rlFive, R.id.rlSix, R.id.rlSeven, R.id.rlEight, R.id.rlNine};
    private int[] operatorButtons = {R.id.rlPlusSign, R.id.rlMinusSign, R.id.rlMultipleSign, R.id.rlDivideSign};
    private boolean lastNumeric = true;
    private boolean stateError;
    private boolean lastDot;
    private boolean lastOperator;
    private DecimalFormat decimalFormat = null;
    private RelativeLayout rlCategory, rlSubCategory;
    private Animation buttonClick;
    private TextView tvAccountName;
    private TextView comment;
    private TextView tvRecordEditCategoryName;
    private TextView tvRecordEditSubCategoryName;
    private EditText comment_add;
    private String commentBackRoll;
    private ImageView ivBackspaceSign;
    private ImageView ivCommentButton;
    private String oraliqComment = "";
    private ImageView ivClear;
    private ImageView ivAccountIcon;
    private RelativeLayout rvAccountChoise;
    boolean keykeboard = false;
    boolean keyForDesideOpenSubCategoryDialog = false;
    private boolean keyForDeleteAllPhotos = true;
    static final int REQUEST_IMAGE_CAPTURE = 112;
    private String uid_code;
    RecyclerView myListPhoto;
    ArrayList<PhotoDetails> myTickets;
    ArrayList<PhotoDetails> myTicketsFromBackRoll;
    PhotoAdapter myTickedAdapter;
    boolean fromEdit = false;
    boolean openAddingDialog = false;
    View mainView;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 18;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    DaoSession daoSession;
    @Inject
    LogicManager logicManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    SubCatAddEditDialog subCatAddEditDialog;
    @Inject
    DataCache dataCache;
    @Inject
    ReportManager reportManager;
    @SuppressLint("ValidFragment")
    public RecordEditFragment(RootCategory category, Calendar date, FinanceRecord record, int parent) {
        this.parent = parent;
        this.date = (Calendar) date.clone();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        decimalFormat = new DecimalFormat("0.00##", otherSymbols);
        this.cameCategory = category;
        this.record = record;
    }

    public interface OpenIntentFromAdapter {
        void startActivityFromFragmentForResult(Intent intent);
    }

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if (parent == PocketAccounterGeneral.MAIN)
            paFragmentManager.setMainReturn(true);
        mainView = inflater.inflate(R.layout.record_edit, container, false);
//        this.date = dataCache.getEndDate();
        if (cameCategory != null) {

            Query<RootCategory> query = daoSession.getRootCategoryDao().queryBuilder()
                    .where(RootCategoryDao.Properties.Id.eq(cameCategory.getId())).build();
            if (!query.list().isEmpty())
                category = query.list().get(0);
            this.subCategory = null;
        } else {
            this.category = record.getCategory();
            this.subCategory = record.getSubCategory();
        }

        toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openAddingDialog) {
                    closeLayout();
                    return;
                } else {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm == null) return;
                    imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((PocketAccounter) getContext()).getSupportFragmentManager().popBackStack();
                            if (parent == PocketAccounterGeneral.MAIN) {
                                paFragmentManager.displayMainWindow();
                            } else {
                                paFragmentManager.displayFragment(new RecordDetailFragment(date));
                            }
                        }
                    }, 100);
                }
            }
        });
        ivAccountIcon = (ImageView) mainView.findViewById(R.id.ivAccountIcon);
        tvAccountName = (TextView) mainView.findViewById(R.id.tvAccountName);
        rvAccountChoise = (RelativeLayout) mainView.findViewById(R.id.rvAccountChoise);

        final List<Account> accountList = daoSession.getAccountDao().loadAll();
        account = accountList.get(0);
        int resId2 = getResources().getIdentifier(account.getIcon(), "drawable", getContext().getPackageName());
        ivAccountIcon.setImageResource(resId2);
        tvAccountName.setText(account.getName());


        rvAccountChoise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                View v = dialog.getWindow().getDecorView();
                v.setBackgroundResource(android.R.color.transparent);
                final ArrayList<Object> subCategories = new ArrayList<>();
                subCategories.add(category);
                for (int i = 0; i < category.getSubCategories().size(); i++)
                    subCategories.add(category.getSubCategories().get(i));
                subCategories.add(null);
                dialogView.findViewById(R.id.llToolBars).setVisibility(View.GONE);

                TextView title = (TextView) dialogView.findViewById(R.id.title);
                title.setText(R.string.choise_account_f);
                RecyclerView rvCategoryChoose = (RecyclerView) dialogView.findViewById(R.id.lvCategoryChoose);
                AccountChoiseDialogAdapter adapter = new AccountChoiseDialogAdapter(accountList, getContext(), new AccountChoiseDialogAdapter.OnItemSelectListner() {
                    @Override
                    public void onItemSelect(Account fromDialog) {
                        int resId = getResources().getIdentifier(fromDialog.getIcon(), "drawable", getContext().getPackageName());
                        ivAccountIcon.setImageResource(resId);
                        tvAccountName.setText(fromDialog.getName());
                        account = fromDialog;
                        dialog.dismiss();
                    }
                });
                dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                rvCategoryChoose.setLayoutManager(new LinearLayoutManager(getContext()));
                rvCategoryChoose.setHasFixedSize(true);
                rvCategoryChoose.setAdapter(adapter);
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int hieght = displayMetrics.heightPixels;
                dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
                dialog.show();








            }
        });

        ivClear = (ImageView) mainView.findViewById(R.id.ivClear);
        ivBackspaceSign = (ImageView) mainView.findViewById(R.id.ivBackspaceSign);
        choosePhoto = (ImageView) mainView.findViewById(R.id.choose_photo);
        spRecordEdit = (Spinner) mainView.findViewById(R.id.spRecordEdit);
        final List<Currency> currencyList = daoSession.getCurrencyDao().loadAll();
        final String[] currencies = new String[currencyList.size()];
        for (int i = 0; i < currencyList.size(); i++)
            currencies[i] = currencyList.get(i).getAbbr();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_single_item_calc, currencies);
        spRecordEdit.setAdapter(adapter);
        for (int i = 0; i < currencyList.size(); i++) {
            if (currencyList.get(i).getIsMain()) {
                spRecordEdit.setSelection(i);
                break;
            }
        }
        spRecordEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = currencyList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        comment = (TextView) mainView.findViewById(R.id.textView18);
        ivCommentButton = (ImageView) mainView.findViewById(R.id.comment_opener);
        comment_add = (EditText) mainView.findViewById(R.id.comment_add);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        uid_code = "record_" + UUID.randomUUID().toString();
        buttonClick = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);

        ivRecordEditCategory = (ImageView) mainView.findViewById(R.id.ivRecordEditCategory);
        ivRecordEditSubCategory = (ImageView) mainView.findViewById(R.id.ivRecordEditSubCategory);
        tvRecordEditDisplay = (TextView) mainView.findViewById(R.id.tvRecordEditDisplay);
        tvRecordEditCategoryName = (TextView) mainView.findViewById(R.id.tvRecordEditCategoryName);
        tvRecordEditSubCategoryName = (TextView) mainView.findViewById(R.id.tvRecordEditSubCategoryName);
        rlCategory = (RelativeLayout) mainView.findViewById(R.id.rlCategory);
        rlCategory.setOnClickListener(this);
        rlSubCategory = (RelativeLayout) mainView.findViewById(R.id.rlSubcategory);
        rlSubCategory.setOnClickListener(this);
        setNumericOnClickListener(mainView);
        setOperatorOnClickListener(mainView);
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("0.00", otherSymbols);
        if (category != null) {
            ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
            tvRecordEditSubCategoryName.setText(R.string.no_category_name);
            int resId = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
            tvRecordEditCategoryName.setText(category.getName());
            ivRecordEditCategory.setImageResource(resId);
        }
        if (record != null) {
            fromEdit = true;
            int resId = getResources().getIdentifier(record.getCategory().getIcon(), "drawable", getContext().getPackageName());
            ivRecordEditCategory.setImageResource(resId);
            tvRecordEditCategoryName.setText(record.getCategory().getName());
            if (record.getSubCategory() != null) {
                resId = getResources().getIdentifier(record.getSubCategory().getIcon(), "drawable", getContext().getPackageName());
                ivRecordEditSubCategory.setImageResource(resId);
                tvRecordEditSubCategoryName.setText(record.getSubCategory().getName());
            } else{
                ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                tvRecordEditSubCategoryName.setText(R.string.no_category_name);

            }
            tvRecordEditDisplay.setText(decimalFormat.format(record.getAmount()));

            for (int i = 0; i < currencyList.size(); i++) {
                if (currencyList.get(i).getId().matches(record.getCurrency().getId())) {
                    spRecordEdit.setSelection(i);
                    break;
                }
            }

            myTickets = (ArrayList<PhotoDetails>) record.getAllTickets()/*.clone()*/;
            myTicketsFromBackRoll = (ArrayList<PhotoDetails>) myTickets.clone();
            if (record.getComment() != null && !record.getComment().matches("")) {
                oraliqComment = record.getComment();
                comment.setText(oraliqComment);
                comment_add.setText(oraliqComment);
            }
        }
        if (myTickets == null)
            myTickets = new ArrayList<>();
        if (myTicketsFromBackRoll == null)
            myTicketsFromBackRoll = new ArrayList<>();
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myListPhoto = (RecyclerView) mainView.findViewById(R.id.recycler_calc);
        myListPhoto.setLayoutManager(layoutManager);
        myTickedAdapter = new PhotoAdapter(myTickets, getContext(), new OpenIntentFromAdapter() {
            @Override
            public void startActivityFromFragmentForResult(Intent intent) {
                PocketAccounter.openActivity = true;
                startActivityForResult(intent, PhotoAdapter.REQUEST_DELETE_PHOTOS);
            }
        });
        myListPhoto.setAdapter(myTickedAdapter);
        return mainView;
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
            toolbarManager.setTitle("");
            toolbarManager.setSubtitle("");
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        }
    }
    private void setNumericOnClickListener(View view) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (tvRecordEditDisplay.getText().toString().length() >= 14) return;
                String text = "";
                switch (v.getId()) {
                    case R.id.rlZero:
                        text = "0";
                        break;
                    case R.id.rlOne:
                        text = "1";
                        break;
                    case R.id.rlTwo:
                        text = "2";
                        break;
                    case R.id.rlThree:
                        text = "3";
                        break;
                    case R.id.rlFour:
                        text = "4";
                        break;
                    case R.id.rlFive:
                        text = "5";
                        break;
                    case R.id.rlSix:
                        text = "6";
                        break;
                    case R.id.rlSeven:
                        text = "7";
                        break;
                    case R.id.rlEight:
                        text = "8";
                        break;
                    case R.id.rlNine:
                        text = "9";
                        break;
                }
                if (stateError) {
                    tvRecordEditDisplay.setText(text);
                    stateError = false;
                } else {
                    String displayText = tvRecordEditDisplay.getText().toString();
                    if (displayText.matches("") || displayText.matches("0"))
                        tvRecordEditDisplay.setText(text);
                    else
                        tvRecordEditDisplay.append(text);
                }
                lastNumeric = true;
                lastOperator = false;
//                lastDot = false;
            }
        };
        for (int id : numericButtons)
            view.findViewById(id).setOnClickListener(listener);
    }

    private void setOperatorOnClickListener(final View view) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (tvRecordEditDisplay.getText().toString().length() >= 14) return;
                String text = "";
                switch (v.getId()) {
                    case R.id.rlPlusSign:
                        text = "+";
                        break;
                    case R.id.rlMinusSign:
                        text = "-";
                        break;
                    case R.id.rlDivideSign:
                        text = "/";
                        break;
                    case R.id.rlMultipleSign:
                        text = "*";
                        break;
                }
                if (lastNumeric && !stateError) {
                    tvRecordEditDisplay.append(text);
                    lastNumeric = false;
                    lastDot = false;
                    lastOperator = true;
                }
                if (lastOperator) {
                    String dispText = tvRecordEditDisplay.getText().toString();
                    dispText = dispText.substring(0, dispText.length() - 1) + text;
                    tvRecordEditDisplay.setText(dispText);
                }
            }
        };
        for (int id : operatorButtons)
            view.findViewById(id).setOnClickListener(listener);
        view.findViewById(R.id.rlDot).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (tvRecordEditDisplay.getText().toString().length() >= 14) return;
                if (lastNumeric && !stateError && !lastDot && !lastOperator) {
                    tvRecordEditDisplay.append(".");
                    lastNumeric = false;
                    lastDot = true;
                }
            }
        });
        choosePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto.setColorFilter(GetterAttributColors.fetchHeadColor(getContext()));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        choosePhoto.setColorFilter(ContextCompat.getColor(getContext(),R.color.seriy_calc));
                    }
                },100);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choesetypeing))
                        .setItems(R.array.adding_ticket_type, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    if (ContextCompat.checkSelfPermission(getContext(),
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    MY_PERMISSIONS_REQUEST_CAMERA);
                                        } else {
                                            ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    MY_PERMISSIONS_REQUEST_CAMERA);
                                        }
                                    } else {
                                        getPhoto();
                                    }
                                } else if (which == 1) {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                                        if (ContextCompat.checkSelfPermission(getContext(),
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        MY_PERMISSIONS_REQUEST_CAMERA);
                                            } else {
                                                ActivityCompat.requestPermissions((PocketAccounter) getContext(),
                                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        MY_PERMISSIONS_REQUEST_CAMERA);
                                            }
                                        } else {
                                            File f = new File(getContext().getExternalFilesDir(null), "temp.jpg");
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                            PocketAccounter.openActivity = true;
                                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                        }
                                    }
                                }
                            }
                        });
                builder.create().show();
            }
        });
        ivClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ivClear.setColorFilter(GetterAttributColors.fetchHeadColor(getContext()));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       ivClear.setColorFilter(ContextCompat.getColor(getContext(),R.color.seriy_calc));
                    }
                },100);
                tvRecordEditDisplay.setText("0");
                lastNumeric = false;
                stateError = false;
                lastDot = false;
                lastOperator = false;
            }
        });
        ivBackspaceSign.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tvRecordEditDisplay.setText("0");
                lastNumeric = false;
                stateError = false;
                lastDot = false;
                lastOperator = false;
                return false;
            }
        });
        ivBackspaceSign.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBackspaceSign.setColorFilter(GetterAttributColors.fetchHeadColor(getContext()));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivBackspaceSign.setColorFilter(ContextCompat.getColor(getContext(),R.color.seriy_calc));
                    }
                },100);
                String dispText = tvRecordEditDisplay.getText().toString();
                char lastChar = dispText.charAt(dispText.length() - 1);
                char[] opers = {'+', '-', '*', '/'};
                for (int i = 0; i < opers.length; i++) {
                    if (opers[i] == lastChar) {
                        lastOperator = false;
                        lastNumeric = true;
                    }
                }
                if (lastChar == '.') {
                    lastDot = false;
                    lastNumeric = true;
                }
                if (tvRecordEditDisplay.getText().toString().length() == 1)
                    tvRecordEditDisplay.setText("0");
                else {
                    dispText = dispText.substring(0, dispText.length() - 1);
                    tvRecordEditDisplay.setText(dispText);
                }
            }
        });

        ivCommentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ivCommentButton.setColorFilter(GetterAttributColors.fetchHeadColor(getContext()));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivCommentButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.seriy_calc));
                    }
                },100);
                LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
                TransitionManager.beginDelayedTransition(linbutview);
                linbutview.setVisibility(View.GONE);
                keyforback = false;
                commentBackRoll = comment_add.getText().toString();
                openAddingDialog = true;
                PocketAccounter.isCalcLayoutOpen = true;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        comment_add.setFocusableInTouchMode(true);
                        comment_add.requestFocus();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager == null)
                            return;
                        inputMethodManager.showSoftInput(comment_add, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 200);


            }
        });
        view.findViewById(R.id.savesecbut).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openAddingDialog = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    if (keykeboard) {
                        RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                        keyforback = true;
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm == null)
                                        return;
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }
                            }
                        }, 120);
                        PocketAccounter.isCalcLayoutOpen = false;
                        comment.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        view.findViewById(R.id.commenee).setVisibility(View.GONE);
                        view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = comment_add.getText().toString();
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                        } else {
                            comment.setText(getString(R.string.with_out_comment));
                        }
                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                    } else {
                        RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                        keyforback = true;
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
                                    myListPhoto.setVisibility(View.VISIBLE);
                                    linbutview.setVisibility(View.VISIBLE);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }

                            }
                        }, 120);
                        comment.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        view.findViewById(R.id.commenee).setVisibility(View.GONE);
                        view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = commentBackRoll;
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                            comment_add.setText(oraliqComment);
                        } else {
                            comment_add.setText("");
                            comment.setText(getString(R.string.with_out_comment));
                        }
                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                    }
                } else if (keykeboard) {
                    RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;
                    PocketAccounter.isCalcLayoutOpen = false;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            if (mainView == null) {
                                return;
                            }
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm == null) return;
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                                    } catch (Exception o) {
                                        o.printStackTrace();
                                    }
                                }
                            }, 120);
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {

                        }

                        @Override
                        public void onTransitionPause(Transition transition) {

                        }

                        @Override
                        public void onTransitionResume(Transition transition) {

                        }
                    });
                    cus.setDuration(200);
                    cus.setStartDelay(0);
                    TransitionManager.beginDelayedTransition(headermain, cus);
                    comment.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    view.findViewById(R.id.commenee).setVisibility(View.GONE);
                    view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = commentBackRoll;
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                        comment_add.setText(oraliqComment);
                    } else {
                        comment_add.setText("");
                        comment.setText(getString(R.string.with_out_comment));
                    }

                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                } else {
                    RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            if (mainView == null) {
                                return;
                            }
                            try {

                                AutoTransition cus = new AutoTransition();
                                cus.setDuration(300);
                                cus.setStartDelay(0);
                                LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
                                TransitionManager.beginDelayedTransition(myListPhoto);
                                myListPhoto.setVisibility(View.VISIBLE);
                                TransitionManager.beginDelayedTransition(linbutview, cus);
                                linbutview.setVisibility(View.VISIBLE);
                            } catch (Exception o) {
                                o.printStackTrace();
                            }

                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {

                        }

                        @Override
                        public void onTransitionPause(Transition transition) {

                        }

                        @Override
                        public void onTransitionResume(Transition transition) {

                        }
                    });
                    cus.setDuration(200);
                    cus.setStartDelay(0);
                    TransitionManager.beginDelayedTransition(headermain, cus);
                    comment.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    view.findViewById(R.id.commenee).setVisibility(View.GONE);
                    view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = commentBackRoll;
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                        comment_add.setText(oraliqComment);
                    } else {
                        comment_add.setText("");
                        comment.setText(getString(R.string.with_out_comment));
                    }
                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                }
            }
        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = view.getRootView().getHeight() - view.getHeight();
                if (heightDiff > commonOperations.convertDpToPixel(200)) {
                    if (!keykeboard) {
                        keykeboard = true;
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                                AutoTransition cus = new AutoTransition();
                                cus.setDuration(200);
                                cus.setStartDelay(0);
                                TransitionManager.beginDelayedTransition(headermain, cus);
                                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                comment.setVisibility(View.GONE);
                                view.findViewById(R.id.addphotopanel).setVisibility(View.GONE);
                                view.findViewById(R.id.pasdigi).setVisibility(View.GONE);
                                myListPhoto.setVisibility(View.GONE);
                                view.findViewById(R.id.scroleditext).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.commenee).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.savepanel).setVisibility(View.VISIBLE);
                            }
                        }, 50);
                    }
                } else {
                    if (keykeboard) {
                        keykeboard = false;

                        if (keyforback||keyForDesideOpenSubCategoryDialog) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    AutoTransition cus = new AutoTransition();
//                                    cus.setDuration(300);
//                                    cus.setStartDelay(0);
//                                    LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
//                                    TransitionManager.beginDelayedTransition(linbutview, cus);
//                                    TransitionManager.beginDelayedTransition(myListPhoto);
//                                    myListPhoto.setVisibility(View.VISIBLE);
//                                    linbutview.setVisibility(View.VISIBLE);
                                    closeLayout();

                                }
                            }, 200);
                        }
                    }
                }
            }
        });
        view.findViewById(R.id.addcomment).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddingDialog = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    if (keykeboard) {
                        RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                        keyforback = true;
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm == null)
                                        return;
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }
                            }
                        }, 120);
                        PocketAccounter.isCalcLayoutOpen = false;
                        comment.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        view.findViewById(R.id.commenee).setVisibility(View.GONE);
                        view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = comment_add.getText().toString();
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                        } else {
                            comment.setText(getString(R.string.with_out_comment));
                        }
                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                    } else {
                        RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                        keyforback = true;
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
                                    myListPhoto.setVisibility(View.VISIBLE);
                                    linbutview.setVisibility(View.VISIBLE);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }

                            }
                        }, 120);
                        comment.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        view.findViewById(R.id.commenee).setVisibility(View.GONE);
                        view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = comment_add.getText().toString();
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                        } else {
                            comment.setText(getString(R.string.with_out_comment));
                        }
                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                    }
                } else if (keykeboard) {
                    RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;
                    PocketAccounter.isCalcLayoutOpen = false;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            if (mainView == null) {
                                return;
                            }
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm == null) return;
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                                    } catch (Exception o) {
                                        o.printStackTrace();
                                    }
                                }
                            }, 120);
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {

                        }

                        @Override
                        public void onTransitionPause(Transition transition) {

                        }

                        @Override
                        public void onTransitionResume(Transition transition) {

                        }
                    });
                    cus.setDuration(200);
                    cus.setStartDelay(0);
                    TransitionManager.beginDelayedTransition(headermain, cus);
                    comment.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    view.findViewById(R.id.commenee).setVisibility(View.GONE);
                    view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = comment_add.getText().toString();
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                    } else {
                        comment.setText(getString(R.string.with_out_comment));
                    }

                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                } else {
                    RelativeLayout headermain = (RelativeLayout) view.findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            if (mainView == null) {
                                return;
                            }
                            try {

                                AutoTransition cus = new AutoTransition();
                                cus.setDuration(300);
                                cus.setStartDelay(0);
                                LinearLayout linbutview = (LinearLayout) view.findViewById(R.id.numbersbut);
                                TransitionManager.beginDelayedTransition(myListPhoto);
                                myListPhoto.setVisibility(View.VISIBLE);
                                TransitionManager.beginDelayedTransition(linbutview, cus);
                                linbutview.setVisibility(View.VISIBLE);
                            } catch (Exception o) {
                                o.printStackTrace();
                            }

                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {

                        }

                        @Override
                        public void onTransitionPause(Transition transition) {

                        }

                        @Override
                        public void onTransitionResume(Transition transition) {

                        }
                    });
                    cus.setDuration(200);
                    cus.setStartDelay(0);
                    TransitionManager.beginDelayedTransition(headermain, cus);
                    comment.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    view.findViewById(R.id.commenee).setVisibility(View.GONE);
                    view.findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = comment_add.getText().toString();
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                    } else {
                        comment.setText(getString(R.string.with_out_comment));
                    }
                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
                }
            }
        });


        view.findViewById(R.id.imOKBut).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                keyForDeleteAllPhotos = false;
                if (keykeboard) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm == null)
                        return;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.startAnimation(buttonClick);
                            if (lastOperator) {
                                return;
                            }
                            createNewRecord();
                        }
                    }, 300);
                } else {
                    v.startAnimation(buttonClick);
                    if (lastOperator) {
                        return;
                    }
                    createNewRecord();
                }
            }
        });
        view.findViewById(R.id.rlEqualSign).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                onEqual();
            }
        });
    }

    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = tvRecordEditDisplay.getText().toString();
            Expression expression = new ExpressionBuilder(txt).build();
            try {
                double result = expression.evaluate();
                tvRecordEditDisplay.setText(decimalFormat.format(result));
            } catch (ArithmeticException ex) {
                tvRecordEditDisplay.setText(getResources().getString(R.string.error));
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    private void getPhoto() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        PocketAccounter.openActivity = true;
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    boolean weNeedUpdate = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_DELETE_PHOTOS && data != null && resultCode == RESULT_OK) {
            if ((int) data.getExtras().get(PhotoAdapter.COUNT_DELETES) != 0) {
                for (int i = 0; i < (int) data.getExtras().get(PhotoAdapter.COUNT_DELETES); i++) {
                    for (int j = myTickets.size() - 1; j >= 0; j--) {
                        if (myTickets.get(j).getPhotopath().matches((String) data.getExtras().get(PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH + i))) {
                            myTicketsFromBackRoll.remove(myTickets.get(j));
                            daoSession.getPhotoDetailsDao().delete(myTickets.get(j));
                            myTickets.remove(j);
                            myTickedAdapter.notifyItemRemoved(j);
                        }
                    }
                }
                weNeedUpdate = true;


                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < (int) data.getExtras().get(PhotoAdapter.COUNT_DELETES); i++) {
                            File fileForDelete = new File((String) data.getExtras().get(PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH + i));
                            File fileForDeleteCache = new File((String) data.getExtras().get(PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH_CACHE + i));
                            try {
                                fileForDelete.delete();
                                fileForDeleteCache.delete();
                            } catch (Exception o) {
                                o.printStackTrace();
                            }
                        }
                    }
                })).start();
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
            File fileDir = new File(picturePath);
            if (!fileDir.exists()) return;
            try {
                Bitmap bitmap;
                Bitmap bitmapCache;
                bitmap = decodeFile(fileDir);
                bitmapCache = decodeFileToCache(fileDir);
                Matrix m = new Matrix();
                m.postRotate(neededRotation(fileDir));
                bitmap = Bitmap.createBitmap(bitmap,
                        0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        m, true);
                bitmapCache = Bitmap.createBitmap(bitmapCache,
                        0, 0, bitmapCache.getWidth(), bitmapCache.getHeight(),
                        m, true);
                String path = android.os.Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "MoneyHolder" + File.separator + "Tickets";
                String path_cache = android.os.Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "MoneyHolder" + File.separator + ".cache";
                File pathik = new File(path);
                if (!pathik.exists()) {
                    pathik.mkdirs();
                    File file = new File(pathik, ".nomedia");
                    file.createNewFile();
                }
                File path_cache_file = new File(path_cache);
                if (!path_cache_file.exists()) {
                    path_cache_file.mkdirs();
                    File file = new File(path_cache_file, ".nomedia");
                    file.createNewFile();
                }
                OutputStream outFile = null;
                OutputStream outFileCache = null;
                SimpleDateFormat sp = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                String filename = "ticket-" + sp.format(System.currentTimeMillis()) + ".jpg";
                File file = new File(path, filename);
                File fileTocache = new File(path_cache, filename);
                try {
                    outFile = new FileOutputStream(file);
                    outFileCache = new FileOutputStream(fileTocache);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                    bitmapCache.compress(Bitmap.CompressFormat.JPEG, 100, outFileCache);
                    outFile.flush();
                    outFileCache.flush();
                    outFile.close();
                    outFileCache.close();
                    PhotoDetails temp;
                    if (record != null)
                        temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), record.getRecordId());
                    else
                        temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), uid_code);
                    myTickets.add(temp);
                    Log.d("testtt", "onActivityResult: " + myTickets.size());
                    myTickedAdapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File fileDir = new File(getContext().getExternalFilesDir(null), "temp.jpg");
            if (!fileDir.exists()) return;
            try {
                Bitmap bitmap;
                Bitmap bitmapCache;
                bitmap = decodeFile(fileDir);
                bitmapCache = decodeFileToCache(fileDir);
                Matrix m = new Matrix();
                m.postRotate(neededRotation(fileDir));
                bitmap = Bitmap.createBitmap(bitmap,
                        0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        m, true);
                bitmapCache = Bitmap.createBitmap(bitmapCache,
                        0, 0, bitmapCache.getWidth(), bitmapCache.getHeight(),
                        m, true);
                String path = android.os.Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "MoneyHolder" + File.separator + "Tickets";
                String path_cache = android.os.Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "MoneyHolder" + File.separator + ".cache";
                fileDir.delete();
                File pathik = new File(path);
                if (!pathik.exists()) {
                    pathik.mkdirs();
                    File file = new File(pathik, ".nomedia");
                    file.createNewFile();
                }
                File path_cache_file = new File(path_cache);
                if (!path_cache_file.exists()) {
                    path_cache_file.mkdirs();
                    File file = new File(path_cache_file, ".nomedia");
                    file.createNewFile();
                }
                OutputStream outFile = null;
                OutputStream outFileCache = null;
                SimpleDateFormat sp = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                String filename = "ticket-" + sp.format(System.currentTimeMillis()) + ".jpg";
                File file = new File(path, filename);
                File fileTocache = new File(path_cache, filename);
                try {
                    outFile = new FileOutputStream(file);
                    outFileCache = new FileOutputStream(fileTocache);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                    bitmapCache.compress(Bitmap.CompressFormat.JPEG, 100, outFileCache);
                    outFile.flush();
                    outFileCache.flush();
                    outFile.close();
                    outFileCache.close();
                    PhotoDetails temp;
                    if (record != null)
                        temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), record.getRecordId());
                    else
                        temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), uid_code);
                    myTickets.add(temp);
                    myTickedAdapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    List<RootCategory> categoryList;
    ChoiseCategoryDialoogItemAdapter choiseCategoryDialoogItemAdapter;
    @Override
    public void onClick(View view) {
        view.startAnimation(buttonClick);
        switch (view.getId()) {
            case R.id.rlCategory:
                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                View v = dialog.getWindow().getDecorView();
                v.setBackgroundResource(android.R.color.transparent);
                 categoryList = daoSession.getRootCategoryDao().loadAll();
                final TextView tvAllView = (TextView)  dialogView.findViewById(R.id.tvAllView);
                final TextView tvExpenseView = (TextView)  dialogView.findViewById(R.id.tvExpenseView);
                final TextView tvIncomeView = (TextView)  dialogView.findViewById(R.id.tvIncomeView);
                RecyclerView rvCategoryChoose = (RecyclerView) dialogView.findViewById(R.id.lvCategoryChoose);
                ArrayList<Object> tempForCastToObject = new ArrayList<>();
                for(int t=0;t<categoryList.size();t++){
                    tempForCastToObject.add(categoryList.get(t));
                }

                choiseCategoryDialoogItemAdapter = new ChoiseCategoryDialoogItemAdapter(tempForCastToObject, getContext(), new ChoiseCategoryDialoogItemAdapter.OnItemSelected() {
                    @Override
                    public void itemPressed(String itemID) {
                        boolean keyBroker = false;
                        for(RootCategory rootCategory:categoryList){
                            if(rootCategory.getId().equals(itemID)){
                                category = rootCategory;
                                ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                                tvRecordEditSubCategoryName.setText(R.string.no_category_name);
                                int resId = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                                tvRecordEditCategoryName.setText(category.getName());
                                ivRecordEditCategory.setImageResource(resId);

                                break;
                            }
                            for(SubCategory subCategoryTemp:rootCategory.getSubCategories()){
                                if(subCategoryTemp.getId().equals(itemID)){
                                    category = rootCategory;
                                    subCategory = subCategoryTemp;
                                    int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
                                    ivRecordEditSubCategory.setImageResource(resId);
                                    tvRecordEditSubCategoryName.setText(subCategory.getName());
                                    int resId2 = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                                    tvRecordEditCategoryName.setText(category.getName());
                                    ivRecordEditCategory.setImageResource(resId2);
                                    keyBroker=true;
                                    break;
                                }
                            }
                            if(keyBroker)
                                break;
                        }
                        dialog.dismiss();

                    }
                });
                tvAllView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));

                        categoryList.clear();
                        categoryList = daoSession.getRootCategoryDao().loadAll();
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);
                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();



                    }
                });
                tvExpenseView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));

                        categoryList.clear();
                        for (RootCategory rootCategory:daoSession.getRootCategoryDao().loadAll()){
                            if(rootCategory.getType() == PocketAccounterGeneral.EXPENSE)
                            categoryList.add(rootCategory);
                        }
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);

                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();

                    }
                });
                tvIncomeView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));

                        categoryList.clear();
                        for (RootCategory rootCategory:daoSession.getRootCategoryDao().loadAll()){
                            if(rootCategory.getType() == PocketAccounterGeneral.INCOME)
                                categoryList.add(rootCategory);
                        }
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);
                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();

                    }
                });
                dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                rvCategoryChoose.setLayoutManager(new GridLayoutManager(getContext(),3));
                rvCategoryChoose.setHasFixedSize(true);
                rvCategoryChoose.setAdapter(choiseCategoryDialoogItemAdapter);
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int hieght = displayMetrics.heightPixels;
                dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
                dialog.show();
                break;
            case R.id.rlSubcategory:
                openSubCategoryDialog();
                break;

        }
    }

    private void createNewRecord() {
        onEqual();
        String value = tvRecordEditDisplay.getText().toString();
        if (value.length() > 14)
            value = value.substring(0, 14);
        if (account.getNoneMinusAccount()) {
            double accounted = logicManager.isLimitAccess(account, date)
                    - commonOperations.getCost(date, currency, Double.parseDouble(tvRecordEditDisplay.getText().toString()));
            if (accounted < 0) {
                Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (account.getIsLimited()) {
            if (-account.getLimite() > logicManager.isLimitAccess(account, date)
                    - commonOperations.getCost(date, currency, Double.parseDouble(tvRecordEditDisplay.getText().toString()))) {
                Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (Double.parseDouble(value) != 0) {
            FinanceRecord savingRecord = null;
            if (record != null)
                savingRecord = record;
            else
                savingRecord = new FinanceRecord();
            savingRecord.setCategory(category);
            savingRecord.setSubCategory(subCategory);
            savingRecord.setDate(date);
            savingRecord.setAccount(account);
            savingRecord.setCurrency(currency);
            savingRecord.setAmount(Math.abs(Double.parseDouble(tvRecordEditDisplay.getText().toString())));
            if (record != null)
                savingRecord.setRecordId(record.getRecordId());
            else
                savingRecord.setRecordId(uid_code);
            savingRecord.setAllTickets(myTickets);
            savingRecord.setComment(comment_add.getText().toString());
            if (record != null) {
                daoSession.getPhotoDetailsDao().deleteInTx(myTicketsFromBackRoll);
            }
            daoSession.getPhotoDetailsDao().insertInTx(myTickets);
            logicManager.insertRecord(savingRecord);
            reportManager.refreshDatas();
            dataCache.updateOneDay(date);
            paFragmentManager.updateVoiceRecognizePage(date);
        } else {
            if (fromEdit) {
                record.setAllTickets(myTicketsFromBackRoll);
                for (PhotoDetails temp : myTicketsFromBackRoll) {
                    myTickets.remove(temp);
                }
            }
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    for (PhotoDetails temp : myTickets) {
                        File forDeleteTicket = new File(temp.getPhotopath());
                        File forDeleteTicketCache = new File(temp.getPhotopathCache());
                        try {
                            forDeleteTicket.delete();
                            forDeleteTicketCache.delete();
                        } catch (Exception o) {
                            o.printStackTrace();
                        }
                    }
                }
            })).start();
        }
        if (parent != PocketAccounterGeneral.MAIN) {
            if (((PocketAccounter) getContext()).getSupportFragmentManager().getBackStackEntryCount() != 0) {
                FragmentManager fm = ((PocketAccounter) getContext()).getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); i++) fm.popBackStack();
                paFragmentManager.displayFragment(new RecordDetailFragment(date));
            }
        } else {
            paFragmentManager.displayMainWindow();
            paFragmentManager.getFragmentManager().popBackStack();
        }
    }



    private void openSubCategoryDialog() {



        final Dialog dialog = new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        final ArrayList<Object> subCategories = new ArrayList<Object>();
        subCategories.add(category);
        for (int i = 0; i < category.getSubCategories().size(); i++)
            subCategories.add(category.getSubCategories().get(i));
        subCategories.add(null);
        dialogView.findViewById(R.id.llToolBars).setVisibility(View.GONE);

         TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText(R.string.choise_subcategory);
         RecyclerView rvCategoryChoose = (RecyclerView) dialogView.findViewById(R.id.lvCategoryChoose);
        choiseCategoryDialoogItemAdapter = new ChoiseCategoryDialoogItemAdapter(subCategories, getContext(), new ChoiseCategoryDialoogItemAdapter.OnItemSelected() {
            @Override
            public void itemPressed(String itemID) {

                boolean keyBroker = false;
                if (itemID.equals("")){
                    keyForDesideOpenSubCategoryDialog = true;
                    subCatAddEditDialog.setRootCategory(category.getId());
                    subCatAddEditDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                      closeLayout();
                                }
                            }, 200);

                            keyForDesideOpenSubCategoryDialog = false;
                        }
                    });
                    subCatAddEditDialog.setSubCat(null, new OnSubcategorySavingListener() {
                        @Override
                        public void onSubcategorySaving(SubCategory subCategory) {
                            List<SubCategory> subCategoryList = new ArrayList<>();
                            subCategoryList.add(subCategory);
                            logicManager.insertSubCategory(subCategoryList);
                            category.getSubCategories().add(subCategory);
                            keyForDesideOpenSubCategoryDialog = false;
                            subCatAddEditDialog.dismiss();

                            openSubCategoryDialog();
                        }
                    });
                    subCatAddEditDialog.show();
                }
                categoryList = daoSession.getRootCategoryDao().loadAll();
                for(RootCategory rootCategory:categoryList){
                    if(rootCategory.getId().equals(itemID)){
                        category = rootCategory;
                        ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                        tvRecordEditSubCategoryName.setText(R.string.no_category_name);
                        int resId = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                        tvRecordEditCategoryName.setText(category.getName());
                        ivRecordEditCategory.setImageResource(resId);

                        break;
                    }
                    for(SubCategory subCategoryTemp:rootCategory.getSubCategories()){
                        if(subCategoryTemp.getId().equals(itemID)){
                            category = rootCategory;
                            subCategory = subCategoryTemp;
                            int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
                            ivRecordEditSubCategory.setImageResource(resId);
                            tvRecordEditSubCategoryName.setText(subCategory.getName());
                            int resId2 = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                            tvRecordEditCategoryName.setText(category.getName());
                            ivRecordEditCategory.setImageResource(resId2);
                            keyBroker=true;
                            break;
                        }
                    }
                    if(keyBroker)
                        break;



                }
                dialog.dismiss();

            }
        });
        choiseCategoryDialoogItemAdapter.toBackedToCategory(true);

        dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        rvCategoryChoose.setLayoutManager(new GridLayoutManager(getContext(),3));
        rvCategoryChoose.setHasFixedSize(true);
        rvCategoryChoose.setAdapter(choiseCategoryDialoogItemAdapter);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int hieght = displayMetrics.heightPixels;
        dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
        dialog.show();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (keyForDeleteAllPhotos) {
            if (fromEdit) {
                for (PhotoDetails temp : myTicketsFromBackRoll) {
                    myTickets.remove(temp);
                }
                record.setAllTickets(myTicketsFromBackRoll);
            }
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    for (PhotoDetails temp : myTickets) {
                        File forDeleteTicket = new File(temp.getPhotopath());
                        File forDeleteTicketCache = new File(temp.getPhotopathCache());
                        try {
                            forDeleteTicket.delete();
                            forDeleteTicketCache.delete();
                        } catch (Exception o) {
                            o.printStackTrace();
                        }
                    }
                }
            })).start();
        }
        if (weNeedUpdate) {
            //TODO NASIMXON YORDAM
            if (parent != PocketAccounterGeneral.MAIN) {
                if (((PocketAccounter) getContext()).getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    FragmentManager fm = ((PocketAccounter) getContext()).getSupportFragmentManager();
                    for (int i = 0; i < fm.getBackStackEntryCount(); i++) fm.popBackStack();
                    paFragmentManager.displayFragment(new RecordDetailFragment(date));
                }
            }
        }
        PocketAccounter.isCalcLayoutOpen = false;
        mainView = null;
    }

    public void closeLayout() {
        openAddingDialog = false;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
            if (keykeboard) {
                RelativeLayout headermain = (RelativeLayout) mainView.findViewById(R.id.headermain);
                keyforback = true;
                PocketAccounter.isCalcLayoutOpen = false;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm == null)
                                return;
                            imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);

                        } catch (Exception o) {
                            o.printStackTrace();
                        }


                    }
                }, 300);
                comment.setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                mainView.findViewById(R.id.commenee).setVisibility(View.GONE);
                mainView.findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.with_out_comment));
                }
                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
            } else {
                RelativeLayout headermain = (RelativeLayout) mainView.findViewById(R.id.headermain);
                keyforback = true;
                PocketAccounter.isCalcLayoutOpen = false;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LinearLayout linbutview = (LinearLayout) mainView.findViewById(R.id.numbersbut);
                            myListPhoto.setVisibility(View.VISIBLE);
                            linbutview.setVisibility(View.VISIBLE);
                        } catch (Exception o) {
                            o.printStackTrace();
                        }
                    }
                }, 300);
                comment.setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                mainView.findViewById(R.id.commenee).setVisibility(View.GONE);
                mainView.findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.with_out_comment));
                }
                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
            }
        } else {
            if (keykeboard) {
                RelativeLayout headermain = (RelativeLayout) mainView.findViewById(R.id.headermain);
                AutoTransition cus = new AutoTransition();
                keyforback = true;
                PocketAccounter.isCalcLayoutOpen = false;
                cus.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        if (mainView == null) {
                            return;
                        }
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm == null)
                                        return;
                                    imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }
                            }
                        }, 100);
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                cus.setDuration(200);
                cus.setStartDelay(0);
                TransitionManager.beginDelayedTransition(headermain, cus);
                comment.setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                mainView.findViewById(R.id.commenee).setVisibility(View.GONE);
                mainView.findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.with_out_comment));
                }

                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
            } else {
                RelativeLayout headermain = (RelativeLayout) mainView.findViewById(R.id.headermain);
                AutoTransition cus = new AutoTransition();
                keyforback = true;
                PocketAccounter.isCalcLayoutOpen = false;
                cus.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        if (mainView == null) {
                            return;
                        }
                        try {
                            AutoTransition cus = new AutoTransition();
                            cus.setDuration(300);
                            cus.setStartDelay(0);
                            LinearLayout linbutview = (LinearLayout) mainView.findViewById(R.id.numbersbut);
                            TransitionManager.beginDelayedTransition(myListPhoto);
                            myListPhoto.setVisibility(View.VISIBLE);
                            TransitionManager.beginDelayedTransition(linbutview, cus);
                            linbutview.setVisibility(View.VISIBLE);
                        } catch (Exception o) {
                            o.printStackTrace();
                        }
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {
                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                cus.setDuration(200);
                cus.setStartDelay(0);
                TransitionManager.beginDelayedTransition(headermain, cus);
                comment.setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);
                mainView.findViewById(R.id.scroleditext).setVisibility(View.GONE);
                mainView.findViewById(R.id.commenee).setVisibility(View.GONE);
                mainView.findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.with_out_comment));
                }
                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) commonOperations.convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density))));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        File f = new File(getContext().getExternalFilesDir(null), "temp.jpg");
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        PocketAccounter.openActivity = true;
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                    return;
                }
            }
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            final int REQUIRED_SIZE = 350;
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

    private Bitmap decodeFileToCache(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            final int REQUIRED_SIZE = 64;
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

    public static int neededRotation(File ff) {
        try {
            ExifInterface exif = new ExifInterface(ff.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            }
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}