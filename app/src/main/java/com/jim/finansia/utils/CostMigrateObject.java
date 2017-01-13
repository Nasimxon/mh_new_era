package com.jim.finansia.utils;

import java.util.Calendar;

public class CostMigrateObject {
    private String currencyId;
    private double amount;
    private Calendar day;
    public String getCurrencyId() {
        return currencyId;
    }
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public Calendar getDay() {
        return (Calendar)day.clone();
    }
    public void setDay(Calendar day) {
        this.day = (Calendar)day.clone();
    }
}
