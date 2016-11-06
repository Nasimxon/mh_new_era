package com.jim.pocketaccounter.managers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;

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
import com.jim.pocketaccounter.fragments.PurposeFragment;
import com.jim.pocketaccounter.fragments.RecordDetailFragment;
import com.jim.pocketaccounter.fragments.SmsParseMainFragment;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

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

    public boolean isMainReturn() {
        return isMainReturn;
    }

    public void setMainReturn(boolean mainReturn) {
        isMainReturn = mainReturn;
    }

    @Inject
    ReportManager reportManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    DataCache dataCache;
    @Inject
    @Named(value = "end")
    Calendar end;
    private MainPageFragment nextPage;
    private ViewPager lvpMain;

    public PAFragmentManager(PocketAccounter activity) {
        this.activity = activity;
        ((PocketAccounterApplication) activity.getApplicationContext()).component().inject(this);
        lvpMain = (ViewPager)activity.findViewById(R.id.lvpMain);
        fragmentManager = activity.getSupportFragmentManager();
    }

    public ViewPager getLvpMain() {
        return lvpMain;
    }

    public void setLvpMain(ViewPager lvpMain) {
        this.lvpMain = lvpMain;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void initialize() {
        final ViewPager lvpMain = (ViewPager) activity.findViewById(R.id.lvpMain);
        FragmentPagerAdapter adapter = new LVPAdapter(getFragmentManager());
        lvpMain.setCurrentItem(5000, false);
        lvpMain.post(new Runnable() {
            @Override
            public void run() {
                lvpMain.setCurrentItem(5000, false);
            }
        });
        lvpMain.setAdapter(adapter);
        lvpMain.setOffscreenPageLimit(0);
        lvpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (lastPos != position && direction == null) {
                    direction = lastPos<position;
                }
                final MainPageFragment page = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+position);
                MainPageFragment prevFragment;
                if (lastPos>position && !direction) {
                    prevFragment = ((MainPageFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(position+1)));
                    if (prevFragment.getDay().compareTo(page.getDay()) <= 0) {
                        Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, -1);
                        page.setDay(day);
                    }
                }
                if (lastPos<position && direction) {
                    prevFragment = ((MainPageFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(position-1)));
                    if (prevFragment.getDay().compareTo(page.getDay()) >= 0) {
                        final Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, 1);
                        page.setDay(day);
                    }
                }
                if (page != null) {
                    page.update();
                    dataCache.setEndDate(page.getDay());
                    dataCache.updatePercentsWhenSwiping();
                    page.update();
                }
                lastPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
        MainPageFragment fragment = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+lvpMain.getCurrentItem());
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
        MainPageFragment leftPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(lvpMain.getCurrentItem()-1));
        if (leftPage != null)
            leftPage.initialize();
        MainPageFragment rightPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(lvpMain.getCurrentItem()+1));
        if (rightPage != null)
            rightPage.initialize();
        MainPageFragment centerPage = (MainPageFragment) fragmentManager.findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+lvpMain.getCurrentItem());
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

    public void displayFragment(Fragment fragment, String tag) {
//        main.setVisibility(View.GONE);
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

    class LVPAdapter extends FragmentPagerAdapter {
        public LVPAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Calendar end = Calendar.getInstance();
            end.add(Calendar.DAY_OF_MONTH, position - 5000);
            Fragment fragment = new MainPageFragment(activity, end);
            nextPage = (MainPageFragment) fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return 10000;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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
