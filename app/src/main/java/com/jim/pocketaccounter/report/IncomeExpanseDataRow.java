package com.jim.pocketaccounter.report;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

public class IncomeExpanseDataRow {
    @Inject
    CommonOperations commonOperations;
    private Calendar date;
    private ArrayList<IncomeExpanseDayDetails> details;
    private double totalIncome;
    private double totalExpanse;
    private double totalProfit;
    public Calendar getDate() {
        return date;
    }
    public void setDate(Calendar date) {
        this.date = (Calendar) date.clone();
    }
    public ArrayList<IncomeExpanseDayDetails> getDetails() {
        return details;
    }
    public void setDetails(ArrayList<IncomeExpanseDayDetails> details) {
        this.details = details;
    }
    public double getTotalIncome() {
        return totalIncome;
    }
    public double getTotalExpanse() {
        return totalExpanse;
    }
    public double getTotalProfit() {
        return totalProfit;
    }
    public IncomeExpanseDataRow(Calendar date) {
        this.date = (Calendar) date.clone();
    }
    public void calculate(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        totalIncome = 0;
        totalExpanse = 0;
        totalProfit = 0;
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i).getCategory().getType() == PocketAccounterGeneral.INCOME) {
                totalIncome = totalIncome + commonOperations.getCost(date, details.get(i).getCurrency(), commonOperations.getMainCurrency(), details.get(i).getAmount());
            } else {
                totalExpanse = totalExpanse + commonOperations.getCost(date, details.get(i).getCurrency(), commonOperations.getMainCurrency(), details.get(i).getAmount());
            }
        }
        totalProfit = totalIncome - totalExpanse;
    }
}
