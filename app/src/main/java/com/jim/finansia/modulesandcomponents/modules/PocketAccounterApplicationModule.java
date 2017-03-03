package com.jim.finansia.modulesandcomponents.modules;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountOperation;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyCost;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.TemplateAccount;
import com.jim.finansia.database.TemplateCurrencyVoice;
import com.jim.finansia.database.TemplateVoice;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import org.greenrobot.greendao.database.Database;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by DEV on 27.08.2016.
 */
@Module
public class PocketAccounterApplicationModule {
    private PocketAccounterApplication pocketAccounterApplication;
    private DaoSession daoSession;
    private DataCache dataCache;
    private SharedPreferences preferences;
    private Calendar begin, end;
    private SimpleDateFormat displayFormatter, commonFormatter;
    private List<TemplateVoice> voices;
    private List<TemplateAccount> accountVoice;
    private List<TemplateCurrencyVoice> currencyVoices;
    private DecimalFormat formatter;
    private ReportManager reportManager;
    private CommonOperations commonOperations;
    private FinansiaFirebaseAnalytics finansiaFiregbaseAnalytics;
    public PocketAccounterApplicationModule(PocketAccounterApplication pocketAccounterApplication) {
        this.pocketAccounterApplication = pocketAccounterApplication;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(pocketAccounterApplication, PocketAccounterGeneral.CURRENT_DB_NAME) {
            @Override
            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                super.onUpgrade(db, oldVersion, newVersion);
                switch (oldVersion) {
                    case 1:
                        /* v1->v2: all changes made in version 2 come here */
                        db.execSQL("ALTER TABLE ACCOUNT_OPERATIONS ADD COLUMN 'TARGET_CURRENCY_ID' TEXT;");
                        db.execSQL("ALTER TABLE ACCOUNT_OPERATIONS ADD COLUMN 'TARGET_AMOUNT' REAL;");
                        db.execSQL("ALTER TABLE ACCOUNT_OPERATIONS ADD COLUMN 'COST' REAL;");
                        break;
                }
            }
        };
        /*{
            @Override
            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                super.onUpgrade(db, oldVersion, newVersion);
                if (oldVersion == 1 && newVersion == 2) {
                    DaoSession tempSession = new DaoMaster(db).newSession();
                    List<AccountOperation> accountOperations = tempSession.loadAll(AccountOperation.class);
                    for (AccountOperation operation : accountOperations) {
                        operation.setCost(1.0);
                        operation.setTargetAmount(operation.getAmount());
                        operation.setTargetCurrency(operation.getCurrency());
                    }
                    tempSession.getAccountOperationDao().dropTable(db, false);
                    DaoMaster.createAllTables(db, true);
                    tempSession.insertOrReplace(accountOperations);
                    tempSession.getDatabase().close();
                }
            }
        };
        */
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        preferences = PreferenceManager.getDefaultSharedPreferences(pocketAccounterApplication);
        finansiaFiregbaseAnalytics = new FinansiaFirebaseAnalytics(pocketAccounterApplication);
    }

    @Provides
    public PocketAccounterApplication getPocketAccounterApplication() {
        return pocketAccounterApplication;
    }

    @Provides
    public DecimalFormat getFormatter() {
        if (formatter == null)
        {
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMaximumFractionDigits(2);
            formatter = (DecimalFormat) numberFormat;
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            formatter.setDecimalFormatSymbols(symbols);
        }
        return formatter;
    }

    @Provides
    public DaoSession getDaoSession() {
        if (daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(pocketAccounterApplication, PocketAccounterGeneral.CURRENT_DB_NAME);
            Database db = helper.getWritableDb();
            daoSession = new DaoMaster(db).newSession();
        }
        return daoSession;
    }

    public void updateDaoSession () {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(pocketAccounterApplication, PocketAccounterGeneral.CURRENT_DB_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    @Provides
    public PocketAccounterApplicationModule getModule () {
        return this;
    }

    @Provides
    public DataCache getDataCache() {
        if (dataCache == null)
            dataCache = new DataCache(pocketAccounterApplication);
        return dataCache;
    }

    @Provides
    public SharedPreferences getSharedPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(pocketAccounterApplication);
        return preferences;
    }

    @Provides
    public ReportManager reportManager() {
        if (reportManager == null)
            reportManager = new ReportManager(pocketAccounterApplication);
        return reportManager;
    }

    @Provides
    public CommonOperations getCommonOperations() {
        if (commonOperations == null)
            commonOperations = new CommonOperations(pocketAccounterApplication);
        return commonOperations;
    }

    @Provides
    @Named(value = "begin")
    public Calendar getBegin() {
        if (begin == null)
            begin = Calendar.getInstance();
        return begin;
    }

    @Provides
    @Named(value = "end")
    public Calendar getEnd() {
        if (end == null)
            end = Calendar.getInstance();
        return end;
    }
    @Provides
    @Named(value = "common_formatter")
    public SimpleDateFormat getCommonFormatter() {
        if (commonFormatter == null)
            commonFormatter = new SimpleDateFormat("dd.MM.yyyy");
        return commonFormatter;
    }
    @Provides
    @Named(value = "display_formatter")
    public SimpleDateFormat getDisplayFormatter() {
        if (displayFormatter == null)
            displayFormatter = new SimpleDateFormat("dd LLLL, yyyy");
        return displayFormatter;
    }


    @Provides
    public List<TemplateAccount> getAccountVoice () {
        if (accountVoice == null) {
            accountVoice = new ArrayList<>();
            daoSession = getDaoSession();
            for (Account ac : daoSession.getAccountDao().loadAll()) {
                CommonOperations.generateRegexAcocuntVoice(accountVoice, ac);
            }
        }
        return accountVoice;
     }

    @Provides
    public List<TemplateCurrencyVoice> getCurrencyVoices () {
        if (currencyVoices == null) {
            currencyVoices = new ArrayList<>();
            daoSession = getDaoSession();
            for (Currency cr: daoSession.getCurrencyDao().loadAll()) {
                CommonOperations.generateRegexCurrencyVoice(currencyVoices, cr, getPocketAccounterApplication());
            }
        }
        return currencyVoices;
    }
    @Provides
    public FinansiaFirebaseAnalytics getFinansiaFiregbaseAnalytics() {
        if (finansiaFiregbaseAnalytics == null)
            finansiaFiregbaseAnalytics = new FinansiaFirebaseAnalytics(pocketAccounterApplication);
        return finansiaFiregbaseAnalytics;

    }
}
