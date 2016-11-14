package com.jim.pocketaccounter;

import android.app.Application;
import android.content.SharedPreferences;

import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.modulesandcomponents.components.DaggerPocketAccounterApplicationComponent;
import com.jim.pocketaccounter.modulesandcomponents.components.PocketAccounterApplicationComponent;
import com.jim.pocketaccounter.modulesandcomponents.modules.PocketAccounterApplicationModule;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by DEV on 27.08.2016.
 */

public class PocketAccounterApplication extends Application {
    private PocketAccounterApplicationComponent pocketAccounterApplicationComponent;
    @Inject DaoSession daoSession;
    @Inject DataCache dataCache;
    @Inject SharedPreferences sharedPreferences;
    @Override
    public void onCreate() {
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "robotoRegular.ttf");
        pocketAccounterApplicationComponent = DaggerPocketAccounterApplicationComponent
                .builder()
                .pocketAccounterApplicationModule(new PocketAccounterApplicationModule(this))
                .build();
        pocketAccounterApplicationComponent.inject(this);
        sharedPreferences
                .edit()
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.ZERO_PAGE_COUNT_KEY, true)
                .commit();
        String  oldDbPath= "//data//data//" + getPackageName().toString()
                        + "//databases//" + PocketAccounterGeneral.OLD_DB_NAME;
        if (!(new File(oldDbPath).exists()) && !sharedPreferences.getBoolean(PocketAccounterGeneral.DB_ONCREATE_ENTER, false)) {
//            try {
//                daoSession.getDatabase().beginTransaction();
                CommonOperations.createDefaultDatas(sharedPreferences, getApplicationContext(), daoSession);
//                daoSession.getDatabase().setTransactionSuccessful();
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//            finally {
//                daoSession.getDatabase().endTransaction();
//            }
        }
        else if (new File(oldDbPath).exists()) {
//            try {
//                daoSession.getDatabase().beginTransaction();
                CommonOperations.migrateDatabase(getApplicationContext(), oldDbPath, daoSession, sharedPreferences);
//                daoSession.getDatabase().setTransactionSuccessful();
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//            finally {
//                daoSession.getDatabase().endTransaction();
//            }
        }
    }
    public PocketAccounterApplicationComponent component() {
        return pocketAccounterApplicationComponent;
    }
}
