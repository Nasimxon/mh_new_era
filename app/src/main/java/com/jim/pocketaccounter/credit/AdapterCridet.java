package com.jim.pocketaccounter.credit;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.fragments.AddCreditFragment;
import com.jim.pocketaccounter.fragments.CreditFragment;
import com.jim.pocketaccounter.fragments.CreditTabLay;
import com.jim.pocketaccounter.fragments.InfoCreditFragment;
import com.jim.pocketaccounter.fragments.ScheduleCreditFragment;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;

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

/**
 * Created by developer on 02.06.2016.
 */

public class AdapterCridet extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Inject
    CommonOperations commonOperations;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    LogicManager logicManager;
    @Inject
    DataCache dataCache;
    @Inject
    DecimalFormat formatter;
    WarningDialog warningDialog;
    CreditDetialsDao creditDetialsDao;
    AccountDao accountDao;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    boolean oplaceniy = false;
    List<CreditDetials> cardDetials;
    CreditTabLay.SvyazkaFragmentov svyaz;
    Context context;
    ArrayList<Account> accaunt_AC;
    DecimalFormat formater;
    DecimalFormat periodFormat;
    NumberFormat numberFormat;
    long forDay = 1000L * 60L * 60L * 24L;
    long forMoth = 1000L * 60L * 60L * 24L * 30L;
    long forYear = 1000L * 60L * 60L * 24L * 365L;
    final static long forWeek = 1000L * 60L * 60L * 24L * 7L;
    public void updateList(){
        this.cardDetials=creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(false)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list();
    }
    public AdapterCridet(Context This, CreditTabLay.SvyazkaFragmentov svyaz) {
        ((PocketAccounter) This).component((PocketAccounterApplication) This.getApplicationContext()).inject(this);
        warningDialog = new WarningDialog(This);
        creditDetialsDao = daoSession.getCreditDetialsDao();
        accountDao = daoSession.getAccountDao();
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        periodFormat = (DecimalFormat) numberFormat;
        periodFormat.setDecimalFormatSymbols(symbols);;
        this.cardDetials=creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(false)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list();
        this.context = This;
        formater = new DecimalFormat("0.00");
        this.svyaz = svyaz;
    }
    boolean prosrecenniy = false ;
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holdeer, final int position) {
        if (holdeer instanceof Fornull) {
            return;
        }

        final myViewHolder holder = (myViewHolder) holdeer;
        final CreditDetials itemCr = cardDetials.get(position);
        oplaceniy = false;
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
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        holder.credit_procent.setText(decimalFormat.format(itemCr.getProcent()) + "%");
        holder.procentCredInfo.setText(decimalFormat.format(itemCr.getProcent()) + "%");
        holder.takedValueInfo.setText(formatter.format(itemCr.getValue_of_credit()) + itemCr.getValyute_currency().getAbbr());
        //total amount hisob kitob
        double total_paid = 0;
        for (ReckingCredit item : itemCr.getReckings())
            total_paid += item.getAmount();

        holder.nameCredit.setText(itemCr.getCredit_name());

        Date AAa = (new Date());
        AAa.setTime(itemCr.getTake_time().getTimeInMillis());
        holder.taken_credit_date.setText(sDateFormat.format(AAa));
        int resId = context.getResources().getIdentifier(itemCr.getIcon_ID(), "drawable", context.getPackageName());
        holder.iconn.setImageResource(resId);

        final ArrayList<CreditsSchedule> creditsSchedules = new ArrayList<>();
        HeaderData headerData;
        if(itemCr.getType_loan()==CommonOperations.ANUTETNIY){
            headerData = ScheduleCreditFragment.calculetAnutetniy(itemCr,creditsSchedules);
        }
        else {
            headerData = ScheduleCreditFragment.calculetDeferinsial(itemCr,creditsSchedules);
        }
        if(headerData.getTotalLoanWithInterest()<=headerData.getTotalPayedAmount()){
            oplaceniy = true;
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

        if(yestDolg){
            holder.tvForThisPeriodTitle.setText(context.getString(R.string.you_have_debt)+":");
            holder.tvForThisPeriod.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));
        }
        else {
            if(!prosrecenniy)
                if(formater.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed()).equals("0")|| currentPeriod.getPaymentSum() - currentPeriod.getPayed()<= 0){
                    if(headerData.getTotalLoanWithInterest()<=headerData.getTotalPayedAmount())
                        holder.tvForThisPeriodTitle.setText(context.getString(R.string.credit_stat));
                    else
                        holder.tvForThisPeriodTitle.setText(context.getString(R.string.for_this_period));

                    holder.tvForThisPeriod.setText(R.string.complete);
                    holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
                }
                else{
                    holder.tvForThisPeriodTitle.setText(R.string.for_this_period);
                    holder.tvForThisPeriod.setText(periodFormat.format(currentPeriod.getPaymentSum() - currentPeriod.getPayed())+itemCr.getValyute_currency().getAbbr());
                    holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
                }
            else {
                holder.tvForThisPeriodTitle.setText(R.string.credit_stat);
                holder.tvForThisPeriod.setText(R.string.complete);
                holder.tvForThisPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }

        }

        holder.totalReturnValueInfo.setText(formatter.format(headerData.getTotalLoanWithInterest())+itemCr.getValyute_currency().getAbbr());
        holder.tvBalanceCredit.setText(formatter.format(headerData.getTotalPayedAmount())+itemCr.getValyute_currency().getAbbr());
        Calendar to;
        if(!prosrecenniy)
        to = (Calendar) currentPeriod.getDate();
        else to = Calendar.getInstance();
        long period_tip = itemCr.getPeriod_time_tip();
        long period_voqt = itemCr.getPeriod_time();
        int voqt_soni = (int) (period_voqt / period_tip);
        String interval ;
        interval = voqt_soni + " ";
        if (period_tip == forMoth) {
            interval = interval + context.getString(R.string.mont);
        } else {
            interval = interval + context.getString(R.string.yearr);
        }

        holder.intervalCreditInfo.setText(interval);

        int t[] = getDateDifferenceInDDMMYYYY(from, to.getTime());
        if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
            holder.tvPeriodEndLeft.setText(R.string.ends);
            holder.tvPeriodEndLeft.setTextColor(Color.parseColor("#832e1c"));
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
            if(!prosrecenniy) {
                holder.tvPeriodEndLeftTitle.setText(R.string.period_ends);
                holder.tvPeriodEndLeft.setText(left_date_string);
                holder.periodEnds.setText(context.getString(R.string.period_end_after) + "\n" + left_date_string);
            }else {
                holder.tvPeriodEndLeftTitle.setText(context.getString(R.string.credit_end_date)+":");
                holder.tvPeriodEndLeft.setText(sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
                holder.periodEnds.setText(context.getString(R.string.credit_end_date)+":" + "\n" + sDateFormat.format(creditsSchedules.get(creditsSchedules.size()-1).getDate().getTime()));
            }
        }


        if (itemCr.getValue_of_credit_with_procent() - total_paid <= 0) {
            holder.tvPayAbout.setText(R.string.archive);
        } else {
            holder.tvPayAbout.setText(R.string.pay);
        }
        holder.llSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleCreditFragment scheduleCreditFragment = new ScheduleCreditFragment();
                scheduleCreditFragment.setCreditObject(itemCr);
                paFragmentManager.displayFragment(scheduleCreditFragment);
            }
        });
        holder.glav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = cardDetials.indexOf(itemCr);
                InfoCreditFragment temp = new InfoCreditFragment();
                temp.setConteent(itemCr, pos, new InfoCreditFragment.ConWithFragments() {
                    @Override
                    public void change_item(CreditDetials creditDetials, int position) {
//                        updateList();
                        double obswiy = 0;
                         notifyItemChanged(position);
//                        notifyDataSetChanged();
                    }

                    @Override
                    public void to_Archive(int position) {

                        svyaz.itemInsertedToArchive();
                        notifyItemChanged(position);
                    }

                    @Override
                    public void delete_item(int position) {
                        updateList();
                        notifyItemRemoved(position);
                    }
                });
                openFragment(temp, "InfoFragment");
            }
        });
        holder.pay_or_archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean toArcive = context.getString(R.string.archive).matches(holder.tvPayAbout.getText().toString());
                int pos = cardDetials.indexOf(itemCr);
                if (toArcive) {
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

                    dataCache.updateAllPercents();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    svyaz.itemInsertedToArchive();
                } else
                    openDialog(itemCr, position,creditsSchedules);
            }
        });
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

    @Override
    public int getItemCount() {
        return cardDetials.size();
    }

    final static int VIEW_NULL = 0;
    final static int VIEW_NOT_NULL = 1;

    @Override
    public int getItemViewType(int position) {
        return cardDetials.get(position).getKey_for_archive() ? VIEW_NULL : VIEW_NOT_NULL;
    }

    public String parseToWithoutNull(double A) {
        if (A == (int) A) {
            return Integer.toString((int) A);
        } else
            return formater.format(A);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Razvetleniya na dve view. odin pustoy odin realniy
        RecyclerView.ViewHolder vh = null;
        if (viewType == VIEW_NULL) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.null_lay, parent, false);
            vh = new Fornull(v);
        } else if (viewType == VIEW_NOT_NULL) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.moder_titem, parent, false);
            vh = new myViewHolder(v);
        }
        return vh;
    }

    public static class Fornull extends RecyclerView.ViewHolder {
        public Fornull(View v) {
            super(v);
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
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

    public void openFragment(Fragment fragment, String tag) {
        if (fragment != null) {
            if (tag.matches("Addcredit"))
                ((AddCreditFragment) fragment).addEventLis(new CreditFragment.EventFromAdding() {
                    @Override
                    public void addedCredit() {
                        notifyItemInserted(0);
                    }
                    @Override
                    public void canceledAdding() {}
                });
            paFragmentManager.displayFragment(fragment,tag);
//            final android.support.v4.app.FragmentTransaction ft = ((PocketAccounter) context).getSupportFragmentManager().beginTransaction().addToBackStack(tag).setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            ft.add(R.id.flMain, fragment, tag);
//            ft.commit();
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
        final RelativeLayout checkInclude = (RelativeLayout) dialogView.findViewById(R.id.checkInclude);
        final RelativeLayout is_calc = (RelativeLayout) dialogView.findViewById(R.id.is_calc);
        final SwitchCompat keyForInclude = (SwitchCompat) dialogView.findViewById(R.id.key_for_balance);
        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
        if(hozirgi){
            periodDate.setText(sDateFormat.format(currentPeriodi.getDate().getTime()));
            if(formater.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()).equals("0")|| currentPeriodi.getPaymentSum() - currentPeriodi.getPayed()<= 0){
                shouldPayPeriod.setText(R.string.complete);
                shouldPayPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
                shouldPayPeriod.setText(periodFormat.format(currentPeriodi.getPaymentSum() - currentPeriodi.getPayed())+current.getValyute_currency().getAbbr());
                shouldPayPeriod.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }
        }
        else {
            periodDate.setText(sDateFormat.format(unPaidPeriod.getDate().getTime()));
            shouldPayPeriod.setText(formater.format(unPaidPeriod.getPaymentSum() - unPaidPeriod.getPayed())+current.getValyute_currency().getAbbr());

        }
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
        abbrrAmount.setText(current.getValyute_currency().getAbbr());

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
                if (current.getTake_time().getTimeInMillis() >= (new GregorianCalendar(year, monthOfYear, dayOfMonth)).getTimeInMillis()) {
                    enterDate.setError(context.getString(R.string.incorrect_date));
                    enterDate.setText(dateFormat.format(current.getTake_time().getTime()));
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
                for (ReckingCredit item : current.getReckings())
                    total_paid += item.getAmount();

                if (!amount.matches("")) {
                    if(keyForInclude.isChecked()){
                    Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
                    if (account.getIsLimited()) {
                        //TODO editda tekwir ozini hisoblamaslini
                        double limit = account.getLimite();
                        double accounted =  logicManager.isLimitAccess(account, date);

                        accounted = accounted - commonOperations.getCost(date, current.getValyute_currency(), account.getCurrency(), Double.parseDouble(amount));
                        if (-limit > accounted) {
                            Toast.makeText(context, R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }}
                    if (Double.parseDouble(amount) > current.getValue_of_credit_with_procent() - total_paid) {
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String amount = enterPay.getText().toString();
                                ReckingCredit rec = null;
                                if (!amount.matches("") && keyForInclude.isChecked())
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), current.getMyCredit_id(), comment.getText().toString());
                                else
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), "", current.getMyCredit_id(), comment.getText().toString());
                                int pos = cardDetials.indexOf(current);
                                logicManager.insertReckingCredit(rec);
                                current.resetReckings();
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
                        warningDialog.setText(context.getString(R.string.payment_balans) + parseToWithoutNull(current.getValue_of_credit_with_procent() - total_paid) +
                                current.getValyute_currency().getAbbr() + "." + context.getString(R.string.payment_balance2) +
                                parseToWithoutNull(Double.parseDouble(amount) - (current.getValue_of_credit_with_procent() - total_paid)) +
                                current.getValyute_currency().getAbbr());
                        warningDialog.show();
                    } else {
                        ReckingCredit rec = null;
                        if (!amount.matches("") && keyForInclude.isChecked())
                            rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), current.getMyCredit_id(), comment.getText().toString());
                        else
                            rec = new ReckingCredit(date, Double.parseDouble(amount), "", current.getMyCredit_id(), comment.getText().toString());
                        int pos = cardDetials.indexOf(current);
                        logicManager.insertReckingCredit(rec);
                        current.resetReckings();
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        notifyItemChanged(pos);
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
}
