package com.jim.finansia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.CreditsSchedule;
import com.jim.finansia.credit.HeaderData;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class CreditArchiveFragment extends Fragment {
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
    @Inject
    FinansiaFirebaseAnalytics analytics;

    private RecyclerView crRV;
    private TextView ifListEmpty;

    public CreditArchiveFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V=inflater.inflate(R.layout.fragment_credit, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        ifListEmpty=(TextView) V.findViewById(R.id.ifListEmpty);
        if(daoSession.getCreditDetialsDao().queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(true)).build().list().size()==0){
            ifListEmpty.setVisibility(View.VISIBLE);
            ifListEmpty.setText(getResources().getString(R.string.credit_arcive_are_empty));
        }
        else {
            ifListEmpty.setVisibility(View.GONE);
        }
        crRV=(RecyclerView) V.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        crRV.setLayoutManager(llm);
        updateList();

        return V;
    }

    public void updateList(){
        List<CreditDetials> creditDetialses = daoSession
                .queryBuilder(CreditDetials.class)
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(true))
                .orderDesc(CreditDetialsDao.Properties.MyCredit_id)
                .build()
                .list();
        AdapterCridetArchive adapterCridetArchive = new AdapterCridetArchive(creditDetialses);
        if(crRV!=null) crRV.setAdapter(adapterCridetArchive);
    }


    public class AdapterCridetArchive extends RecyclerView.Adapter<AdapterCridetArchive.myViewHolder> {

        private CreditDetialsDao creditDetialsDao;
        private List<CreditDetials> cardDetials;
        private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
        private DecimalFormat decimalFormat;
        private NumberFormat numberFormat;
        private long forMoth = 1000L * 60L * 60L * 24L * 30L;

        private DecimalFormat formater = new DecimalFormat("0.00");;

        public AdapterCridetArchive(List<CreditDetials> creditDetialses) {
            cardDetials = creditDetialses;
            numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            decimalFormat = (DecimalFormat) numberFormat;
            decimalFormat.setDecimalFormatSymbols(symbols);
        }



        @Override
        public void onBindViewHolder(final  myViewHolder holder, final int position) {

            final CreditDetials itemCr = cardDetials.get(position);

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


            holder.nameCredit.setText(itemCr.getCredit_name());

            Date AAa = (new Date());
            AAa.setTime(itemCr.getTake_time().getTimeInMillis());
            holder.taken_credit_date.setText(sDateFormat.format(AAa));
            int resId = getResources().getIdentifier(itemCr.getIcon_ID(), "drawable", getContext().getPackageName());
            holder.iconn.setImageResource(resId);

            final ArrayList<CreditsSchedule> creditsSchedules = new ArrayList<>();
            HeaderData headerData;
            if(itemCr.getType_loan()== CommonOperations.ANUTETNIY){
                headerData = ScheduleCreditFragment.calculetAnutetniy(itemCr,creditsSchedules);
            }
            else {
                headerData = ScheduleCreditFragment.calculetDeferinsial(itemCr,creditsSchedules);
            }

            holder.totalReturnValueInfo.setText(formatter.format(headerData.getTotalLoanWithInterest())+itemCr.getValyute_currency().getAbbr());

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
            holder.tvForThisPeriod.setText(decimalFormat.format(headerData.getTotalPayedAmount())+itemCr.getValyute_currency().getAbbr());
            holder.glav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InfoCreditFragmentForArchive temp = new InfoCreditFragmentForArchive();
                    Bundle bundle = new Bundle();
                    bundle.putLong(CreditTabLay.CREDIT_ID,itemCr.getMyCredit_id());
                    temp.setArguments(bundle);
                    paFragmentManager.displayFragment(temp);
                }
            });

        }


        @Override
        public int getItemCount() {
            return cardDetials.size();
        }


        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.moder_titem_arch, parent, false);
            return  new myViewHolder(v);
        }

        public class myViewHolder extends RecyclerView.ViewHolder {
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


    }


}
