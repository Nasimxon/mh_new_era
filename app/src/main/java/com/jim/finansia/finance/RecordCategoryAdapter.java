package com.jim.finansia.finance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.finansia.R;
import com.jim.finansia.database.RootCategory;

import java.util.ArrayList;

@SuppressLint("ViewHolder")
public class RecordCategoryAdapter extends BaseAdapter {
	private ArrayList<RootCategory> result;
	private LayoutInflater inflater;
	private Context context;
	public RecordCategoryAdapter(Context context, ArrayList<RootCategory> result) {
	    this.result = result;
		this.context = context;
	    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		View view = inflater.inflate(R.layout.category_choose_list_item, parent, false);
		ImageView ivCategoryListItem = (ImageView) view.findViewById(R.id.ivCategoryListItem);
		TextView tvCategoryListItem = (TextView) view.findViewById(R.id.tvCategoryListItem);
		int resId = context.getResources().getIdentifier(result.get(position).getIcon(), "drawable", context.getPackageName());
		ivCategoryListItem.setImageResource(resId);
		tvCategoryListItem.setText(result.get(position).getName());
		return view;
	}
}
