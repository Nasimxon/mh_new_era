package com.jim.finansia.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.CommonOperations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

@SuppressLint("ViewHolder")
public class ReportByIncomeExpanseDialogAdapter extends BaseAdapter {
    @Inject
    CommonOperations commonOperations;
    private ArrayList<IncomeExpanseDayDetails> result;
    private LayoutInflater inflater;
    private Calendar date;

    public ReportByIncomeExpanseDialogAdapter(Context context, ArrayList<IncomeExpanseDayDetails> result, Calendar date) {
        this.result = result;
        this.date = (Calendar) date.clone();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return result.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.report_by_category_list_item, parent, false);
        TextView tvReportByCategoryListSubCatName = (TextView) view.findViewById(R.id.tvReportByCategoryListSubCatName);
        String text = "- " + result.get(position).getCategory().getName();
        if (result.get(position).getSubCategory() != null)
            text = text + ", " + result.get(position).getSubCategory().getName();
        tvReportByCategoryListSubCatName.setText(text);
        TextView tvReportByCategoryListSubCatAmount = (TextView) view.findViewById(R.id.tvReportByCategoryListSubCatAmount);
        DecimalFormat format = new DecimalFormat("0.00##");
        tvReportByCategoryListSubCatAmount.setText(format.format(commonOperations.getCost(date, result.get(position).getCurrency(), result.get(position).getAmount())) + commonOperations.getMainCurrency().getAbbr());
        return view;
    }
}
