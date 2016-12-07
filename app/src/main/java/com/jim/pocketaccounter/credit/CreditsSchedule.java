package com.jim.pocketaccounter.credit;

import com.jim.pocketaccounter.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;

import java.util.Calendar;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by root on 11/25/16.
 */
public class CreditsSchedule {
    private String id;
    private String parId;
    private Calendar date;
    private double paymentSum;
    private double interest;
    private double principal;
    private double monthlyCom;
    private double balance;
    private double payed;
    public CreditsSchedule(){}
    public CreditsSchedule(String id, String parId, Calendar date,
            double paymentSum, double interest, double principal,
            double monthlyCom, double balance) {
        this.id = id;
        this.parId = parId;
        this.date = date;
        this.paymentSum = paymentSum;
        this.interest = interest;
        this.principal = principal;
        this.monthlyCom = monthlyCom;
        this.balance = balance;
    }

    public double getPayed() {
        return payed;
    }

    public void setPayed(double payed) {
        this.payed = payed;
    }

    public double getLeftResidue() {
        return this.balance;
    }
    public void setLeftResidue(double leftResidue) {
        this.balance = leftResidue;
    }
    public double getMonthlyCom() {
        return this.monthlyCom;
    }
    public void setMonthlyCom(double monthlyCom) {
        this.monthlyCom = monthlyCom;
    }
    public double getProcentAmount() {
        return this.principal;
    }
    public void setProcentAmount(double procentAmount) {
        this.principal = procentAmount;
    }
    public double getMainDebt() {
        return this.interest;
    }
    public void setMainDebt(double mainDebt) {
        this.interest = mainDebt;
    }
    public double getPaymentSum() {
        return this.paymentSum;
    }
    public void setPaymentSum(double paymentSum) {
        this.paymentSum = paymentSum;
    }
    public Calendar getDate() {
        return this.date;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    public String getParId() {
        return this.parId;
    }
    public void setParId(String parId) {
        this.parId = parId;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public double getBalance() {
        return this.balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public double getPrincipal() {
        return this.principal;
    }
    public void setPrincipal(double principal) {
        this.principal = principal;
    }
    public double getInterest() {
        return this.interest;
    }
    public void setInterest(double interest) {
        this.interest = interest;
    }
    
}
