package com.jim.finansia.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.CreditsSchedule;
import com.jim.finansia.credit.HeaderData;
import com.jim.finansia.credit.LinearManagerWithOutEx;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SpinnerAdapter;
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

public class CreditFragment extends Fragment {

    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    SharedPreferences sPref;
    @Inject
    CommonOperations commonOperations;

    @Inject
    DaoSession daoSession;
    @Inject
    LogicManager logicManager;
    @Inject
    DataCache dataCache;
    @Inject
    ReportManager reportManager;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    DecimalFormat decimalFormatter;
    @Inject
    SharedPreferences preferences;


    private DecimalFormat formater = new DecimalFormat("0.00");
    private CreditDetialsDao creditDetialsDao;
    private RecyclerView crRV;
    private AdapterCridet crAdap;
    private TextView ifListEmpty;
    private CreditTabLay creditTabLay;




    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View V=inflater.inflate(R.layout.fragment_credit, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        creditDetialsDao = daoSession.getCreditDetialsDao();
        if (toolbarManager != null)
        {
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
            toolbarManager.setSubtitle("");
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
        }
        ifListEmpty=(TextView) V.findViewById(R.id.ifListEmpty);

        if(creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(false)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list().size()==0){
            ifListEmpty.setVisibility(View.VISIBLE);
            ifListEmpty.setText(getResources().getString(R.string.credit_are_empty));
        }
        else ifListEmpty.setVisibility(View.GONE);
        crRV=(RecyclerView) V.findViewById(R.id.my_recycler_view);
        LinearManagerWithOutEx llm = new LinearManagerWithOutEx(getContext());
        crRV.setLayoutManager(llm);
        updateList();

        crRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(creditTabLay==null) {
                    for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()){
                        if (fragment == null) continue;
                        if (fragment.getClass().getName().equals(CreditTabLay.class.getName())){
                            creditTabLay = (CreditTabLay) fragment;
                            break;
                        }
                    }
                }
                creditTabLay.onScrolledList(dy > 0);

            }
        });
        return V;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }



    public void updateList(){
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        toolbarManager.setSubtitle("");
        toolbarManager.setOnTitleClickListener(null);
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        List<CreditDetials> creditDetialses = creditDetialsDao
                .queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(false))
                .orderDesc(CreditDetialsDao.Properties.MyCredit_id)
                .build()
                .list();
        if (creditDetialses.isEmpty())
            ifListEmpty.setVisibility(View.VISIBLE);
        else
            ifListEmpty.setVisibility(View.GONE);
        AdapterCridet adapterCridet = new AdapterCridet(creditDetialses);
        if(crRV!=null) crRV.setAdapter(adapterCridet);
    }



    class AdapterCridet extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private WarningDialog warningDialog;
        private CreditDetialsDao creditDetialsDao;
        private AccountDao accountDao;
        private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");


        private List<CreditDetials> cardDetials;
        private ArrayList<Account> accaunt_AC;
        private DecimalFormat periodFormat;
        private NumberFormat numberFormat;
        private long forMoth = 1000L * 60L * 60L * 24L * 30L;


        public AdapterCridet(List<CreditDetials> creditDetialses) {
            warningDialog = new WarningDialog(getContext());
            creditDetialsDao = daoSession.getCreditDetialsDao();
            accountDao = daoSession.getAccountDao();
            numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            periodFormat = (DecimalFormat) numberFormat;
            periodFormat.setDecimalFormatSymbols(symbols);
            this.cardDetials= creditDetialses;

        }
        boolean prosrecenniy = false ;
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holdeer, final int position) {


            final myViewHolder holder = (myViewHolder) holdeer;
            final CreditDetials itemCr = cardDetials.get(position);
            prosrecenniy = false;
            holder.wlyuzOpenOpener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.wlyuzOpen.getVisibility() == View.GONE){
                        holder.wlyuzOpen.setVisibility(View.VISIBLE);
                        holder.openCloseIcon.setImageResource(R.drawable.info_pastga);

                    }
                    else {
                        holder.wlyuzOpen.setVisibility(View.GONE);
                        holder.openCloseIcon.setImageResource(R.drawable.info_open);
                    }
                }
            });
            DecimalFormat decimalFormatLocal = new DecimalFormat("0.##");
            holder.credit_procent.setText(decimalFormatLocal.format(itemCr.getProcent()) + "%");
            holder.procentCredInfo.setText(decimalFormatLocal.format(itemCr.getProcent()) + "%");
            holder.takedValueInfo.setText(decimalFormatter.format(itemCr.getValue_of_credit()) + itemCr.getValyute_currency().getAbbr());
            //total amount hisob kitob
            double total_paid = 0;
            for (ReckingCredit item : itemCr.getReckings())
                total_paid += item.getAmount();

            holder.nameCredit.setText(itemCr.getCredit_name());
            Date date = (new Date());
            date.setTime(itemCr.getTake_time().getTimeInMillis());
            holder.taken_credit_date.setText(sDateFormat.format(date));
            int resId = getResources().getIdentifier(itemCr.getIcon_ID(), "drawable", getContext().getPackageName());
            holder.iconn.setImageResource(resId);

            final ArrayList<CreditsSchedule> creditsSchedules = new ArrayList<>();
            HeaderData headerData;
            if(itemCr.getType_loan()==CommonOperations.ANUTETNIY){
                headerData = ScheduleCreditFragment.calculetAnutetniy(itemCr,creditsSchedules);
            }
            else {
                headerData = ScheduleCreditFragment.calculetDeferinsial(itemCr,creditsSchedules);
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
                else if(!(((int)((creditsSchedule.getPaymentSum() - creditsSchedule.getPayed() )*100))==0||creditsSchedule.getPaymentSum() - creditsSchedule.getPayed()<=0)&&creditsSchedule.getDate().getTimeInMillis()<from.getTime()&&!yestDolg){
                    yestDolg = true;
                    unPaidPeriod = creditsSchedule;
                }
            }
            if(currentPeriod == null)
                prosrecenniy = true;
            if(yestDolg){
                holder.tvForThisPeriodTitle.setText(getString(R.string.you_have_debt)+":");
                holder.tvForThisPeriod.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));
            }
            else {
                if(!prosrecenniy)
                    if(((int)((currentPeriod.getPaymentSum() - currentPeriod.getPayed() )*100))==0|| currentPeriod.getPaymentSum() - currentPeriod.getPayed()<= 0){
                        if(headerData.getTotalLoanWithInterest()<=headerData.getTotalPayedAmount())
                            holder.tvForThisPeriodTitle.setText(getString(R.string.credit_stat));
                        else
                            holder.tvForThisPeriodTitle.setText(getString(R.string.for_this_period));

                        holder.tvForThisPeriod.setText(R.string.complete);
                        holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(getContext(),R.color.credit_och_yashil));
                    }
                    else{
                        holder.tvForThisPeriodTitle.setText(R.string.for_this_period);
                        holder.tvForThisPeriod.setText(periodFormat.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed())+itemCr.getValyute_currency().getAbbr());
                        holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(getContext(),R.color.credit_yellow));
                    }
                else {
                    holder.tvForThisPeriodTitle.setText(R.string.credit_stat);
                    holder.tvForThisPeriod.setText(R.string.complete);
                    holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(getContext(),R.color.credit_och_yashil));
                }

            }

            holder.totalReturnValueInfo.setText(decimalFormatter.format(headerData.getTotalLoanWithInterest())+itemCr.getValyute_currency().getAbbr());
            holder.tvBalanceCredit.setText(decimalFormatter.format(headerData.getTotalPayedAmount())+itemCr.getValyute_currency().getAbbr());
            Calendar to;
            if(!prosrecenniy)
                to = currentPeriod.getDate();
            else to = Calendar.getInstance();
            long period_tip = itemCr.getPeriod_time_tip();
            long period_voqt = itemCr.getPeriod_time();
            int voqt_soni = (int) (period_voqt / period_tip);
            String interval ;
            interval = voqt_soni + " ";
            if (period_tip == forMoth) {
                interval = interval + getString(R.string.mont);
            } else {
                interval = interval + getString(R.string.yearr);
            }
            holder.intervalCreditInfo.setText(interval);

            int t[] = CommonOperations.getDateDifferenceInDDMMYYYY(from, to.getTime());
            if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
                holder.tvPeriodEndLeft.setText(R.string.ends);
                holder.tvPeriodEndLeft.setTextColor(Color.parseColor("#832e1c"));
            } else {
                String left_date_string = "";
                if (t[0] != 0) {
                    if (t[0] > 1) {
                        left_date_string += Integer.toString(t[0]) + " " + getString(R.string.years);
                    } else {
                        left_date_string += Integer.toString(t[0]) + " " + getString(R.string.year);
                    }
                }
                if (t[1] != 0) {
                    if (!left_date_string.matches("")) {
                        left_date_string += " ";
                    }
                    if (t[1] > 1) {
                        left_date_string += Integer.toString(t[1]) + " " + getString(R.string.moths);
                    } else {
                        left_date_string += Integer.toString(t[1]) + " " + getString(R.string.moth);
                    }
                }
                if (t[2] != 0) {
                    if (!left_date_string.matches("")) {
                        left_date_string += " ";
                    }
                    if (t[2] > 1) {
                        left_date_string += Integer.toString(t[2]) + " " + getString(R.string.days);
                    } else {
                        left_date_string += Integer.toString(t[2]) + " " + getString(R.string.day);
                    }
                }
                if(!prosrecenniy) {
                    holder.tvPeriodEndLeftTitle.setText(R.string.period_ends);
                    holder.tvPeriodEndLeft.setText(left_date_string);
                    holder.periodEnds.setText(getString(R.string.period_end_after) + "\n" + left_date_string);
                }else {
                    holder.tvPeriodEndLeftTitle.setText(getString(R.string.credit_end_date)+":");
                    holder.tvPeriodEndLeft.setText(sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
                    holder.periodEnds.setText(getString(R.string.credit_end_date)+":" + "\n" + sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
                }
            }

            if (itemCr.getValue_of_credit_with_procent() - total_paid <= 0) {
                holder.tvPayAbout.setText(R.string.to_archive);
            } else {
                holder.tvPayAbout.setText(R.string.pay);
            }
            holder.llSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ScheduleCreditFragment scheduleCreditFragment = new ScheduleCreditFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(CreditTabLay.CREDIT_ID,itemCr.getMyCredit_id());
                    bundle.putInt(CreditTabLay.LOCAL_APPEREANCE, CreditTabLay.LOCAL_MAIN);
                    scheduleCreditFragment.setArguments(bundle);
                    paFragmentManager.displayFragment(scheduleCreditFragment);
                }
            });
            holder.glav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InfoCreditFragment temp = new InfoCreditFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(CreditTabLay.CREDIT_ID,itemCr.getMyCredit_id());
                    temp.setArguments(bundle);
                    paFragmentManager.displayFragment(temp);
                }
            });
            holder.pay_or_archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.tvPayAbout.getText().equals(getString(R.string.to_archive))) {
                        CreditDetials toArc = cardDetials.get(position);
                        toArc.setKey_for_archive(true);
                        logicManager.insertCredit(toArc);
                        cardDetials.set(position,toArc);
                        notifyItemChanged(position);

                        List<BoardButton> boardButtons=daoSession.getBoardButtonDao().loadAll();
                        for(BoardButton boardButton:boardButtons){
                            if(boardButton.getCategoryId()!=null)
                                if(boardButton.getCategoryId().equals(Long.toString(cardDetials.get(position).getMyCredit_id()))){
                                    if(boardButton.getTable()== PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE,boardButton.getPos(),null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME,boardButton.getPos(),null);
                                    commonOperations.changeIconToNull(boardButton.getPos(),dataCache,boardButton.getTable());

                                }
                        }
                        reportManager.clearCache();
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        if(creditTabLay==null) {
                            for (Fragment fragment : paFragmentManager.getFragmentManager().getFragments()){
                                if (fragment == null) continue;
                                if (fragment.getClass().getName().equals(CreditTabLay.class.getName())){
                                    creditTabLay = (CreditTabLay) fragment;
                                    break;
                                }
                            }
                        }
                        creditTabLay.updateArchive();
                        updateList();
                    } else
                        openDialog(itemCr, position,creditsSchedules);
                }
            });
        }


        @Override
        public int getItemCount() {
            return cardDetials.size();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.moder_titem, parent, false);
            return new myViewHolder(v);
        }



        public class myViewHolder extends RecyclerView.ViewHolder {
            TextView credit_procent;
            TextView procentCredInfo;
            TextView taken_credit_date;
            LinearLayout pay_or_archive;
            LinearLayout llSchedule;
            LinearLayout wlyuzOpenOpener;
            LinearLayout wlyuzOpen;

            TextView tvPayAbout;
            TextView nameCredit;
            TextView tvForThisPeriod;
            TextView tvBalanceCredit;
            TextView tvPeriodEndLeft;
            TextView intervalCreditInfo;
            TextView periodEnds;
            TextView tvForThisPeriodTitle;
            TextView tvPeriodEndLeftTitle;
            ImageView openCloseIcon;


            TextView takedValueInfo;
            TextView totalReturnValueInfo;
            View glav;
            ImageView iconn;
            public myViewHolder(View v) {
                super(v);
                credit_procent = (TextView) v.findViewById(R.id.procent_of_credit);
                procentCredInfo = (TextView) v.findViewById(R.id.procentCredInfo);
                tvPayAbout = (TextView) v.findViewById(R.id.tvPayAbout);
                taken_credit_date = (TextView) v.findViewById(R.id.date_start);
                totalReturnValueInfo = (TextView) v.findViewById(R.id.totalReturnValueInfo);
                pay_or_archive = (LinearLayout) v.findViewById(R.id.pay);
                wlyuzOpenOpener = (LinearLayout) v.findViewById(R.id.wlyuzOpenOpener);
                wlyuzOpen = (LinearLayout) v.findViewById(R.id.wlyuzOpen);
                llSchedule = (LinearLayout) v.findViewById(R.id.llSchedule);
                nameCredit = (TextView) v.findViewById(R.id.NameCr);
                tvForThisPeriod = (TextView) v.findViewById(R.id.tvForThisPeriod);
                tvPeriodEndLeft = (TextView) v.findViewById(R.id.tvPeriodEndLeft);
                tvBalanceCredit = (TextView) v.findViewById(R.id.tvBalanceCredit);
                tvPeriodEndLeftTitle = (TextView) v.findViewById(R.id.tvPeriodEndLeftTitle);
                takedValueInfo = (TextView) v.findViewById(R.id.takedValueInfo);
                tvForThisPeriodTitle = (TextView) v.findViewById(R.id.textView5);
                intervalCreditInfo = (TextView) v.findViewById(R.id.intervalCreditInfo);
                periodEnds = (TextView) v.findViewById(R.id.periodEnds);
                openCloseIcon = (ImageView) v.findViewById(R.id.openCloseIcon);
                iconn = (ImageView) v.findViewById(R.id.iconaaa);
                glav = v;
            }
        }


        CreditsSchedule unPaidPeriod;
        Calendar date;
        private void openDialog(final CreditDetials current, final int position,final ArrayList<CreditsSchedule> creditsSchedules) {
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


            final Dialog dialog = new Dialog(getContext());
            final View dialogView = ((PocketAccounter) getContext()).getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
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
            final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
            if(hozirgi){
                periodDate.setText(sDateFormat.format(currentPeriodi.getDate().getTime()));
                if(formater.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()).equals("0")|| currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()<= 0){
                    shouldPayPeriod.setText(R.string.complete);
                    shouldPayPeriod.setTextColor(ContextCompat.getColor(getContext(),R.color.credit_och_yashil));
                }
                else{
                    shouldPayPeriod.setText(periodFormat.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed())+current.getValyute_currency().getAbbr());
                    shouldPayPeriod.setTextColor(ContextCompat.getColor(getContext(),R.color.credit_yellow));
                }
            }
            else {
                periodDate.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));
                shouldPayPeriod.setText(formater.format(unPaidPeriod.getPaymentSum() - unPaidPeriod.getPayed())+current.getValyute_currency().getAbbr());

            }
            accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
            ArrayList accounts = new ArrayList();
            for (int i = 0; i < accaunt_AC.size(); i++) {
                accounts.add(accaunt_AC.get(i).getId());
            }
            accountSp.setAdapter(new TransferAccountAdapter(getContext(),accounts));
            String lastAccountId = preferences.getString("CHOSEN_ACCOUNT_ID",  "");
            if (lastAccountId != null && !lastAccountId.isEmpty()) {
                int pos = 0;
                for (int i = 0; i < accaunt_AC.size(); i++) {
                    if (accaunt_AC.get(i).getId().equals(lastAccountId)) {
                        pos = i;
                        break;
                    }
                }
                accountSp.setSelection(pos);
            }
            accountSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    preferences.edit().putString("CHOSEN_ACCOUNT_ID", accaunt_AC.get(i).getId()).commit();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
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
            abbrrAmount.setText(current.getValyute_currency().getAbbr());

            date = Calendar.getInstance();
            if(unPaidPeriod.getDate().getTimeInMillis()/1000/60/60/24<date.getTimeInMillis()/1000/60/60/24){
                date= (Calendar) unPaidPeriod.getDate().clone();
                date.add(Calendar.DAY_OF_MONTH,-1);
                enterDate.setText(dateFormat.format(date.getTime()));
            }
            else if (current.getTake_time().getTimeInMillis() > date.getTimeInMillis()) {
                date = (Calendar) current.getTake_time().clone();
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
                    if (current.getTake_time().getTimeInMillis() > (new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()) {
                        enterDate.setError(getContext().getString(R.string.incorrect_date));
                        date = (Calendar) current.getTake_time().clone();
                        date.add(Calendar.DAY_OF_MONTH,+1);
                        enterDate.setText(dateFormat.format(date.getTime()));
                    } else if( unPaidPeriod.getDate().getTimeInMillis()<(new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()){
                        Toast.makeText(getContext(), getString(R.string.you_can_jump), Toast.LENGTH_SHORT).show();
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
                    Dialog mDialog = new DatePickerDialog(getContext(),
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
                    for (ReckingCredit item : current.getReckings())
                        total_paid += item.getAmount();

                    if (!amount.equals("")) {
                        if(keyForInclude.isChecked()){
                            Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
                            if (account.getIsLimited()) {
                                //TODO editda tekwir ozini hisoblamaslini
                                double limit = account.getLimite();
                                double accounted =  logicManager.isLimitAccess(account, date);

                                accounted = accounted - commonOperations.getCost(date, current.getValyute_currency(), account.getCurrency(), Double.parseDouble(amount));
                                if (-limit > accounted) {
                                    Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }}
                        if (Double.parseDouble(amount) > current.getValue_of_credit_with_procent() - total_paid+1) {
                            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String amount = enterPay.getText().toString();
                                    ReckingCredit rec = null;
                                    if (!amount.matches("") && keyForInclude.isChecked())
                                        rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), current.getMyCredit_id(), comment.getText().toString());
                                    else
                                        rec = new ReckingCredit(date, Double.parseDouble(amount), "", current.getMyCredit_id(), comment.getText().toString());

                                    logicManager.insertReckingCredit(rec);
                                    current.resetReckings();
                                    reportManager.clearCache();
                                    dataCache.updateAllPercents();
                                    paFragmentManager.updateAllFragmentsOnViewPager();
                                    notifyItemChanged(position);
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
                            warningDialog.setText(getString(R.string.payment_balans) + decimalFormatter.format(current.getValue_of_credit_with_procent() - total_paid) +
                                    current.getValyute_currency().getAbbr() + "." + getString(R.string.payment_balance2) +
                                    decimalFormatter.format(Double.parseDouble(amount) - (current.getValue_of_credit_with_procent() - total_paid)) +
                                    current.getValyute_currency().getAbbr());
                            warningDialog.show();
                        } else {
                            ReckingCredit rec ;
                            if (!amount.matches("") && keyForInclude.isChecked())
                                rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), current.getMyCredit_id(), comment.getText().toString());
                            else
                                rec = new ReckingCredit(date, Double.parseDouble(amount), "", current.getMyCredit_id(), comment.getText().toString());
                            int pos = cardDetials.indexOf(current);
                            logicManager.insertReckingCredit(rec);
                            current.resetReckings();
                            reportManager.clearCache();
                            dataCache.updateAllPercents();
                            paFragmentManager.updateAllFragmentsOnViewPager();
                            notifyItemChanged(pos);
                            dialog.dismiss();
                        }
                    }

                }
            });
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
            dialog.show();
        }
    }
}