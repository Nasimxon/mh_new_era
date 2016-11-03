package com.jim.pocketaccounter.modulesandcomponents.components;

import android.content.SharedPreferences;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.SettingsActivity;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.finance.CurrencyChooseAdapter;
import com.jim.pocketaccounter.finance.TransferAccountAdapter;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.report.IncomeExpanseDataRow;
import com.jim.pocketaccounter.syncbase.SyncBase;
import com.jim.pocketaccounter.utils.SmsService;
import com.jim.pocketaccounter.utils.SubcatAdapterCircles;
import com.jim.pocketaccounter.utils.SubcatItemChecker;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.modulesandcomponents.modules.PocketAccounterApplicationModule;
import com.jim.pocketaccounter.utils.catselector.DrawingSelectorView;
import com.jim.pocketaccounter.utils.record.BalanceStripe;
import com.jim.pocketaccounter.utils.record.RecordButtonExpanse;
import com.jim.pocketaccounter.utils.record.RecordButtonIncome;
import com.jim.pocketaccounter.widget.WidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Named;

import dagger.Component;

/**
 * Created by DEV on 27.08.2016.
 */

@Component(modules = {PocketAccounterApplicationModule.class})
public interface PocketAccounterApplicationComponent {
    PocketAccounterApplication getPocketAccounterApplication();
    DaoSession getDaoSession();
    SharedPreferences getSharedPreferences();
    DataCache getDataCache();
    CommonOperations getCommonOperations();
    ReportManager reportManager();
    PocketAccounterApplicationModule getPocketAccounterApplicationModule();
    @Named(value = "begin") Calendar getBegin();
    @Named(value = "end") Calendar getEnd();
    @Named(value = "common_formatter") SimpleDateFormat getCommonFormatter();
    @Named(value = "display_formatter") SimpleDateFormat getDisplayFormatter();
    void inject(PocketAccounterApplication pocketAccounterApplication);
    void inject(RecordButtonExpanse recordButtonExpense);
    void inject(RecordButtonIncome recordButtonIncome);
    void inject(CurrencyChooseAdapter currencyChooseAdapter);
    void inject(ReportManager reportManager);
    void inject(CommonOperations commonOperations);
    void inject(LogicManager logicManager);
    void inject(TransferAccountAdapter transferAccountAdapter);
    void inject(DataCache dataCache);
    void inject(PAFragmentManager paFragmentManager);
    void inject(WidgetProvider widgetProvider);
    void inject(SyncBase syncBase);
    void inject(SettingsActivity settingsActivity);
    void inject(IncomeExpanseDataRow incomeExpanseDataRow);
    void inject(SmsService smsService);
    void inject(BalanceStripe balanceStripe);
    void inject(DrawingSelectorView drawingSelectorView);
    void inject(SubcatItemChecker subcatItemChecker);
    void inject(SubcatAdapterCircles subcatAdapterCircles);
}
