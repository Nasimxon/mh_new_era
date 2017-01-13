package com.jim.finansia.report;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;


public class CategoryReportView extends LinearLayout {
    @Inject
    ReportManager reportManager;

    private PieChart pieChart;
    private int type;
    private ArrayList<CategoryDataRow> datas;
    private Calendar begin, end;
    public CategoryReportView(Context context, int type, Calendar begin, Calendar end) {
        super(context);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        this.type = type;
        this.begin = begin;
        this.end = end;
        pieChart = new PieChart(context);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(55f);
        pieChart.setCenterTextSize(16f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setDrawCenterText(true);
        pieChart.setNoDataText(getResources().getString(R.string.diagram_no_data_text));
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        pieChart.setDrawSliceText(false);
        invalidate(begin, end);
        Legend l = pieChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(7f);
        l.setYOffset(15f);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pieChart.setLayoutParams(lp);
        addView(pieChart);
    }
    public Calendar getBeginTime() {return begin;}
    public Calendar getEndTime() {return end;}
    public PieChart getPieChart() {return pieChart;}
    public CategoryReportView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CategoryReportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CategoryReportView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public List<CategoryDataRow> getDatas() {return datas;}
    public void invalidate(Calendar begin, Calendar end) {
        this.begin = (Calendar) begin.clone();
        this.end = (Calendar) end.clone();
        datas = new ArrayList<>();
        if (type == PocketAccounterGeneral.INCOME) {
            pieChart.setCenterText(getResources().getString(R.string.income));
        }
        else {
            pieChart.setCenterText(getResources().getString(R.string.expanse));
        }
        List<CategoryDataRow> temp = reportManager.getReportByCategories(begin, end);
        for (CategoryDataRow categoryDataRow : temp) {
            if (categoryDataRow.getCategory().getType() == type) {
                datas.add(categoryDataRow);
            }
        }
        drawReport(datas);
    }
    public void drawReport(ArrayList<CategoryDataRow> datas) {
        List<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < datas.size(); i++) {
            yVals.add(new Entry((float) datas.get(i).getTotalAmount(), i));
        }
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++)
            xVals.add(datas.get(i).getCategory().getName());
        PieDataSet dataSet = new PieDataSet(yVals, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(ContextCompat.getColor(getContext(), R.color.toolbar_text_color));
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}
