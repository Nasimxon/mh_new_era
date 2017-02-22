package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.billing.LockViewButtonClickListener;
import com.jim.finansia.utils.billing.MainPageLockView;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.utils.record.BalanceStripe;
import com.jim.finansia.utils.record.BoardView;
import com.jim.finansia.utils.record.PageChangeListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.jim.finansia.PocketAccounter.PRESSED;
import static com.jim.finansia.PocketAccounter.toolbar;

@SuppressLint("ValidFragment")
public class MainPageFragment extends Fragment {
    private LinearLayout llMainPageBackground;
    private Calendar day = Calendar.getInstance();
    private int position = 0;
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
    @Inject FinansiaFirebaseAnalytics analytics;
    private boolean isCurrentDay = false;
    private boolean infosVisibility;
    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.main_page_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        ((PocketAccounter) getContext()).findViewById(R.id.main).setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                Calendar current = Calendar.getInstance();
                isCurrentDay = getArguments().getString(MainFragment.DATE).equals(format.format(current.getTime()));
                day.setTime(format.parse(getArguments().getString(MainFragment.DATE)));
                position = getArguments().getInt(MainFragment.POSITION);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //is keyboard panel opened?
        ((PocketAccounter) getContext()).findViewById(R.id.main).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getContext()== null) return;
                int heightDiff = ((PocketAccounter) getContext()).findViewById(R.id.main).getRootView().getHeight() - ((PocketAccounter) getContext()).findViewById(R.id.main).getHeight();
                if (heightDiff > dpToPx(getContext(), 200)) { // if more than 200 dp, it's probably a keyboard...
                    keyboardVisible = true;
                } else {
                    keyboardVisible = false;
                }
            }
        });
        if (keyboardVisible) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(((PocketAccounter) getContext()).findViewById(R.id.main).getWindowToken(), 0);
            ((PocketAccounter) getContext()).findViewById(R.id.main).postDelayed(new Runnable() {
                @Override
                public void run() {
                    keyboardVisible=false;
                    initialize();
                }
            },100);
        }
        llMainPageBackground = (LinearLayout) rootView.findViewById(R.id.llMainPageBackground);
        rlMainPageContainer = (RelativeLayout) rootView.findViewById(R.id.rlMainPageContainer);
        //balance stripe
        balanceStripe = new BalanceStripe(getContext(), day, begin, end, preferences, reportManager, dataCache, commonOperations);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        balanceStripe.setLayoutParams(lp);
        PRESSED = false;
        balanceStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PRESSED) return;
                ((PocketAccounter) getContext()).findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                bundle.putString(RecordDetailFragment.DATE, format.format(dataCache.getEndDate().getTime()));
                RecordDetailFragment fragment = new RecordDetailFragment();
                fragment.setArguments(bundle);
                paFragmentManager.displayFragment(fragment);
                PRESSED = true;
            }
        });
        initialize();
        return rootView;
    }

    private int getExpenseBoardViewHeight() {
        int height = 0;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (screenWidth == 240 && screenHeight == 320) {
            height = screenWidth/2;
        }
        if (screenWidth == 320 && screenHeight == 480 ||
                screenWidth == 240 && screenHeight == 400) {
            height = 74*screenWidth/100;
        }
        return height;
    }

    private boolean checkAccessForPage(int position) {
        String key = "";
        switch (position) {
            case 0:
                return true;
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

    public void startAnimation() {
        lockView.animateClouds();
    }

    public void initialize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        double width = (double) dm.widthPixels;
        double height = (double) dm.heightPixels;
        double ratio = height/width;
        rlMainPageContainer.removeAllViews();
        expenseView = new BoardView(getContext(), PocketAccounterGeneral.EXPENSE, day);
        int tempHeight = getExpenseBoardViewHeight();
        if (tempHeight == 0)
            tempHeight = dm.widthPixels;
        RelativeLayout.LayoutParams lpExpenses = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tempHeight);
        lpExpenses.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lockView = new MainPageLockView(getContext(), expenseView.getCurrentPage());
        lockView.setLayoutParams(lpExpenses);
        expenseView.setLayoutParams(lpExpenses);
        expenseView.setId(R.id.main_expense);
        expenseView.setOnPageChangeListener(new PageChangeListener() {
            @Override
            public void onPageChange(int position) {
                boolean visibility = false;
                if (checkAccessForPage(position)) {
                    expenseView.unlockPage();
                    visibility = preferences.getBoolean(PocketAccounterGeneral.INFO_VISIBILITY, infosVisibility);
                    lockView.setVisibility(View.GONE);
                }
                else {
                    expenseView.lockPage();
                    visibility = false;
                    lockView.setVisibility(View.VISIBLE);
                }
                lockView.setPage(position);
                paFragmentManager.notifyInfosVisibility(visibility);
            }
        });
        rlMainPageContainer.addView(expenseView);
        lockView.setPage(expenseView.getCurrentPage());
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
                boolean visibility = false;
                if (checkAccessForPage(expenseView.getCurrentPage())) {
                    expenseView.lockPage();
                    visibility = preferences.getBoolean(PocketAccounterGeneral.INFO_VISIBILITY, infosVisibility);
                }
                else {
                    expenseView.unlockPage();
                    visibility = false;
                }
                if (toForward) {
                    expenseView.incCurrentPage();
                    lockView.animateButtons(false);
                }
                else {
                    expenseView.decCurrentPage();
                    lockView.animateButtons(true);
                }
                lockView.setPage(expenseView.getCurrentPage());
                paFragmentManager.updateAllFragmentsPageChanges();
                paFragmentManager.notifyInfosVisibility(visibility);
            }
        });
        incomeView = new BoardView(getContext(), PocketAccounterGeneral.INCOME, day);
        RelativeLayout.LayoutParams lpIncomes = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels/4 + (int) getResources().getDimension(R.dimen.ten_dp));
        lpIncomes.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        incomeView.setLayoutParams(lpIncomes);
        incomeView.setId(R.id.main_income);
        rlMainPageContainer.addView(incomeView);
        RelativeLayout.LayoutParams lpBalance = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpBalance.addRule(RelativeLayout.BELOW, R.id.main_expense);
        lpBalance.addRule(RelativeLayout.ABOVE, R.id.main_income);
        if (balanceStripe == null) {
            balanceStripe = new BalanceStripe(getContext(), day, begin, end, preferences, reportManager, dataCache, commonOperations);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            balanceStripe.setLayoutParams(lp);
            PRESSED = false;
            balanceStripe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PRESSED) return;
                    Bundle bundle = new Bundle();
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    bundle.putString(RecordDetailFragment.DATE, format.format(dataCache.getEndDate().getTime()));
                    RecordDetailFragment fragment = new RecordDetailFragment();
                    fragment.setArguments(bundle);
                    paFragmentManager.displayFragment(fragment);
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
        infosVisibility = preferences.getBoolean(PocketAccounterGeneral.INFO_VISIBILITY, false);
        if (infosVisibility) {
            expenseView.showText();
            incomeView.showText();
        }
        else {
            expenseView.hideText();
            incomeView.hideText();
        }
        incomeView.invalidate();
        expenseView.invalidate();
    }

    public void swipingUpdate() {
        if (checkAccessForPage(expenseView.getCurrentPage())) {
            lockView.setVisibility(View.GONE);
            expenseView.unlockPage();
        }
        else {
            lockView.setVisibility(View.VISIBLE);
            expenseView.lockPage();
        }
        lockView.setPage(expenseView.getCurrentPage());
        toolbarManager.setSubtitle(simpleDateFormat.format(day.getTime()));
        if (isCurrentDay) {
            ImageView icon = toolbarManager.getSubtitleIcon();
            if (icon != null)
                icon.setColorFilter(ContextCompat.getColor(getContext(),R.color.red));
        }
        else {
            ImageView icon = toolbarManager.getSubtitleIcon();
            if (icon != null)
                icon.setColorFilter(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
        }
        balanceStripe.calculateBalance();
        incomeView.updateText();
        expenseView.updateText();
        incomeView.invalidate();
        expenseView.invalidate();
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
        lockView.setPage(expenseView.getCurrentPage());
        balanceStripe.calculateBalance();
        expenseView.refreshPagesCount();
        expenseView.init();
        expenseView.invalidate();
        incomeView.refreshPagesCount();
        incomeView.init();
        incomeView.invalidate();
    }

    public void visibilityOfInfos(boolean visibility) {
        boolean isAccess = true;
        switch (expenseView.getCurrentPage()) {
                case 1:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY, false);
                    break;
                case 2:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY, false);
                    break;
                case 3:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.THIRD_PAGE_COUNT_KEY, false);
                    break;
                case 4:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY, false);
                    break;
                case 5:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY, false);
                    break;
                case 6:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY, false);
                    break;
                case 7:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY, false);
                    break;
                case 8:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY, false);
                    break;
                case 9:
                    isAccess = preferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.NINTH_PAGE_COUNT_KEY, false);
                    break;
                }
        if (!isAccess) {
            expenseView.hideText();
            incomeView.hideText();
            infosVisibility = false;
        }
        else {
            infosVisibility = visibility;
            if (infosVisibility){
                expenseView.showText();
                incomeView.showText();
            } else {
                expenseView.hideText();
                incomeView.hideText();
            }
        }
        preferences
                .edit()
                .putBoolean(PocketAccounterGeneral.INFO_VISIBILITY, infosVisibility)
                .commit();

    }
    public int getPosition() {
        return position;
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
