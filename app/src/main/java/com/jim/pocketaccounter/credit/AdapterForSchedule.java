package com.jim.pocketaccounter.credit;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.managers.CommonOperations;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by developer on 29.11.2016.
 */

public class AdapterForSchedule extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    ArrayList<Object> creditsSchedules;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    Currency currency;
    Context context;
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
        }
        return  vh;
    }
    @Override
    public int getItemViewType(int position) {
        return ((position==0)?1:2);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holdeer, int position) {
        if (holdeer instanceof HeaderViewHolder) {
            HeaderViewHolder headHolder = (HeaderViewHolder) holdeer;
            HeaderData hdData = (HeaderData) creditsSchedules.get(position);
            if(hdData.getCreditType()== CommonOperations.DEFERINSIAL){
                headHolder.tvMothlyPayment.setText(decimalFormat.format(hdData.getMothlyPayment1())+currency.getAbbr()+"-"+decimalFormat.format(hdData.getMothlyPayment2())+currency.getAbbr());
            }
            else if(hdData.getCreditType()== CommonOperations.ANUTETNIY){
                if(decimalFormat.format(hdData.getMothlyPayment1()).equals(decimalFormat.format(hdData.getMothlyPayment2())))
                headHolder.tvMothlyPayment.setText(decimalFormat.format(hdData.getMothlyPayment1())+currency.getAbbr());
                else
                    headHolder.tvMothlyPayment.setText(decimalFormat.format(hdData.getMothlyPayment1())+currency.getAbbr()+"-"+decimalFormat.format(hdData.getMothlyPayment2())+currency.getAbbr());

            }
            headHolder.rvPeriodCount.setText(Integer.toString(creditsSchedules.size()-1));
            headHolder.tvBankFee.setText(decimalFormat.format(hdData.getBankFee())+currency.getAbbr());
            headHolder.tvOverPayemtInterest.setText(decimalFormat.format(hdData.getOverpaymentInterest())+currency.getAbbr());
            headHolder.tvTotalPayed.setText(decimalFormat.format(hdData.getTotalPayedAmount())+currency.getAbbr());
            headHolder.tvTotalWithInterest.setText(decimalFormat.format(hdData.getTotalLoanWithInterest())+currency.getAbbr());
        }
        else {
            MyViewHolder holder = (MyViewHolder) holdeer;
            CreditsSchedule crSchedule = (CreditsSchedule) creditsSchedules.get(position);
            holder.tvCountItemPerios.setText(""+(position));
            holder.tvPeriodEndDate.setText(sDateFormat.format(crSchedule.getDate().getTime()));
            if(decimalFormat.format(crSchedule.getPaymentSum() - crSchedule.getPayed()).equals("0.00")||crSchedule.getPaymentSum() - crSchedule.getPayed() <= 0){
                holder.tvTitleSum.setText(R.string.complete);
                holder.tvTitleSum.setTextColor(ContextCompat.getColor(context,R.color.credit_och_yashil));
            }
            else{
            holder.tvTitleSum.setText(decimalFormat.format(crSchedule.getPaymentSum() - crSchedule.getPayed())+currency.getAbbr());
            holder.tvTitleSum.setTextColor(ContextCompat.getColor(context,R.color.credit_yellow));
            }

            holder.tvPrincipal.setText(decimalFormat.format(crSchedule.getPrincipal())+currency.getAbbr());
            holder.tvInterest.setText(decimalFormat.format(crSchedule.getInterest())+currency.getAbbr());
            holder.tvMonthlyFee.setText(decimalFormat.format(crSchedule.getMonthlyCom())+currency.getAbbr());
            holder.tvPaymentSum.setText(decimalFormat.format(crSchedule.getPaymentSum())+currency.getAbbr());
            holder.tvPeriodPayed.setText(decimalFormat.format(crSchedule.getPayed())+currency.getAbbr());
            holder.tvBalance.setText(decimalFormat.format(crSchedule.getBalance())+currency.getAbbr());}
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

        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMothlyPayment;
        TextView tvOverPayemtInterest;
        TextView tvBankFee;
        TextView tvTotalWithInterest;
        TextView tvTotalPayed;
        TextView rvPeriodCount;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvMothlyPayment = (TextView) itemView.findViewById(R.id.tvMothlyPayment);
            tvOverPayemtInterest = (TextView) itemView.findViewById(R.id.tvOverPayemtInterest);
            tvBankFee = (TextView) itemView.findViewById(R.id.tvBankFee);
            tvTotalWithInterest = (TextView) itemView.findViewById(R.id.tvTotalWithInterest);
            tvTotalPayed = (TextView) itemView.findViewById(R.id.tvTotalPayed);
            rvPeriodCount = (TextView) itemView.findViewById(R.id.rvPeriodCount);


        }
    }
}
