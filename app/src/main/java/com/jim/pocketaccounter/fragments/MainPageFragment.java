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
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.record.BalanceStripe;
import com.jim.pocketaccounter.utils.record.BoardView;
import com.jim.pocketaccounter.utils.record.DecorationBoardView;
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
    private Map<String, Double> balance;
    private BalanceStripe balanceStripe;
    private BoardView expenseView, incomeView;
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
    public void initialize() {
        DisplayMetrics dm = pocketAccounter.getResources().getDisplayMetrics();
        double width = (double) dm.widthPixels;
        double height = (double) dm.heightPixels;
        double ratio = height/width;
        rlMainPageContainer.removeAllViews();
        expenseView = new BoardView(getContext(), PocketAccounterGeneral.EXPENSE, day);
        RelativeLayout.LayoutParams lpExpenses = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels);
        lpExpenses.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        expenseView.setLayoutParams(lpExpenses);
        expenseView.setId(R.id.main_expense);
        rlMainPageContainer.addView(expenseView);

        incomeView = new BoardView(getContext(), PocketAccounterGeneral.INCOME, day);
        RelativeLayout.LayoutParams lpIncomes = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels / 4);
        lpIncomes.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        incomeView.setLayoutParams(lpIncomes);
        incomeView.setId(R.id.main_income);
        rlMainPageContainer.addView(incomeView);

        RelativeLayout.LayoutParams lpBalance = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpBalance.addRule(RelativeLayout.BELOW, R.id.main_expense);
        lpBalance.addRule(RelativeLayout.ABOVE, R.id.main_income);
        balanceStripe.setLayoutParams(lpBalance);
        rlMainPageContainer.addView(balanceStripe);
        if (ratio < 1.7d)
            balanceStripe.hideNotImportantPart();
        else
            balanceStripe.showNotImportantPart();
        balanceStripe.calculateBalance();

    }
    public void refreshCurrencyChanges() {
        commonOperations.refreshCurrency();
    }
    public void update() {
        toolbarManager.setSubtitle(simpleDateFormat.format(day.getTime()));
        balanceStripe.calculateBalance();
        expenseView.invalidate();
        incomeView.invalidate();
    }
    public void updatePageChanges() {
        expenseView.refreshPagesCount();
        expenseView.invalidate();
        incomeView.refreshPagesCount();
        incomeView.invalidate();
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
