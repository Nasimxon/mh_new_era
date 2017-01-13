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
import java.util.List;

import javax.inject.Inject;



@SuppressLint("ViewHolder")
public class ReportByCategoryDialogAdapter extends BaseAdapter {
	private List<SubCategoryWitAmount> result;
	private LayoutInflater inflater;
	@Inject
	CommonOperations commonOperations;
	public ReportByCategoryDialogAdapter(Context context, List<SubCategoryWitAmount> result) {
	    this.result = result;
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
		tvReportByCategoryListSubCatName.setText("- "+result.get(position).getSubCategory().getName());
		TextView tvReportByCategoryListSubCatAmount = (TextView) view.findViewById(R.id.tvReportByCategoryListSubCatAmount);
		DecimalFormat format = new DecimalFormat("0.00##");
		tvReportByCategoryListSubCatAmount.setText(format.format(result.get(position).getAmount()) +
				commonOperations.getMainCurrency().getAbbr());
		return view;
	}
}
