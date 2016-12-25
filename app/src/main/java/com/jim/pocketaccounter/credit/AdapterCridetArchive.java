package com.jim.pocketaccounter.credit;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.fragments.InfoCreditFragment;
import com.jim.pocketaccounter.fragments.InfoCreditFragmentForArchive;
import com.jim.pocketaccounter.fragments.ScheduleCreditFragment;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by developer on 02.06.2016.
 */

public class AdapterCridetArchive extends RecyclerView.Adapter<AdapterCridetArchive.myViewHolder> {
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    LogicManager logicManager;
    @Inject
    DecimalFormat formatter;
    CreditDetialsDao creditDetialsDao;
    List<CreditDetials> cardDetials;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    Context context;
    DecimalFormat decimalFormat;
    NumberFormat numberFormat;
    long forDay = 1000L * 60L * 60L * 24L;
    long forMoth = 1000L * 60L * 60L * 24L * 30L;
    long forYear = 1000L * 60L * 60L * 24L * 365L;
    final static long forWeek = 1000L * 60L * 60L * 24L * 7L;

    DecimalFormat formater;

    public AdapterCridetArchive(Context This) {
        ((PocketAccounter) This).component((PocketAccounterApplication) This.getApplicationContext()).inject(this);
        creditDetialsDao = daoSession.getCreditDetialsDao();
        cardDetials = new ArrayList<>();
        cardDetials = creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(true)).build().list();
        cardDetials = creditDetialsDao.queryBuilder().where(CreditDetialsDao.Properties.Key_for_archive.eq(true)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list();
        this.context = This;
        formater = new DecimalFormat("0.##");
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setDecimalFormatSymbols(symbols);
    }
     public  void updateBase(){
        cardDetials = creditDetialsDao.queryBuilder().where(CreditDetialsDao.Properties.Key_for_archive.eq(true)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list();
     }

    AdapterCridetArchive.GoCredFragForNotify svyazForNotifyFromArchAdap;

    public void setSvyazToAdapter(AdapterCridetArchive.GoCredFragForNotify goNotify) {
        svyazForNotifyFromArchAdap = goNotify;
    }

    public interface ListnerDel {
        void delete_item(int position);
    }

    public interface GoCredFragForNotify {
        void notifyCredFrag();
    }
    boolean oplaceniy=false;
    boolean prosrecenniy = false ;
    @Override
    public void onBindViewHolder(final  myViewHolder holder, final int position) {

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
        holder.credit_procent.setText(formater.format(itemCr.getProcent()) + "%");
        holder.procentCredInfo.setText(formater.format(itemCr.getProcent()) + "%");
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
        if(itemCr.getType_loan()== CommonOperations.ANUTETNIY){
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
        for (CreditsSchedule creditsSchedule:creditsSchedules){
            if(creditsSchedule.getDate().getTimeInMillis()>from.getTime()){
                currentPeriod = creditsSchedule;
                break;
            }
        }
        if(currentPeriod == null)
            prosrecenniy = true;
        holder.totalReturnValueInfo.setText(formatter.format(headerData.getTotalLoanWithInterest())+itemCr.getValyute_currency().getAbbr());



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

        holder.tvForThisPeriod.setText(decimalFormat.format(headerData.getTotalPayedAmount())+itemCr.getValyute_currency().getAbbr());









        holder.glav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoCreditFragmentForArchive temp = new InfoCreditFragmentForArchive();
                int pos = cardDetials.indexOf(itemCr);
                temp.setConteent(itemCr, pos, new ListnerDel() {
                    @Override
                    public void delete_item(int position) {
                        CreditDetials Az = cardDetials.get(position);
                        logicManager.deleteCredit(Az);
                        cardDetials.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                openFragment(temp, "InfoFragment");
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

    public String parseToWithoutNull(double A) {
        if (A == (int) A)
            return Integer.toString((int) A);
        else
            return formater.format(A);
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.moder_titem_arch, parent, false);
        // set the view's size, margins, paddings and layout parameters
        myViewHolder vh = new myViewHolder(v);
        return vh;
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView credit_procent;
        TextView procentCredInfo;
        TextView taken_credit_date;
        LinearLayout wlyuzOpenOpener;
        LinearLayout wlyuzOpen;

        TextView nameCredit;
        TextView tvForThisPeriod;
        TextView intervalCreditInfo;
        ImageView openCloseIcon;


        TextView takedValueInfo;
        TextView totalReturnValueInfo;
        View glav;
        ImageView iconn;
        public myViewHolder(View v) {
            super(v);
            credit_procent = (TextView) v.findViewById(R.id.procent_of_credit);
            procentCredInfo = (TextView) v.findViewById(R.id.procentCredInfo);
            taken_credit_date = (TextView) v.findViewById(R.id.date_start);
            totalReturnValueInfo = (TextView) v.findViewById(R.id.totalReturnValueInfo);
            wlyuzOpenOpener = (LinearLayout) v.findViewById(R.id.wlyuzOpenOpener);
            wlyuzOpen = (LinearLayout) v.findViewById(R.id.wlyuzOpen);
            nameCredit = (TextView) v.findViewById(R.id.NameCr);
            tvForThisPeriod = (TextView) v.findViewById(R.id.tvForThisPeriod);
            takedValueInfo = (TextView) v.findViewById(R.id.takedValueInfo);
            intervalCreditInfo = (TextView) v.findViewById(R.id.intervalCreditInfo);
            openCloseIcon = (ImageView) v.findViewById(R.id.openCloseIcon);
            iconn = (ImageView) v.findViewById(R.id.iconaaa);
            glav = v;
        }
    }

    public void openFragment(Fragment fragment, String tag) {
        if (fragment != null) {
            paFragmentManager.displayFragment(fragment);
        }
    }
}