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
        fullBought();
    }
    private void fullBought() {
        sharedPreferences
                .edit()
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CATEGORY_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CREDIT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_DEBT_BORROW_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_FUNCTION, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_PAGE, true)
                .putInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.CREDIT_COUNT_KEY, 5)
                .putInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.DEBT_BORROW_COUNT_KEY, 15)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.BLUE_THEME, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.YELLOW_THEME, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIOLA_THEME, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys .THIRD_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.NINTH_PAGE_COUNT_KEY, true)
                .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.VOICE_RECOGNITION_KEY, true)
                .putInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SMS_PARSING_COUNT_KEY, 10)
                .commit();
    }
    public PocketAccounterApplicationComponent component() {
        return pocketAccounterApplicationComponent;
    }
}
