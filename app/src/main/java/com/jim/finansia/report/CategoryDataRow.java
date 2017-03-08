package com.jim.finansia.report;

import com.jim.finansia.database.RootCategory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CategoryDataRow {
    private RootCategory category;
    private double totalAmount;
    private Calendar date;
    private List<SubCategoryWitAmount> subCats = new ArrayList<SubCategoryWitAmount>();
    public RootCategory getCategory() {return category;}
    public void setCategory(RootCategory category) {this.category = category;}
    public double getTotalAmount() {return totalAmount;}
    public void setTotalAmount(double totalAmount) {this.totalAmount = totalAmount;}
    public List<SubCategoryWitAmount> getSubCats() {return subCats;}
    public void setSubCats(List<SubCategoryWitAmount> subCats) {this.subCats = subCats;}
    public Calendar getDate() {return date;}
    public void setDate(Calendar date) {this.date = date;}
}