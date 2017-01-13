package com.jim.finansia.finance;

import com.jim.finansia.database.Account;
import com.jim.finansia.database.CurrencyAmount;

import java.util.ArrayList;

public class AccountManagementObject {
    private Account account;
    private ArrayList<CurrencyAmount> curAmounts;
    public void setAccount(Account account) {
        this.account = account;
    }
    public Account getAccount() {
        return account;
    }
    public void setCurAmounts(ArrayList<CurrencyAmount> curAmounts) {
        this.curAmounts = curAmounts;
    }
    public ArrayList<CurrencyAmount> getCurAmounts() {
        return curAmounts;
    }
}
