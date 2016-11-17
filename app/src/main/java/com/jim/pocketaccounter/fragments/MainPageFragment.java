package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.billing.LockViewButtonClickListener;
import com.jim.pocketaccounter.utils.billing.MainPageLockView;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.record.BalanceStripe;
import com.jim.pocketaccounter.utils.record.BoardView;
import com.jim.pocketaccounter.utils.record.DecorationBoardView;
import com.jim.pocketaccounter.utils.record.PageChangeListener;
import com.jim.pocketaccounter.utils.record.RecordExpanseView;
import com.jim.pocketaccounter.utils.record.RecordIncomesView;
import com.jim.pocketaccounter.utils.record.TextDrawingBoardView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.jim.pocketaccounter.PocketAccounter.PRESSED;

@SuppressLint("ValidFragment")
public class MainPageFragment extends Fragment {
    private LinearLayout llMainPageBackground;
    private Calendar day;
    private PocketAccounter pocketAccounter;
    private boolean keyboardVisible = false;
    private RelativeLayout rlMainPageContainer;
    private BalanceStripe balanceStripe;
    private BoardView expenseView, incomeView;
    private MainPageLockView lockView;
    @Inject ReportManager reportManager;
    @Inject DataCache dataCache;
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject @Named(value = "begin") Calendar begin;
    @Inject @Named(value = "end") Calendar end;
    @Inject SharedPreferences preferences;
    private boolean pressed = false;
    public MainPageFragment(Context context, Calendar day) {
        this.day = (Calendar) day.clone();
        this.pocketAccounter = (PocketAccounter) context;
        pocketAccounter.component((PocketAccounterApplication) pocketAccounter.getApplicationContext()).inject(this);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.main_page_fragment, container, false);
        pocketAccounter.findViewById(R.id.main).setVisibility(View.VISIBLE);
        pocketAccounter.findViewById(R.id.main).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = pocketAccounter.findViewById(R.id.main).getRootView().getHeight() - pocketAccounter.findViewById(R.id.main).getHeight();
                if (heightDiff > dpToPx(pocketAccounter, 200)) { // if more than 200 dp, it's probably a keyboard...
                    keyboardVisible = true;
                } else {
                    keyboardVisible = false;
                }
            }
        });
        if (keyboardVisible) {
            InputMethodManager imm = (InputMethodManager) pocketAccounter.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(pocketAccounter.findViewById(R.id.main).getWindowToken(), 0);
            pocketAccounter.findViewById(R.id.main).postDelayed(new Runnable() {
                @Override
                public void run() {
                    keyboardVisible=false;
                    initialize();
                }
            },100);
        }
        llMainPageBackground = (LinearLayout) rootView.findViewById(R.id.llMainPageBackground);
        rlMainPageContainer = (RelativeLayout) rootView.findViewById(R.id.rlMainPageContainer);
        balanceStripe = new BalanceStripe(pocketAccounter, day);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        balanceStripe.setLayoutParams(lp);
        PRESSED = false;
        balanceStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PRESSED) return;
                paFragmentManager.displayFragment(new RecordDetailFragment(dataCache.getEndDate()));
                PRESSED = true;
            }
        });
        initialize();
        return rootView;
    }

    private boolean checkAccessForPage(int position) {
        String key = "";
        switch (position) {
            case 0:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.ZERO_PAGE_COUNT_KEY;
                break;
            case 1:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY;
                break;
            case 2:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY;
                break;
            case 3:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.THIRD_PAGE_COUNT_KEY;
                break;
            case 4:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY;
                break;
            case 5:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY;
                break;
            case 6:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY;
                break;
            case 7:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY;
                break;
            case 8:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY;
                break;
            case 9:
                key = PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY;
                break;
        }
        return preferences.getBoolean(key, false);
    }

    public void visiblityForInfos(boolean visible) {
        if (visible) {
            expenseView.showText();
            incomeView.showText();
        } else {
            expenseView.hideText();
            incomeView.hideText();
        }
    }

    public void startAnimation() {
        lockView.animateClouds();
    }

    public void initialize() {
        DisplayMetrics dm = pocketAccounter.getResources().getDisplayMetrics();
        double width = (double) dm.widthPixels;
        double height = (double) dm.heightPixels;
        double ratio = height/width;
        rlMainPageContainer.removeAllViews();
        expenseView = new BoardView(getContext(), PocketAccounterGeneral.EXPENSE, day);
        RelativeLayout.LayoutParams lpExpenses = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels);
        lpExpenses.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lockView = new MainPageLockView(getContext(), expenseView.getCurrentPage());
        lockView.setLayoutParams(lpExpenses);
        expenseView.setLayoutParams(lpExpenses);
        expenseView.setId(R.id.main_expense);
        expenseView.setOnPageChangeListener(new PageChangeListener() {
            @Override
            public void onPageChange(int position) {
                if (checkAccessForPage(position)) {
                    expenseView.unlockPage();
                    lockView.setVisibility(View.GONE);
                }
                else {
                    expenseView.lockPage();
                    lockView.setVisibility(View.VISIBLE);
                }
                lockView.setPage(position+1);
            }
        });
        rlMainPageContainer.addView(expenseView);
        lockView.setPage(expenseView.getCurrentPage()+1);
        rlMainPageContainer.addView(lockView);
        if (checkAccessForPage(expenseView.getCurrentPage())) {
            lockView.setVisibility(View.GONE);
            expenseView.unlockPage();
        }
        else {
            lockView.setVisibility(View.VISIBLE);
            expenseView.lockPage();
        }
        lockView.setLockViewButtonClickListener(new LockViewButtonClickListener() {
            @Override
            public void onLockViewButtonClickListener(boolean toForward) {
                if (checkAccessForPage(expenseView.getCurrentPage()))
                    expenseView.unlockPage();
                else
                    expenseView.unlockPage();
                if (toForward) {
                    expenseView.incCurrentPage();
                    lockView.animateButtons(false);
                }
                else {
                    expenseView.decCurrentPage();
                    lockView.animateButtons(true);
                }

                lockView.setPage(expenseView.getCurrentPage()+1);
                paFragmentManager.updateAllFragmentsPageChanges();
            }
        });
        incomeView = new BoardView(getContext(), PocketAccounterGeneral.INCOME, day);
        RelativeLayout.LayoutParams lpIncomes = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels/4);
        lpIncomes.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        incomeView.setLayoutParams(lpIncomes);
        incomeView.setId(R.id.main_income);
        rlMainPageContainer.addView(incomeView);

        RelativeLayout.LayoutParams lpBalance = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpBalance.addRule(RelativeLayout.BELOW, R.id.main_expense);
        lpBalance.addRule(RelativeLayout.ABOVE, R.id.main_income);
        if (balanceStripe == null) {
            balanceStripe = new BalanceStripe(pocketAccounter, day);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            balanceStripe.setLayoutParams(lp);
            PRESSED = false;
            balanceStripe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PRESSED) return;
                    paFragmentManager.displayFragment(new RecordDetailFragment(dataCache.getEndDate()));
                    PRESSED = true;
                }
            });
        }
        balanceStripe.setLayoutParams(lpBalance);
        rlMainPageContainer.addView(balanceStripe);
        balanceStripe.calculateBalance();
        if (ratio > 1.7) {
            balanceStripe.showNotImportantPart();
        }
        else {
            balanceStripe.hideNotImportantPart();
        }
    }
    public void refreshCurrencyChanges() {
        commonOperations.refreshCurrency();
    }
    public void update() {
        if (checkAccessForPage(expenseView.getCurrentPage())) {
            lockView.setVisibility(View.GONE);
            expenseView.unlockPage();
        }
        else {
            lockView.setVisibility(View.VISIBLE);
            expenseView.lockPage();
        }
        lockView.setPage(expenseView.getCurrentPage()+1);

        toolbarManager.setSubtitle(simpleDateFormat.format(day.getTime()));
        balanceStripe.calculateBalance();
        expenseView.invalidate();
        incomeView.invalidate();
    }
    public void updatePageChanges() {
        if (checkAccessForPage(expenseView.getCurrentPage())) {
            lockView.setVisibility(View.GONE);
            expenseView.unlockPage();
        }
        else {
            lockView.setVisibility(View.VISIBLE);
            expenseView.lockPage();
        }
        lockView.setPage(expenseView.getCurrentPage()+1);
        expenseView.refreshPagesCount();
        expenseView.init();
        expenseView.invalidate();
        incomeView.refreshPagesCount();
        incomeView.init();
        incomeView.invalidate();
    }

    public void hideClouds() {
        lockView.hideClouds();
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    public Calendar getDay() {return day;}
    public void setDay(Calendar day) {
        this.day = day;
    }
}
