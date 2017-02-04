package com.jim.finansia.finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.fragments.CurrencyEditFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
				CommonOperations.buttonClickCustomAnimation(0.95f,holder.llCurrencyListItemRoot, new CommonOperations.AfterAnimationEnd() {
					@Override
					public void onAnimoationEnd() {
						if (mode == PocketAccounterGeneral.EDIT_MODE) {

							holder.chbCurrencyEdit.setChecked(!holder.chbCurrencyEdit.isChecked());
							selected[position] = holder.chbCurrencyEdit.isChecked();
						} else {
							if (daoSession.getCurrencyDao().loadAll().get(position).getMain()) {
								Toast.makeText(context, context.getResources().getString(R.string.main_currency_edit), Toast.LENGTH_SHORT).show();
								return;
							}
							paFragmentManager
									.displayFragment(CurrencyEditFragment.newInstance(daoSession.getCurrencyDao().loadAll().get(position)));
						}
					}
				});


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
					" = "+decFormat.format(result.get(position).getCosts().get(result.get(position).getCosts().size()-1).getCost())+commonOperations.getMainCurrency().getAbbr());
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
		setAnimation(holder.llCurrencyListItemRoot,position);
	}
	int lastPosition = -1;
	private void setAnimation(View viewToAnimate, int position)
	{
		// If the bound view wasn't previously displayed on screen, it's animated
		if (position > lastPosition)
		{
			Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
			viewToAnimate.startAnimation(animation);
			lastPosition = position;
		}
	}

	@Override
	public void onViewDetachedFromWindow(final CurrencyAdapter.myViewHolder holder)
	{
		((myViewHolder)holder).clearAnimation();
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
		public void clearAnimation()
		{
			llCurrencyListItemRoot.clearAnimation();
		}
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
