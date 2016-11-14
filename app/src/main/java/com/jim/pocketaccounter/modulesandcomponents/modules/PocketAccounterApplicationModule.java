package com.jim.pocketaccounter.modulesandcomponents.modules;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoMaster;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.database.TemplateVoice;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.billing.PurchaseImplementation;
import com.jim.pocketaccounter.utils.cache.DataCache;

import org.greenrobot.greendao.database.Database;

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
    private PurchaseImplementation purchaseImplementation;
    private List<TemplateVoice> voices;

    public PocketAccounterApplicationModule(PocketAccounterApplication pocketAccounterApplication) {
        this.pocketAccounterApplication = pocketAccounterApplication;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(pocketAccounterApplication, PocketAccounterGeneral.CURRENT_DB_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        preferences = PreferenceManager.getDefaultSharedPreferences(pocketAccounterApplication);
        purchaseImplementation = new PurchaseImplementation(pocketAccounterApplication, preferences);

    }

    @Provides
    public PocketAccounterApplication getPocketAccounterApplication() {
        return pocketAccounterApplication;
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
        return new ReportManager(pocketAccounterApplication);
    }

    @Provides
    public CommonOperations getCommonOperations() {
        return new CommonOperations(pocketAccounterApplication);
    }

    @Provides
    public PurchaseImplementation getPurchaseImplementation() {
        if (purchaseImplementation == null)
            purchaseImplementation = new PurchaseImplementation(pocketAccounterApplication, preferences);
        return purchaseImplementation;

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
    public List<TemplateVoice> getVoices() {
        if (voices == null) {
            voices = new ArrayList<>();
            daoSession = getDaoSession();
            for (Account ac : daoSession.getAccountDao().loadAll()) {
                CommonOperations.generateRegexVoice(getDaoSession(), voices, ac.getName().toLowerCase(), ac.getId());
            }
            for (RootCategory cat : daoSession.getRootCategoryDao().loadAll()) {
                CommonOperations.generateRegexVoice(daoSession, voices, cat.getName().toLowerCase(), cat.getId());
            }
            for (SubCategory subCategory : daoSession.getSubCategoryDao().loadAll()) {
                CommonOperations.generateRegexVoice(daoSession, voices, subCategory.getName().toLowerCase(), subCategory.getId());
            }
            for (Currency currency : daoSession.getCurrencyDao().loadAll()) {
                CommonOperations.generateRegexVoice(daoSession, voices, currency.getName().toLowerCase(), currency.getId());
            }
        }
        return voices;
    }
}