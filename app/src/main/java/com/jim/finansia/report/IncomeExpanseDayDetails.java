package com.jim.finansia.report;

import com.jim.finansia.database.Currency;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;

import java.util.Calendar;

public class IncomeExpanseDayDetails {
    private Calendar date;
    private int type;
    private RootCategory category;
    private SubCategory subCategory;
    private double amount;
    private Currency currency;
    public Calendar getDate() {
        return date;
    }
    public void setDate(Calendar date) {
        this.date = (Calendar) date.clone();
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public Currency getCurrency() {return currency;}
    public void setCurrency(Currency currency) {this.currency = currency;}
    public RootCategory getCategory() {return category;}
    public void setCategory(RootCategory category) {this.category = category;}
    public SubCategory getSubCategory() {return subCategory;}
    public void setSubCategory(SubCategory subCategory) {this.subCategory = subCategory;}
    public double getAmount() {return amount;}
    public void setAmount(double amount) {this.amount = amount;}
}
