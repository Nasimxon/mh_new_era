package com.jim.pocketaccounter.finance;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.utils.CurrencyChecbox;
import com.jim.pocketaccounter.database.DaoSession;

import java.util.ArrayList;

import javax.inject.Inject;

public class CurrencyChooseAdapter extends RecyclerView.Adapter<CurrencyChooseAdapter.myViewHolder> {
	private ArrayList<Currency> result;
	private LayoutInflater inflater;
	private boolean[] chbs;
	@Inject
	DaoSession daoSession;

	public CurrencyChooseAdapter(Context context, ArrayList<Currency> result, boolean[] chbs) {
	    this.result = result;
		this.chbs = chbs;
		((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.currency_choose_item, parent, false);
		myViewHolder myViewHolder= new myViewHolder(view);
		return myViewHolder;
	}

	@Override
	public void onBindViewHolder(myViewHolder holder,final int position) {
		holder.tvChooseAbbr.setText(result.get(position).getAbbr());

		for (Currency currency : daoSession.getCurrencyDao().loadAll()) {
			if (result.get(position).getId().matches(currency.getId())) {
				holder.chbChoose.setChecked(true);
				break;
			}
		}
		holder.chbChoose.setChecked(chbs[position]);
		holder.chbChoose.setOnCheckListener(new CurrencyChecbox.OnCheckListener() {

			@Override
			public void onCheck(boolean isChecked) {
				chbs[position] = isChecked;
			}
		});
	}

	@Override
	public int getItemCount() {
		return result.size();
	}


	public static class myViewHolder extends RecyclerView.ViewHolder {
		TextView tvChooseAbbr;
		CurrencyChecbox chbChoose;
		public myViewHolder(View view) {
			super(view);
			tvChooseAbbr = (TextView) view.findViewById(R.id.tvCurrencyChooseSign);
			chbChoose = (CurrencyChecbox) view.findViewById(R.id.chbCurrencyChoose);
		}
	}


}
