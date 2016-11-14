package com.jim.pocketaccounter.database;

/**
 * Created by root on 11/12/16.
 */
public class TemplateVoice {
    private String regex;
    private String CategoryId;
    public TemplateVoice () {
    }
    public TemplateVoice(String regex, String CategoryId) {
        this.regex = regex;
        this.CategoryId = CategoryId;
    }
    public String getCategoryId() {
        return this.CategoryId;
    }
    public void setCategoryId(String CategoryId) {
        this.CategoryId = CategoryId;
    }
    public String getRegex() {
        return this.regex;
    }
    public void setRegex(String regex) {
        this.regex = regex;
    }
}
