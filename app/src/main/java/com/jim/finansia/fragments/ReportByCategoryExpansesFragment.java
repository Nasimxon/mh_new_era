package com.jim.finansia.fragments;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.CategoryDataRow;
import com.jim.finansia.report.CategoryReportView;
import com.jim.finansia.report.ReportByCategoryDialogAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

public class ReportByCategoryExpansesFragment extends Fragment implements OnChartValueSelectedListener {
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat format;
    @Inject
    CommonOperations commonOperations;
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
        init();
        llReportByCategory = (LinearLayout) rootView.findViewById(R.id.llReportByCategory);
        categoryReportView = new CategoryReportView(getContext(), PocketAccounterGeneral.EXPENSE, begin, end);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryReportView.setLayoutParams(lp);
        categoryReportView.getPieChart().setOnChartValueSelectedListener(this);
        llReportByCategory.addView(categoryReportView);
        return rootView;
    }
    public void invalidate(Calendar begin, Calendar end) {
        this.begin = (Calendar) begin.clone();
        this.end = (Calendar) end.clone();
        categoryReportView.invalidate(begin, end);
    }
//    @Override
//    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
//        final ReportObject row = categoryReportView.getDatas().get(e.getXIndex());
//        final Dialog dialog=new Dialog(getActivity());
//        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.report_by_category_info, null);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(dialogView);
//        TextView tvReportByCategoryRootCatName = (TextView) dialogView.findViewById(R.id.tvReportByCategoryRootCatName);
////        tvReportByCategoryRootCatName.setText(row.getCategory().getName());
//        tvReportByCategoryRootCatName.setText(row.getDescription());
//        ImageView ivReportByCategoryRootCat = (ImageView) dialogView.findViewById(R.id.ivReportByCategoryRootCat);
//        int resId=getResources().getIdentifier("icons_9", "drawable", getContext().getPackageName());
////        if(row.getCategory().getIcon()!=null){
////            resId = getResources().getIdentifier(row.getCategory().getIcon(), "drawable", getContext().getPackageName());
////        }
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
//        Calendar begin = (Calendar)categoryReportView.getBeginTime().clone();
//        Calendar end = (Calendar)categoryReportView.getEndTime().clone();
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
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int setting = sharedPreferences.getInt("filter_pos", 0);
        begin = Calendar.getInstance();
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        switch (setting) {
            case 0:
                begin.set(Calendar.DAY_OF_MONTH, 1);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                break;
            case 1:
                begin.add(Calendar.DAY_OF_MONTH, -2);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                break;
            case 2:
                begin.add(Calendar.DAY_OF_MONTH, -6);
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                break;
            case 3:
            case 4:
            case 5:
                Long begTime = sharedPreferences.getLong("filter_begin_time", 0L);
                Long endTime = sharedPreferences.getLong("filter_end_time", 0L);
                begin.setTimeInMillis(begTime);
                end.setTimeInMillis(endTime);
                break;
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataIndex, Highlight h) {
        final CategoryDataRow row = categoryReportView.getDatas().get(e.getXIndex());
        final Dialog dialog=new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.report_by_category_info, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        TextView tvReportByCategoryRootCatName = (TextView) dialogView.findViewById(R.id.tvReportByCategoryRootCatName);
        tvReportByCategoryRootCatName.setText(row.getCategory().getName());
//        tvReportByCategoryRootCatName.setText(row.getCategory().getName());
        Log.d("datas_"," "+row.getCategory().getName());
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
        Calendar begin = (Calendar)categoryReportView.getBeginTime().clone();
        Calendar end = (Calendar)categoryReportView.getEndTime().clone();
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
        tvReportByCategoryInfoTotal.setText(decimalFormat.format(row.getTotalAmount())+ commonOperations.getMainCurrency().getAbbr());
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
    public void onNothingSelected() {
    }
}
