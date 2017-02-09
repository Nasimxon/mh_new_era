package com.jim.finansia;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.jim.finansia.credit.notificat.NotificationManagerCredit;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.debt.PocketClassess;
import com.jim.finansia.fragments.MainFragment;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.fragments.SearchFragment;
import com.jim.finansia.intropage.IntroIndicator;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.DrawerInitializer;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.SettingsManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.modulesandcomponents.components.DaggerPocketAccounterActivityComponent;
import com.jim.finansia.modulesandcomponents.components.PocketAccounterActivityComponent;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterActivityModule;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SMSMonitor;
import com.jim.finansia.utils.SmsService;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.billing.PurchaseImplementation;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.utils.password.OnPasswordRightEntered;
import com.jim.finansia.utils.password.PasswordWindow;
import com.jim.finansia.widget.WidgetKeys;
import com.jim.finansia.widget.WidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import static com.jim.finansia.utils.billing.PurchaseImplementation.BILLING_RESPONSE_RESULT_OK;
import static com.jim.finansia.utils.billing.PurchaseImplementation.REQUEST_CODE_BUY;

public class PocketAccounter extends AppCompatActivity {
    public static Toolbar toolbar;
    private PasswordWindow pwPassword;
    private Calendar date;
    public static boolean isCalcLayoutOpen = false;
    public static boolean openActivity = false;
    public static final int key_for_restat = 10101;
    private NotificationManagerCredit notific;
    boolean keyFromCalc = false;
    public static boolean PRESSED = false;
    int WidgetID;
    DatePickerDialog.OnDateSetListener getDatesetListener;
    private SMSMonitor receiver;
    public static boolean keyboardVisible = false;
    @Inject DaoSession daoSession;
    @Inject SharedPreferences preferences;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject SettingsManager settingsManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat format;
    @Inject DrawerInitializer drawerInitializer;
    @Inject CommonOperations commonOperations;
    @Inject DataCache dataCache;
    @Inject SharedPreferences sharedPreferences;
    @Inject PurchaseImplementation purchaseImplementation;
    @Inject ReportManager reportManager;
    PocketAccounterActivityComponent component;
    public PocketAccounterActivityComponent component(PocketAccounterApplication application) {
        if (component == null) {
            component = DaggerPocketAccounterActivityComponent
                    .builder()
                    .pocketAccounterActivityModule(new PocketAccounterActivityModule(this, (Toolbar) findViewById(R.id.toolbar)))
                    .pocketAccounterApplicationComponent(application.component())
                    .build();
        }
        return component;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init theme begin
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = prefs.getString(PocketAccounterGeneral.CHOOSEN_THEME_NAME_KEY, PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.BLUE_THEME);
        int themeId = getResources().getIdentifier(themeName, "style", getPackageName());
        setTheme(themeId);
        //init theme end
        setContentView(R.layout.pocket_accounter);
        //injecting all necessary variables
        component((PocketAccounterApplication) getApplication()).inject(this);
        //display measurement(for all types of device displays
        initDisplaySettings();
        //initing languaga
        String lang = preferences.getString("language", getResources().getString(R.string.language_default));
        if (lang.matches(getResources().getString(R.string.language_default)))
            setLocale(Locale.getDefault().getLanguage());
        else
            setLocale(lang);
        //intropage settings
        if (getSharedPreferences("infoFirst", MODE_PRIVATE).getBoolean("FIRST_KEY", true)) {
            try {
                Intent first = new Intent(this, IntroIndicator.class);
                PocketAccounter.openActivity = true;
                startActivity(first);
                finish();
            } finally {}
        }
        //toolbar initialization
        toolbarManager.init();
        //init toolbar
        treatToolbar();
        //initializing main window elements and show them
        paFragmentManager.initMainWindow();
        //secure layer configuring
        pwPassword = (PasswordWindow) findViewById(R.id.pwPassword);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("secure", false)) {
            pwPassword.setVisibility(View.VISIBLE);
            pwPassword.setOnPasswordRightEnteredListener(new OnPasswordRightEntered() {
                @Override
                public void onPasswordRight() {
                    pwPassword.setVisibility(View.GONE);
                }

                @Override
                public void onExit() {
                    finish();
                }
            });
        }
        //notification of credit
        notific = new NotificationManagerCredit(this);
        boolean notif = sharedPreferences.getBoolean("general_notif", true);
        if (!notif) {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        notific.cancelAllNotifs();
                    } catch (Exception o) {
                    }
                }
            })).start();
        }
        //switching toolbar for two modes
        int mode = preferences.getInt(PocketAccounterGeneral.VERTICAL_SELECTED_PAGE, 1);
        if (mode == 0)
            setToToolbarVoiceMode();
        else
            setToToolbarManualEnterMode();
        //receiver for updating ui after getting sms message
        receiver = new SMSMonitor() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(SmsService.PA_MESSAGE);
                if (s != null) {
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    paFragmentManager.updateTemplatesInVoiceRecognitionFragment();
                    paFragmentManager.updateAllFragmentsPageChanges();
                    paFragmentManager.updateSmsFragmentChanges();
                }
            }
        };

    }

    public void setToToolbarVoiceMode() {
        if (toolbarManager != null)
            toolbarManager.setToolbarIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
    }
    public void setToToolbarManualEnterMode() {
        if (toolbarManager != null)
            toolbarManager.setToolbarIconsVisibility(View.VISIBLE, View.GONE, View.VISIBLE);
    }


    private void initDisplaySettings() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        if (width == 240 && height == 320) {
            sharedPreferences.edit().putInt(PocketAccounterGeneral.EXPENSE_BOARD_KEYS_COUNT, 2).commit();
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public Calendar getDate() {
        return date;
    }

    public void treatToolbar() {
        // toolbar set
        toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
        toolbarManager.setTitle(getResources().getString(R.string.app_name));
        toolbarManager.setSubtitle(format.format(dataCache.getEndDate().getTime()));
        toolbarManager.setSubtitleIconVisibility(View.VISIBLE);
        toolbarManager.setOnSearchButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                paFragmentManager.displayFragment(new SearchFragment());
            }
        });
        toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerInitializer.getDrawer().openLeftSide();
            }
        });
        toolbarManager.setToolbarIconsVisibility(View.VISIBLE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.ic_info_outline_black_48dp);
        toolbarManager.setImageToStartImage(R.drawable.ic_search_black_24dp);
        getDatesetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
               Calendar calend = new GregorianCalendar(arg1, arg2, arg3);
                 String key = preferences.getString("balance_solve", "0");
                Calendar begin, end;
                if (key.equals("0")) {
                    Calendar firstDay = commonOperations.getFirstDay();
                    if (firstDay == null)
                        firstDay = commonOperations.getFirstDay();
                    begin = (Calendar) firstDay.clone();
                    end = (Calendar) calend.clone();
                } else {
                    begin = (Calendar) calend.clone();
                    begin.set(Calendar.HOUR_OF_DAY, 0);
                    begin.set(Calendar.MINUTE, 0);
                    begin.set(Calendar.SECOND, 0);
                    begin.set(Calendar.MILLISECOND, 0);
                    end = (Calendar) calend.clone();
                    end.set(Calendar.HOUR_OF_DAY, 23);
                    end.set(Calendar.MINUTE, 59);
                    end.set(Calendar.SECOND, 59);
                    end.set(Calendar.MILLISECOND, 59);
                }
                dataCache.setBeginDate(begin);
                dataCache.setEndDate(end);
                long countOfDays = 0;
                if (end.compareTo(Calendar.getInstance()) >= 0) {
                    countOfDays = commonOperations.betweenDays(Calendar.getInstance(), end) - 1;
                    paFragmentManager.setPage(5000 + (int) countOfDays);
                } else {
                    countOfDays = commonOperations.betweenDays(end, Calendar.getInstance()) - 1;
                    paFragmentManager.setPage(5000 - (int) countOfDays);
                }
            }
        };
        toolbarManager.setOnTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog mDialog = new DatePickerDialog(PocketAccounter.this,
                        getDatesetListener, dataCache.getEndDate().get(Calendar.YEAR),
                        dataCache.getEndDate().get(Calendar.MONTH), dataCache.getEndDate()
                        .get(Calendar.DAY_OF_MONTH));
                mDialog.show();
            }
        });
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visibility = preferences.getBoolean(PocketAccounterGeneral.INFO_VISIBILITY, false);
                visibility = !visibility;
                paFragmentManager.notifyInfosVisibility(visibility);
                preferences.edit().putBoolean(PocketAccounterGeneral.INFO_VISIBILITY, visibility).commit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!drawerInitializer.getDrawer().isClosed()) {
            drawerInitializer.getDrawer().close();
        } else if (paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain) != null &&
                paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain).
                        getClass().getName().equals(PocketClassess.RECORD_EDIT_FRAGMENT) && isCalcLayoutOpen) {
            ((RecordEditFragment) paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain)).closeLayout();
        }
        else if (paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain) != null &&
                !paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain).getClass().getName().equals(MainFragment.class.getName())) {

                paFragmentManager.remoteBackPress(drawerInitializer);
        } else if (paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain) != null &&
                    paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain).
                            getClass().getName().equals(PocketClassess.SEARCH_FRAGMENT)) {
                toolbarManager.disableSearchMode();
        }
        else if (paFragmentManager.getFragmentManager().findFragmentById(R.id.flMain) == null){
            final WarningDialog warningDialog = new WarningDialog(this);
            warningDialog.setMyTitle(getResources().getString(R.string.warning));
            warningDialog.setText(getResources().getString(R.string.dou_you_want_quit));
            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.dismiss();
                    PocketAccounter.super.onBackPressed();
                    finish();
                }
            });
            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.dismiss();
                }
            });
            int width = getResources().getDisplayMetrics().widthPixels;
            warningDialog.getWindow().setLayout(8 * width / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
            warningDialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(SmsService.PA_RESULT)
        );
        reportManager.clearCache();
        dataCache.updateAllPercents();
        paFragmentManager.updateAllFragmentsPageChanges();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        boolean notif = sharedPreferences.getBoolean("general_notif", true);
        if (notif) {
            try {
                notific.cancelAllNotifs();
                notific.notificSetDebt();
                notific.notificSetCredit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            notific.cancelAllNotifs();
        }
        SharedPreferences sPref;
        sPref = getSharedPreferences("infoFirst", MODE_PRIVATE);
        WidgetID = sPref.getInt(WidgetKeys.SPREF_WIDGET_ID, -1);
        if (WidgetID >= 0) {
            if (AppWidgetManager.INVALID_APPWIDGET_ID != WidgetID)
                WidgetProvider.updateWidget(this, AppWidgetManager.getInstance(this), WidgetID);
        }
        drawerInitializer.onStopSuniy();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        keyFromCalc = true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("secure", false) && !openActivity) {
            if (!drawerInitializer.getDrawer().isClosed())
                drawerInitializer.getDrawer().close();
            pwPassword.setVisibility(View.VISIBLE);
            pwPassword.setOnPasswordRightEnteredListener(new OnPasswordRightEntered() {
                @Override
                public void onPasswordRight() {
                    pwPassword.setVisibility(View.GONE);
                }
                @Override
                public void onExit() {
                    finish();
                }
            });
        }
        openActivity = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        findViewById(R.id.change).setVisibility(View.VISIBLE);
        drawerInitializer.onActivResultForDrawerCalls(requestCode, resultCode, data);
        if (requestCode == key_for_restat && resultCode == 1111) {
            if (WidgetID >= 0) {
                try {
                    if (AppWidgetManager.INVALID_APPWIDGET_ID != WidgetID)
                        WidgetProvider.updateWidget(this, AppWidgetManager.getInstance(this),
                                WidgetID);
                } catch (Exception e) {
                }
            }
            finish();
        }
        if (requestCode == REQUEST_CODE_BUY) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", -1);
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                // можете проверить цифровую подпись
                purchaseImplementation.readPurchase(purchaseData);
            } else {
                // обрабатываем ответ
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        purchaseImplementation.unbindService();
    }
}