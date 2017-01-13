package com.jim.finansia.fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.report.CategoryDataRow;
import com.jim.finansia.report.CategoryReportView;
import com.jim.finansia.report.ReportByCategoryDialogAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

public class ReportByCategoryIncomesFragment extends Fragment implements OnChartValueSelectedListener {
    @Inject
    CommonOperations commonOperations;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat format;
    @Inject
    SharedPreferences sharedPreferences;

    private LinearLayout llReportByCategory;
    private CategoryReportView categoryReportView;
    private Calendar begin, end;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.report_by_category_expanse, container, false);
        llReportByCategory = (LinearLayout) rootView.findViewById(R.id.llReportByCategory);
        init();
        categoryReportView = new CategoryReportView(getContext(), PocketAccounterGeneral.INCOME, begin, end);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryReportView.setLayoutParams(lp);
        categoryReportView.getPieChart().setOnChartValueSelectedListener(this);
        llReportByCategory.addView(categoryReportView);
        return rootView;
    }
    public void invalidate(Calendar begin , Calendar end) {
        ReportByCategoryIncomesFragment.this.begin = (Calendar) begin.clone();
        ReportByCategoryIncomesFragment.this.end = (Calendar) end.clone();
        categoryReportView.invalidate(begin, end);
    }
//    @Override
//    public void onValueSelected(final Entry e, int dataSetIndex, Highlight h) {
//        final ReportObject row = categoryReportView.getDatas().get(e.getXIndex());
//        final Dialog dialog=new Dialog(getActivity());
//        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.report_by_category_info, null);
//        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.report_by_category_info, null);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(dialogView);
//        TextView tvReportByCategoryRootCatName = (TextView) dialogView.findViewById(R.id.tvReportByCategoryRootCatName);
//        tvReportByCategoryRootCatName.setText(row.getDescription());
//        ImageView ivReportByCategoryRootCat = (ImageView) dialogView.findViewById(R.id.ivReportByCategoryRootCat);
//        int resId=getResources().getIdentifier("icons_9", "drawable", getContext().getPackageName());
////        if(row.getCategory().getIcon()!=null){
////            resId = getResources().getIdentifier(row.getCategory().getIcon(), "drawable", getContext().getPackageName());
////        }
//
////        ivReportByCategoryRootCat.setImageResource(resId);
//        ListView lvReportByCategoryInfo = (ListView) dialogView.findViewById(R.id.lvReportByCategoryInfo);
//        ImageView ivReportByCategoryClose = (ImageView) dialogView.findViewById(R.id.ivReportByCategoryClose);
//        ivReportByCategoryClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        TextView tvReportByCategoryPeriod = (TextView) dialogView.findViewById(R.id.tvReportByCategoryPeriod);
//        Calendar begin = categoryReportView.getBeginTime();
//        Calendar end = categoryReportView.getEndTime();
////        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//        String text = format.format(begin.getTime())+" - "+format.format(end.getTime());
//        tvReportByCategoryPeriod.setText(text);
////        if (row.getSubCats().size() == 1 && row.getSubCats().get(0).getSubCategory().getId().matches(getResources().getString(R.string.no_category)))
////            lvReportByCategoryInfo.setVisibility(View.GONE);
////        else {
////            ReportByCategoryDialogAdapter adapter = new ReportByCategoryDialogAdapter(getContext(), row.getSubCats());
////            lvReportByCategoryInfo.setAdapter(adapter);
////        }
//        TextView tvReportByCategoryInfoTotal = (TextView) dialogView.findViewById(R.id.tvReportByCategoryInfoTotal);
//        DecimalFormat decimalFormat = new DecimalFormat("0.00##");
//        tvReportByCategoryInfoTotal.setText(decimalFormat.format(row.getAmount())+ commonOperations.getMainCurrency().getAbbr());
//        TextView tvReportByCategoryInfoAverage = (TextView) dialogView.findViewById(R.id.tvReportByCategoryInfoAverage);
//        int countOfDays = 0;
//        Calendar beg = (Calendar) begin.clone();
//        while(beg.compareTo(end) <= 0) {
//            countOfDays++;
//            beg.add(Calendar.DAY_OF_MONTH, 1);
//        }
//        double average = row.getAmount()/countOfDays;
//        tvReportByCategoryInfoAverage.setText(decimalFormat.format(average)+commonOperations.getMainCurrency().getAbbr());
//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        int width = dm.widthPixels;
//        dialog.getWindow().setLayout(7*width/8, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.show();
//    }
    private void init() {
        String setting = sharedPreferences.getString("report_filter", "0");
        begin = Calendar.getInstance();
        end = Calendar.getInstance();
        switch (setting) {
            case "0":
                begin.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "1":
                begin.add(Calendar.DAY_OF_MONTH, -2);
                break;
            case "2":
                begin.add(Calendar.DAY_OF_MONTH, -6);
                break;
        }
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        final CategoryDataRow row = categoryReportView.getDatas().get(e.getXIndex());
        final Dialog dialog=new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.report_by_category_info, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        TextView tvReportByCategoryRootCatName = (TextView) dialogView.findViewById(R.id.tvReportByCategoryRootCatName);
        tvReportByCategoryRootCatName.setText(row.getCategory().getName());
        ImageView ivReportByCategoryRootCat = (ImageView) dialogView.findViewById(R.id.ivReportByCategoryRootCat);
        int resId=getResources().getIdentifier("icons_9", "drawable", getContext().getPackageName());
        if(row.getCategory().getIcon()!=null){
            resId = getResources().getIdentifier(row.getCategory().getIcon(), "drawable", getContext().getPackageName());
        }
        ivReportByCategoryRootCat.setImageResource(resId);
        ListView lvReportByCategoryInfo = (ListView) dialogView.findViewById(R.id.lvReportByCategoryInfo);
        ImageView ivReportByCategoryClose = (ImageView) dialogView.findViewById(R.id.ivReportByCategoryClose);
        ivReportByCategoryClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView tvReportByCategoryPeriod = (TextView) dialogView.findViewById(R.id.tvReportByCategoryPeriod);
        Calendar begin = categoryReportView.getBeginTime();
        Calendar end = categoryReportView.getEndTime();
        String text = format.format(begin.getTime())+" - "+format.format(end.getTime());
        tvReportByCategoryPeriod.setText(text);
        if (row.getSubCats().size() == 1 && row.getSubCats().get(0).getSubCategory().getId().matches(getResources().getString(R.string.no_category)))
            lvReportByCategoryInfo.setVisibility(View.GONE);
        else {
            ReportByCategoryDialogAdapter adapter = new ReportByCategoryDialogAdapter(getContext(), row.getSubCats());
            lvReportByCategoryInfo.setAdapter(adapter);
        }
        TextView tvReportByCategoryInfoTotal = (TextView) dialogView.findViewById(R.id.tvReportByCategoryInfoTotal);
        DecimalFormat decimalFormat = new DecimalFormat("0.00##");
        tvReportByCategoryInfoTotal.setText(decimalFormat.format(row.getTotalAmount())
                + commonOperations.getMainCurrency().getAbbr());
        TextView tvReportByCategoryInfoAverage = (TextView) dialogView.findViewById(R.id.tvReportByCategoryInfoAverage);
        Long countOfDays = commonOperations.betweenDays(begin, end);
        double average = row.getTotalAmount()/countOfDays;
        tvReportByCategoryInfoAverage.setText(decimalFormat.format(average)+commonOperations.getMainCurrency().getAbbr());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(7*width/8, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void onNothingSelected() {}
}
