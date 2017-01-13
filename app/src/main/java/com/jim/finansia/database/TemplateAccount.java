package com.jim.finansia.database;

/**
 * Created by root on 11/16/16.
 */

public class TemplateAccount {
    private String regex;
    private String accountId;
    private String accountName;

    public TemplateAccount() {
    }

    public TemplateAccount(String regex, String accountId) {
        this.regex = regex;
        this.accountId = accountId;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
