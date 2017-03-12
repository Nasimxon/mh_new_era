package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.reportviews.PieData;
import com.jim.finansia.utils.reportviews.ReportPieView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@SuppressLint("ValidFragment")
public class ReportByCategoryRootCategoryFragment extends Fragment {
    private ReportPieView rpvReport;
    private String id;
    private List<PieData> catDatas; // data for rootcategory viewpager adapter
    private TextView tvReportByCategoryRootCatSum, tvReportByCategoryRootCatName;
    private Map<String, Integer> colors;
    private Calendar begin, end;
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;
    @Inject DecimalFormat formatter;
    @Inject FinansiaFirebaseAnalytics analytics;
    public static String ID = "stringid";

    public ReportByCategoryRootCategoryFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_by_category_rootcategory_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if(getArguments()!=null){
            Bundle bundle = getArguments();
            id= bundle.getString(ID);
            String valueKey = "keyvalue";
            String keys = "keys";
            int count = 0;
            colors = new HashMap<>();
            while (true){
                if(bundle.getString(keys+count)!=null){
                    colors.put(bundle.getString(keys+count),bundle.getInt(valueKey+count));
                    count++;
                }
                else break;
            }
        }
        analytics.sendText("User enters "+getClass().getName());
        rpvReport = (ReportPieView) rootView.findViewById(R.id.rpvReport);
        tvReportByCategoryRootCatName = (TextView) rootView.findViewById(R.id.tvReportByCategoryRootCatName);
        tvReportByCategoryRootCatSum = (TextView) rootView.findViewById(R.id.tvReportByCategoryRootCatSum);
        init();
        visualize();
        rpvReport.setDisabled(false, false);
        return rootView;
    }

    public void setInterval(Calendar begin, Calendar end) {
        this.begin = begin == null ? null : (Calendar) begin.clone();
        this.end = end == null ? null : (Calendar) end.clone();
    }
    public Map<String, Integer> getColors() { return colors; }

    void init() {
        if ((id != null || !id.isEmpty()) && daoSession != null) {
            catDatas = new ArrayList<>();
            final RootCategory category = daoSession.load(RootCategory.class, id);
            if (category != null) {
                tvReportByCategoryRootCatName.setText(category.getName());
            }
            double total = 0.0d;
            List<FinanceRecord> tempRecords = daoSession
                    .queryBuilder(FinanceRecord.class)
                    .where(FinanceRecordDao.Properties.CategoryId.eq(id))
                    .list();
            List<FinanceRecord> records = new ArrayList<>();
            if (begin != null && end != null) {
                for (FinanceRecord record : tempRecords) {
                    if (record.getDate().compareTo(begin) >= 0 &&
                            record.getDate().compareTo(end) <= 0 ) {
                        records.add(record);
                    }
                }
            } else {
                records.addAll(tempRecords);
            }
            if (!records.isEmpty()) {
                double nullAmount = 0.0d;
                for (FinanceRecord record : records) {
                    if (record.getSubCategory() == null) {
                        nullAmount += commonOperations.getCost(record);
                    }
                }
                PieData data = new PieData();
                data.setColor(colors.get("null"));
                data.setAmount(nullAmount);
                catDatas.add(data);
                List<SubCategory> subCategories = category.getSubCategories();
                if (subCategories != null) {
                    for (SubCategory subCategory : subCategories) {
                        double subcatAmount = 0.0d;
                        for (FinanceRecord record : records) {
                            if (record.getSubCategory() != null && record.getSubCategoryId().equals(subCategory.getId())) {
                                subcatAmount += commonOperations.getCost(record);
                            }

                        }
                        if (subcatAmount != 0) {
                            PieData newData = new PieData();
                            newData.setColor(colors.get(subCategory.getId()));
                            newData.setAmount(subcatAmount);
                            catDatas.add(newData);
                        }
                    }
                }
                for (FinanceRecord record : records) {
                    total += commonOperations.getCost(record);
                }
                DecimalFormat format = new DecimalFormat("0.##");
                if (category.getType() == PocketAccounterGeneral.EXPENSE)
                    tvReportByCategoryRootCatSum.setText(getString(R.string.total_expense) + ": " + formatter.format(total) +commonOperations.getMainCurrency().getAbbr());
                if (category.getType() == PocketAccounterGeneral.INCOME)
                    tvReportByCategoryRootCatSum.setText(getString(R.string.total_income) + ": " + formatter.format(total)+commonOperations.getMainCurrency().getAbbr());

            }
        }
    }

    public String getCagetoryId() {
        return id;
    }

    void visualize() {
        if (catDatas != null)
            rpvReport.setDatas(catDatas);
        Bitmap centerBitmap = getCenterBitmap(id);
        if (centerBitmap != null) rpvReport.setCenterBitmap(centerBitmap);
    }
    private Bitmap getCenterBitmap(String selectedId) {
        if (selectedId != null && !selectedId.isEmpty() && daoSession != null) {
            RootCategory category = daoSession.load(RootCategory.class, selectedId);
            if (category != null) {
                String icon = category.getIcon();
                int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                return BitmapFactory.decodeResource(getResources(), resId);
            }
        }
        return null;
    }
}