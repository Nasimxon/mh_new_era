package com.jim.finansia.database;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by root on 11/12/16.
 */
public class TemplateVoice {
    private String regex;
    private String CategoryId;
    private String subCatId;
    private String catName;
    private String subCatName;
    private int priority = 0;
    private Map<Integer, List<Integer>> pairs;

    public TemplateVoice () {
        pairs = new TreeMap<>();
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

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getSubCatName() {
        return subCatName;
    }

    public void setSubCatName(String subCatName) {
        this.subCatName = subCatName;
    }

    public Map<Integer, List<Integer>> getPairs() {
        return pairs;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getSubCatId() {
        return subCatId;
    }

    public void setSubCatId(String subCatId) {
        this.subCatId = subCatId;
    }
}
