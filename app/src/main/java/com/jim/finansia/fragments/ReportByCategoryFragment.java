package com.jim.finansia.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
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
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.CategoryDataRow;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.utils.reportfilter.IntervalPickDialog;
import com.jim.finansia.utils.reportfilter.IntervalPickerView;
import com.jim.finansia.utils.reportviews.CategorySliding;
import com.jim.finansia.utils.reportviews.CategorySlidingInterface;
import com.jim.finansia.utils.reportviews.LineChartView;
import com.jim.finansia.utils.reportviews.SubcatData;
import com.jim.finansia.utils.reportviews.SubcatDetailedData;
import com.jim.finansia.utils.reportviews.SubcatReportView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

public class ReportByCategoryFragment extends Fragment {
    @Inject ReportManager reportManager;
    @Inject DataCache dataCache;
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject SharedPreferences preferences;
    @Inject DaoSession daoSession;
    @Inject DecimalFormat formatter;
    @Inject FinansiaFirebaseAnalytics analytics;
    private RecyclerView rvReportByCategorySubcatPercents;
    private List<String> allCategories; //allcategories = rootcategories + debtborrow + credit
    private List<SubcatData> subcatDatas; // subcategories data for selected id
    private CategorySliding csReportByCategory;
    private List<SubcatDetailedData> subcatDetailDatas;
    private IntervalPickDialog dialog;
    private Calendar begin, end;
    private LinearLayout llPickDate, llCategories, llInfo, llTotal;
    private TextView tvBeginDate;
    private TextView tvEndDate;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private DecimalFormat decimalFormat = new DecimalFormat("0.##");
    private ValueAnimator animator;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_by_category_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters" + getClass().getName());

        llCategories = (LinearLayout) rootView.findViewById(R.id.llCategories);
        llInfo = (LinearLayout) rootView.findViewById(R.id.rlInfo);
        llTotal = (LinearLayout) rootView.findViewById(R.id.llTotal);
        llPickDate = (LinearLayout) rootView.findViewById(R.id.llPickDate);
        llPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.getWindow().setLayout(9 * getContext().getResources().getDisplayMetrics().widthPixels / 10, RelativeLayout.LayoutParams.WRAP_CONTENT);
                dialog.show();
            }
        });
        tvBeginDate = (TextView) rootView.findViewById(R.id.tvBeginDate);
        tvEndDate = (TextView) rootView.findViewById(R.id.tvEndDate);

        rvReportByCategorySubcatPercents = (RecyclerView) rootView.findViewById(R.id.rvReportByCategorySubcatPercents);
        rvReportByCategorySubcatPercents.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false));
        csReportByCategory = (CategorySliding) rootView.findViewById(R.id.csReportByCategory);
        csReportByCategory.setListener(new CategorySlidingInterface() {
            @Override
            public void onSlide(String id, Map<String, Integer> colorSet, int position, boolean isActive) {
                if (!isActive) {
                    if (subcatDatas != null) subcatDatas = null;
                    replaceSubcats(id, colorSet);
                } else {
                    if (animator != null && (animator.isStarted() || animator.isRunning())) {
                        animator.cancel();
                    }
                }
            }
        });
        dialog = new IntervalPickDialog(getContext());
        dialog.setListener(new IntervalPickerView.IntervalPickListener() {
            @Override
            public void onIntervalPick(Calendar begin, Calendar end) {
                ReportByCategoryFragment.this.begin = (Calendar) begin.clone();
                ReportByCategoryFragment.this.end = (Calendar) end.clone();
                csReportByCategory.setInterval(begin, end);
                tvBeginDate.setText(getString(R.string.c_interval)+": "+sDateFormat.format(begin.getTime()));
                tvEndDate.setText(getString(R.string.do_interval)+": "+sDateFormat.format(end.getTime()));
                dialog.dismiss();
            }

            @Override
            public void onCancelPick() {
                dialog.dismiss();
            }
        });

        return rootView;
    }

    private void prepareSubcatData(String id, Map<String, Integer> colors) {
        subcatDatas = new ArrayList<>();
        RootCategory category = daoSession.load(RootCategory.class, id);
        if (category != null) {
            SubcatData data = new SubcatData();
            data.setPercent(0.0f);
            data.setId("null");
            data.setColor(colors.get("null"));
            data.setIcon(category.getIcon());
            data.setText(category.getName());
            data.setAmounts(new ArrayList<Double>());
            subcatDatas.add(data);
            List<SubCategory> subCategories = category.getSubCategories();
            for (int i = 0; i < subCategories.size(); i++) {
                SubcatData newData = new SubcatData();
                newData.setPercent(0.0f);
                newData.setId(subCategories.get(i).getId());
                newData.setColor(colors.get(subCategories.get(i).getId()));
                newData.setText(subCategories.get(i).getName());
                newData.setIcon(subCategories.get(i).getIcon());
                newData.setAmounts(new ArrayList<Double>());
                subcatDatas.add(newData);
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
            float minLength = 15.0f;
            Calendar beg;
            if (begin != null && end != null) {
                beg = (Calendar) begin.clone();

            } else {
                begin = Calendar.getInstance();
                end = Calendar.getInstance();
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 59);
                if (!records.isEmpty()) {
                    for (FinanceRecord record : records) {
                        if (record.getDate().compareTo(begin) <= 0)
                            begin = (Calendar) record.getDate().clone();
                    }
                }
                else {
                    begin.add(Calendar.MONTH, -1);
                }
                tvBeginDate.setText(getString(R.string.c_interval)+':'+sDateFormat.format(begin.getTime()));
                tvEndDate.setText(getString(R.string.do_interval)+':'+sDateFormat.format(end.getTime()));
                beg = (Calendar) begin.clone();
            }

            float betweenDays = ((float)(end.getTimeInMillis() - begin.getTimeInMillis()))/(1000.0f*60.0f*60.0f*24.0f);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            double amount = 0.0d;
            while (beg.compareTo(end) <= 0) {
                for (SubcatData subcatData : subcatDatas) {
                    for (FinanceRecord record : records) {
                        if (subcatData.getId().equals("null")) {
                            if (format.format(record.getDate().getTime()).equals(format.format(beg.getTime())) &&
                                    record.getCategoryId().equals(category.getId()) &&
                                    record.getSubCategory() == null) {
                                amount += commonOperations.getCost(record);
                            }
                        } else {
                            if (format.format(record.getDate().getTime()).equals(format.format(beg.getTime())) &&
                                    record.getSubCategory() != null && record.getSubCategoryId().equals(subcatData.getId())) {
                                amount += commonOperations.getCost(record);
                            }
                        }
                    }
                    subcatData.getAmounts().add(amount);
                    amount = 0.0d;
                }
                beg.add(Calendar.DAY_OF_MONTH, 1);
            }
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
    }

    private void replaceSubcats(final String id, final Map<String, Integer> colors) {
        animator = ValueAnimator.ofFloat(1.0f, 0.0f);
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
                Collections.sort(subcatDatas, new Comparator<SubcatData>() {
                    @Override
                    public int compare(SubcatData subcatData, SubcatData t1) {
                        if(subcatData.getPercent() - t1.getPercent()<0)  return -1;
                        else if(subcatData.getPercent() - t1.getPercent()>0)   return 1;
                        return 0;
                    }
                });
                Collections.reverse(subcatDatas);
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
                anim.start();
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        animator.start();
    }
    public void onResume() {
        super.onResume();
            if (toolbarManager != null)
            {
                toolbarManager.setOnTitleClickListener(null);
                toolbarManager.setSubtitle("");
                toolbarManager.setSubtitleIconVisibility(View.GONE);
                toolbarManager.setTitle(getResources().getString(R.string.categories_report));
                toolbarManager.setToolbarSwitchVisibilty(View.VISIBLE);
                toolbarManager.setOnSwitchCheckedChangedListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b)
                        {
                            llCategories.setVisibility(View.GONE);
                            llInfo.setVisibility(View.GONE);
                            llTotal.setVisibility(View.VISIBLE);
                        } else {
                            llCategories.setVisibility(View.VISIBLE);
                            llInfo.setVisibility(View.VISIBLE);
                            llTotal.setVisibility(View.GONE);
                        }
                    }
                });
        }
    }
    public void onDetach() {
        super.onDetach();
        toolbarManager.setToolbarSwitchVisibilty(View.GONE);
        toolbarManager.setToolbarSwitchChecked(false);
    }
    //adapter percent subcategory
    private class PercentSubcategoryAdapter extends RecyclerView.Adapter<ReportByCategoryFragment.ViewHolder> {
        private List<SubcatData> result;
        public PercentSubcategoryAdapter(List<SubcatData> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ReportByCategoryFragment.ViewHolder view, final int position) {
            if (result.get(position) != null) {
                view.srvReportByCategory.setData(result.get(position));
                view.tvReportSubcatPercentsName.setText(result.get(position).getText());
                view.ivReportByCategorySubcat.setImageResource(getResources()
                        .getIdentifier(result.get(position).getIcon(), "drawable", getContext().getPackageName()));
                double total = 0.0d;
                for (Double amount : result.get(position).getAmounts())
                    total += amount;
                view.tvReportByCategoryInterval.setText(formatter.format(total)+commonOperations.getMainCurrency().getAbbr());
                view.lchvReportByCategoryItem.setData(result.get(position).getAmounts());
            }
        }
        public ReportByCategoryFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_by_category_percent_subcats_item, parent, false);
            return new ReportByCategoryFragment.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SubcatReportView srvReportByCategory;
        TextView tvReportSubcatPercentsName, tvReportByCategoryInterval;
        LineChartView lchvReportByCategoryItem;
        ImageView ivReportByCategorySubcat;
        View view;
        public ViewHolder(View view) {
            super(view);
            srvReportByCategory = (SubcatReportView) view.findViewById(R.id.srvReportByCategory);
            tvReportSubcatPercentsName = (TextView) view.findViewById(R.id.tvReportSubcatPercentsName);
            tvReportByCategoryInterval = (TextView) view.findViewById(R.id.tvReportByCategoryInterval);
            lchvReportByCategoryItem = (LineChartView) view.findViewById(R.id.lchvReportByCategoryItem);
            ivReportByCategorySubcat = (ImageView) view.findViewById(R.id.ivReportByCategorySubcat);
            this.view = view;
        }
    }
}
