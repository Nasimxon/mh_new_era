package com.jim.finansia.managers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.debt.AddBorrowFragment;
import com.jim.finansia.debt.BorrowFragment;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.InfoDebtBorrowFragment;
import com.jim.finansia.debt.PocketClassess;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AddCreditFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.CreditArchiveFragment;
import com.jim.finansia.fragments.CreditFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.InfoCreditFragment;
import com.jim.finansia.fragments.MainFragment;
import com.jim.finansia.fragments.MainPageFragment;
import com.jim.finansia.fragments.ManualEnterFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.fragments.ReportFragment;
import com.jim.finansia.fragments.SMSParseInfoFragment;
import com.jim.finansia.fragments.ScheduleCreditFragment;
import com.jim.finansia.fragments.SearchFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.fragments.VoiceRecognizerFragment;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import static com.jim.finansia.PocketAccounter.PRESSED;

public class PAFragmentManager {
    private PocketAccounter activity;
    private FragmentManager fragmentManager;
    private int lastCount = 0;
    @Inject ReportManager reportManager;
    @Inject CommonOperations commonOperations;
    @Inject DataCache dataCache;
    @Inject @Named(value = "end") Calendar end;
    @Inject SharedPreferences preferences;
    @Inject DaoSession daoSession;
    @Inject FinansiaFirebaseAnalytics analytics;

    public PAFragmentManager(final PocketAccounter activity) {
        this.activity = activity;
        ((PocketAccounterApplication) activity.getApplicationContext()).component().inject(this);
        fragmentManager = activity.getSupportFragmentManager();
        if (fragmentManager != null && fragmentManager.getFragments() != null)
            lastCount = fragmentManager.getFragments().size();
    }

    public void notifyInfosVisibility(boolean visibility) {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()))
                ((MainPageFragment) fragment).visibilityOfInfos(visibility);
        }
    }
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void updateAllFragmentsOnViewPager() {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName())) {
                ((MainPageFragment) fragment).swipingUpdate();
            }
        }
    }

    public void updateAllFragmentsPageChanges() {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName())) {
                ((MainPageFragment) fragment).updatePageChanges();
            }
        }
    }

    public void updateVoiceRecognizePage(Calendar day) {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(VoiceRecognizerFragment.class.getName())) {
                ((VoiceRecognizerFragment) fragment).setDay(day);
                break;
            }
        }
    }

    public void updateTemplatesInVoiceRecognitionFragment() {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(VoiceRecognizerFragment.class.getName())) {
                ((VoiceRecognizerFragment) fragment).initVoices();
                break;
            }
        }
    }

    public void updateVoiceRecognizePageCurrencyChanges() {
        if (fragmentManager == null || fragmentManager.getFragments() == null) return;
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
        FrameLayout mainWhite = (FrameLayout) activity.findViewById(R.id.mainWhite);
        LinearLayout change = (LinearLayout) activity.findViewById(R.id.change);
        if (mainWhite != null) mainWhite.setVisibility(View.GONE);
        if (change != null) change.setVisibility(View.VISIBLE);
        int count = fragmentManager.getBackStackEntryCount();
        while (count > 0) {
            fragmentManager.popBackStack();
            count--;
        }
        updateAllFragmentsOnViewPager();
        updateAllFragmentsPageChanges();
    }

    public void displayFragment(Fragment fragment) {
        if (fragmentManager.findFragmentById(R.id.flMain) != null && fragment.getClass().getName().equals(fragmentManager.findFragmentById(R.id.flMain).getClass().getName()))
            return;
        PRESSED = true;
        fragmentManager
                .beginTransaction()
                .add(R.id.flMain, fragment, fragment.getClass().getName())
                .addToBackStack(null)
                .commit();
    }

    public void initMainWindow() {
        fragmentManager
                .beginTransaction()
                .add(R.id.flMainWindow, new MainFragment())
                .commit();
    }

    public void updateSmsFragmentChanges() {
        int size = fragmentManager.getFragments().size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            if (fragment != null && fragment.getClass().getName().equals(SmsParseMainFragment.class.getName())) {
                ((SmsParseMainFragment) fragment).refreshList();
            }
            if (fragment != null && fragment.getClass().getName().equals(SMSParseInfoFragment.class.getName())) {
                ((SMSParseInfoFragment) fragment).refresh();
            }
        }
    }

    public void remoteBackPress(DrawerInitializer drawerInitializer) {
        fragmentManager.popBackStack();
        Fragment fragment = fragmentManager.findFragmentById(R.id.flMain);
        String fragName = fragment.getClass().getName();
        if (fragName.equals(PocketClassess.DEBTBORROW_FRAG)
                || fragName.equals(PocketClassess.AUTOMARKET_FRAG)
                || fragName.equals(PocketClassess.CURRENCY_FRAG)
                || fragName.equals(PocketClassess.CATEGORY_FRAG)
                || fragName.equals(PocketClassess.ACCOUNT_FRAG)
                || fragName.equals(PocketClassess.CREDIT_FRAG)
                || fragName.equals(PocketClassess.PURPOSE_FRAG)
                || fragName.equals(PocketClassess.THEMES)
                || fragName.equals(PocketClassess.SMS_PARSE_FRAGMENT)
                || fragName.equals(PocketClassess.RECORD_DETEIL_FRAGMENT)
                || fragName.equals(PocketClassess.REPORT)
                || fragName.equals(PocketClassess.SEARCH_FRAGMENT)) {
            drawerInitializer.inits();
            displayMainWindow();
        } else if (fragName.equals(PocketClassess.ADD_DEBTBORROW)) {
            int localAppereance = ((AddBorrowFragment) fragment).getLocalAppereance();
            if (localAppereance == DebtBorrowFragment.FROM_INFO) {
                for (Fragment frag : fragmentManager.getFragments()) {
                    if (frag == null) continue;
                    if (frag.getClass().getName().equals(InfoDebtBorrowFragment.class.getName())) {
                        InfoDebtBorrowFragment infoDebtBorrowFragment = (InfoDebtBorrowFragment) frag;
                        if (infoDebtBorrowFragment != null)
                            infoDebtBorrowFragment.updateToolbar();
                    }
                }
            }

        }
        else if (fragName.equals(PocketClassess.INFO_DEBTBORROW)) {
            int mode = ((InfoDebtBorrowFragment) fragment).getMode();
            if (mode == PocketAccounterGeneral.NO_MODE) {
                if (((InfoDebtBorrowFragment) fragment).getLocalAppereance() == DebtBorrowFragment.FROM_MAIN) {
                    for (Fragment frag : fragmentManager.getFragments()) {
                        if (frag == null) continue;
                        if (frag.getClass().getName().equals(BorrowFragment.class.getName())) {
                            BorrowFragment f = (BorrowFragment) frag;
                            if (f != null) f.refreshList();
                        }
                        if (frag.getClass().getName().equals(DebtBorrowFragment.class.getName())) {
                            DebtBorrowFragment f = (DebtBorrowFragment) frag;
                            if (f != null) f.updateToolbar();
                        }
                    }
                }
            } else if (mode == PocketAccounterGeneral.MAIN ||
                    mode == PocketAccounterGeneral.EXPANSE_MODE ||
                    mode == PocketAccounterGeneral.INCOME_MODE) {
                displayMainWindow();
            } else if (mode == PocketAccounterGeneral.DETAIL) {
                for (Fragment frag : fragmentManager.getFragments()) {
                    if (frag == null) continue;
                    if (frag.getClass().getName().equals(RecordDetailFragment.class.getName())) {
                        ((RecordDetailFragment) frag).updateFragments();
                    }
                }
            }
        } else if (fragName.equals(PocketClassess.ADD_AUTOMARKET)) {
            displayFragment(new AutoMarketFragment());
        } else if (fragName.equals(PocketClassess.REPORT_CATEGORY) || fragName.equals(PocketClassess.REPORT_DAILY_TABLE)
                || fragName.equals(PocketClassess.REPORT_DAILY) || fragName.equals(PocketClassess.REPORT_MONTHLY)) {
            for (Fragment frag : getFragmentManager().getFragments()) {
                if (frag == null) continue;
                if (frag.getClass().getName().equals(ReportFragment.class.getName())) {
                    ReportFragment f = (ReportFragment) frag;
                    if (f != null) f.updateToolbar();
                }
            }
        } else if (fragName.equals(PocketClassess.CURRENCY_CHOOSE) || fragName.equals(PocketClassess.CURRENCY_EDIT)) {
            for (Fragment frag : fragmentManager.getFragments()) {
                if (frag == null) continue;
                if (frag.getClass().getName().equals(CurrencyFragment.class.getName())) {
                    CurrencyFragment currencyFragment = (CurrencyFragment) frag;
                    if (currencyFragment != null) currencyFragment.updateToolbar();
                    break;
                }
            }
        } else if (fragName.equals(PocketClassess.CATEGORY_INFO) || fragName.equals(PocketClassess.ADD_CATEGORY)) {
            displayFragment(new CategoryFragment());
        } else if (fragName.equals(PocketClassess.ACCOUNT_EDIT) || fragName.equals(PocketClassess.ACCOUNT_INFO)) {
            displayFragment(new AccountFragment());
        } else if (fragName.equals(PocketClassess.ADD_AUTOMARKET)) {
            displayFragment(new AutoMarketFragment());
        } else if (fragName.equals(PocketClassess.INFO_CREDIT)) {
            InfoCreditFragment infoCreditFragment = (InfoCreditFragment) fragment;
            if (infoCreditFragment.getMode() == PocketAccounterGeneral.NO_MODE) {
                boolean found = false;
                for (Fragment frag : fragmentManager.getFragments()) {
                    if (frag == null) continue;
                    if (frag.getClass().getName().equals(CreditFragment.class.getName())) {
                        CreditFragment creditTabLay = (CreditFragment) frag;
                        if (creditTabLay != null) {
                            creditTabLay.updateList();
                            found = true;
                        }
                    }
                    if (frag.getClass().getName().equals(CreditArchiveFragment.class.getName())) {
                        CreditArchiveFragment creditTabLay = (CreditArchiveFragment) frag;
                        if (creditTabLay != null) {
                            creditTabLay.updateList();
                            found = true;
                        }
                    }
                }
                if (!found) displayFragment(new CreditTabLay());
            } else if (infoCreditFragment.getMode() == PocketAccounterGeneral.MAIN ||
                    infoCreditFragment.getMode() == PocketAccounterGeneral.INCOME_MODE ||
                    infoCreditFragment.getMode() == PocketAccounterGeneral.EXPANSE_MODE) {
                displayMainWindow();
            } else if (infoCreditFragment.getMode() == PocketAccounterGeneral.SEARCH_MODE) {
                for (Fragment fr : fragmentManager.getFragments()) {
                    if (fr == null) continue;
                    if (fr.getClass().getName().equals(RecordDetailFragment.class.getName())) {
                        ((RecordDetailFragment) fr).updateFragments();
                    }
                }
            }

        } else if (fragName.equals(PocketClassess.INFO_PURPOSE) || fragName.equals(PocketClassess.ADD_PURPOSE)) {
            displayFragment(new PurposeFragment());
        } else if (fragName.equals(PocketClassess.RECORD_EDIT_FRAGMENT)) {
            int parent = ((RecordEditFragment)fragment).getParent();
            if (parent == PocketAccounterGeneral.DETAIL) {
                activity.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                bundle.putString(RecordDetailFragment.DATE, format.format(dataCache.getEndDate().getTime()));
                RecordDetailFragment fr = new RecordDetailFragment();
                fr.setArguments(bundle);
                displayFragment(fr);
            } else if (parent == PocketAccounterGeneral.MAIN){
                displayMainWindow();
            } else if (parent == PocketAccounterGeneral.SEARCH_MODE)
                displayFragment(new SearchFragment());
        } else if (fragName.equals(PocketClassess.INFO_SMS_PARSE_FRAGMENT)) {

            displayFragment(new SmsParseMainFragment());
        } else if (fragName.equals(PocketClassess.CREDIT_SCHEDULE)) {
            ScheduleCreditFragment scheduleCreditFragment = (ScheduleCreditFragment) fragment;
            if (scheduleCreditFragment.getLocalAppereance() == CreditTabLay.LOCAL_EDIT) {
                for (Fragment fr : fragmentManager.getFragments()) {
                    if (fr.getClass().getName().equals(AddCreditFragment.class.getName())) {
                        ((AddCreditFragment) fr).toolbarBackupMethod();
                        break;
                    }
                }
            }
        }
    }
}