package com.jim.finansia;

import android.app.Application;
import android.content.SharedPreferences;

import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.modulesandcomponents.components.DaggerPocketAccounterApplicationComponent;
import com.jim.finansia.modulesandcomponents.components.PocketAccounterApplicationComponent;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterApplicationModule;

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
        super.onCreate();
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
                CommonOperations.createDefaultDatas(sharedPreferences, getApplicationContext(), daoSession);
        }
    }
    public PocketAccounterApplicationComponent component() {
        return pocketAccounterApplicationComponent;
    }



}
