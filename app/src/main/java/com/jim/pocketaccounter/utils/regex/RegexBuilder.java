package com.jim.pocketaccounter.utils.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 10/16/16.
 */

public class RegexBuilder {
    private String template = "";
    private int numberOfGroups = 0;
    public RegexBuilder builder() {
        template = "";
        return this;
    }
    public RegexBuilder beginsWithWord(String beginWord) {
        template += "^(\\b"+beginWord+")";
        numberOfGroups++;
        return this;
    }
    public RegexBuilder beginsWithSequence(String seq) {
        template += "^("+seq+")";
        numberOfGroups++;
        return this;
    }
    public RegexBuilder openGroup() {
        template += "(";
        numberOfGroups++;
        return this;
    }
    public RegexBuilder closeGroup() {
        template += ")";
        return this;
    }
    public RegexBuilder anyVisibleCharSeq() {
        template += ".*";
        return this;
    }
    public RegexBuilder anyWhitespaceSeq() {
        template += "\\s*";
        return this;
    }

    public RegexBuilder defineWord(String word) {
        template += "(\\b"+word+")";
        return this;
    }

    public RegexBuilder defineSequence(String seq) {
        template += "("+seq+")";
        numberOfGroups++;
        return this;
    }

    public RegexBuilder mustContainAChar(char c) {
        template += c;
        return this;
    }

    public RegexBuilder defineNumber() {
        template += "([0-9]+[.,]?[0-9]*)";
        numberOfGroups++;
        return this;
    }

    public RegexBuilder or() {
        template += "|";
        return this;
    }

    public RegexBuilder and() {
        template += "&";
        return this;
    }

    public RegexBuilder addRegexPattern(String pattern) {
        template += pattern;
        return this;
    }

    public RegexBuilder abcentable () {
        template += "*";
        return this;
    }
    public String build() {
        return template;
    }

    public int getNumberOfGroups() {
        return numberOfGroups;
    }
}
