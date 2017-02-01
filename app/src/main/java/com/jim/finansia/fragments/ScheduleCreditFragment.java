package com.jim.finansia.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.AdapterForSchedule;
import com.jim.finansia.credit.HeaderData;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.credit.CreditsSchedule;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ScheduleCreditFragment extends PABaseFragment {


    RecyclerView recyclerView;
    AdapterForSchedule adapterForSchedule;
    CreditDetials currentCredit;
    ArrayList<Object> creditsSchedule;
    int modeFromMain = PocketAccounterGeneral.NO_MODE;
    boolean isEdit = false;
    boolean fromAdding = false;
    int posFromMain;

    final static String FROM_ADDING = "from_adding";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_credit, container, false);

        if(getArguments()!=null){
            modeFromMain = getArguments().getInt(CreditTabLay.MODE);
            posFromMain = getArguments().getInt(CreditTabLay.POSITION);
            if(getArguments().getBoolean(FROM_ADDING,false)){
                fromAdding = true;
                Bundle bundle = getArguments();
                isEdit = bundle.getBoolean(AddCreditFragment.FROM_EDIT);
                CreditDetials creditDetials = new CreditDetials();
                creditDetials.setMyCredit_id(bundle.getLong(AddCreditFragment.CREDIT_ID));
                creditDetials.setIcon_ID(bundle.getString(AddCreditFragment.ICON_ID));
                creditDetials.setCredit_name(bundle.getString(AddCreditFragment.CREDIT_NAME));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(bundle.getLong(AddCreditFragment.TAKE_TIME));
                creditDetials.setTake_time(cal);
                creditDetials.setProcent(bundle.getDouble(AddCreditFragment.PROCENT));
                creditDetials.setProcent_interval(bundle.getDouble(AddCreditFragment.PROCENT_INTERVAL));
                creditDetials.setPeriod_time(bundle.getLong(AddCreditFragment.PERIOD_TIME));
                creditDetials.setValue_of_credit(bundle.getDouble(AddCreditFragment.VALUE_OF_CREDIT));
                creditDetials.setCurrencyId(bundle.getString(AddCreditFragment.CURRENCY_ID));
                creditDetials.setValue_of_credit_with_procent(bundle.getDouble(AddCreditFragment.VALUE_OF_CREDIT_WITH_PROCENT));
                creditDetials.setPeriod_time_tip(bundle.getLong(AddCreditFragment.PERIOD_TIME_TIP));
                creditDetials.setKey_for_include(bundle.getBoolean(AddCreditFragment.KEY_FOR_INCLUDE));
                creditDetials.setAccountID(bundle.getString(AddCreditFragment.ACCOUNT_ID));
                creditDetials.setMonthly_fee(bundle.getDouble(AddCreditFragment.MONTHLY_FEE));
                creditDetials.setType_loan(bundle.getInt(AddCreditFragment.TYPE_LOAN));
                creditDetials.setMonthly_fee_type(bundle.getInt(AddCreditFragment.MONTHLY_FEE_TYPE));
                creditDetials.setPervonacalniy(bundle.getDouble(AddCreditFragment.PERVONACALNIY));
                creditDetials.__setDaoSession(daoSession);
                currentCredit = creditDetials;
            }
            else {
                currentCredit = daoSession.load(CreditDetials.class,getArguments().getLong(CreditTabLay.CREDIT_ID));
            }
        }
        recyclerView = (RecyclerView) view.findViewById(R.id.scheduleMain);
        creditsSchedule = new ArrayList<>();

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

                    if (isEdit&&modeFromMain==PocketAccounterGeneral.NO_MODE) {

                        if(!daoSession.getBoardButtonDao().queryBuilder()
                                .where(BoardButtonDao.Properties.CategoryId.eq(Long.toString(currentCredit.getMyCredit_id())))
                                .list().isEmpty()) {

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap temp = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(currentCredit.getIcon_ID(), "drawable", getContext().getPackageName()), options);
                            temp = Bitmap.createScaledBitmap(temp, (int) getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), true);
                            dataCache.getBoardBitmapsCache().put(daoSession.getBoardButtonDao().queryBuilder()
                                    .where(BoardButtonDao.Properties.CategoryId.eq(Long.toString(currentCredit.getMyCredit_id())))
                                    .list().get(0).getId(), temp);
                            reportManager.clearCache();
                            dataCache.updateAllPercents();
                            paFragmentManager.updateAllFragmentsOnViewPager();
                        }
                        toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
                        paFragmentManager.displayFragment(new CreditTabLay());


                    } else if (modeFromMain != PocketAccounterGeneral.NO_MODE && modeFromMain !=PocketAccounterGeneral.DETAIL && modeFromMain!= PocketAccounterGeneral.SEARCH_MODE) {
                        Log.d("testttt", "fromMainWindow");
                        if(isEdit) {
                            List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                            for (BoardButton boardButton : boardButtons) {
                                if (boardButton.getCategoryId() != null) {
                                    if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {

                                        if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE) {
                                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), Long.toString(currentCredit.getMyCredit_id()));
                                        } else {
                                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), Long.toString(currentCredit.getMyCredit_id()));
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            if (modeFromMain == PocketAccounterGeneral.EXPANSE_MODE)
                                logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, posFromMain, Long.toString(currentCredit.getMyCredit_id()));
                            else
                                logicManager.changeBoardButton(PocketAccounterGeneral.INCOME,posFromMain,Long.toString(currentCredit.getMyCredit_id()));

                        }
                        BitmapFactory.Options options=new BitmapFactory.Options();
                        options.inPreferredConfig= Bitmap.Config.RGB_565;
                        Bitmap temp=BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(currentCredit.getIcon_ID(),"drawable",getContext().getPackageName()),options);
                        temp=Bitmap.createScaledBitmap(temp,(int)getResources().getDimension(R.dimen.thirty_dp),(int)getResources().getDimension(R.dimen.thirty_dp),true);

                        List<BoardButton> boardButtonss=daoSession.getBoardButtonDao().queryBuilder().where(BoardButtonDao.Properties.CategoryId.eq(Long.toString(currentCredit.getMyCredit_id()))).build().list();
                        if(!boardButtonss.isEmpty()){
                            for(BoardButton boardButton:boardButtonss){
                                dataCache.getBoardBitmapsCache().put(boardButton.getId(), temp);
                            }
                        }
                        reportManager.clearCache();
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
                        paFragmentManager.displayMainWindow();
                    }
                    else {
                        toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
                        paFragmentManager.getFragmentManager().popBackStack();
                    }
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
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            if(fromAdding){
                toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.VISIBLE);
                toolbarManager.setImageToSecondImage(R.drawable.ic_save_black_48dp);
                toolbarManager.setSubtitle("");
                toolbarManager.setSubtitleIconVisibility(View.GONE);
            } else {
                toolbarManager.setSubtitle("");
                toolbarManager.setSubtitleIconVisibility(View.GONE);
                toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
            }
        }
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
            crSchedule.setDate(calendarik);
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
                if(calendarikOld.getTimeInMillis()<=reckingCredit.getPayDate().getTimeInMillis()&&calendarik.getTimeInMillis()>=reckingCredit.getPayDate().getTimeInMillis())
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
