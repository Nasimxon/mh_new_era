package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.R;
import com.jim.finansia.credit.AdapterForSchedule;
import com.jim.finansia.credit.HeaderData;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.credit.CreditsSchedule;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.managers.CommonOperations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ScheduleCreditFragment extends PABaseFragment {
    RecyclerView recyclerView;
    AdapterForSchedule adapterForSchedule;
    CreditDetials currentCredit;
    ArrayList<Object> creditsSchedule;
    boolean fromAdding = false;
    public ScheduleCreditFragment() {
        // Required empty public constructor

    }
    public void isFromAdding(){
        fromAdding = true;
    }
    public void setCreditObject(CreditDetials creditObject){
        currentCredit = creditObject;
        creditsSchedule = new ArrayList<>();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_credit, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.scheduleMain);
        if(currentCredit.getType_loan()== CommonOperations.ANUTETNIY)  {
            HeaderData headerData = calculetAnutetniy(currentCredit,creditsSchedule);
            creditsSchedule.add(0,headerData);
        }
        else if(currentCredit.getType_loan()== CommonOperations.DEFERINSIAL) {
            HeaderData headerData = calculetDeferinsial(currentCredit,creditsSchedule);
            creditsSchedule.add(0,headerData);
        }

        if(fromAdding){
            toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.VISIBLE);
            toolbarManager.setImageToSecondImage(R.drawable.ic_save_black_48dp);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(ReckingCredit reckingCredit:currentCredit.getReckings() )
                    logicManager.insertReckingCredit(reckingCredit);
                    logicManager.insertCredit(currentCredit);
                    paFragmentManager.displayFragment(new CreditTabLay());
                }
            });
        }
        else {
            toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
        }

        adapterForSchedule = new AdapterForSchedule(creditsSchedule,currentCredit.getValyute_currency(),getContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterForSchedule);
        return view;
    }
    public static  double calculatePayment(CreditDetials creditDetials) {
        long forMoth = 1000L * 60L * 60L * 24L * 30L;
        long forYear = 1000L * 60L * 60L * 24L * 365L;
        double procent = 0;
        if(creditDetials.getProcent_interval() == forYear){
            procent = creditDetials.getProcent()/12;
        }
        else if(creditDetials.getProcent_interval() == forMoth){
            procent = creditDetials.getProcent();     }
        procent = procent/100;

        long period_tip = creditDetials.getPeriod_time_tip();
        long period_voqt = creditDetials.getPeriod_time();
        double period = (int) (period_voqt / period_tip);
        if(creditDetials.getPeriod_time_tip() == forYear){
            period = period*12;
        }

        double kofietsent = (procent*Math.pow(1+procent,period))/(Math.pow(1+procent,period)-1);
        double rezultat = creditDetials.getValue_of_credit() * kofietsent;
        return rezultat;
    }
    public static HeaderData calculetAnutetniy(CreditDetials creditDetials,ArrayList creditsSchedule){
        long forMoth = 1000L * 60L * 60L * 24L * 30L;
        long forYear = 1000L * 60L * 60L * 24L * 365L;
        HeaderData headerData = new HeaderData();
        headerData.setCreditType(CommonOperations.ANUTETNIY);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double platej  = calculatePayment(creditDetials );


        double procent = 0;
        if(creditDetials.getProcent_interval() == forYear){
            procent = creditDetials.getProcent()/12;
        }
        else if(creditDetials.getProcent_interval() == forMoth){
            procent = creditDetials.getProcent();     }
        procent = procent/100;

        long period_tip = creditDetials.getPeriod_time_tip();
        long period_voqt = creditDetials.getPeriod_time();
        double period = (int) (period_voqt / period_tip);
        if(creditDetials.getPeriod_time_tip() == forYear){
            period = period*12;
        }

        Calendar calendarik = (Calendar) creditDetials.getTake_time().clone();
        Calendar calendarikOld = (Calendar) creditDetials.getTake_time().clone();

        double allamount = creditDetials.getValue_of_credit();
        double osnovnoyDolg= 0;
        double procentStavka = 0;
        double sumPlatej = 0,sumOsnovnoyDolg = 0,sumProcentStavka = 0, sumMothlyFee = 0;
        for(int i=1;i<=period;i++){
            procentStavka = allamount * procent;
            osnovnoyDolg =  platej - procentStavka;

            if(allamount<=osnovnoyDolg){
                osnovnoyDolg = allamount;
                platej = osnovnoyDolg + procentStavka;
            }
            calendarik.add(Calendar.MONTH,1);
            if(i!=1)
                calendarikOld.add(Calendar.MONTH,1);
            double amountPayed = 0;
            for (ReckingCredit reckingCredit:creditDetials.getReckings()){
                if(calendarikOld.getTimeInMillis()<reckingCredit.getPayDate().getTimeInMillis()&&calendarik.getTimeInMillis()>reckingCredit.getPayDate().getTimeInMillis())
                {
                    amountPayed += reckingCredit.getAmount();
                }
            }

            allamount -= osnovnoyDolg;

            double monthlyFee = 0;
            if(creditDetials.getMonthly_fee_type() == 0){
                monthlyFee = creditDetials.getValue_of_credit() * creditDetials.getMonthly_fee()/100;
            }
            else {
                monthlyFee = allamount * creditDetials.getMonthly_fee()/100;

            }
            sumMothlyFee += monthlyFee;
            if(i==1){
                headerData.setMothlyPayment1(platej+monthlyFee);
            }

            if((platej+monthlyFee) < amountPayed){
                allamount -= (amountPayed - (platej+monthlyFee));
                osnovnoyDolg +=(amountPayed - (platej+monthlyFee));
            }

            sumPlatej += platej;
            sumOsnovnoyDolg += osnovnoyDolg;
            sumProcentStavka += procentStavka;

            headerData.setMothlyPayment2(platej+monthlyFee);
            CreditsSchedule crSchedule = new CreditsSchedule();
            crSchedule.setPaymentSum(platej+monthlyFee);
            crSchedule.setPrincipal(osnovnoyDolg);
            crSchedule.setMonthlyCom(monthlyFee);
            crSchedule.setInterest(procentStavka);
            crSchedule.setPayed(amountPayed);
            if(allamount <= 0.001){
                crSchedule.setBalance(0);
            } else
                crSchedule.setBalance(allamount);
            crSchedule.setDate((Calendar) calendarik.clone());
            creditsSchedule.add(crSchedule);
            if(allamount <= 0.001){
                break;
            }


        }
        headerData.setBankFee(sumMothlyFee);
        headerData.setOverpaymentInterest(sumProcentStavka);
        headerData.setTotalLoanWithInterest(creditDetials.getValue_of_credit()+sumProcentStavka+sumMothlyFee);
        //TODO payedSystem
        double allPaid=0;
        for(ReckingCredit reckingCredit:creditDetials.getReckings()){
            allPaid+=reckingCredit.getAmount();
        }
        headerData.setTotalPayedAmount(allPaid);

        creditDetials.setValue_of_credit_with_procent(creditDetials.getValue_of_credit()+sumProcentStavka+sumMothlyFee);
        System.out.printf(decimalFormat.format(sumPlatej)+"         "+decimalFormat.format(sumOsnovnoyDolg) + "         "+decimalFormat.format(sumProcentStavka)+"         "+decimalFormat.format(allamount)+"\n");
        return headerData;
    }


    public static HeaderData calculetDeferinsial(CreditDetials creditDetials,ArrayList creditsSchedule){

        long forMoth = 1000L * 60L * 60L * 24L * 30L;
        long forYear = 1000L * 60L * 60L * 24L * 365L;
        Calendar calendarik = (Calendar) creditDetials.getTake_time().clone();
        Calendar calendarikOld = (Calendar) creditDetials.getTake_time().clone();

        HeaderData headerData = new HeaderData();
        headerData.setCreditType(CommonOperations.DEFERINSIAL);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double platej = 0;



        long period_tip = creditDetials.getPeriod_time_tip();
        long period_voqt = creditDetials.getPeriod_time();

        double period = (int) (period_voqt / period_tip);
        if(creditDetials.getPeriod_time_tip() == forYear){
            period = period*12;
        }

        double procent = 0;
        if(creditDetials.getProcent_interval() == forYear){
            procent = creditDetials.getProcent()/12;
        }
        else if(creditDetials.getProcent_interval() == forMoth){
            procent = creditDetials.getProcent();     }
        procent = procent/100;
        double allamount = creditDetials.getValue_of_credit();
        double osnovnoyDolg= allamount / period;
        double procentStavka = 0;
        double sumPlatej = 0,sumOsnovnoyDolg = 0,sumProcentStavka = 0 , sumMothlyFee=0;
        for(int i=1;i<=period;i++){
            procentStavka = allamount * procent;
            platej = procentStavka + osnovnoyDolg;

            calendarik.add(Calendar.MONTH,1);
            if(i!=1)
                calendarikOld.add(Calendar.MONTH,1);
            double amountPayed = 0;
            for (ReckingCredit reckingCredit:creditDetials.getReckings()){
                if(calendarikOld.getTimeInMillis()<reckingCredit.getPayDate().getTimeInMillis()&&calendarik.getTimeInMillis()>reckingCredit.getPayDate().getTimeInMillis())
                {
                    amountPayed += reckingCredit.getAmount();
                }
            }

            allamount -= osnovnoyDolg;




            double monthlyFee = 0;
            if(creditDetials.getMonthly_fee_type() == 0){
                monthlyFee = creditDetials.getValue_of_credit() * creditDetials.getMonthly_fee()/100;
            }
            else {
                monthlyFee = allamount * creditDetials.getMonthly_fee()/100;

            }
            if(i==1) headerData.setMothlyPayment1(platej+monthlyFee);
                headerData.setMothlyPayment2(platej+monthlyFee);



            double raznitsa = 0;
            if((platej+monthlyFee) < amountPayed){
                allamount -= (amountPayed - (platej+monthlyFee));
                raznitsa=(amountPayed - (platej+monthlyFee));
            }


            sumMothlyFee+=monthlyFee;

            sumPlatej += platej;
            sumOsnovnoyDolg += osnovnoyDolg;
            sumProcentStavka += procentStavka;

            CreditsSchedule crSchedule = new CreditsSchedule();
            crSchedule.setPaymentSum(platej+monthlyFee);
            crSchedule.setPrincipal(osnovnoyDolg+raznitsa);
            crSchedule.setMonthlyCom(monthlyFee);
            crSchedule.setInterest(procentStavka);
            if(allamount <= 0.001){
                crSchedule.setBalance(0);
            } else
            crSchedule.setBalance(allamount);
            crSchedule.setDate((Calendar) calendarik.clone());
            crSchedule.setPayed(amountPayed);
            creditsSchedule.add(crSchedule);
            if(allamount <= 0.001){
                break;
            }
            if(allamount<=osnovnoyDolg){
                osnovnoyDolg = allamount;
            }
        }
        headerData.setBankFee(sumMothlyFee);
        headerData.setOverpaymentInterest(sumProcentStavka);
        headerData.setTotalLoanWithInterest(creditDetials.getValue_of_credit()+sumProcentStavka+sumMothlyFee);
        //TODO payedSystem
        double allPaid=0;
        for(ReckingCredit reckingCredit:creditDetials.getReckings()){
            allPaid+=reckingCredit.getAmount();
        }
        headerData.setTotalPayedAmount(allPaid);

        creditDetials.setValue_of_credit_with_procent(creditDetials.getValue_of_credit()+sumProcentStavka+sumMothlyFee);
        System.out.printf(decimalFormat.format(sumPlatej)+"         "+decimalFormat.format(sumOsnovnoyDolg) + "         "+decimalFormat.format(sumProcentStavka)+"         "+decimalFormat.format(allamount)+"\n");
        return headerData;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
