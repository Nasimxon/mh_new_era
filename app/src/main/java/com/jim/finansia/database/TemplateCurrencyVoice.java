package com.jim.finansia.database;

/**
 * Created by root on 11/18/16.
 */

public class TemplateCurrencyVoice {

    private String regex;
    private String curId;
    private String curName;

    public TemplateCurrencyVoice(String regex, String curId, String curName) {
        this.regex = regex;
        this.curId = curId;
        this.curName = curName;
    }

    public TemplateCurrencyVoice() {
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getCurId() {
        return curId;
    }

    public void setCurId(String curId) {
        this.curId = curId;
    }

    public String getCurName() {
        return curName;
    }

    public void setCurName(String curName) {
        this.curName = curName;
    }
}
