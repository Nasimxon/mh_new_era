package com.jim.pocketaccounter.database;

import com.jim.pocketaccounter.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;

import java.util.Calendar;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by root on 11/25/16.
 */
@Entity
public class CreditsSchedule {
    @Property
    private String id;
    @Property
    private String parId;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar date;
    @Property
    private double paymentSum;
    @Property
    private double mainDebt;
    @Property
    private double procentAmount;
    @Property
    private double monthlyCom;
    @Property
    private double leftResidue;
    public double getLeftResidue() {
        return this.leftResidue;
    }
    public void setLeftResidue(double leftResidue) {
        this.leftResidue = leftResidue;
    }
    public double getMonthlyCom() {
        return this.monthlyCom;
    }
    public void setMonthlyCom(double monthlyCom) {
        this.monthlyCom = monthlyCom;
    }
    public double getProcentAmount() {
        return this.procentAmount;
    }
    public void setProcentAmount(double procentAmount) {
        this.procentAmount = procentAmount;
    }
    public double getMainDebt() {
        return this.mainDebt;
    }
    public void setMainDebt(double mainDebt) {
        this.mainDebt = mainDebt;
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
    @Generated(hash = 950199802)
    public CreditsSchedule(String id, String parId, Calendar date,
            double paymentSum, double mainDebt, double procentAmount,
            double monthlyCom, double leftResidue) {
        this.id = id;
        this.parId = parId;
        this.date = date;
        this.paymentSum = paymentSum;
        this.mainDebt = mainDebt;
        this.procentAmount = procentAmount;
        this.monthlyCom = monthlyCom;
        this.leftResidue = leftResidue;
    }
    @Generated(hash = 886918860)
    public CreditsSchedule() {
    }
}
