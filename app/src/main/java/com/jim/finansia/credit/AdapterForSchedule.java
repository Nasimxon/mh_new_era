package com.jim.finansia.credit;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.managers.CommonOperations;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by developer on 29.11.2016.
 */

public class AdapterForSchedule extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    ArrayList<Object> creditsSchedules;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    DecimalFormat formatter;
    Currency currency;
    Context context;
    boolean allPeriods = true;
    public AdapterForSchedule(ArrayList<Object> creditsSchedules, Currency currency, Context context){
        this.creditsSchedules = creditsSchedules;
        this.currency = currency;
        this.context = context;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh = null;
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.credit_amortization_header, parent, false);
            vh = new HeaderViewHolder(v);
        } else if (viewType == 2) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item, parent, false);
            vh = new AdapterForSchedule.MyViewHolder(v);
        } else if (viewType == 3) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.null_lay, parent, false);
            vh = new Fornull(v);
        }
        return  vh;
    }
    @Override
    public int getItemViewType(int position) {
        int type=2;
        if(position==0){ type = 1; return type;}
        if(!allPeriods){
            CreditsSchedule crSchedule = (CreditsSchedule) creditsSchedules.get(position);
            if (((int)((crSchedule.getPaymentSum() - crSchedule.getPayed() )*100))==0||crSchedule.getPaymentSum() - crSchedule.getPayed() <= 0) type= 3;
        }

        return type;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holdeer, int position) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        formatter = (DecimalFormat) numberFormat;
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        if (holdeer instanceof Fornull) {
            return;
        }
        if (holdeer instanceof HeaderViewHolder) {
            final HeaderViewHolder headHolder = (HeaderViewHolder) holdeer;
            HeaderData hdData = (HeaderData) creditsSchedules.get(position);
            if(hdData.getCreditType()== CommonOperations.DEFERINSIAL){
                headHolder.tvMothlyPayment.setText(formatter.format(hdData.getMothlyPayment1())+currency.getAbbr()+"-"+formatter.format(hdData.getMothlyPayment2())+currency.getAbbr());
            }
            else if(hdData.getCreditType()== CommonOperations.ANUTETNIY){
                if(decimalFormat.format(hdData.getMothlyPayment1()).equals(decimalFormat.format(hdData.getMothlyPayment2())))
                headHolder.tvMothlyPayment.setText(formatter.format(hdData.getMothlyPayment1())+currency.getAbbr());
                else
                    headHolder.tvMothlyPayment.setText(formatter.format(hdData.getMothlyPayment1())+currency.getAbbr()+"-"+formatter.format(hdData.getMothlyPayment2())+currency.getAbbr());

            }
            headHolder.rvPeriodCount.setText(Integer.toString(creditsSchedules.size()-1));
            headHolder.tvBankFee.setText(formatter.format(hdData.getBankFee())+currency.getAbbr());
            headHolder.tvOverPayemtInterest.setText(formatter.format(hdData.getOverpaymentInterest())+currency.getAbbr());
            headHolder.tvTotalPayed.setText(formatter.format(hdData.getTotalPayedAmount())+currency.getAbbr());
            headHolder.tvTotalWithInterest.setText(formatter.format(hdData.getTotalLoanWithInterest())+currency.getAbbr());
            if(allPeriods){
                headHolder.chbAllPeriods.setChecked(true);
                headHolder.chbNotCompleted.setChecked(false);

            }
            else {
                headHolder.chbAllPeriods.setChecked(false);
                headHolder.chbNotCompleted.setChecked(true);
            }
            headHolder.chbAllPeriods.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allPeriods = true;
                    headHolder.chbAllPeriods.setChecked(true);
                    headHolder.chbNotCompleted.setChecked(false);
                    notifyDataSetChanged();
                }
            });
            headHolder.chbNotCompleted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allPeriods = false;
                    headHolder.chbNotCompleted.setChecked(true);
                    headHolder.chbAllPeriods.setChecked(false);
                    notifyDataSetChanged();
                }
            });
//            headHolder.chbAllPeriods.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                }
//            });
//            headHolder.chbNotCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                }
//            });
        }
        else {
            MyViewHolder holder = (MyViewHolder) holdeer;
            CreditsSchedule crSchedule = (CreditsSchedule) creditsSchedules.get(position);
            holder.tvCountItemPerios.setText(""+(position));
            holder.tvPeriodEndDate.setText(sDateFormat.format(crSchedule.getDate().getTime()));
            if(((int)((crSchedule.getPaymentSum() - crSchedule.getPayed() )*100))==0||crSchedule.getPaymentSum() - crSchedule.getPayed() <= 0){
                if(!allPeriods){
                    holder.mainView.addView(new View(context));
                    return;
                }
                holder.tvTitleSum.setText(R.string.complete);
                holder.tvTitleSum.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
            holder.tvTitleSum.setText(formatter.format(crSchedule.getPaymentSum() - crSchedule.getPayed())+currency.getAbbr());
            holder.tvTitleSum.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }

            holder.tvPrincipal.setText(formatter.format(crSchedule.getPrincipal())+currency.getAbbr());
            holder.tvInterest.setText(formatter.format(crSchedule.getInterest())+currency.getAbbr());
            holder.tvMonthlyFee.setText(formatter.format(crSchedule.getMonthlyCom())+currency.getAbbr());
            holder.tvPaymentSum.setText(formatter.format(crSchedule.getPaymentSum())+currency.getAbbr());
            holder.tvPeriodPayed.setText(formatter.format(crSchedule.getPayed())+currency.getAbbr());
            holder.tvBalance.setText(formatter.format(crSchedule.getBalance())+currency.getAbbr());}
    }
    public class Fornull extends RecyclerView.ViewHolder {
        public Fornull(View v) {
            super(v);
        }
    }

    @Override
    public int getItemCount() {
        return creditsSchedules.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCountItemPerios;
        TextView tvPeriodEndDate;
        TextView tvTitleSum;
        TextView tvPrincipal;
        TextView tvInterest;
        TextView tvMonthlyFee;
        TextView tvPaymentSum;
        TextView tvPeriodPayed;
        TextView tvBalance;
        LinearLayout mainView;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvCountItemPerios = (TextView) itemView.findViewById(R.id.tvCountItemPerios);
            tvPeriodEndDate = (TextView) itemView.findViewById(R.id.tvPeriodEndDate);
            tvTitleSum = (TextView) itemView.findViewById(R.id.tvTitleSum);
            tvPrincipal = (TextView) itemView.findViewById(R.id.tvPrincipal);
            tvInterest = (TextView) itemView.findViewById(R.id.tvInterest);
            tvMonthlyFee = (TextView) itemView.findViewById(R.id.tvMonthlyFee);
            tvPaymentSum = (TextView) itemView.findViewById(R.id.tvPaymentSum);
            tvPeriodPayed = (TextView) itemView.findViewById(R.id.tvPeriodPayed);
            tvBalance = (TextView) itemView.findViewById(R.id.tvBalance);
            mainView = (LinearLayout) itemView.findViewById(R.id.mainView);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMothlyPayment;
        TextView tvOverPayemtInterest;
        TextView tvBankFee;
        TextView tvTotalWithInterest;
        TextView tvTotalPayed;
        TextView rvPeriodCount;
        CheckBox chbNotCompleted;
        CheckBox chbAllPeriods;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvMothlyPayment = (TextView) itemView.findViewById(R.id.tvMothlyPayment);
            tvOverPayemtInterest = (TextView) itemView.findViewById(R.id.tvOverPayemtInterest);
            tvBankFee = (TextView) itemView.findViewById(R.id.tvBankFee);
            tvTotalWithInterest = (TextView) itemView.findViewById(R.id.tvTotalWithInterest);
            tvTotalPayed = (TextView) itemView.findViewById(R.id.tvTotalPayed);
            rvPeriodCount = (TextView) itemView.findViewById(R.id.rvPeriodCount);
            chbNotCompleted = (CheckBox) itemView.findViewById(R.id.chbNotCompleted);
            chbAllPeriods = (CheckBox) itemView.findViewById(R.id.chbAllPeriods);


        }

    }
}
