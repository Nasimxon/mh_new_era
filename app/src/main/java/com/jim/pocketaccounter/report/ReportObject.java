package com.jim.pocketaccounter.report;

import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.Currency;

import java.util.Calendar;

public class ReportObject {
    private int type;
    private String description;
    private double amount;
    private Account account;
    private Currency currency;
    private Calendar date;

    public Calendar getDate() {
        return date;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
    public Currency getCurrency() {
        return currency;
    }
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
