package com.jim.finansia.report;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class DecFormat implements ValueFormatter {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//        DecimalFormat format = new DecimalFormat("0.00##");
//        String text = format.format(entry.getVal()) + PocketAccounter.financeManager.getMainCurrency().getAbbr();
//        return text;
        return "";
    }
}
