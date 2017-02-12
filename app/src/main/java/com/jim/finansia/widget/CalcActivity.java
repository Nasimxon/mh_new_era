package com.jim.finansia.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.Currency;
//    import com.jim.pocketaccounter.finance.FinanceManager;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.finance.AccountChoiseDialogAdapter;
import com.jim.finansia.finance.ChoiseCategoryDialoogItemAdapter;
import com.jim.finansia.finance.RecordAccountAdapter;
import com.jim.finansia.finance.RecordCategoryAdapter;
import com.jim.finansia.finance.RecordSubCategoryAdapter;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.photocalc.PhotoAdapter;
import com.jim.finansia.utils.GetterAttributColors;
import com.jim.finansia.utils.OnSubcategorySavingListener;
import com.jim.finansia.utils.PocketAccounterGeneral;
//    import com.jim.pocketaccounter.photocalc.PhotoAdapter;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.utils.SubCatAddEditDialog;
import com.jim.finansia.utils.cache.DataCache;
import com.transitionseverywhere.AutoTransition;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;


import org.greenrobot.greendao.database.Database;

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

import static com.jim.finansia.debt.AddBorrowFragment.RESULT_LOAD_IMAGE;
import static com.jim.finansia.photocalc.PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH;
import static com.jim.finansia.photocalc.PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH_CACHE;
import static com.jim.finansia.photocalc.PhotoAdapter.COUNT_DELETES;
import static com.jim.finansia.photocalc.PhotoAdapter.REQUEST_DELETE_PHOTOS;
//    import static com.jim.pocketaccounter.photocalc.PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH;
//    import static com.jim.pocketaccounter.photocalc.PhotoAdapter.BEGIN_DELETE_TICKKETS_PATH_CACHE;
//    import static com.jim.pocketaccounter.photocalc.PhotoAdapter.COUNT_DELETES;
//    import static com.jim.pocketaccounter.photocalc.PhotoAdapter.REQUEST_DELETE_PHOTOS;

public class CalcActivity extends AppCompatActivity implements View.OnClickListener {


    private boolean keyforback = false;
    private TextView tvRecordEditDisplay;
    private ImageView ivRecordEditCategory, ivRecordEditSubCategory;
    private Spinner spRecordEdit;
    private RootCategory category;
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
    private final int PERMISSION_READ_STORAGE = 6;
    boolean keyForDesideOpenSubCategoryDialog = false;
    private boolean keyForDeleteAllPhotos = true;
    boolean isCalcLayoutOpen = false;
    static final int REQUEST_IMAGE_CAPTURE = 112;
    private String uid_code;
    RecyclerView myListPhoto;
    ArrayList<PhotoDetails> myTickets;
    ArrayList<PhotoDetails> myTicketsFromBackRoll;
    PhotoAdapter myTickedAdapter;
    boolean openAddingDialog = false;
    private int WIDGET_ID;
    DaoSession daoSession;
    View mainView;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 18;
    Database db;
    public static String KEY_FOR_INSTALAZING = "key_for_init";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = prefs.getString(PocketAccounterGeneral.CHOOSEN_THEME_NAME_KEY, PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.BLUE_THEME);
        int themeId = getResources().getIdentifier(themeName, "style", getPackageName());
        setTheme(themeId);
        setContentView(R.layout.activity_calc);
        mainView = (LinearLayout) findViewById(R.id.llRoot);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, PocketAccounterGeneral.CURRENT_DB_NAME);
        db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        comment = (TextView) findViewById(R.id.textView18);
        ivCommentButton = (ImageView) mainView.findViewById(R.id.comment_opener);
        comment_add = (EditText) findViewById(R.id.comment_add);
        date = Calendar.getInstance();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        decimalFormat = new DecimalFormat("0.##", otherSymbols);
        uid_code = "record_" + UUID.randomUUID().toString();
        category = new RootCategory();
        String catId = getIntent().getStringExtra(WidgetKeys.KEY_FOR_INTENT_ID);
        WIDGET_ID = getIntent().getIntExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        for (RootCategory rootCategory:daoSession.getRootCategoryDao().loadAll()) {
            if(rootCategory.getId().equals(catId)){
                category = rootCategory;
                break;
            }
            for(SubCategory subCategoryTemp : rootCategory.getSubCategories())
            if (subCategoryTemp.getId().equals(catId)) {
                category = rootCategory;
                subCategory = subCategoryTemp;
                break;
            }
        }

        ivRecordEditCategory = (ImageView) mainView.findViewById(R.id.ivRecordEditCategory);
        ivRecordEditSubCategory = (ImageView) mainView.findViewById(R.id.ivRecordEditSubCategory);
        tvRecordEditDisplay = (TextView) mainView.findViewById(R.id.tvRecordEditDisplay);
        tvRecordEditCategoryName = (TextView) mainView.findViewById(R.id.tvRecordEditCategoryName);
        tvRecordEditSubCategoryName = (TextView) mainView.findViewById(R.id.tvRecordEditSubCategoryName);

        buttonClick = AnimationUtils.loadAnimation(CalcActivity.this, R.anim.button_click);

        spRecordEdit = (Spinner) findViewById(R.id.spRecordEdit);
        RecordAccountAdapter accountAdapter = new RecordAccountAdapter(CalcActivity.this, daoSession.getAccountDao().loadAll());
        ivAccountIcon = (ImageView) mainView.findViewById(R.id.ivAccountIcon);
        tvAccountName = (TextView) mainView.findViewById(R.id.tvAccountName);
        rvAccountChoise = (RelativeLayout) mainView.findViewById(R.id.rvAccountChoise);
        final List<Account> accountList = daoSession.getAccountDao().loadAll();
        account = accountList.get(0);
        int resId2 = getResources().getIdentifier(account.getIcon(), "drawable", getPackageName());
        ivAccountIcon.setImageResource(resId2);
        tvAccountName.setText(account.getName());
        final String[] currencies = new String[daoSession.getCurrencyDao().loadAll().size()];
        for (int i = 0; i < daoSession.getCurrencyDao().loadAll().size(); i++)
            currencies[i] = daoSession.getCurrencyDao().loadAll().get(i).getAbbr();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CalcActivity.this, R.layout.spinner_single_item_calc, currencies);
        spRecordEdit.setAdapter(adapter);
        final List<Currency> curlist = daoSession.getCurrencyDao().loadAll();
        Currency mainCur = getMainCurrency(daoSession);
        for (int i = 0; i < curlist.size(); i++) {
            if (curlist.get(i).getId().matches(mainCur.getId())) {
                spRecordEdit.setSelection(i);
                break;
            }
        }

        rvAccountChoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(CalcActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.category_choose_list, null);
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
                AccountChoiseDialogAdapter adapter = new AccountChoiseDialogAdapter(accountList, CalcActivity.this, new AccountChoiseDialogAdapter.OnItemSelectListner() {
                    @Override
                    public void onItemSelect(Account fromDialog) {
                        int resId = getResources().getIdentifier(fromDialog.getIcon(), "drawable", getPackageName());
                        ivAccountIcon.setImageResource(resId);
                        tvAccountName.setText(fromDialog.getName());
                        account = fromDialog;
                        dialog.dismiss();
                    }
                });
                dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                rvCategoryChoose.setLayoutManager(new LinearLayoutManager(CalcActivity.this));
                rvCategoryChoose.setHasFixedSize(true);
                rvCategoryChoose.setAdapter(adapter);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int hieght = displayMetrics.heightPixels;
                dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
                dialog.show();








            }
        });

        spRecordEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = curlist.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ivClear = (ImageView) mainView.findViewById(R.id.ivClear);
        ivBackspaceSign = (ImageView) mainView.findViewById(R.id.ivBackspaceSign);
        choosePhoto = (ImageView) mainView.findViewById(R.id.choose_photo);
        spRecordEdit = (Spinner) mainView.findViewById(R.id.spRecordEdit);

        ivRecordEditCategory = (ImageView) findViewById(R.id.ivRecordEditCategory);
        ivRecordEditSubCategory = (ImageView) findViewById(R.id.ivRecordEditSubCategory);
        tvRecordEditDisplay = (TextView) findViewById(R.id.tvRecordEditDisplay);
        rlCategory = (RelativeLayout) findViewById(R.id.rlCategory);
        rlCategory.setOnClickListener(this);
        rlSubCategory = (RelativeLayout) findViewById(R.id.rlSubcategory);
        rlSubCategory.setOnClickListener(this);
        setNumericOnClickListener();
        setOperatorOnClickListener();
        if (category != null) {
            ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
            tvRecordEditSubCategoryName.setText(R.string.no_category_name);
            int resId = getResources().getIdentifier(category.getIcon(), "drawable", getPackageName());
            tvRecordEditCategoryName.setText(category.getName());
            ivRecordEditCategory.setImageResource(resId);
        }
        if(subCategory!=null){
            int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getPackageName());
            ivRecordEditSubCategory.setImageResource(resId);
            tvRecordEditSubCategoryName.setText(subCategory.getName());
        }


        if (myTickets == null)
            myTickets = new ArrayList<>();
        if (myTicketsFromBackRoll == null)
            myTicketsFromBackRoll = new ArrayList<>();
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(CalcActivity.this, LinearLayoutManager.HORIZONTAL, false);
        myListPhoto = (RecyclerView) findViewById(R.id.recycler_calc);
        myListPhoto.setLayoutManager(layoutManager);

        myTickedAdapter = new PhotoAdapter(myTickets, CalcActivity.this, new RecordEditFragment.OpenIntentFromAdapter() {
            @Override
            public void startActivityFromFragmentForResult(Intent intent) {

                startActivityForResult(intent, REQUEST_DELETE_PHOTOS);
            }
        });
        myListPhoto.setAdapter(myTickedAdapter);


    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
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
                lastDot = false;
            }
        };
        for (int id : numericButtons)
            findViewById(id).setOnClickListener(listener);
    }

    private void setOperatorOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
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
            findViewById(id).setOnClickListener(listener);
        findViewById(R.id.rlDot).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.choose_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CalcActivity.this);
                builder.setTitle("Choose type adding")
                        .setItems(R.array.adding_ticket_type, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    int permission = ContextCompat.checkSelfPermission(CalcActivity.this,
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    if (permission != PackageManager.PERMISSION_GRANTED) {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale((CalcActivity.this),
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CalcActivity.this);
                                            builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                                    .setTitle("Permission required");

                                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    ActivityCompat.requestPermissions(CalcActivity.this,
                                                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_READ_STORAGE);
                                                }
                                            });
                                            android.app.AlertDialog dialogik = builder.create();
                                            dialogik.show();

                                        } else {
                                            ActivityCompat.requestPermissions(CalcActivity.this,
                                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    PERMISSION_READ_STORAGE);
                                        }
                                    } else {
                                        getPhoto();
                                    }
                                } else if (which == 1) {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        if (ContextCompat.checkSelfPermission(CalcActivity.this,
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(CalcActivity.this,
                                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                ActivityCompat.requestPermissions( CalcActivity.this,
                                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        MY_PERMISSIONS_REQUEST_CAMERA);
                                            } else {
                                                ActivityCompat.requestPermissions(CalcActivity.this,
                                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        MY_PERMISSIONS_REQUEST_CAMERA);
                                            }
                                        } else {
                                            File f = new File(getExternalFilesDir(null), "temp.jpg");
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
        ivBackspaceSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBackspaceSign.setColorFilter(GetterAttributColors.fetchHeadColor(CalcActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivBackspaceSign.setColorFilter(ContextCompat.getColor(CalcActivity.this,R.color.seriy_calc));
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

        ivCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivCommentButton.setColorFilter(GetterAttributColors.fetchHeadColor(CalcActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivCommentButton.setColorFilter(ContextCompat.getColor(CalcActivity.this,R.color.seriy_calc));
                    }
                },100);
                LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
                TransitionManager.beginDelayedTransition(linbutview);
                linbutview.setVisibility(View.GONE);
                keyforback = false;
                openAddingDialog = true;
                isCalcLayoutOpen = true;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        comment_add.setFocusableInTouchMode(true);
                        comment_add.requestFocus();
                        final InputMethodManager inputMethodManager = (InputMethodManager) CalcActivity.this
                                .getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
                        if (inputMethodManager == null)
                            return;
                        inputMethodManager.showSoftInput(comment_add, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 200);


            }
        });
        findViewById(R.id.savesecbut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                keyForDeleteAllPhotos = false;
                if (keykeboard) {
                    InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    isCalcLayoutOpen = false;

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.startAnimation(buttonClick);
                            if (lastDot || lastOperator) {
                                return;
                            }
                            createNewRecord();
                        }
                    }, 300);
                } else {
                    v.startAnimation(buttonClick);
                    if (lastDot || lastOperator) {
                        return;
                    }
                    createNewRecord();

                }
            }
        });


        mainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mainView.getRootView().getHeight() - mainView.getHeight();
                if (heightDiff > convertDpToPixel(200, CalcActivity.this)) { // if more than 200 dp, it's probably a keyboard...
                    if (!keykeboard) {
                        Log.d("test", "onGlobalLayout: KeyBoardOpen");

                        keykeboard = true;

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {


                                RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                                AutoTransition cus = new AutoTransition();
                                cus.setDuration(200);
                                cus.setStartDelay(0);
                                TransitionManager.beginDelayedTransition(headermain, cus);

                                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


                                comment.setVisibility(View.GONE);
                                findViewById(R.id.addphotopanel).setVisibility(View.GONE);
                                findViewById(R.id.pasdigi).setVisibility(View.GONE);
                                myListPhoto.setVisibility(View.GONE);

                                findViewById(R.id.scroleditext).setVisibility(View.VISIBLE);
                                findViewById(R.id.commenee).setVisibility(View.VISIBLE);
                                findViewById(R.id.savepanel).setVisibility(View.VISIBLE);
                            }
                        }, 50);


                    }
                } else {
                    if (keykeboard) {
                        Log.d("test", "onGlobalLayout: KeyBoardClose");
                        keykeboard = false;

                        if (keyforback) {

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AutoTransition cus = new AutoTransition();
                                    cus.setDuration(300);
                                    cus.setStartDelay(0);
                                    LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
                                    TransitionManager.beginDelayedTransition(linbutview, cus);
                                    TransitionManager.beginDelayedTransition(myListPhoto);
                                    myListPhoto.setVisibility(View.VISIBLE);
                                    linbutview.setVisibility(View.VISIBLE);

                                }
                            }, 200);

                        }


                    }


                }
            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivClear.setColorFilter(GetterAttributColors.fetchHeadColor(CalcActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivClear.setColorFilter(ContextCompat.getColor(CalcActivity.this,R.color.seriy_calc));
                    }
                },100);
                tvRecordEditDisplay.setText("0");
                lastNumeric = false;
                stateError = false;
                lastDot = false;
                lastOperator = false;
            }
        });
        findViewById(R.id.addcomment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddingDialog = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    if (keykeboard) {
                        RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                        keyforback = true;

                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
                                    if (imm == null)
                                        return;
                                    imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }

                            }
                        }, 120);

                        isCalcLayoutOpen = false;
                        comment.setVisibility(View.VISIBLE);
                        findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);

                        findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        findViewById(R.id.commenee).setVisibility(View.GONE);
                        findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = comment_add.getText().toString();
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                        } else {
                            comment.setText(getString(R.string.add_comment));
                        }

                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));
                    } else {
                        RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                        keyforback = true;


                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
                                    myListPhoto.setVisibility(View.VISIBLE);
                                    linbutview.setVisibility(View.VISIBLE);
                                } catch (Exception o) {
                                    o.printStackTrace();
                                }

                            }
                        }, 120);


                        comment.setVisibility(View.VISIBLE);
                        findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                        findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                        findViewById(R.id.scroleditext).setVisibility(View.GONE);
                        findViewById(R.id.commenee).setVisibility(View.GONE);
                        findViewById(R.id.savepanel).setVisibility(View.GONE);
                        oraliqComment = comment_add.getText().toString();
                        if (!oraliqComment.matches("")) {
                            comment.setText(oraliqComment);
                        } else {
                            comment.setText(getString(R.string.add_comment));
                        }

                        headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));

                    }
                } else if (keykeboard) {
                    Log.d("testtt", "onClick: ");
                    RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;

                    isCalcLayoutOpen = false;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
//                                if(mainView==null){
//                                    return;
//                                }
                            Log.d("testtt", "onClick: pip");

                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
                                        if (imm == null)
                                            return;
                                        imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);

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
                    findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);

                    findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    findViewById(R.id.commenee).setVisibility(View.GONE);
                    findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = comment_add.getText().toString();
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                    } else {
                        comment.setText(getString(R.string.add_comment));
                    }

                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));
                } else {
                    RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                    AutoTransition cus = new AutoTransition();
                    keyforback = true;
                    cus.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
//                                if(mainView==null){
//                                    return;
//                                }
                            try {

                                AutoTransition cus = new AutoTransition();
                                cus.setDuration(300);
                                cus.setStartDelay(0);
                                LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
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
                    findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                    findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                    findViewById(R.id.scroleditext).setVisibility(View.GONE);
                    findViewById(R.id.commenee).setVisibility(View.GONE);
                    findViewById(R.id.savepanel).setVisibility(View.GONE);
                    oraliqComment = comment_add.getText().toString();
                    if (!oraliqComment.matches("")) {
                        comment.setText(oraliqComment);
                    } else {
                        comment.setText(getString(R.string.add_comment));
                    }

                    headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));

                }


            }
        });


        findViewById(R.id.imOKBut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                keyForDeleteAllPhotos = false;
                if (keykeboard) {
                    InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
                    if (imm == null)
                        return;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.startAnimation(buttonClick);
                            if (lastDot || lastOperator) {
                                return;
                            }
                            createNewRecord();
                        }
                    }, 300);
                } else {
                    v.startAnimation(buttonClick);
                    if (lastDot || lastOperator) {
                        return;
                    }
                    createNewRecord();

                }

            }
        });
        findViewById(R.id.rlEqualSign).setOnClickListener(new View.OnClickListener() {
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
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    static public Currency getMainCurrency(DaoSession daoSession) {
        List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
        for (Currency currency : currencies) {
            if (currency.getMain()) return currency;
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
//        super.onActivityResult(requestCode,resultCode,data);
        Log.d("resulttt", "onActivityResult: Keldi " + ((data != null) ? "PUSTOY" : "NIMADIRLA"));
        if (requestCode == REQUEST_DELETE_PHOTOS && data != null && resultCode == RESULT_OK) {
            Log.d("resulttt", "onActivityResult: " + (int) data.getExtras().get(COUNT_DELETES));
            if ((int) data.getExtras().get(COUNT_DELETES) != 0) {
                for (int i = 0; i < (int) data.getExtras().get(COUNT_DELETES); i++) {

                    for (int j = myTickets.size() - 1; j >= 0; j--) {
                        if (myTickets.get(j).getPhotopath().matches((String) data.getExtras().get(BEGIN_DELETE_TICKKETS_PATH + i))) {
                            myTicketsFromBackRoll.remove(myTickets.get(j));
                            myTickets.remove(j);
                            myTickedAdapter.notifyItemRemoved(j);
                        }
                    }
                }

                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < (int) data.getExtras().get(COUNT_DELETES); i++) {
                            File fileForDelete = new File((String) data.getExtras().get(BEGIN_DELETE_TICKKETS_PATH + i));
                            File fileForDeleteCache = new File((String) data.getExtras().get(BEGIN_DELETE_TICKKETS_PATH_CACHE + i));

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
            Cursor cursor = CalcActivity.this.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File fileDir = new File(picturePath);

            if (!fileDir.exists()) {

                return;

            }

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

                    PhotoDetails temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), uid_code);
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            File fileDir = new File(CalcActivity.this.getExternalFilesDir(null), "temp.jpg");

            if (!fileDir.exists()) {
                return;

            }

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

                    PhotoDetails temp = new PhotoDetails(file.getAbsolutePath(), fileTocache.getAbsolutePath(), uid_code);
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
                final Dialog dialog = new Dialog(CalcActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.category_choose_list, null);
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

                choiseCategoryDialoogItemAdapter = new ChoiseCategoryDialoogItemAdapter(tempForCastToObject, CalcActivity.this, new ChoiseCategoryDialoogItemAdapter.OnItemSelected() {
                    @Override
                    public void itemPressed(String itemID) {
                        boolean keyBroker = false;
                        for(RootCategory rootCategory:categoryList){
                            if(rootCategory.getId().equals(itemID)){
                                category = rootCategory;
                                ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                                tvRecordEditSubCategoryName.setText(R.string.no_category_name);
                                int resId = getResources().getIdentifier(category.getIcon(), "drawable", getPackageName());
                                tvRecordEditCategoryName.setText(category.getName());
                                ivRecordEditCategory.setImageResource(resId);

                                break;
                            }
                            for(SubCategory subCategoryTemp:rootCategory.getSubCategories()){
                                if(subCategoryTemp.getId().equals(itemID)){
                                    category = rootCategory;
                                    subCategory = subCategoryTemp;
                                    int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getPackageName());
                                    ivRecordEditSubCategory.setImageResource(resId);
                                    tvRecordEditSubCategoryName.setText(subCategory.getName());
                                    int resId2 = getResources().getIdentifier(category.getIcon(), "drawable", getPackageName());
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
                tvAllView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_myagkiy_glavniy));
                        tvExpenseView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));

                        categoryList.clear();
                        categoryList = daoSession.getRootCategoryDao().loadAll();
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);
                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();



                    }
                });
                tvExpenseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_myagkiy_glavniy));
                        tvIncomeView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));

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
                tvIncomeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(CalcActivity.this,R.color.black_for_myagkiy_glavniy));

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
                dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                rvCategoryChoose.setLayoutManager(new GridLayoutManager(CalcActivity.this,3));
                rvCategoryChoose.setHasFixedSize(true);
                rvCategoryChoose.setAdapter(choiseCategoryDialoogItemAdapter);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
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
        if (account.getIsLimited()) {
            double limit = account.getLimite();
            double accounted = isLimitAccess(daoSession, getApplicationContext(), account, Calendar.getInstance());
            accounted = accounted - getCost(date, currency, daoSession.getCurrencyDao().load(account.getLimitCurId()), Double.parseDouble(tvRecordEditDisplay.getText().toString()));
            if (-limit > accounted) {
                Toast.makeText(getApplicationContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (Double.parseDouble(value) != 0) {

            FinanceRecord newRecord = new FinanceRecord();
            newRecord.setCategory(category);
            newRecord.setSubCategory(subCategory);
            newRecord.setDate(date);
            newRecord.setAccount(account);
            newRecord.setCurrency(currency);
            newRecord.setAmount(Math.abs(Double.parseDouble(tvRecordEditDisplay.getText().toString())));
            newRecord.setRecordId(uid_code);
            newRecord.setAllTickets(myTickets);
            for (PhotoDetails photoDetails : myTickets) {
                Log.d("testtt", "" + photoDetails.getRecordId() + " == " + uid_code + "   ;   " + photoDetails.getPhotopath());
            }
            newRecord.setComment(comment_add.getText().toString());
            daoSession.getPhotoDetailsDao().insertInTx(myTickets);
            daoSession.getFinanceRecordDao().insertOrReplace(newRecord);
            db.close();
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.getDefaultSharedPreferences(CalcActivity.this).edit().putBoolean(KEY_FOR_INSTALAZING, true).apply();
                    if (AppWidgetManager.INVALID_APPWIDGET_ID != WIDGET_ID)
                        WidgetProvider.updateWidget(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),
                                WIDGET_ID);
                }
            })).start();


            finish();

        } else {

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
            db.close();
            finish();
        }

    }

    private void openCategoryDialog(final ArrayList<RootCategory> categories) {
        final Dialog dialog = new Dialog(CalcActivity.this);
        View dialogView = CalcActivity.this.getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvCategoryChoose = (ListView) dialogView.findViewById(R.id.lvCategoryChoose);
        RecordCategoryAdapter adapter = new RecordCategoryAdapter(CalcActivity.this, categories);
        lvCategoryChoose.setAdapter(adapter);
        lvCategoryChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = getResources().getIdentifier(categories.get(position).getIcon(), "drawable", CalcActivity.this.getPackageName());
                ivRecordEditCategory.setImageResource(resId);
                ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                category = categories.get(position);
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(8 * width / 9, ActionBarOverlayLayout.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
    private void openSubCategoryDialog() {



        final Dialog dialog = new Dialog(CalcActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        final ArrayList<Object> subCategories = new ArrayList<Object>();
        subCategories.add(category);
        for (int i = 0; i < category.getSubCategories().size(); i++)
            subCategories.add(category.getSubCategories().get(i));
        dialogView.findViewById(R.id.llToolBars).setVisibility(View.GONE);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText(R.string.choise_subcategory);
        RecyclerView rvCategoryChoose = (RecyclerView) dialogView.findViewById(R.id.lvCategoryChoose);
        choiseCategoryDialoogItemAdapter = new ChoiseCategoryDialoogItemAdapter(subCategories, CalcActivity.this, new ChoiseCategoryDialoogItemAdapter.OnItemSelected() {
            @Override
            public void itemPressed(String itemID) {

                boolean keyBroker = false;

                categoryList = daoSession.getRootCategoryDao().loadAll();
                for(RootCategory rootCategory:categoryList){
                    if(rootCategory.getId().equals(itemID)){
                        category = rootCategory;
                        ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                        tvRecordEditSubCategoryName.setText(R.string.no_category_name);
                        int resId = getResources().getIdentifier(category.getIcon(), "drawable", getPackageName());
                        tvRecordEditCategoryName.setText(category.getName());
                        ivRecordEditCategory.setImageResource(resId);

                        break;
                    }
                    for(SubCategory subCategoryTemp:rootCategory.getSubCategories()){
                        if(subCategoryTemp.getId().equals(itemID)){
                            category = rootCategory;
                            subCategory = subCategoryTemp;
                            int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getPackageName());
                            ivRecordEditSubCategory.setImageResource(resId);
                            tvRecordEditSubCategoryName.setText(subCategory.getName());
                            int resId2 = getResources().getIdentifier(category.getIcon(), "drawable", getPackageName());
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

        dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        rvCategoryChoose.setLayoutManager(new GridLayoutManager(CalcActivity.this,3));
        rvCategoryChoose.setHasFixedSize(true);
        rvCategoryChoose.setAdapter(choiseCategoryDialoogItemAdapter);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int hieght = displayMetrics.heightPixels;
        dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
        dialog.show();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (keyForDeleteAllPhotos) {
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
        isCalcLayoutOpen = false;

    }

    public void closeLayout() {
        openAddingDialog = false;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
            if (keykeboard) {
                RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                keyforback = true;
                isCalcLayoutOpen = false;


                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
                            if (imm == null)
                                return;
                            imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);

                        } catch (Exception o) {
                            o.printStackTrace();
                        }


                    }
                }, 300);

                comment.setVisibility(View.VISIBLE);
                findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                findViewById(R.id.scroleditext).setVisibility(View.GONE);
                findViewById(R.id.commenee).setVisibility(View.GONE);
                findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.add_comment));
                }

                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));
            } else {
                RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                keyforback = true;
                isCalcLayoutOpen = false;


                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
                            myListPhoto.setVisibility(View.VISIBLE);
                            linbutview.setVisibility(View.VISIBLE);
                        } catch (Exception o) {
                            o.printStackTrace();
                        }

                    }
                }, 300);

                comment.setVisibility(View.VISIBLE);
                findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                findViewById(R.id.scroleditext).setVisibility(View.GONE);
                findViewById(R.id.commenee).setVisibility(View.GONE);
                findViewById(R.id.savepanel).setVisibility(View.GONE);

                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.add_comment));
                }

                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));

            }
        } else {
            if (keykeboard) {
                RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                AutoTransition cus = new AutoTransition();
                keyforback = true;
                isCalcLayoutOpen = false;
                cus.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
//                            if(mainView==null){
//                                return;
//                            }


                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    InputMethodManager imm = (InputMethodManager) CalcActivity.this.getSystemService(CalcActivity.this.INPUT_METHOD_SERVICE);
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
                findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                findViewById(R.id.scroleditext).setVisibility(View.GONE);
                findViewById(R.id.commenee).setVisibility(View.GONE);
                findViewById(R.id.savepanel).setVisibility(View.GONE);
                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.add_comment));
                }

                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));
            } else {
                RelativeLayout headermain = (RelativeLayout) findViewById(R.id.headermain);
                AutoTransition cus = new AutoTransition();
                keyforback = true;
                isCalcLayoutOpen = false;

                cus.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
//                            if(mainView==null){
//                                return;
//                            }
                        try {
                            AutoTransition cus = new AutoTransition();
                            cus.setDuration(300);
                            cus.setStartDelay(0);
                            LinearLayout linbutview = (LinearLayout) findViewById(R.id.numbersbut);
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
                findViewById(R.id.addphotopanel).setVisibility(View.VISIBLE);
                findViewById(R.id.pasdigi).setVisibility(View.VISIBLE);


                findViewById(R.id.scroleditext).setVisibility(View.GONE);
                findViewById(R.id.commenee).setVisibility(View.GONE);
                findViewById(R.id.savepanel).setVisibility(View.GONE);

                comment_add.setText(oraliqComment);
                if (!oraliqComment.matches("")) {
                    comment.setText(oraliqComment);
                } else {
                    comment.setText(getString(R.string.add_comment));
                }

                headermain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPixel((getResources().getDimension(R.dimen.hundred_fivety_four) / getResources().getDisplayMetrics().density), CalcActivity.this)));

            }
        }


    }


    private Bitmap decodeFile(File f) {
        try {
//            Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
//            The new size we want to scale to
            final int REQUIRED_SIZE = 350;
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

    private Bitmap decodeFileToCache(File f) {
        try {
//            Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
//            The new size we want to scale to
            final int REQUIRED_SIZE = 64;
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

    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public void onBackPressed() {
        if (isCalcLayoutOpen) {
            Log.d("kakdi", "onBackPressed: ");
            closeLayout();
            return;

        }
        super.onBackPressed();
    }

    public double isLimitAccess(DaoSession daoSession, Context context, Account account, Calendar date) {
        FinanceRecordDao recordDao = daoSession.getFinanceRecordDao();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        double accounted = getCost(date, account.getStartMoneyCurrency(), account.getCurrency(), account.getAmount());
        for (int i = 0; i < recordDao.queryBuilder().list().size(); i++) {
            FinanceRecord tempac = recordDao.queryBuilder().list().get(i);
            if (tempac.getAccount().getId().matches(account.getId())) {
                if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
                    accounted = accounted + getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
                else
                    accounted = accounted - getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
            }
        }
        for (DebtBorrow debtBorrow : daoSession.getDebtBorrowDao().queryBuilder().list()) {
            if (debtBorrow.getCalculate()) {
                if (debtBorrow.getAccount().getId().matches(account.getId())) {
                    if (debtBorrow.getType() == DebtBorrow.BORROW) {
                        accounted = accounted - getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                    } else {
                        accounted = accounted + getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = recking.getPayDate();

                        if (debtBorrow.getType() == DebtBorrow.DEBT) {
                            accounted = accounted - getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                        } else {
                            accounted = accounted + getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                        }
                    }
                } else {
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = recking.getPayDate();
                        if (recking.getAccountId().matches(account.getId())) {

                            if (debtBorrow.getType() == DebtBorrow.BORROW) {
                                accounted = accounted + getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                            } else {
                                accounted = accounted - getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                            }
                        }
                    }
                }
            }
        }
        for (CreditDetials creditDetials : daoSession.getCreditDetialsDao().queryBuilder().list()) {
            if (creditDetials.getKey_for_include()) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                    if (reckingCredit.getAccountId().matches(account.getId())) {
                        accounted = accounted - getCost(reckingCredit.getPayDate(), creditDetials.getValyute_currency(), account.getCurrency(), reckingCredit.getAmount());
                    }
                }
            }
        }
        return accounted;
    }

    public double getCost(Calendar date, Currency fromCurrency, Currency toCurrency, double amount) {
        //TODO tekwir bir yana

        if (fromCurrency.getId().matches(toCurrency.getId())) return amount;
        double tokoeff = 1.0;
        double fromkoeff2 = 1.0;
        long todiff1 = date.getTimeInMillis() - toCurrency.getCosts().get(0).getDay().getTimeInMillis();
        long fromdiff = date.getTimeInMillis() - fromCurrency.getCosts().get(0).getDay().getTimeInMillis();
        if (todiff1 < 0) {
            tokoeff = toCurrency.getCosts().get(0).getCost();
        }
        if (fromdiff < 0) {
            fromkoeff2 = fromCurrency.getCosts().get(0).getCost();
        }
        int pos = 0;
        while (todiff1 >= 0 && pos < toCurrency.getCosts().size()) {
            todiff1 = date.getTimeInMillis() - toCurrency.getCosts().get(pos).getDay().getTimeInMillis();
            if (todiff1 >= 0)
                tokoeff = toCurrency.getCosts().get(pos).getCost();
            pos++;
        }
        pos = 0;
        while (fromdiff >= 0 && pos < fromCurrency.getCosts().size()) {
            fromdiff = date.getTimeInMillis() - fromCurrency.getCosts().get(pos).getDay().getTimeInMillis();
            if (fromdiff >= 0)
                fromkoeff2 = fromCurrency.getCosts().get(pos).getCost();
            pos++;
        }
        amount = tokoeff * amount / fromkoeff2;
        return amount;
    }

}