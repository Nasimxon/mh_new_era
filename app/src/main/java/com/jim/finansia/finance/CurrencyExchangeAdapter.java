package com.jim.finansia.finance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.CurrencyCost;
import com.jim.finansia.fragments.CurrencyEditFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

@SuppressLint("ViewHolder")
public class CurrencyExchangeAdapter extends RecyclerView.Adapter<CurrencyExchangeAdapter.myViewHolder> {
	private ArrayList<CurrencyCost> result;
	private int mode;
	private boolean[] selected;
	private String abbr;
	@Inject
	LogicManager logicManager;
	@Inject
	CommonOperations commonOperations;
	CurrencyEditFragment.OpenDialog openDialog;
	public CurrencyExchangeAdapter(CurrencyEditFragment.OpenDialog openDialog,Context context, ArrayList<CurrencyCost> result, boolean[] selected, int mode, String abbr) {
	    this.result = result;
	    this.mode = mode;
	    this.selected = selected;
		this.abbr = abbr;
		this.openDialog = openDialog;
		Collections.sort(result, new Comparator<CurrencyCost>() {
			@Override
			public int compare(CurrencyCost lhs, CurrencyCost rhs) {
				return lhs.getDay().compareTo(rhs.getDay())*(-1);
			}
		});
		((PocketAccounter) context).component((PocketAccounterApplication)context.getApplicationContext()).inject(this);
	  }


	@Override
	public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.curr_exchange_list_item, parent, false);
		CurrencyExchangeAdapter.myViewHolder vh = new CurrencyExchangeAdapter.myViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(final myViewHolder holder, final int position) {
		DecimalFormat decFormat = new DecimalFormat("0.00##");

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		String text = dateFormat.format(result.get(position).getDay().getTime());
		holder.tvCurrencyExchangeListItem.setText(text);
		holder.tvCourseValyute.setText( "1"+ abbr+" = " + decFormat.format(result.get(position).getCost())+commonOperations.getMainCurrency().getAbbr());
		holder.chbCurrencyExchangeListItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				selected[position] = isChecked;

			}
		});
		holder.mainRootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openDialog.openDialogForDate(result.get(position));
			}
		});
		if (mode == PocketAccounterGeneral.EDIT_MODE) {
			holder.chbCurrencyExchangeListItem.setVisibility(View.VISIBLE);
			holder.chbCurrencyExchangeListItem.setChecked(selected[position]);
		}
		else
			holder.chbCurrencyExchangeListItem.setVisibility(View.GONE);
	}

		@Override
	public int getItemCount() {
		return result.size();
	}









	public static class myViewHolder extends RecyclerView.ViewHolder {
		TextView tvCurrencyExchangeListItem;
		TextView tvCourseValyute;
		LinearLayout mainRootView;
		CheckBox chbCurrencyExchangeListItem;
		public myViewHolder(View view) {
			super(view);
			 tvCurrencyExchangeListItem = (TextView) view.findViewById(R.id.tvCurrencyExchangeListItem);
			tvCourseValyute = (TextView) view.findViewById(R.id.tvCourseValyute);
			mainRootView = (LinearLayout) view.findViewById(R.id.mainRootView);
			 chbCurrencyExchangeListItem = (CheckBox) view.findViewById(R.id.chbCurrencyExchangeListItem);
		}
	}
}
