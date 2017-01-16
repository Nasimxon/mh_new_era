package com.jim.finansia.managers;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.PocketClassess;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.InfoCreditFragment;
import com.jim.finansia.fragments.InfoCreditFragmentForArchive;
import com.jim.finansia.fragments.MainPageFragment;
import com.jim.finansia.fragments.ManualEnterFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.fragments.ReportFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.fragments.VoiceRecognizerFragment;
import com.jim.finansia.helper.MyVerticalViewPager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import static com.jim.finansia.PocketAccounter.PRESSED;

/**
 * Created by DEV on 27.08.2016.
 */

public class PAFragmentManager {
    private PocketAccounter activity;
    private FragmentManager fragmentManager;
    private int lastPos = 5000;
    private Boolean direction = null;
    private boolean isMainReturn = false;
    private MyVerticalViewPager vpVertical;
    public boolean isMainReturn() {
        return isMainReturn;
    }
    public void setMainReturn(boolean mainReturn) {
        isMainReturn = mainReturn;
    }
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    @Inject DataCache dataCache;
    @Inject @Named(value = "end") Calendar end;
    @Inject SharedPreferences preferences;
    @Inject DaoSession daoSession;
    private VerticalViewPagerAdapter adapter;
    public PAFragmentManager(final PocketAccounter activity) {
        this.activity = activity;
        ((PocketAccounterApplication) activity.getApplicationContext()).component().inject(this);
        fragmentManager = activity.getSupportFragmentManager();
        vpVertical = (MyVerticalViewPager) activity.findViewById(R.id.vpVertical);
        adapter = new VerticalViewPagerAdapter(fragmentManager);
        vpVertical.setOnTouchListener(null);
        vpVertical.setAdapter(adapter);
        int mainSelectedPage = preferences.getInt(PocketAccounterGeneral.VERTICAL_SELECTED_PAGE, 1);
        vpVertical.setCurrentItem(mainSelectedPage, false);
        vpVertical.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                preferences
                        .edit()
                        .putInt(PocketAccounterGeneral.VERTICAL_SELECTED_PAGE, position)
                        .commit();
                if (position == 0)
                    activity.setToToolbarVoiceMode();
                else
                    activity.setToToolbarManualEnterMode();

            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
    public void notifyInfosVisibility() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()))
                ((MainPageFragment) fragment).toggleVisibilityForInfos();
        }
    }
    public void setVerticalScrolling (boolean isSwipe) {
        vpVertical.setSwipe(isSwipe);
    }
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
    public void updateAllFragmentsOnViewPager() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName())) {
                ((MainPageFragment) fragment).update();
            }
        }
    }
    public MainPageFragment getCurrentFragment() {
        ManualEnterFragment manualEnterFragment = (ManualEnterFragment) adapter.getItem(1);
        ViewPager lvpMain = null;
        if (manualEnterFragment.getLvpMain() != null)
            lvpMain = manualEnterFragment.getLvpMain();
        else
            lvpMain = new ViewPager(activity);
        MainPageFragment fragment = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:" + lvpMain + ":" + lvpMain.getCurrentItem());
        return fragment;
    }
    public void updateAllFragmentsPageChanges() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName())) {
                ((MainPageFragment) fragment).updatePageChanges();
            }
        }
    }
    public void updateCurrencyChanges() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(ManualEnterFragment.class.getName())) {
                ((ManualEnterFragment) fragment).refreshCurrencyChanges();
                break;
            }
        }
    }

    public void updateVoiceRecognizePage(Calendar day) {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(VoiceRecognizerFragment.class.getName())) {
                ((VoiceRecognizerFragment) fragment).setDay(day);
                break;
            }
        }
    }

    public void updateVoiceRecognizePageCurrencyChanges() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(VoiceRecognizerFragment.class.getName())) {
                ((VoiceRecognizerFragment) fragment).refreshCurrencyChanges();
                break;
            }
        }
    }

    public void setPage(int page) {
        ManualEnterFragment fragment = null;
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            if (fragmentManager.getFragments() != null && fragmentManager.getFragments().get(i).getClass().getName().equals(ManualEnterFragment.class.getName())) {
                fragment = (ManualEnterFragment) fragmentManager.getFragments().get(i);
                break;
            }
        }
        if (fragment != null) {
            fragment.setCurrentPage(page);
        }
    }

    public void displayMainWindow() {
        activity.treatToolbar();
        PRESSED = false;
        activity.findViewById(R.id.mainWhite).setVisibility(View.GONE);
        activity.findViewById(R.id.change).setVisibility(View.VISIBLE);
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            if (fragmentManager.getFragments().get(i) != null && fragmentManager.getFragments().get(i).getClass().getName().equals(MainPageFragment.class.getName())) {
                MainPageFragment page = (MainPageFragment) fragmentManager.getFragments().get(i);
                if (page != null)
                    page.initialize();
            }
        }
        if (fragmentManager.getBackStackEntryCount() > 0)
            fragmentManager.popBackStack();
    }
    public void displayFragment(Fragment fragment) {
        if (fragmentManager.findFragmentById(R.id.flMain) != null && fragment.getClass().getName().equals(fragmentManager.findFragmentById(R.id.flMain).getClass().getName()))
            return;
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        PRESSED = true;
        fragmentManager
                .beginTransaction()
//                .setCustomAnimations(
//                        R.anim.slide_left_enter_custom_animation,
//                        R.anim.slide_left_exit_custom_animation,
//                        R.anim.slide_left_enter_custom_animation,
//                        R.anim.slide_left_exit_custom_animation
//                )
                .addToBackStack(null)
                .replace(R.id.flMain, fragment)
                .commit();
    }
    public void updateMain() {
        dataCache.updateAllPercents();
    }
    public void displayFragment(Fragment fragment, String tag) {
        if (fragmentManager.findFragmentById(R.id.flMain) != null && fragment.getClass().getName().equals(fragmentManager.findFragmentById(R.id.flMain).getClass().getName()))
            return;
        PRESSED = true;
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .add(R.id.flMain, fragment, tag)
                .commit();
    }
    class VerticalViewPagerAdapter extends FragmentStatePagerAdapter {
        public VerticalViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0)
                fragment = new VoiceRecognizerFragment(dataCache.getEndDate());
            else {
                fragment = new ManualEnterFragment();
            }
            return fragment;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }
    public void remoteBackPress(DrawerInitializer drawerInitializer) {
        String fragName = getFragmentManager().findFragmentById(R.id.flMain).getClass().getName();
        int count = getFragmentManager().getBackStackEntryCount();
        while (count > 0) {
            getFragmentManager().popBackStack();
            count--;
        }
        if (isMainReturn) {
            isMainReturn = false;
            displayMainWindow();
        } else {
            if (fragName.equals(PocketClassess.DEBTBORROW_FRAG) || fragName.equals(PocketClassess.AUTOMARKET_FRAG)
                    || fragName.equals(PocketClassess.CURRENCY_FRAG) || fragName.equals(PocketClassess.CATEGORY_FRAG)
                    || fragName.equals(PocketClassess.ACCOUNT_FRAG) || fragName.equals(PocketClassess.CREDIT_FRAG)
                    || fragName.equals(PocketClassess.PURPOSE_FRAG) || fragName.equals(PocketClassess.REPORT_ACCOUNT)
                    || fragName.equals(PocketClassess.THEMES) || fragName.equals(PocketClassess.SMS_PARSE_FRAGMENT)
                    || fragName.equals(PocketClassess.RECORD_DETEIL_FRAGMENT) || fragName.equals(PocketClassess.REPORT)) {
                drawerInitializer.inits();
                displayMainWindow();
            } else if (fragName.equals(PocketClassess.ADD_DEBTBORROW) || fragName.equals(PocketClassess.INFO_DEBTBORROW)) {
                displayFragment(new DebtBorrowFragment());
            } else if (fragName.equals(PocketClassess.ADD_AUTOMARKET) || fragName.equals(PocketClassess.INFO_DEBTBORROW)) {
                displayFragment(new AutoMarketFragment());
            } else if (fragName.equals(PocketClassess.REPORT_CATEGORY) || fragName.equals(PocketClassess.REPORT_DAILY_TABLE)
                    || fragName.equals(PocketClassess.REPORT_DAILY) || fragName.equals(PocketClassess.REPORT_MONTHLY)) {
                displayFragment(new ReportFragment());
            } else if (fragName.equals(PocketClassess.CURRENCY_CHOOSE) || fragName.equals(PocketClassess.CURRENCY_EDIT)) {
                displayFragment(new CurrencyFragment());
            } else if (fragName.equals(PocketClassess.CATEGORY_INFO) || fragName.equals(PocketClassess.ADD_CATEGORY)) {
                displayFragment(new CategoryFragment());
            } else if (fragName.equals(PocketClassess.ACCOUNT_EDIT) || fragName.equals(PocketClassess.ACCOUNT_INFO)) {
                displayFragment(new AccountFragment());
            } else if (fragName.equals(PocketClassess.ADD_AUTOMARKET)) {
                displayFragment(new AutoMarketFragment());
            } else if (fragName.equals(PocketClassess.INFO_CREDIT) || fragName.equals(PocketClassess.ADD_CREDIT)) {
                displayFragment(new CreditTabLay());
            } else if (fragName.equals(PocketClassess.INFO_CREDIT_ARCHIVE)) {
                CreditTabLay creditTabL = new CreditTabLay();
                creditTabL.setArchivePosition();
                displayFragment(new CreditTabLay());
            } else if (fragName.equals(PocketClassess.INFO_PURPOSE) || fragName.equals(PocketClassess.ADD_PURPOSE)) {
                displayFragment(new PurposeFragment());
            } else if (fragName.equals(PocketClassess.RECORD_EDIT_FRAGMENT)) {
                displayFragment(new RecordDetailFragment(dataCache.getEndDate()));
            } else if (fragName.equals(PocketClassess.ADD_SMS_PARSE_FRAGMENT) || fragName.equals(PocketClassess.INFO_SMS_PARSE_FRAGMENT)) {
                displayFragment(new SmsParseMainFragment());
            } else if (fragName.equals(PocketClassess.CREDIT_SCHEDULE)) {
                if(preferences.getInt("FRAG_ID",0) == 1) {
                    InfoCreditFragment infoCreditFragment = new InfoCreditFragment();
                    long credit_id = preferences.getLong("CREDIT_ID", 0);
                    if(credit_id != 0){
                        CreditDetials creditDetials = daoSession.getCreditDetialsDao().load(credit_id);
                        infoCreditFragment.setConteent(creditDetials,0,null);
                        displayFragment(infoCreditFragment);
                    }
                    else displayMainWindow();
                }
                else if (preferences.getInt("FRAG_ID",0) == 2){
                    InfoCreditFragmentForArchive infoCreditFragmentForArchive = new InfoCreditFragmentForArchive();
                     long credit_id = preferences.getLong("CREDIT_ID", 0);
                     if(credit_id != 0){
                         CreditDetials creditDetials = daoSession.getCreditDetialsDao().load(credit_id);
                         infoCreditFragmentForArchive.setConteent(creditDetials,0,null);
                         displayFragment(infoCreditFragmentForArchive);
                     }
                      else displayMainWindow();
                } else if (preferences.getInt("FRAG_ID", 0) == 3){
                    CreditTabLay creditTabL = new CreditTabLay();
                    creditTabL.setArchivePosition();
                    displayFragment(new CreditTabLay());
                }
            }
        }
    }
}