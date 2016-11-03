package com.jim.pocketaccounter.finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.StyleSetter;
import com.jim.pocketaccounter.utils.Styleable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CurrencyAdapter extends BaseAdapter {
	private List<Currency> result;
	private LayoutInflater inflater;
	private int mode;
	private boolean[] selected;
	@Styleable(colorLayer = PocketAccounterGeneral.HELPER_COLOR)
	private ImageView ivCurrencyMain;
	@Inject LogicManager manager;
	@Inject CommonOperations commonOperations;
	@Inject SharedPreferences preferences;
	public CurrencyAdapter(Context context, List<Currency> result, boolean[] selected, int mode) {
	    this.result = result;
	    this.selected = selected;
	    this.mode = mode;
		((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
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
		View view = inflater.inflate(R.layout.currency_list_item, parent, false);
		TextView tvCurrencyItemAbbr = (TextView) view.findViewById(R.id.tvCurrencyItemAbbr);
		tvCurrencyItemAbbr.setText(result.get(position).getAbbr());
		TextView tvCurrencyName = (TextView) view.findViewById(R.id.tvCurrencyName);
		tvCurrencyName.setText(result.get(position).getName());
		ivCurrencyMain = (ImageView) view.findViewById(R.id.ivCurrencyMain);
		if (result.get(position).getMain()) {
			ivCurrencyMain.setImageResource(R.drawable.main_currency);
			view.findViewById(R.id.tvCurrencyCost).setVisibility(View.GONE);
		}
		else {
			ivCurrencyMain.setImageResource(R.drawable.not_main_currency);
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			DecimalFormat decFormat = new DecimalFormat("0.00##");
			TextView tvCurrencyCost = (TextView) view.findViewById(R.id.tvCurrencyCost);
			tvCurrencyCost.setText(format.format(result.get(position).getCosts().get(result.get(position).getCosts().size()-1).getDay().getTime())+"  "+"1"+result.get(position).getAbbr()+
					": "+decFormat.format(result.get(position).getCosts().get(result.get(position).getCosts().size()-1).getCost())+commonOperations.getMainCurrency().getAbbr());
		}
		if (mode == PocketAccounterGeneral.EDIT_MODE) {
			ivCurrencyMain.setVisibility(View.GONE);
			CheckBox chbCurrencyEdit = (CheckBox)view.findViewById(R.id.chbCurrencyEdit);
			chbCurrencyEdit.setVisibility(View.VISIBLE);
			chbCurrencyEdit.setChecked(selected[position]);
			chbCurrencyEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					selected[position] = isChecked;
				}
			});
		}
		new StyleSetter(this, preferences).set();
		return view;
	}
}
