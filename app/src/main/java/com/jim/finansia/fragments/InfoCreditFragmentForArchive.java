package com.jim.finansia.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    private WarningDialog warningDialog;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private AccountDao accountDao;
    private ArrayList<CreditsSchedule> creditsSchedules;
    private ImageView expandableBut;
    private ImageView cancel_button;
    private RelativeLayout expandablePanel;
    private RelativeLayout rlBottom;
    private RecyclerView tranact_recyc;
    private CreditDetials currentCredit;
    private TextView myCreditName;
    private TextView myLefAmount;
    private TextView myProcent;
    private TextView myTakedCredTime;
    private TextView myTakedValue;
    private TextView myReturnValue;
    private TextView myTotalPaid;
    private TextView intervalCreditInfo;
    private TextView calculeted;
    private TextView tvEndPeriodDay;
    private TextView tvPeriodPayment;
    private TextView tvBalancePer;
    private TextView tvPeriodPaymentTitle;
    private TextView tvEndPeriodDayTitle;
    private ImageView icon_credit;
    private PaysCreditAdapter adapRecyc;
    private List<ReckingCredit> rcList;
    private boolean delete_flag = false;
    final static long forMoth = 1000L * 60L * 60L * 24L * 30L;
    private boolean isExpandOpen = false;
    private Context context;
    private DecimalFormat formater =new DecimalFormat("0.##");
    private DecimalFormat decimalFormat;
    private NumberFormat numberFormat;
    private TextView myPay;
    private PopupMenu popupMenu;
    private boolean[] isCheks;
    private int positionOfBourdMain;
    private int modeOfMain;



     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        accountDao = daoSession.getAccountDao();
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setDecimalFormatSymbols(symbols);
        context = getActivity();
        warningDialog = new WarningDialog(context);
     }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_info_credit_archive, container, false);
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
                editor.putInt("FRAG_ID", 2).apply();
                ScheduleCreditFragment scheduleCreditFragment  = new ScheduleCreditFragment();
                Bundle bundle= new Bundle();
                bundle.putLong(CreditTabLay.CREDIT_ID,currentCredit.getMyCredit_id());
                scheduleCreditFragment.setArguments(bundle);
                paFragmentManager.displayFragment(scheduleCreditFragment);
            }
        });
        isCheks = new boolean[currentCredit.getReckings().size()];
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupMenu = new PopupMenu(getContext(), v);
                popupMenu.inflate(R.menu.toolbar_popup_without_delete);
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

                                            logicManager.deleteCredit(currentCredit);
                                            dataCache.updateAllPercents();
                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                            reportManager.clearCache();
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
                                            reportManager.clearCache();

                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                            logicManager.deleteCredit(currentCredit);

                                        } else {
                                            if (modeOfMain == PocketAccounterGeneral.EXPANSE_MODE) {
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
                                        if (modeOfMain != PocketAccounterGeneral.NO_MODE && modeOfMain !=PocketAccounterGeneral.DETAIL && modeOfMain!= PocketAccounterGeneral.SEARCH_MODE){
                                            dataCache.updateOneDay(dataCache.getEndDate());
                                            getActivity().getSupportFragmentManager().popBackStack();
                                            paFragmentManager.displayMainWindow();
                                        } else {
                                            getActivity().getSupportFragmentManager().popBackStack();
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
    Calendar date;




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
