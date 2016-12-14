package com.jim.pocketaccounter.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.FilterDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.reportfilter.IntervalPickDialog;
import com.jim.pocketaccounter.utils.reportfilter.IntervalPickerView;
import com.jim.pocketaccounter.utils.reportviews.CategorySliding;
import com.jim.pocketaccounter.utils.reportviews.CategorySlidingInterface;
import com.jim.pocketaccounter.utils.reportviews.ReportProgress;
import com.jim.pocketaccounter.utils.reportviews.SubcatData;
import com.jim.pocketaccounter.utils.reportviews.SubcatDetailedData;
import com.jim.pocketaccounter.utils.reportviews.SubcatReportView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("ValidFragment")
public class ReportByCategoryFragment extends Fragment {
    @Inject ReportManager reportManager;
    @Inject DataCache dataCache;
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject SharedPreferences preferences;
    @Inject DaoSession daoSession;
    private RecyclerView rvReportByCategorySubcatDetails, rvReportByCategorySubcatPercents;
    private List<String> allCategories; //allcategories = rootcategories + debtborrow + credit
    private List<SubcatData> subcatDatas; // subcategories data for selected id
    private CategorySliding csReportByCategory;
    private List<SubcatDetailedData> subcatDetailDatas;
    private IntervalPickDialog dialog;
    private Calendar begin, end;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_by_category_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.ic_filter);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        rvReportByCategorySubcatPercents = (RecyclerView) rootView.findViewById(R.id.rvReportByCategorySubcatPercents);
        rvReportByCategorySubcatPercents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvReportByCategorySubcatDetails = (RecyclerView) rootView.findViewById(R.id.rvReportByCategorySubcatDetails);
        rvReportByCategorySubcatDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        csReportByCategory = (CategorySliding) rootView.findViewById(R.id.csReportByCategory);
        csReportByCategory.setListener(new CategorySlidingInterface() {
            @Override
            public void onSlide(String id, Map<String, Integer> colorSet, int position) {
                if (subcatDatas != null) subcatDatas = null;
                prepareSubcatData(id, colorSet);
                replaceSubcats(id, colorSet);
                prepareSubcatDetailedData(id);
                replaceDetailedSubcats(id);
            }
        });
        dialog = new IntervalPickDialog(getContext());
        dialog.setListener(new IntervalPickerView.IntervalPickListener() {
            @Override
            public void onIntervalPick(Calendar begin, Calendar end) {
                ReportByCategoryFragment.this.begin = (Calendar) begin.clone();
                ReportByCategoryFragment.this.end = (Calendar) end.clone();
                csReportByCategory.setInterval(begin, end);
                dialog.dismiss();
            }

            @Override
            public void onCancelPick() {
                dialog.dismiss();
            }
        });

        return rootView;
    }

    private void prepareSubcatDetailedData(String id) {
        subcatDetailDatas = new ArrayList<>();
        RootCategory category = daoSession.load(RootCategory.class, id);
        if (category != null) {
            SubcatDetailedData data = new SubcatDetailedData();
            data.setUnit(commonOperations.getMainCurrency().getAbbr());
            data.setImage(category.getIcon());
            data.setTitle(category.getName());
            data.setId(category.getId());
            subcatDetailDatas.add(data);
            if (category.getSubCategories() != null || !category.getSubCategories().isEmpty()) {
                for (SubCategory subCategory : category.getSubCategories()) {
                    data = new SubcatDetailedData();
                    data.setUnit(commonOperations.getMainCurrency().getAbbr());
                    data.setImage(subCategory.getIcon());
                    data.setTitle(subCategory.getName());
                    data.setId(subCategory.getId());
                    subcatDetailDatas.add(data);
                }
            }
            float maxValue = 0.0f;
            List<FinanceRecord> tempRecords = daoSession
                    .queryBuilder(FinanceRecord.class)
                    .where(FinanceRecordDao.Properties.CategoryId.eq(id))
                    .list();
            List<FinanceRecord> records = new ArrayList<>();
            if (begin != null && end != null) {
                for (FinanceRecord record : tempRecords) {
                    if (record.getDate().compareTo(begin) >= 0 &&
                            record.getDate().compareTo(end) <= 0)
                        records.add(record);
                }
            }
            else
                records.addAll(tempRecords);
            for (FinanceRecord record : records) {
                if (commonOperations.getCost(record) >= maxValue)
                    maxValue = (float) commonOperations.getCost(record);
            }
            for (FinanceRecord record : records) {
                String tempId = record.getSubCategory() == null  ? record.getCategoryId() : record.getSubCategoryId();
                for (SubcatDetailedData temp : subcatDetailDatas) {
                    if (temp.getId().equals(tempId)) {
                        temp.setMaxValue(maxValue);
                        temp.setValue((float) commonOperations.getCost(record));
                    }
                }
            }
        }
    }

    private void replaceDetailedSubcats(final String id) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = ((Float) (valueAnimator.getAnimatedValue())).floatValue();
                rvReportByCategorySubcatDetails.setAlpha(value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                rvReportByCategorySubcatDetails.setAlpha(0.0f);
                prepareSubcatDetailedData(id);
                rvReportByCategorySubcatDetails.setAdapter(new PercentSubcategoryDetailAdapter(subcatDetailDatas));
                ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
                anim.setDuration(200);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        rvReportByCategorySubcatDetails.setAlpha(value);
                    }
                });
                anim.start();
            }
            @Override
            public void onAnimationCancel(Animator animator) { }
            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        animator.start();
    }

    private void prepareSubcatData(String id, Map<String, Integer> colors) {
        subcatDatas = new ArrayList<>();
        RootCategory category = daoSession.load(RootCategory.class, id);
        if (category != null) {
            SubcatData data = new SubcatData();
            data.setPercent(0.0f);
            data.setId("null");
            data.setColor(colors.get("null"));
            data.setText(category.getName());
            subcatDatas.add(data);
            List<SubCategory> subCategories = category.getSubCategories();
            for (int i = 0; i < subCategories.size(); i++) {
                data = new SubcatData();
                data.setPercent(0.0f);
                data.setId(subCategories.get(i).getId());
                data.setColor(colors.get(subCategories.get(i).getId()));
                data.setText(subCategories.get(i).getName());
                subcatDatas.add(data);
            }
        }
        List<FinanceRecord> tempRecords = daoSession
                .queryBuilder(FinanceRecord.class)
                .where(FinanceRecordDao.Properties.CategoryId.eq(id))
                .list();
        List<FinanceRecord> records = new ArrayList<>();
        if (begin != null && end != null) {
            for (FinanceRecord record : tempRecords) {
                if (record.getDate().compareTo(begin) >= 0 &&
                        record.getDate().compareTo(end) <= 0)
                    records.add(record);
            }
        }
        else
            records.addAll(tempRecords);
        if (!records.isEmpty()) {
            double total = 0.0d;
            for (FinanceRecord record : records)
                total += commonOperations.getCost(record);
            for (SubcatData subcatData : subcatDatas) {
                double subcatAmount = 0.0f;
                if (subcatData.getId().equals("null")) {
                    for (FinanceRecord record : records) {
                        if (record.getSubCategory() == null) {
                            subcatAmount += commonOperations.getCost(record);
                        }
                    }
                } else {
                    for (FinanceRecord record : records) {
                        if (record.getSubCategory() != null && subcatData.getId().equals(record.getSubCategoryId())) {
                            subcatAmount += commonOperations.getCost(record);
                        }
                    }
                }
                subcatData.setPercent((float) (100.0f * subcatAmount / total));
            }
        }
    }

    private void replaceSubcats(final String id, final Map<String, Integer> colors) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = ((Float) (valueAnimator.getAnimatedValue())).floatValue();
                rvReportByCategorySubcatPercents.setAlpha(value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                rvReportByCategorySubcatPercents.setAlpha(0.0f);
                prepareSubcatData(id, colors);
                rvReportByCategorySubcatPercents.setAdapter(new PercentSubcategoryAdapter(subcatDatas));
                ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
                anim.setDuration(200);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        rvReportByCategorySubcatPercents.setAlpha(value);
                    }
                });
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        for (int i = 0; i < rvReportByCategorySubcatPercents.getChildCount(); i++) {
                            View view = rvReportByCategorySubcatPercents.getChildAt(i);
                            if (view != null) {
                                SubcatReportView srvReportByCategory = (SubcatReportView) view.findViewById(R.id.srvReportByCategory);
                                srvReportByCategory.setDisabled(false, false);
                                srvReportByCategory.animateView();
                            }
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animator) { }
                    @Override
                    public void onAnimationRepeat(Animator animator) { }
                });
                anim.start();
            }
            @Override
            public void onAnimationCancel(Animator animator) { }
            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        animator.start();
    }

    //adapter percent subcategory
    private class PercentSubcategoryAdapter extends RecyclerView.Adapter<ReportByCategoryFragment.ViewHolder> {
        private List<SubcatData> result;
        private SubcatReportView srvReportByCategory;
        public PercentSubcategoryAdapter(List<SubcatData> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByCategoryFragment.ViewHolder view, final int position) {
            if (result.get(position) != null) {
                srvReportByCategory = view.srvReportByCategory;
                srvReportByCategory.setData(result.get(position));
                view.tvReportSubcatPercentsName.setText(result.get(position).getText());
            }
        }
        public ReportByCategoryFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_category_percent_subcats_item, parent, false);
            return new ReportByCategoryFragment.ViewHolder(view);
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        SubcatReportView srvReportByCategory;
        TextView tvReportSubcatPercentsName;
        View view;
        public ViewHolder(View view) {
            super(view);
            srvReportByCategory = (SubcatReportView) view.findViewById(R.id.srvReportByCategory);
            tvReportSubcatPercentsName = (TextView) view.findViewById(R.id.tvReportSubcatPercentsName);
            this.view = view;
        }
    }

    //adapter percent subcategory detail
    private class PercentSubcategoryDetailAdapter extends RecyclerView.Adapter<ReportByCategoryFragment.DetailedViewHolder> {
        private List<SubcatDetailedData> result;
        public PercentSubcategoryDetailAdapter(List<SubcatDetailedData> result) { this.result = result; }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByCategoryFragment.DetailedViewHolder view, final int position) {
            view.rpReport.setMaxValue(result.get(position).getMaxValue());
            view.rpReport.setValue(result.get(position).getValue());
            view.rpReport.setImage(result.get(position).getImage());
            view.rpReport.setUnit(result.get(position).getUnit());
            view.rpReport.setTitle(result.get(position).getTitle());
            view.rpReport.animateProgress();
        }
        public ReportByCategoryFragment.DetailedViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_category_detail_list_item, parent, false);
            return new ReportByCategoryFragment.DetailedViewHolder(view);
        }
    }
    public class DetailedViewHolder extends RecyclerView.ViewHolder {
        ReportProgress rpReport;
        View view;
        public DetailedViewHolder(View view) {
            super(view);
            rpReport = (ReportProgress) view.findViewById(R.id.rpReport);
            this.view = view;
        }
    }
}
