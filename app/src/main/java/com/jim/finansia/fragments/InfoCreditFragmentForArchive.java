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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.AdapterCridetArchive;
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


public class InfoCreditFragmentForArchive extends Fragment {
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

    WarningDialog warningDialog;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    CreditDetialsDao creditDetialsDao;
    ReckingCreditDao reckingCreditDao;
    AccountDao accountDao;
    FinanceRecordDao financeRecordDao;
    DebtBorrowDao debtBorrowDao;
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
    PopupMenu popupMenu;
    //    ImageView myDelete;
    boolean fromMainWindow = false;
    private boolean[] isCheks;
    private int positionOfBourdMain;
    private int modeOfMain;
    boolean fromSearch = false;
    int POSITIOn;
    public InfoCreditFragmentForArchive() {
        // Required empty public constructor
    }
    public void setConteentFragment(CreditDetials temp){
        currentCredit=temp;
        formater=new DecimalFormat("0.##");
        fromSearch=true;
    }
    public void setConteent(CreditDetials temp, int position){
        currentCredit=temp;
        formater=new DecimalFormat("0.##");
        POSITIOn=position;
    }

    public void setContentFromMainWindow(CreditDetials temp, int positionOfBourd, int modeOfMain) {
        fromMainWindow = true;

        currentCredit = temp;
        this.positionOfBourdMain = positionOfBourd;
        this.modeOfMain = modeOfMain;
    }

    public void setDefaultContent(CreditDetials temp) {
        currentCredit = temp;
        fromSearch = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
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
        warningDialog = new WarningDialog(context);
        if (currentCredit != null)
        {
            sPref.edit().putLong("CREDIT_ID", currentCredit.getMyCredit_id()).commit();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_info_credit_archive, container, false);
        Date dateForSimpleDate = (new Date());
        if (fromMainWindow)
            paFragmentManager.setMainReturn(true);
        expandableBut = (ImageView) V.findViewById(R.id.wlyuzik_opener);
        expandablePanel = (RelativeLayout) V.findViewById(R.id.shlyuzik);
        myCreditName = (TextView) V.findViewById(R.id.name_of_credit);
        myLefAmount = (TextView) V.findViewById(R.id.value_credit_all);
        myProcent = (TextView) V.findViewById(R.id.procentCredInfo);
//        myLefDate = (TextView) V.findViewById(R.id.leftDateInfo);
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
        V.findViewById(R.id.moainnnn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        rcList = currentCredit.getReckings();
        currentCredit.resetReckings();
        adapRecyc = new PaysCreditAdapter(rcList);
        myPay = (TextView) V.findViewById(R.id.paybut);
//        myDelete = (ImageView) V.findViewById(R.id.deleterbut);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        tranact_recyc.setLayoutManager(llm);
        V.findViewById(R.id.llDebtBOrrowItemEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putInt("FRAG_ID", 2).commit();
                ScheduleCreditFragment scheduleCreditFragment  = new ScheduleCreditFragment();
                scheduleCreditFragment.setCreditObject(currentCredit);
                paFragmentManager.displayFragment(scheduleCreditFragment);
            }
        });
        isCheks = new boolean[currentCredit.getReckings().size()];
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupMenu = new PopupMenu(getContext(), v);
                popupMenu.inflate(R.menu.toolbar_popup_debt);
                MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
                menuHelper.setForceShowIcon(true);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.delete: {
                                warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!fromMainWindow) {
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

                                            logicManager.deleteCredit(currentCredit);
                                            dataCache.updateAllPercents();
                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                            reportManager.clearCache();
                                        } else if (fromSearch) {
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
                                            reportManager.clearCache();

                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                            logicManager.deleteCredit(currentCredit);

                                        } else {
                                            if (modeOfMain == PocketAccounterGeneral.EXPENSE) {
                                                logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, positionOfBourdMain, null);
                                            } else {
                                                logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, positionOfBourdMain, null);
                                            }
                                            commonOperations.changeIconToNull(positionOfBourdMain, dataCache, modeOfMain);

                                            List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                            for (BoardButton boardButton : boardButtons) {
                                                if (boardButton.getCategoryId() != null) {
                                                    if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {

                                                        if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE) {
                                                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                        } else {
                                                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                                        }

                                                        commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                                    }
                                                }
                                            }
                                            logicManager.deleteCredit(currentCredit);
                                            dataCache.updateAllPercents();
                                            reportManager.clearCache();

                                            paFragmentManager.updateAllFragmentsOnViewPager();

                                        }
                                        if (fromMainWindow)
                                            dataCache.updateOneDay(dataCache.getEndDate());
                                        if (fromMainWindow) {
                                            getActivity().getSupportFragmentManager().popBackStack();
                                            paFragmentManager.displayMainWindow();
                                        } else {

                                            getActivity().getSupportFragmentManager().popBackStack();

                                            paFragmentManager.displayFragment(new CreditTabLay());
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
        tvBalancePer.setText(decimalFormat.format(headerData.getTotalPayedAmount())+currentCredit.getValyute_currency().getAbbr());
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

    public String parseToWithoutNull(double A) {
        if (A == (int) A)
            return Integer.toString((int) A);
        else
            return formater.format(A);
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

    public interface ConWithFragments {
        void change_item(CreditDetials creditDetials, int position);

        void to_Archive(int position);

        void delete_item(int position);
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
            if((int)((lastUnPaidPeriod.getPaymentSum() - lastUnPaidPeriod.getPayed() )*100)==0||lastUnPaidPeriod.getPaymentSum() - lastUnPaidPeriod.getPayed()<=0){
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
        if (currentCredit.getKey_for_include()) {
            accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
            String[] accaounts = new String[accaunt_AC.size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = accaunt_AC.get(i).getName();
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    context, R.layout.spiner_gravity_left, accaounts);
            accountSp.setAdapter(arrayAdapter);
        } else {
            dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
        }

        date = Calendar.getInstance();
        if(unPaidPeriod.getDate().getTimeInMillis()<date.getTimeInMillis()){
            date= (Calendar) unPaidPeriod.getDate().clone();
            date.set(Calendar.DAY_OF_MONTH,-1);
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
                if (currentCredit.getTake_time().getTimeInMillis() >= (new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()) {
                    enterDate.setError(context.getString(R.string.incorrect_date));
                    enterDate.setText(dateFormat.format(currentCredit.getTake_time().getTime()));
                } else if( unPaidPeriod.getDate().getTimeInMillis()<(new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()){
                    Toast.makeText(context, "You can not jump from periods!", Toast.LENGTH_SHORT).show();
                    Calendar calendar = (Calendar) unPaidPeriod.getDate().clone();
                    calendar.set(Calendar.DAY_OF_MONTH,-1);
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
                    calendar.set(Calendar.DAY_OF_MONTH,-1);
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
                    if (Double.parseDouble(amount) > currentCredit.getValue_of_credit_with_procent() - total_paid) {
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String amount = enterPay.getText().toString();
                                ReckingCredit rec = null;
                                if (!amount.matches("") && currentCredit.getKey_for_include())
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
                                else
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
                                logicManager.insertReckingCredit(rec);
                                currentCredit.resetReckings();
                                rcList = currentCredit.getReckings();
                                adapRecyc.setMyList(rcList);
                                dataCache.updateAllPercents();
                                paFragmentManager.updateAllFragmentsOnViewPager();
                                adapRecyc.notifyDataSetChanged();
                                updateDate();
                                isCheks = new boolean[rcList.size()];
                                for (int i = 0; i < isCheks.length; i++) {
                                    isCheks[i] = false;
                                }
//                                if (!fromMainWindow)
//                                    A1.change_item(currentCredit, currentPOS);
//                                else if (fromSearch) {
//
//                                }
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
                        if (!amount.matches("") && currentCredit.getKey_for_include())
                            rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
                        else
                            rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
                        logicManager.insertReckingCredit(rec);
                        currentCredit.resetReckings();
                        rcList = currentCredit.getReckings();
                        adapRecyc.setMyList(rcList);
                        dataCache.updateAllPercents();
                        adapRecyc.notifyDataSetChanged();
                        updateDate();
                        isCheks = new boolean[rcList.size()];
                        for (int i = 0; i < isCheks.length; i++) {
                            isCheks[i] = false;
                        }
//                        if (!fromMainWindow)
//                            A1.change_item(currentCredit, currentPOS);
//                        else if (fromSearch) {
//
//                        }
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

//
//        final Dialog dialog = new Dialog(context);
//        View dialogView = ((PocketAccounter) context).getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(dialogView);
//        View v = dialog.getWindow().getDecorView();
//        v.setBackgroundResource(android.R.color.transparent);
//        final TextView enterDate = (TextView) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
//        final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
//        final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
//        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
//        accaunt_AC = null;
//        if (currentCredit.getKey_for_include()) {
//            accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
//            String[] accaounts = new String[accaunt_AC.size()];
//            for (int i = 0; i < accaounts.length; i++) {
//                accaounts[i] = accaunt_AC.get(i).getName();
//            }
//            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                    context, R.layout.spiner_gravity_left, accaounts);
//
//            accountSp.setAdapter(arrayAdapter);
//
//        } else {
//            dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
//        }
//        final Calendar date;
//        if (fromMainWindow)
//            date = dataCache.getEndDate();
//        else date = Calendar.getInstance();
//
//        enterDate.setText(dateFormat.format(date.getTime()));
//        ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
//        TextView save = (TextView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
//            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
//                enterDate.setText(dateFormat.format((new GregorianCalendar(arg1, arg2, arg3)).getTime()));
//                date.set(arg1, arg2, arg3);
//            }
//        };
//        dialogView.findViewById(R.id.dateMainLay).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Calendar calendar = Calendar.getInstance();
//                Dialog mDialog = new DatePickerDialog(context,
//                        getDatesetListener, calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH), calendar
//                        .get(Calendar.DAY_OF_MONTH));
//                mDialog.show();
//            }
//        });
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String amount = enterPay.getText().toString();
//                double total_paid = 0;
//                for (ReckingCredit item : rcList)
//                    total_paid += item.getAmount();
//
//                if (!amount.matches("")) {
//                    if (currentCredit.getKey_for_include()) {
//                        Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
//                        if (account.getIsLimited()) {
//                            //TODO editda tekwir ozini hisoblamaslini
//                            double limit = account.getLimite();
//                            double accounted = logicManager.isLimitAccess(account, date);
//
//                            accounted = accounted - commonOperations.getCost(date, currentCredit.getValyute_currency(), account.getCurrency(), Double.parseDouble(amount));
//                            if (-limit > accounted) {
//                                Toast.makeText(context, R.string.limit_exceed, Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                    }
//
//
//                    if (Double.parseDouble(amount) > currentCredit.getValue_of_credit_with_procent() - total_paid) {
//                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                warningDialog.dismiss();
//                            }
//                        });
//                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ReckingCredit rec = null;
//                                if (!amount.matches("") && currentCredit.getKey_for_include())
//                                    rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(),
//                                            currentCredit.getMyCredit_id(), comment.getText().toString());
//                                else
//                                    rec = new ReckingCredit(date, Double.parseDouble(amount), "",
//                                            currentCredit.getMyCredit_id(), comment.getText().toString());
////                                        rcList.add(rec);
////                                        currentCredit.getReckings().addAll(rcList);
//                                logicManager.insertReckingCredit(rec);
//                                dataCache.updateAllPercents();
//                                paFragmentManager.updateAllFragmentsOnViewPager();
////                                        paFragmentManager.getCurrentFragment().swipingUpdate();
//                                currentCredit.resetReckings();
//                                rcList = currentCredit.getReckings();
//                                updateDate();
//                                adapRecyc.setMyList(rcList);
//                                if (!fromMainWindow && A1 != null)
//                                    A1.change_item(currentCredit, currentPOS);
//                                isCheks = new boolean[rcList.size()];
//                                for (int i = 0; i < isCheks.length; i++) {
//                                    isCheks[i] = false;
//                                }
//                                dialog.dismiss();
//                                adapRecyc.notifyDataSetChanged();
//                                warningDialog.dismiss();
//                            }
//                        });
//                        warningDialog.setText(context.getString(R.string.payment_balans) + parseToWithoutNull(currentCredit.getValue_of_credit_with_procent() - total_paid) +
//                                currentCredit.getValyute_currency().getAbbr() + "." + context.getString(R.string.payment_balance2) +
//                                parseToWithoutNull(Double.parseDouble(amount) - (currentCredit.getValue_of_credit_with_procent() - total_paid)) +
//                                currentCredit.getValyute_currency().getAbbr());
//                        warningDialog.show();
//                    } else {
//                        ReckingCredit rec = null;
//                        if (!amount.matches("") && currentCredit.getKey_for_include())
//                            rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
//                        else
//                            rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
////                        currentCredit.getReckings().add(rec);
////                        rcList.add(rec);
//                        logicManager.insertReckingCredit(rec);
//                        currentCredit.resetReckings();
//                        rcList = currentCredit.getReckings();
//                        adapRecyc.setMyList(rcList);
//                        updateDate();
//                        dataCache.updateAllPercents();
//                        paFragmentManager.updateAllFragmentsOnViewPager();
//                        isCheks = new boolean[rcList.size()];
//                        for (int i = 0; i < isCheks.length; i++) {
//                            isCheks[i] = false;
//                        }
//                        if (!fromMainWindow)
//                            A1.change_item(currentCredit, currentPOS);
//                        else if (fromSearch) {
//
//                        }
//                        adapRecyc.notifyDataSetChanged();
//                        dialog.dismiss();
//                    }
//                }
//
//            }
//        });
//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//        int width = displayMetrics.widthPixels;
//        dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        dialog.show();
    }

    public void openFragment(Fragment fragment) {
        if (fragment != null) {
            final android.support.v4.app.FragmentTransaction ft = ((PocketAccounter) context)
                    .getSupportFragmentManager().beginTransaction()
                    .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            ft.add(R.id.flMain);
            ft.commit();
        }
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
                    tvPeriodPayment.setText(formater.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed())+currentCredit.getValyute_currency().getAbbr());
                    tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
                }
            else {
                tvPeriodPaymentTitle.setText(R.string.credit_stat);
                tvPeriodPayment.setText(R.string.complete);
                tvPeriodPayment.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
        }
        //exep
        myTakedValue.setText(formater.format(currentCredit.getValue_of_credit()) + currentCredit.getValyute_currency().getAbbr());
        myReturnValue.setText(formater.format(headerData.getTotalLoanWithInterest()) + currentCredit.getValyute_currency().getAbbr());
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
//                            if (!fromMainWindow)
//                                A1.change_item(currentCredit, currentPOS);
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
//        myPay.setText(getString(R.string.pay));
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
            if (currentCredit.getKey_for_include()) {
                ArrayList<Account> accounts = (ArrayList<Account>) accountDao.queryBuilder().list();
                String accs = accounts.get(0).getName();
                for (int i = 0; i < accounts.size(); i++) {
                    if (item.getAccountId().equals(accounts.get(i).getId())) {
                        accs = accounts.get(i).getName();
                    }
                }
                view.infoAccount.setText(getString(R.string.via) + ": " + accs);
            } else {
                view.infoAccount.setVisibility(View.GONE);
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
    public void onResume() {
        super.onResume();
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
