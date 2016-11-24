package com.jim.pocketaccounter.utils.regex;

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

    private String anyErrorChar = ".*[/^*~&%@!+()$#-\\/'\\{`\\];\\[:].*";

    public RegexBuilder beginsWithWord(String beginWord) {
        String partRegex = "";
        if (beginWord.matches(anyErrorChar)) {
            partRegex = "^(";
            String keyPart = "\\b";
            for (Character c : beginWord.toCharArray()) {
                if (("" + c).matches(anyErrorChar)) {
                    if (!keyPart.equals("\\b")) {
                        partRegex = partRegex + keyPart;
                        keyPart = "\\b";
                    }
                    String s = "" + (char) 92 + c;
                    partRegex = partRegex + "[" + s + "]";
                } else {
                    keyPart = keyPart + c;
                }
            }
            if (!keyPart.equals("\\b")) {
                partRegex += keyPart;
            }
            partRegex += ")";
            template += partRegex;
        } else
            template += "^(\\b" + beginWord + ")";
        numberOfGroups++;
        return this;
    }

    public RegexBuilder beginsWithSequence(String seq) {
        template += "^(" + seq + ")";
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
        String partRegex = "";
        if (word.matches(anyErrorChar)) {
            partRegex = "(";
            String keyPart = "\\b";
            for (Character c : word.toCharArray()) {
                if (("" + c).matches(anyErrorChar)) {
                    if (!keyPart.equals("\\b")) {
                        partRegex = partRegex + keyPart;
                        keyPart = "\\b";
                    }
                    String s = "" + (char) 92 + c;
                    partRegex = partRegex + "[" + s + "]";
                } else {
                    keyPart = keyPart + c;
                }
            }
            if (!keyPart.equals("\\b")) {
                partRegex += keyPart;
            }
            partRegex += ")";
            template += partRegex;
        } else
            template += "(\\b" + word + ")";
        return this;
    }

    public RegexBuilder defineSequence(String seq) {
        template += "(" + seq + ")";
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

    public RegexBuilder abcentable() {
        template += "*";
        return this;
    }

    public RegexBuilder openBlock() {
        template += "[";
        return this;
    }

    public RegexBuilder closeBlock() {
        template += "]";
        return this;
    }

    public RegexBuilder anyErrorChars() {
        template += "[/^*~&%@!+()$#-\\/'\\\\{}`;:]";
        return this;
    }

    public String build() {
        return template;
    }

    public int getNumberOfGroups() {
        return numberOfGroups;
    }
}
