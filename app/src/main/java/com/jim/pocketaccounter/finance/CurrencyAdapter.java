package com.jim.pocketaccounter.finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.fragments.CurrencyEditFragment;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.myViewHolder> {
	private List<Currency> result;
	private int mode;
	private boolean[] selected;
	private Context context;
	@Inject LogicManager manager;
	@Inject CommonOperations commonOperations;
	@Inject SharedPreferences preferences;
	@Inject PAFragmentManager paFragmentManager;
	@Inject DaoSession daoSession;
	public CurrencyAdapter(Context context, List<Currency> result, boolean[] selected, int mode) {
	    this.result = result;
	    this.selected = selected;
	    this.mode = mode;
		this.context = context;
		((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);

	}


	@Override
	public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.currency_list_item, parent, false);
		myViewHolder vh = new myViewHolder(v);

		return vh;
	}

	@Override
	public void onBindViewHolder( final myViewHolder holder, final int position) {
		holder.tvCurrencyItemAbbr.setText(result.get(position).getAbbr());
		holder.tvCurrencyName.setText(result.get(position).getName());
		holder.llCurrencyListItemRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mode == PocketAccounterGeneral.EDIT_MODE) {

					holder.chbCurrencyEdit.setChecked(!holder.chbCurrencyEdit.isChecked());
					selected[position] = holder.chbCurrencyEdit.isChecked();
				} else {
					if (daoSession.getCurrencyDao().loadAll().get(position).getMain()) {
						Toast.makeText(context, context.getResources().getString(R.string.main_currency_edit), Toast.LENGTH_SHORT).show();
						return;
					}
					paFragmentManager.displayFragment(new CurrencyEditFragment(daoSession.getCurrencyDao().loadAll().get(position)));
				}
			}
		});

		if (result.get(position).getMain()) {
			holder.ivCurrencyMain.setImageResource(R.drawable.main_currency_vertical);
			holder.tvCurrencyCost.setText(R.string.main_curr);
		}
		else {
			holder.ivCurrencyMain.setImageResource(R.drawable.not_main_currency_ver);
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			DecimalFormat decFormat = new DecimalFormat("0.00##");

			holder.tvCurrencyCost.setText("1"+result.get(position).getAbbr()+
					": "+decFormat.format(result.get(position).getCosts().get(result.get(position).getCosts().size()-1).getCost())+commonOperations.getMainCurrency().getAbbr());
		}
		if (mode == PocketAccounterGeneral.EDIT_MODE) {
			holder.ivCurrencyMain.setVisibility(View.GONE);

			holder.chbCurrencyEdit.setVisibility(View.VISIBLE);
			holder.chbCurrencyEdit.setChecked(selected[position]);
			holder.chbCurrencyEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					selected[position] = isChecked;
				}
			});
		}
	}


	@Override
	public int getItemCount() {
		return result.size();
	}


	public static class myViewHolder extends RecyclerView.ViewHolder {
		TextView tvCurrencyItemAbbr;
		TextView tvCurrencyName;
		ImageView ivCurrencyMain;
		LinearLayout llCurrencyListItemRoot;
		TextView tvCurrencyCost;
		CheckBox chbCurrencyEdit;
		public myViewHolder(View view) {
			super(view);
			tvCurrencyItemAbbr = (TextView) view.findViewById(R.id.tvCurrencyItemAbbr);
			tvCurrencyName = (TextView) view.findViewById(R.id.tvCurrencyName);
			ivCurrencyMain = (ImageView) view.findViewById(R.id.ivCurrencyMain);
			llCurrencyListItemRoot = (LinearLayout) view.findViewById(R.id.llCurrencyListItemRoot);
			tvCurrencyCost = (TextView) view.findViewById(R.id.tvCurrencyCost);
			chbCurrencyEdit = (CheckBox)view.findViewById(R.id.chbCurrencyEdit);
			}
	}


}
