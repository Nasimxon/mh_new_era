package com.jim.pocketaccounter.managers;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.debt.DebtBorrowFragment;
import com.jim.pocketaccounter.debt.PocketClassess;
import com.jim.pocketaccounter.fragments.AccountFragment;
import com.jim.pocketaccounter.fragments.AutoMarketFragment;
import com.jim.pocketaccounter.fragments.CategoryFragment;
import com.jim.pocketaccounter.fragments.CreditTabLay;
import com.jim.pocketaccounter.fragments.CurrencyFragment;
import com.jim.pocketaccounter.fragments.MainPageFragment;
import com.jim.pocketaccounter.fragments.ManualEnterFragment;
import com.jim.pocketaccounter.fragments.PurposeFragment;
import com.jim.pocketaccounter.fragments.RecordDetailFragment;
import com.jim.pocketaccounter.fragments.SmsParseMainFragment;
import com.jim.pocketaccounter.fragments.VoiceRecognizerFragment;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import me.kaelaela.verticalviewpager.VerticalViewPager;

import static com.jim.pocketaccounter.PocketAccounter.PRESSED;

/**
 * Created by DEV on 27.08.2016.
 */

public class    PAFragmentManager {
    private PocketAccounter activity;
    private FragmentManager fragmentManager;
    private int lastPos = 5000;
    private Boolean direction = null;
    private boolean isMainReturn = false;
    private VerticalViewPager vpVertical;

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
    private VerticalViewPagerAdapter adapter;
    public PAFragmentManager(PocketAccounter activity) {
        this.activity = activity;
        ((PocketAccounterApplication) activity.getApplicationContext()).component().inject(this);
        fragmentManager = activity.getSupportFragmentManager();
        vpVertical = (VerticalViewPager) activity.findViewById(R.id.vpVertical);
        adapter = new VerticalViewPagerAdapter(fragmentManager);
        vpVertical.setAdapter(adapter);
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
        MainPageFragment fragment = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:"+ lvpMain+":"+lvpMain.getCurrentItem());
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
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName())) {
                ((MainPageFragment) fragment).refreshCurrencyChanges();
            }
        }
    }

    public void displayMainWindow() {
        activity.treatToolbar();
        PRESSED = false;
        activity.findViewById(R.id.mainWhite).setVisibility(View.GONE);
        activity.findViewById(R.id.change).setVisibility(View.VISIBLE);
        ManualEnterFragment manualEnterFragment = (ManualEnterFragment) adapter.getItem(1);
        ViewPager lvpMain = null;
        if (manualEnterFragment.getLvpMain() != null)
            lvpMain = manualEnterFragment.getLvpMain();
        else
            lvpMain = new ViewPager(activity);
        MainPageFragment leftPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+ lvpMain+":"+(lvpMain.getCurrentItem()-1));
        if (leftPage != null)
            leftPage.initialize();
        MainPageFragment rightPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+ lvpMain+":"+(lvpMain.getCurrentItem()+1));
        if (rightPage != null)
            rightPage.initialize();
        MainPageFragment centerPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+ lvpMain+":"+lvpMain.getCurrentItem());
        if (centerPage != null)
            centerPage.initialize();
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
                .setCustomAnimations(
                        R.anim.slide_left_enter_custom_animation,
                        R.anim.slide_left_exit_custom_animation,
                        R.anim.slide_left_enter_custom_animation,
                        R.anim.slide_left_exit_custom_animation
                )
                .addToBackStack(null)
                .replace(R.id.flMain, fragment)
                .commit();
    }

    public ViewPager getLvpMain() {
        ManualEnterFragment manualEnterFragment = (ManualEnterFragment) adapter.getItem(1);
        ViewPager lvpMain;
        if (manualEnterFragment.getLvpMain() != null)
            lvpMain = manualEnterFragment.getLvpMain();
        else
            lvpMain = new ViewPager(activity);
        return lvpMain;
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
                fragment = new VoiceRecognizerFragment();
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

    public void remoteBackPress() {
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
                    || fragName.equals(PocketClassess.REPORT_CATEGORY) || fragName.equals(PocketClassess.SMS_PARSE_FRAGMENT)
                    || fragName.equals(PocketClassess.RECORD_DETEIL_FRAGMENT) || fragName.equals(PocketClassess.REPORT_BY_INCOME_EXPANCE)) {
                displayMainWindow();
            } else if (fragName.equals(PocketClassess.ADD_DEBTBORROW) || fragName.equals(PocketClassess.INFO_DEBTBORROW)) {
                displayFragment(new DebtBorrowFragment());
            } else if (fragName.equals(PocketClassess.ADD_AUTOMARKET) || fragName.equals(PocketClassess.INFO_DEBTBORROW)) {
                displayFragment(new AutoMarketFragment());
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
                CreditTabLay creditTabL=new CreditTabLay();
                creditTabL.setArchivePosition();
                displayFragment(new CreditTabLay());
            } else if (fragName.equals(PocketClassess.INFO_PURPOSE) || fragName.equals(PocketClassess.ADD_PURPOSE)) {
                displayFragment(new PurposeFragment());
            } else if (fragName.equals(PocketClassess.RECORD_EDIT_FRAGMENT)) {
                displayFragment(new RecordDetailFragment(dataCache.getEndDate()));
            } else if (fragName.equals(PocketClassess.ADD_SMS_PARSE_FRAGMENT) || fragName.equals(PocketClassess.INFO_SMS_PARSE_FRAGMENT)) {
                displayFragment(new SmsParseMainFragment());
            }
        }
    }
}
