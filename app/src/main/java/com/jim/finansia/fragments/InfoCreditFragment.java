package com.jim.finansia.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.CreditsSchedule;
import com.jim.finansia.credit.HeaderData;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.ReckingCreditDao;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;


public class InfoCreditFragment extends Fragment {
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    DataCache dataCache;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    LogicManager logicManager;
    @Inject
    LogicManager financeManager;
    @Inject
    SharedPreferences sPref;
    @Inject
    ReportManager reportManager;
    @Inject
    FinansiaFirebaseAnalytics analytics;

    WarningDialog warningDialog;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    CreditDetialsDao creditDetialsDao;
    ReckingCreditDao reckingCreditDao;
    AccountDao accountDao;
    ArrayList<CreditsSchedule> creditsSchedules;
    ImageView expandableBut;
    ImageView cancel_button;
    RelativeLayout expandablePanel;
    RelativeLayout rlBottom;
    RecyclerView tranact_recyc;
    CreditDetials currentCredit;
    boolean toArcive = false;
    TextView myCreditName;
    TextView myLefAmount;
    TextView myProcent;
    TextView myPeriodOfCredit;
    TextView myTakedCredTime;
    TextView myTakedValue;
    TextView myReturnValue;
    TextView myTotalPaid;
    TextView intervalCreditInfo;
    TextView calculeted;
    TextView tvEndPeriodDay;
    TextView tvPeriodPayment;
    TextView tvBalancePer;
    TextView tvPeriodPaymentTitle;
    TextView tvEndPeriodDayTitle;
    ImageView icon_credit;
    PaysCreditAdapter adapRecyc;
    List<ReckingCredit> rcList;
    boolean delete_flag = false;
    int currentPOS = 0;
    final static long forDay = 1000L * 60L * 60L * 24L;
    final static long forMoth = 1000L * 60L * 60L * 24L * 30L;
    final static long forWeek = 1000L * 60L * 60L * 24L * 7L;
    final static long forYear = 1000L * 60L * 60L * 24L * 365L;
    boolean isExpandOpen = false;
    private Context context;
    DecimalFormat formater;
    DecimalFormat decimalFormat;
    NumberFormat numberFormat;
    TextView myPay;
    private boolean[] isCheks;
    private int positionOfBourdMain;
    private int modeOfMain = PocketAccounterGeneral.NO_MODE;
    PopupMenu popupMenu;





    public int getMode() {
        return modeOfMain;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_info_credit_modern, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        creditDetialsDao = daoSession.getCreditDetialsDao();
        reckingCreditDao = daoSession.getReckingCreditDao();
        accountDao = daoSession.getAccountDao();
        formater = new DecimalFormat("0.00");
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setDecimalFormatSymbols(symbols);
        context = getActivity();
        if (toolbarManager != null)
        {
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
        if(getArguments()!=null){
            currentCredit = daoSession.load(CreditDetials.class,getArguments().getLong(CreditTabLay.CREDIT_ID));
            modeOfMain = getArguments().getInt(CreditTabLay.MODE);
            positionOfBourdMain = getArguments().getInt(CreditTabLay.POSITION);
            if (currentCredit != null){
                sPref.edit().putLong("CREDIT_ID", currentCredit.getMyCredit_id()).apply();
            }
        }
        Date dateForSimpleDate = (new Date());
        expandableBut = (ImageView) V.findViewById(R.id.wlyuzik_opener);
        expandablePanel = (RelativeLayout) V.findViewById(R.id.shlyuzik);
        myCreditName = (TextView) V.findViewById(R.id.name_of_credit);
        myLefAmount = (TextView) V.findViewById(R.id.value_credit_all);
        myProcent = (TextView) V.findViewById(R.id.procentCredInfo);
        myPeriodOfCredit = (TextView) V.findViewById(R.id.intervalCreditInfo);
        myTakedCredTime = (TextView) V.findViewById(R.id.takedtimeInfo);
        myTakedValue = (TextView) V.findViewById(R.id.takedValueInfo);
        myReturnValue = (TextView) V.findViewById(R.id.totalReturnValueInfo);
        myTotalPaid = (TextView) V.findViewById(R.id.total_transaction);
        tvEndPeriodDay = (TextView) V.findViewById(R.id.tvEndPeriodDay);
        calculeted = (TextView) V.findViewById(R.id.it_is_include_balance);
        tvEndPeriodDayTitle = (TextView) V.findViewById(R.id.tvEndPeriodDayTitle);
        intervalCreditInfo = (TextView) V.findViewById(R.id.intervalCreditInfo);
        tvPeriodPayment = (TextView) V.findViewById(R.id.tvPeriodPayment);
        tvBalancePer = (TextView) V.findViewById(R.id.tvBalancePer);
        tvPeriodPaymentTitle = (TextView) V.findViewById(R.id.tvPeriodPaymentTitle);
        tranact_recyc = (RecyclerView) V.findViewById(R.id.recycler_for_transactions);
        icon_credit = (ImageView) V.findViewById(R.id.icon_creditt);
        cancel_button = (ImageView) V.findViewById(R.id.cancel_button);
        rlBottom = (RelativeLayout) V.findViewById(R.id.rlBottom);

        rcList = currentCredit.getReckings();
        currentCredit.resetReckings();
        adapRecyc = new PaysCreditAdapter(rcList);
        myPay = (TextView) V.findViewById(R.id.paybut);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        tranact_recyc.setLayoutManager(llm);
        V.findViewById(R.id.llDebtBOrrowItemEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putInt("FRAG_ID", 1).commit();
                ScheduleCreditFragment scheduleCreditFragment  = new ScheduleCreditFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(CreditTabLay.CREDIT_ID,currentCredit.getMyCredit_id());
                bundle.putInt(CreditTabLay.LOCAL_APPEREANCE, CreditTabLay.LOCAL_INFO);
                scheduleCreditFragment.setArguments(bundle);
                paFragmentManager.displayFragment(scheduleCreditFragment);
            }
        });
        isCheks = new boolean[currentCredit.getReckings().size()];
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add remove and edit
                popupMenu = new PopupMenu(getContext(), v);
                popupMenu.inflate(R.menu.toolbar_popup);
                MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
                menuHelper.setForceShowIcon(true);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                paFragmentManager.getFragmentManager().popBackStack();
                                AddCreditFragment forEdit = new AddCreditFragment();
                                Bundle bundle = new Bundle();
                                if (modeOfMain != PocketAccounterGeneral.NO_MODE){
                                    bundle.putInt(CreditTabLay.MODE, modeOfMain);
                                    bundle.putInt(CreditTabLay.POSITION, positionOfBourdMain);
                                }
                                bundle.putLong(CreditTabLay.CREDIT_ID,currentCredit.getMyCredit_id());
                                forEdit.setArguments(bundle);
                                paFragmentManager.displayFragment(forEdit);
                                break;
                            case R.id.delete: {
                                warningDialog = new WarningDialog(context);
                                warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                        for (BoardButton boardButton : boardButtons) {
                                            if (boardButton.getCategoryId() != null)
                                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {

                                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                    else
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);

                                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                                }
                                        }
                                        reportManager.clearCache();
                                        dataCache.updateAllPercents();
                                        logicManager.deleteCredit(currentCredit);
                                        paFragmentManager.updateAllFragmentsPageChanges();

                                        logicManager.deleteCredit(currentCredit);
                                        if (modeOfMain == PocketAccounterGeneral.NO_MODE) {
                                            boolean found = false;
                                            for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                                                if(fragment==null) continue;
                                                if (fragment instanceof CreditFragment) {
                                                    ((CreditFragment)fragment).updateList();
                                                    found = true;
                                                }
                                                if (fragment instanceof CreditArchiveFragment) {
                                                    ((CreditArchiveFragment)fragment).updateList();
                                                    found = true;
                                                }
                                            }
                                            paFragmentManager.getFragmentManager().popBackStack();
                                            if (!found) {
                                                paFragmentManager.getFragmentManager().popBackStack();
                                                paFragmentManager.displayFragment(new CreditTabLay());
                                            }
                                        } else if (modeOfMain == PocketAccounterGeneral.SEARCH_MODE) {
                                            paFragmentManager.getFragmentManager().popBackStack();
                                            paFragmentManager.displayFragment(new SearchFragment());
                                        }
                                        else if (modeOfMain == PocketAccounterGeneral.MAIN) {
                                            paFragmentManager.displayMainWindow();
                                        }
                                        else if (modeOfMain == PocketAccounterGeneral.DETAIL) {
                                            for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                                                if (fragment.getClass().getName().equals(RecordDetailFragment.class.getName())) {
                                                    ((RecordDetailFragment)fragment).updateFragments();
                                                    break;
                                                }
                                            }
                                            paFragmentManager.getFragmentManager().popBackStack();
                                        }
                                        warningDialog.dismiss();
                                    }
                                });
                                warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        warningDialog.dismiss();
                                    }
                                });
                                warningDialog.setText(getString(R.string.delete_credit));
                                warningDialog.show();
                                break;
                            }

                        }
                        return false;
                    }
                });
                popupMenu.show();



            }
        });

        tranact_recyc.setAdapter(adapRecyc);
        double total_paid = 0;
        for (ReckingCredit item : rcList) {
            total_paid += item.getAmount();
        }
        creditsSchedules = new ArrayList<>();
        adapRecyc.notifyDataSetChanged();
        V.findViewById(R.id.frameLayout2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toArcive && !delete_flag) {
                    currentCredit.setKey_for_archive(true);
                    logicManager.insertCredit(currentCredit);
                    if (modeOfMain == PocketAccounterGeneral.NO_MODE) {
                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()) {
                            if (fragment.getClass().getName().equals(CreditFragment.class.getName())) {
                                ((CreditFragment)fragment).updateList();
                                break;
                            }
                            if (fragment.getClass().getName().equals(CreditArchiveFragment.class.getName())) {
                                ((CreditArchiveFragment)fragment).updateList();
                                break;
                            }
                        }
                        paFragmentManager.getFragmentManager().popBackStack();

                    } else if (modeOfMain == PocketAccounterGeneral.SEARCH_MODE) {

                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();

                        paFragmentManager.getFragmentManager().popBackStack();
                    } else {
                        if (modeOfMain == PocketAccounterGeneral.EXPANSE_MODE)
                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, positionOfBourdMain, null);
                        else
                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, positionOfBourdMain, null);

                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        commonOperations.changeIconToNull(positionOfBourdMain, dataCache, modeOfMain);
                        paFragmentManager.displayMainWindow();
                    }
                } else if (!delete_flag) {
                    openDialog(creditsSchedules);
                } else if (toArcive && delete_flag) {
                    delete_flag = false;
                    myPay.setText(R.string.to_archive);
                    adapRecyc.notifyDataSetChanged();
                }


            }
        });
        boolean prosrecenniy=false;
        HeaderData headerData;
        if(currentCredit.getType_loan()==CommonOperations.ANUTETNIY){
            headerData = ScheduleCreditFragment.calculetAnutetniy(currentCredit,creditsSchedules);
        }
        else {
            headerData = ScheduleCreditFragment.calculetDeferinsial(currentCredit,creditsSchedules);
        }
        Date from = new Date();
        CreditsSchedule currentPeriod = null;

        boolean yestDolg=false;
        CreditsSchedule unPaidPeriod = null;
        for (CreditsSchedule creditsSchedule:creditsSchedules){
            if(creditsSchedule.getDate().getTimeInMillis()>from.getTime()){
                currentPeriod = creditsSchedule;
                break;
            }
            else if(!((int)((creditsSchedule.getPaymentSum() - creditsSchedule.getPayed() )*100)==0||creditsSchedule.getPaymentSum() - creditsSchedule.getPayed()<=0)&&creditsSchedule.getDate().getTimeInMillis()<from.getTime()&&!yestDolg){
                yestDolg = true;
                unPaidPeriod = creditsSchedule;
            }
        }
        if(currentPeriod == null)
            prosrecenniy = true;
        //exep
        if(yestDolg){
            tvPeriodPaymentTitle.setText(R.string.you_have_debt);
            tvPeriodPayment.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));

        } else {
            if(!prosrecenniy)
            if(formater.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed()).equals("0")|| currentPeriod.getPaymentSum() - currentPeriod.getPayed()<= 0){
                tvPeriodPaymentTitle.setText(R.string.credit_stat);
                tvPeriodPayment.setText(R.string.complete);
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
                tvPeriodPaymentTitle.setText(R.string.period_payment);
                tvPeriodPayment.setText(decimalFormat.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed())+currentCredit.getValyute_currency().getAbbr());
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }
            else {
                tvPeriodPaymentTitle.setText(R.string.credit_stat);
                tvPeriodPayment.setText(R.string.complete);
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
        }
        myTakedValue.setText(decimalFormat.format(currentCredit.getValue_of_credit()) + currentCredit.getValyute_currency().getAbbr());
        myReturnValue.setText(decimalFormat.format(headerData.getTotalLoanWithInterest()) + currentCredit.getValyute_currency().getAbbr());
        tvBalancePer.setText(formater.format(headerData.getTotalPayedAmount())+currentCredit.getValyute_currency().getAbbr());
        Calendar to;
        if(!prosrecenniy)
         to = (Calendar) currentPeriod.getDate();
        else to = (Calendar) Calendar.getInstance();
        long period_tip = currentCredit.getPeriod_time_tip();
        long period_voqt = currentCredit.getPeriod_time();
        int voqt_soni = (int) (period_voqt / period_tip);
        String interval ;
        interval = voqt_soni + " ";
        if (period_tip == forMoth) {
            interval = interval + context.getString(R.string.mont);
        } else {
            interval = interval + context.getString(R.string.yearr);
        }

        intervalCreditInfo.setText(interval);


        int t[] = getDateDifferenceInDDMMYYYY(from, to.getTime());
        if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
            tvEndPeriodDay.setText(R.string.ends);
            tvEndPeriodDay.setTextColor(Color.parseColor("#832e1c"));
        } else {
            String left_date_string = "";
            if (t[0] != 0) {
                if (t[0] > 1) {
                    left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.years);
                } else {
                    left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.year);
                }
            }
            if (t[1] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[1] > 1) {
                    left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moths);
                } else {
                    left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moth);
                }
            }
            if (t[2] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[2] > 1) {
                    left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.days);
                } else {
                    left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.day);
                }
            }
            if(!prosrecenniy){
                tvEndPeriodDayTitle.setText(R.string.period_ends);
                tvEndPeriodDay.setText(left_date_string);

            }
            else  {
                tvEndPeriodDayTitle.setText(R.string.credit_end_date);
                tvEndPeriodDay.setText(sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
            }
        }


        int resId = context.getResources().getIdentifier(currentCredit.getIcon_ID(), "drawable", context.getPackageName());
        icon_credit.setImageResource(resId);
        dateForSimpleDate.setTime(currentCredit.getTake_time().getTimeInMillis());
        myTakedCredTime.setText(sDateFormat.format(dateForSimpleDate));
        myCreditName.setText(currentCredit.getCredit_name());
        calculeted.setText((currentCredit.getKey_for_include()) ? getString(R.string.calculaed) : getString(R.string.not_calc));
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_flag = false;
                cancel_button.setVisibility(View.GONE);
//                    myPay.setText(R.string.pay);
                for (int i = 0; i < isCheks.length; i++) {
                    isCheks[i] = false;
                }
                adapRecyc.notifyDataSetChanged();
            }
        });
        myTotalPaid.setText(decimalFormat.format(total_paid) + currentCredit.getValyute_currency().getAbbr());
        if (headerData.getTotalLoanWithInterest() - total_paid <= 0) {
            myLefAmount.setText(getString(R.string.repaid));
            toArcive = true;
            myPay.setText(getString(R.string.archive));
        } else
            myLefAmount.setText(decimalFormat.format(currentCredit.getValue_of_credit_with_procent() - total_paid) + currentCredit.getValyute_currency().getAbbr());

        String suffix = "";
         if (currentCredit.getProcent_interval() == forMoth) {
            suffix = getString(R.string.per_month);
        } else {
            suffix = getString(R.string.per_year);
        }

        myProcent.setText(formater.format(currentCredit.getProcent()) + "%" + " " + suffix);
        V.findViewById(R.id.frameLayout3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delete_flag) {
                    delete_checked_items();
                } else {
                    delete_flag = true;
                    adapRecyc.notifyDataSetChanged();
                    cancel_button.setVisibility(View.VISIBLE);
//                    myPay.setText(getString(R.string.cancel));
                }
            }
        });


        rlBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandablePanel.setVisibility(View.GONE);
                expandableBut.setImageResource(R.drawable.info_open);
                isExpandOpen = false;
            }
        });
        V.findViewById(R.id.infoooc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpandOpen) {
                    expandablePanel.setVisibility(View.GONE);
                    expandableBut.setImageResource(R.drawable.info_open);
                    isExpandOpen = false;
                } else {
                    expandablePanel.setVisibility(View.VISIBLE);
                    expandableBut.setImageResource(R.drawable.info_pastga);
                    isExpandOpen = true;
                }
            }
        });





        return V;
    }

    public static int[] getDateDifferenceInDDMMYYYY(Date from, Date to) {
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();
        fromDate.setTime(from);
        toDate.setTime(to);
        int increment = 0;
        int year, month, day;
        if (fromDate.get(Calendar.DAY_OF_MONTH) > toDate.get(Calendar.DAY_OF_MONTH)) {
            increment = fromDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (increment != 0) {
            day = (toDate.get(Calendar.DAY_OF_MONTH) + increment) - fromDate.get(Calendar.DAY_OF_MONTH);
            increment = 1;
        } else {
            day = toDate.get(Calendar.DAY_OF_MONTH) - fromDate.get(Calendar.DAY_OF_MONTH);
        }

        if ((fromDate.get(Calendar.MONTH) + increment) > toDate.get(Calendar.MONTH)) {
            month = (toDate.get(Calendar.MONTH) + 12) - (fromDate.get(Calendar.MONTH) + increment);
            increment = 1;
        } else {
            month = (toDate.get(Calendar.MONTH)) - (fromDate.get(Calendar.MONTH) + increment);
            increment = 0;
        }

        year = toDate.get(Calendar.YEAR) - (fromDate.get(Calendar.YEAR) + increment);
        return new int[]{year, month, day};
    }

    ArrayList<Account> accaunt_AC;
    CreditsSchedule unPaidPeriod;
    Calendar date;
    private void openDialog(final ArrayList<CreditsSchedule> creditsSchedules) {
        boolean hozirgi  = false;
        unPaidPeriod = null;
        CreditsSchedule currentPeriodi =null;

        for(CreditsSchedule lastUnPaidPeriod:creditsSchedules){
            if (lastUnPaidPeriod.getDate().getTimeInMillis()>Calendar.getInstance().getTimeInMillis()&&!hozirgi){
                currentPeriodi = lastUnPaidPeriod;
                hozirgi = true;
            }
            if(((int)((lastUnPaidPeriod.getPaymentSum() - lastUnPaidPeriod.getPayed() )*100))==0||lastUnPaidPeriod.getPaymentSum() - lastUnPaidPeriod.getPayed()<=0){
                continue;
            }
            else {
                unPaidPeriod = lastUnPaidPeriod;
                break;
            }

        }


        final Dialog dialog = new Dialog(context);
        final View dialogView = ((PocketAccounter) context).getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);

        final TextView enterDate = (TextView) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
        final TextView abbrrAmount = (TextView) dialogView.findViewById(R.id.abbrrAmount);
        final TextView periodDate = (TextView) dialogView.findViewById(R.id.periodDate);
        final TextView shouldPayPeriod = (TextView) dialogView.findViewById(R.id.shouldPayPeriod);
        final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
        final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
        final RelativeLayout checkInclude = (RelativeLayout) dialogView.findViewById(R.id.checkInclude);
        final RelativeLayout is_calc = (RelativeLayout) dialogView.findViewById(R.id.is_calc);
        final SwitchCompat keyForInclude = (SwitchCompat) dialogView.findViewById(R.id.key_for_balance);
        if(currentCredit!=null){
            if(currentCredit.getKey_for_include())
                keyForInclude.toggle();
        }
        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
        if(hozirgi){
            periodDate.setText(sDateFormat.format(currentPeriodi.getDate().getTime()));
            if(formater.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()).equals("0")|| currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()<= 0){
                shouldPayPeriod.setText(R.string.complete);
                shouldPayPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
                shouldPayPeriod.setText(decimalFormat.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed())+currentCredit.getValyute_currency().getAbbr());
                shouldPayPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }
        }
        else {
            periodDate.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));
            shouldPayPeriod.setText(decimalFormat.format(unPaidPeriod.getPaymentSum() - unPaidPeriod.getPayed())+currentCredit.getValyute_currency().getAbbr());

        }
        abbrrAmount.setText(currentCredit.getValyute_currency().getAbbr());
        accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
        String[] accaounts = new String[accaunt_AC.size()];
        for (int i = 0; i < accaounts.length; i++) {
            accaounts[i] = accaunt_AC.get(i).getName();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                context, R.layout.spiner_gravity_left, accaounts);
        accountSp.setAdapter(arrayAdapter);

        keyForInclude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(keyForInclude.isChecked()){

                    is_calc.setVisibility(View.VISIBLE);
                }
                else {
                    is_calc.setVisibility(View.GONE);
                }
            }
        });
        checkInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyForInclude.toggle();
            }
        });
        date = Calendar.getInstance();
        if(unPaidPeriod.getDate().getTimeInMillis()/1000/60/60/24<date.getTimeInMillis()/1000/60/60/24){
            date= (Calendar) unPaidPeriod.getDate().clone();
            date.add(Calendar.DAY_OF_MONTH,-1);
            enterDate.setText(dateFormat.format(date.getTime()));
        }
        else if (currentCredit.getTake_time().getTimeInMillis() > date.getTimeInMillis()) {
            date = (Calendar) currentCredit.getTake_time().clone();
            date.add(Calendar.DAY_OF_MONTH,+1);
            enterDate.setText(dateFormat.format(date.getTime()));
        }
        else
            enterDate.setText(dateFormat.format(date.getTime()));

        ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
        final TextView save = (TextView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (currentCredit.getTake_time().getTimeInMillis() > (new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()) {
                    enterDate.setError(context.getString(R.string.incorrect_date));
                    date = (Calendar) currentCredit.getTake_time().clone();
                    date.add(Calendar.DAY_OF_MONTH,+1);
                    enterDate.setText(dateFormat.format(date.getTime()));
                } else if( unPaidPeriod.getDate().getTimeInMillis()<(new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()){
                    Toast.makeText(context, getString(R.string.you_can_jump), Toast.LENGTH_SHORT).show();
                    Calendar calendar = (Calendar) unPaidPeriod.getDate().clone();
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                    date = calendar;
                    enterDate.setText(dateFormat.format(calendar.getTime()));
                }
                else {
                    enterDate.setText(dateFormat.format((new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTime()));
                    date.set(year, monthOfYear, dayOfMonth);}
            }
        };

        enterDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                if(unPaidPeriod.getDate().getTimeInMillis()<calendar.getTimeInMillis()){
                    calendar = (Calendar) unPaidPeriod.getDate().clone();
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                }
                Dialog mDialog = new DatePickerDialog(context,
                        getDatesetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH));
                mDialog.show();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String amount = enterPay.getText().toString();

                double total_paid = 0;
                for (ReckingCredit item : currentCredit.getReckings())
                    total_paid += item.getAmount();

                if (!amount.matches("")) {
                    if(currentCredit.getKey_for_include()){
                        Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
                        if (account.getIsLimited()) {
                            //TODO editda tekwir ozini hisoblamaslini
                            double limit = account.getLimite();
                            double accounted =  logicManager.isLimitAccess(account, date);

                            accounted = accounted - commonOperations.getCost(date, currentCredit.getValyute_currency(), account.getCurrency(), Double.parseDouble(amount));
                            if (-limit > accounted) {
                                Toast.makeText(context, R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }}
                    if (Double.parseDouble(amount) > currentCredit.getValue_of_credit_with_procent() - total_paid+1) {
                        warningDialog = new WarningDialog(context);
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String amount = enterPay.getText().toString();
                                ReckingCredit rec = null;
                                if (!amount.matches("") && keyForInclude.isChecked())
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
                                else
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
                                logicManager.insertReckingCredit(rec);
                                currentCredit.resetReckings();
                                rcList = currentCredit.getReckings();
                                adapRecyc.setMyList(rcList);
                                dataCache.updateAllPercents();
                                reportManager.clearCache();
                                paFragmentManager.updateAllFragmentsOnViewPager();
                                adapRecyc.notifyDataSetChanged();
                                updateDate();
                                isCheks = new boolean[rcList.size()];
                                for (int i = 0; i < isCheks.length; i++) {
                                    isCheks[i] = false;
                                }

                                dialog.dismiss();
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setText(context.getString(R.string.payment_balans) + formater.format(currentCredit.getValue_of_credit_with_procent() - total_paid) +
                                currentCredit.getValyute_currency().getAbbr() + "." + context.getString(R.string.payment_balance2) +
                                formater.format(Double.parseDouble(amount) - (currentCredit.getValue_of_credit_with_procent() - total_paid)) +
                                currentCredit.getValyute_currency().getAbbr());
                        warningDialog.show();
                    } else {
                        ReckingCredit rec = null;
                        if (!amount.matches("") && keyForInclude.isChecked())
                            rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
                        else
                            rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
                        logicManager.insertReckingCredit(rec);
                        currentCredit.resetReckings();
                        rcList = currentCredit.getReckings();
                        adapRecyc.setMyList(rcList);
                        reportManager.clearCache();
                        dataCache.updateAllPercents();
                        adapRecyc.notifyDataSetChanged();
                        updateDate();
                        isCheks = new boolean[rcList.size()];
                        for (int i = 0; i < isCheks.length; i++) {
                            isCheks[i] = false;
                        }

                        paFragmentManager.updateAllFragmentsOnViewPager();
                        dialog.dismiss();
                    }
                }

            }
        });
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();

    }

    public void updateDate() {
        double total_paid = 0;
        for (ReckingCredit item : rcList)
            total_paid += item.getAmount();
        if (currentCredit.getValue_of_credit_with_procent() - total_paid <= 0) {
            myLefAmount.setText(getString(R.string.repaid));
            myPay.setText(getString(R.string.archive));
            toArcive = true;
        } else {
            toArcive = false;
            myLefAmount.setText(formater.format(currentCredit.getValue_of_credit_with_procent() - total_paid) + currentCredit.getValyute_currency().getAbbr());
        }

        HeaderData headerData;
        creditsSchedules =new ArrayList<>();
        if(currentCredit.getType_loan()==CommonOperations.ANUTETNIY){
            headerData = ScheduleCreditFragment.calculetAnutetniy(currentCredit,creditsSchedules);
        }
        else {
            headerData = ScheduleCreditFragment.calculetDeferinsial(currentCredit,creditsSchedules);
        }
        Date from = new Date();
        CreditsSchedule currentPeriod = null;

        boolean yestDolg=false;
        boolean prosrecenniy=false;
        CreditsSchedule unPaidPeriod = null;
        for (CreditsSchedule creditsSchedule:creditsSchedules){
            if(creditsSchedule.getDate().getTimeInMillis()>from.getTime()){
                currentPeriod = creditsSchedule;
                break;
            }
            else if(!((int)((creditsSchedule.getPaymentSum() - creditsSchedule.getPayed() )*100)==0||creditsSchedule.getPaymentSum() - creditsSchedule.getPayed()<=0)&&creditsSchedule.getDate().getTimeInMillis()<from.getTime()){
                yestDolg = true;
                unPaidPeriod = creditsSchedule;
            }
        }
        if(currentPeriod == null)
            prosrecenniy = true;
        //exep
        if(yestDolg){
            tvPeriodPaymentTitle.setText(R.string.you_have_debt);
            tvPeriodPayment.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));

        } else {
            if(!prosrecenniy)
            if(formater.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed()).equals("0")|| currentPeriod.getPaymentSum() - currentPeriod.getPayed()<= 0){
                tvPeriodPaymentTitle.setText(R.string.credit_stat);
                tvPeriodPayment.setText(R.string.complete);
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
                tvPeriodPaymentTitle.setText(R.string.period_payment);
                tvPeriodPayment.setText(decimalFormat.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed())+currentCredit.getValyute_currency().getAbbr());
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }
            else {
                tvPeriodPaymentTitle.setText(R.string.credit_stat);
                tvPeriodPayment.setText(R.string.complete);
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
        }

        myTakedValue.setText(decimalFormat.format(currentCredit.getValue_of_credit()) + currentCredit.getValyute_currency().getAbbr());
        myReturnValue.setText(decimalFormat.format(headerData.getTotalLoanWithInterest()) + currentCredit.getValyute_currency().getAbbr());
        tvBalancePer.setText(decimalFormat.format(headerData.getTotalPayedAmount())+currentCredit.getValyute_currency().getAbbr());
        Calendar to;
        if(!prosrecenniy)
            to =  currentPeriod.getDate();
        else to =  Calendar.getInstance();
        long period_tip = currentCredit.getPeriod_time_tip();
        long period_voqt = currentCredit.getPeriod_time();
        int voqt_soni = (int) (period_voqt / period_tip);
        String interval ;
        interval = voqt_soni + " ";
        if (period_tip == forMoth) {
            interval = interval + context.getString(R.string.mont);
        } else {
            interval = interval + context.getString(R.string.yearr);
        }

        intervalCreditInfo.setText(interval);


        int t[] = getDateDifferenceInDDMMYYYY(from, to.getTime());
        if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
            tvEndPeriodDay.setText(R.string.ends);
            tvEndPeriodDay.setTextColor(Color.parseColor("#832e1c"));
        } else {
            String left_date_string = "";
            if (t[0] != 0) {
                if (t[0] > 1) {
                    left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.years);
                } else {
                    left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.year);
                }
            }
            if (t[1] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[1] > 1) {
                    left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moths);
                } else {
                    left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moth);
                }
            }
            if (t[2] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[2] > 1) {
                    left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.days);
                } else {
                    left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.day);
                }
            }
            if(!prosrecenniy){
                tvEndPeriodDayTitle.setText(R.string.period_ends);
                tvEndPeriodDay.setText(left_date_string);

            }
            else  {
                tvEndPeriodDayTitle.setText(R.string.credit_end_date);
                tvEndPeriodDay.setText(sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
            }

        }


        myTotalPaid.setText(formater.format(total_paid) + currentCredit.getValyute_currency().getAbbr());
        if (headerData.getTotalLoanWithInterest() - total_paid <= 0) {
            myLefAmount.setText(getString(R.string.repaid));
            toArcive = true;
            myPay.setText(getString(R.string.archive));
        } else
            myLefAmount.setText(formater.format(currentCredit.getValue_of_credit_with_procent() - total_paid) + currentCredit.getValyute_currency().getAbbr());






    }

    public void delete_checked_items() {
        boolean keyfor = false;
        final int lenght = rcList.size() - 1;
        for (boolean isChek : isCheks) {
            if (isChek) {
                keyfor = true;
                break;
            }
        }
        delete_flag = false;
        if (keyfor) {
            warningDialog = new WarningDialog(context);
            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int t = isCheks.length - 1; t >= 0; t--) {
                        if (isCheks[t]) {
                            logicManager.deleteReckingCredit(rcList.get(t));
                            currentCredit.resetReckings();
                            rcList = currentCredit.getReckings();
                            adapRecyc.setMyList(rcList);
                            adapRecyc.notifyItemRemoved(t);

                        } else adapRecyc.notifyItemChanged(t);
                    }
                    isCheks = new boolean[rcList.size()];
                    for (int i = 0; i < isCheks.length; i++) {
                        isCheks[i] = false;
                    }
                    updateDate();
                    dataCache.updateAllPercents();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    warningDialog.dismiss();
                }
            });
            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.dismiss();
                }
            });
            warningDialog.setText(getString(R.string.accept_delete_reck));
            warningDialog.show();
        } else {
            adapRecyc.notifyDataSetChanged();
        }
        cancel_button.setVisibility(View.GONE);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private class PaysCreditAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<ReckingCredit> list;

        public PaysCreditAdapter(List<ReckingCredit> list) {
            this.list = list;
        }

        public void setMyList(List<ReckingCredit> list) {
            this.list = list;
        }

        public int getItemCount() {
            return list.size();
        }

        public void onBindViewHolder(final ViewHolder view, final int position) {
            ReckingCredit item = list.get(position);
            view.infoDate.setText(dateFormat.format(item.getPayDate().getTime()));
            view.infoSumm.setText(decimalFormat.format(item.getAmount()) + currentCredit.getValyute_currency().getAbbr());
            if (!item.getAccountId().equals("")) {
              try {
                  ArrayList<Account> accounts = (ArrayList<Account>) accountDao.queryBuilder().list();
                  String accs = accounts.get(0).getName();
                  for (int i = 0; i < accounts.size(); i++) {
                      if (item.getAccountId().equals(accounts.get(i).getId())) {
                          accs = accounts.get(i).getName();
                      }
                  }
                  view.infoAccount.setText(getString(R.string.via) + ": " + accs);
              }catch (Exception o ){
                  view.infoAccount.setText(R.string.ne_uchitavaetsya);
              }

            } else {
                view.infoAccount.setText(R.string.ne_uchitavaetsya);
            }
            if (!item.getComment().matches(""))
                view.comment.setText(getString(R.string.comment) + ": " + item.getComment());
            else
                view.comment.setVisibility(View.GONE);
            if (delete_flag) {
                view.forDelete.setVisibility(View.VISIBLE);
                view.forDelete.setChecked(isCheks[position]);
                view.glav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (view.forDelete.isChecked())
                            view.forDelete.setChecked(false);
                        else view.forDelete.setChecked(true);
                        isCheks[position] = !isCheks[position];
                    }
                });
            } else {
                view.forDelete.setChecked(false);
                view.forDelete.setVisibility(View.GONE);
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payed_item, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView infoDate;
        public TextView infoSumm;
        public TextView infoAccount;
        public TextView comment;
        public CheckBox forDelete;
        public View glav;

        public ViewHolder(View view) {
            super(view);
            infoDate = (TextView) view.findViewById(R.id.date_of_trans);
            infoAccount = (TextView) view.findViewById(R.id.via_acount);
            comment = (TextView) view.findViewById(R.id.comment_trans);
            infoSumm = (TextView) view.findViewById(R.id.paid_value);
            forDelete = (CheckBox) view.findViewById(R.id.for_delete_check_box);
            glav = view;
        }
    }

    public void updateToolbar() {
        if (toolbarManager != null)
        {
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle("");
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }
}
