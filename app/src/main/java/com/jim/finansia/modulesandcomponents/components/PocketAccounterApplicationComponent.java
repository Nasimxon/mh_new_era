package com.jim.finansia.modulesandcomponents.components;

import android.content.SharedPreferences;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.SettingsActivity;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.TemplateAccount;
import com.jim.finansia.database.TemplateCurrencyVoice;
import com.jim.finansia.database.TemplateVoice;
import com.jim.finansia.finance.CurrencyChooseAdapter;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.fragments.ReportByIncomExpenseMonthDetailedByDaysFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterApplicationModule;
import com.jim.finansia.report.IncomeExpanseDataRow;
import com.jim.finansia.syncbase.SyncBase;
import com.jim.finansia.utils.SmsService;
import com.jim.finansia.utils.SpaceTabLayout;
import com.jim.finansia.utils.SubcatAdapterCircles;
import com.jim.finansia.utils.SubcatItemChecker;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.utils.calculator.CategoryAndSubcategoryChooserView;
import com.jim.finansia.utils.catselector.DrawingSelectorView;
import com.jim.finansia.utils.record.BalanceStripe;
import com.jim.finansia.utils.record.RecordButtonExpanse;
import com.jim.finansia.utils.record.RecordButtonIncome;
import com.jim.finansia.utils.reportfilter.CircleReportFilterView;
import com.jim.finansia.utils.reportviews.OneYearWithMonthsFragment;
import com.jim.finansia.widget.WidgetProvider;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
    List<TemplateAccount> getTemplateAccounts();
    List<TemplateCurrencyVoice> getCurrencyVoices();
    DecimalFormat getFormatter();
    FinansiaFirebaseAnalytics getFinansiaFiregbaseAnalytics();
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
    void inject(SubcatItemChecker subcatItemChecker);
    void inject(SubcatAdapterCircles subcatAdapterCircles);
    void inject(CircleReportFilterView circleReportFilterView);
    void inject(ReportByIncomExpenseMonthDetailedByDaysFragment reportByIncomExpenseMonthDetailedByDaysFragment);
    void inject(CategoryAndSubcategoryChooserView categoryAndSubcategoryChooserView);
    void inject(SpaceTabLayout spaceTabLayout);
}
