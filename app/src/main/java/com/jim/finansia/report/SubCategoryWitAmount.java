package com.jim.finansia.report;

import com.jim.finansia.database.SubCategory;

public class SubCategoryWitAmount {
    private SubCategory subCategory;
    private double amount;
    public SubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(SubCategory subCategory) { this.subCategory = subCategory;}
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
