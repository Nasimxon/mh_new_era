package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.utils.reportviews.PieData;
import com.jim.pocketaccounter.utils.reportviews.ReportPieView;

import java.util.ArrayList;
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
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;

    public ReportByCategoryRootCategoryFragment(String id, Map<String, Integer> colors) {
        this.id = id;
        this.colors = colors;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_by_category_rootcategory_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        rpvReport = (ReportPieView) rootView.findViewById(R.id.rpvReport);
        tvReportByCategoryRootCatName = (TextView) rootView.findViewById(R.id.tvReportByCategoryRootCatName);
        tvReportByCategoryRootCatSum = (TextView) rootView.findViewById(R.id.tvReportByCategoryRootCatSum);
        init();
        visualize();
        rpvReport.setDisabled(false, false);
        rpvReport.animatePie();
        return rootView;
    }

    public Map<String, Integer> getColors() { return colors; }

    void init() {
        if (id != null || !id.isEmpty()) {
            catDatas = new ArrayList<>();
            double total = 0.0d;
            RootCategory category = daoSession.load(RootCategory.class, id);
            if (category != null) {
                tvReportByCategoryRootCatName.setText(category.getName());
            }
            List<FinanceRecord> records = daoSession
                    .queryBuilder(FinanceRecord.class)
                    .where(FinanceRecordDao.Properties.CategoryId.eq(id))
                    .list();
            if (!records.isEmpty()) {
                for (FinanceRecord record : records) {
                    String subcatId = record.getSubCategory() == null ? "null" : record.getSubCategoryId();
                    PieData data = new PieData();
                    data.setColor(colors.get(subcatId));
                    total += commonOperations.getCost(record);
                    data.setAmount(commonOperations.getCost(record));
                    catDatas.add(data);
                }
                tvReportByCategoryRootCatSum.setText(getResources().getString(R.string.balance) + ": " + total);
                return;
            }
            try {
                CreditDetials credit = daoSession.load(CreditDetials.class, Long.parseLong(id));
                if (credit != null) {
                    for (ReckingCredit recking : credit.getReckings()) {
                        PieData data = new PieData();
                        data.setColor(colors.get(id));
                        total += commonOperations.getCost(recking.getPayDate(),
                                credit.getValyute_currency(),
                                recking.getAmount());
                        data.setAmount(commonOperations.getCost(recking.getPayDate(),
                                credit.getValyute_currency(),
                                recking.getAmount()));

                        catDatas.add(data);
                    }
                    tvReportByCategoryRootCatName.setText(credit.getCredit_name());
                    tvReportByCategoryRootCatSum.setText(getResources().getString(R.string.balance) + ": " + total);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            DebtBorrow debtBorrow = daoSession.load(DebtBorrow.class, id);
            if (debtBorrow != null) {
                for (Recking recking : debtBorrow.getReckings()) {
                    PieData data = new PieData();
                    data.setColor(colors.get(id));
                    total += commonOperations.getCost(recking.getPayDate(),
                            debtBorrow.getCurrency(),
                            recking.getAmount());
                    data.setAmount(commonOperations.getCost(recking.getPayDate(),
                            debtBorrow.getCurrency(),
                            recking.getAmount()));
                    catDatas.add(data);
                }
                tvReportByCategoryRootCatName.setText(debtBorrow.getPerson().getName());
            }
            tvReportByCategoryRootCatSum.setText(getResources().getString(R.string.balance) + ": " + total);
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
        if (selectedId != null && !selectedId.isEmpty()) {
            RootCategory category = daoSession.load(RootCategory.class, selectedId);
            if (category != null) {
                String icon = category.getIcon();
                int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                return BitmapFactory.decodeResource(getResources(), resId);
            }
            CreditDetials creditDetials = daoSession.load(CreditDetials.class, selectedId);
            if (creditDetials != null) {
                String icon = creditDetials.getIcon_ID();
                int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                return BitmapFactory.decodeResource(getResources(), resId);
            }
            DebtBorrow debtBorrow = daoSession.load(DebtBorrow.class, selectedId);
            if (debtBorrow != null) {
                int resId = getResources().getIdentifier("no_category", "drawable", getContext().getPackageName());
                return BitmapFactory.decodeResource(getResources(), resId);
            }
        }
        return null;
    }
}