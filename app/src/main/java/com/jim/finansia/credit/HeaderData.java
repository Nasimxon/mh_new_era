package com.jim.finansia.credit;

/**
 * Created by developer on 02.12.2016.
 */

public class HeaderData {
    private int creditType;
    private double mothlyPayment1;
    private double mothlyPayment2;
    private double overpaymentInterest;
    private double bankFee;
    private double totalLoanWithInterest;
    private double totalPayedAmount;
    public HeaderData(){

    }
    public HeaderData(int creditType, double mothlyPayment1, double mothlyPayment2, double overpaymentInterest, double bankFee, double totalLoanWithInterest, double totalPayedAmount) {
        this.creditType = creditType;
        this.mothlyPayment1 = mothlyPayment1;
        this.mothlyPayment2 = mothlyPayment2;
        this.overpaymentInterest = overpaymentInterest;
        this.bankFee = bankFee;
        this.totalLoanWithInterest = totalLoanWithInterest;
        this.totalPayedAmount = totalPayedAmount;
    }

    public int getCreditType() {
        return creditType;
    }

    public void setCreditType(int creditType) {
        this.creditType = creditType;
    }

    public double getMothlyPayment1() {
        return mothlyPayment1;
    }

    public void setMothlyPayment1(double mothlyPayment1) {
        this.mothlyPayment1 = mothlyPayment1;
    }

    public double getMothlyPayment2() {
        return mothlyPayment2;
    }

    public void setMothlyPayment2(double mothlyPayment2) {
        this.mothlyPayment2 = mothlyPayment2;
    }

    public double getOverpaymentInterest() {
        return overpaymentInterest;
    }

    public void setOverpaymentInterest(double overpaymentInterest) {
        this.overpaymentInterest = overpaymentInterest;
    }

    public double getBankFee() {
        return bankFee;
    }

    public void setBankFee(double bankFee) {
        this.bankFee = bankFee;
    }

    public double getTotalLoanWithInterest() {
        return totalLoanWithInterest;
    }

    public void setTotalLoanWithInterest(double totalLoanWithInterest) {
        this.totalLoanWithInterest = totalLoanWithInterest;
    }

    public double getTotalPayedAmount() {
        return totalPayedAmount;
    }

    public void setTotalPayedAmount(double totalPayedAmount) {
        this.totalPayedAmount = totalPayedAmount;
    }
}
