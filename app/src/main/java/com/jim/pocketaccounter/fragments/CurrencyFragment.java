package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.finance.CurrencyAdapter;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.utils.FloatingActionButton;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.ScrollDirectionListener;
import com.jim.pocketaccounter.utils.StyleSetter;
import com.jim.pocketaccounter.utils.Styleable;
import com.jim.pocketaccounter.utils.WarningDialog;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("InflateParams")
public class CurrencyFragment extends PABaseListFragment implements OnClickListener, OnItemClickListener {
	@Styleable(colorLayer = PocketAccounterGeneral.HELPER_COLOR)
	private FloatingActionButton fabCurrencyAdd;
	private ListView lvCurrency;
	private int mode = PocketAccounterGeneral.NORMAL_MODE;
	private boolean[] selected;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.currency_fragment, container, false);
		rootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(PocketAccounter.keyboardVisible){
					InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);}
			}
		},100);
		toolbarManager.setTitle(getResources().getString(R.string.currencies));
		toolbarManager.setSubtitle(getResources().getString(R.string.main_currency)+" "+ commonOperations.getMainCurrency().getAbbr());
		toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
		toolbarManager.setImageToSecondImage(R.drawable.pencil);
		toolbarManager.setOnSecondImageClickListener(this);
		fabCurrencyAdd = (FloatingActionButton) rootView.findViewById(R.id.fabCurrencyAdd);
		fabCurrencyAdd.setOnClickListener(this);
		lvCurrency = (ListView) rootView.findViewById(R.id.lvCurrency);
		lvCurrency.setOnItemClickListener(this);
		fabCurrencyAdd.attachToListView(lvCurrency, new ScrollDirectionListener() {
			@Override
			public void onScrollUp() {
				if (mode == PocketAccounterGeneral.EDIT_MODE) return;
				if (fabCurrencyAdd.getVisibility() == View.GONE) return;
				Animation down = AnimationUtils.loadAnimation(getContext(), R.anim.fab_down);
				synchronized (down) {
					down.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							fabCurrencyAdd.setClickable(false);
							fabCurrencyAdd.setVisibility(View.GONE);
						}
						@Override
						public void onAnimationEnd(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});
					fabCurrencyAdd.startAnimation(down);
				}
			}
			@Override
			public void onScrollDown() {
				if (mode == PocketAccounterGeneral.EDIT_MODE) return;
				if (fabCurrencyAdd.getVisibility() == View.VISIBLE) return;
				Animation up = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_up);
				synchronized (up) {
					up.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							fabCurrencyAdd.setVisibility(View.VISIBLE);
							fabCurrencyAdd.setClickable(true);
						}
						@Override
						public void onAnimationEnd(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});
					fabCurrencyAdd.startAnimation(up);
				}
			}
		});
		refreshList();
		new StyleSetter(this, preferences).set();
		return rootView;
	}

	private void setEditMode() {
		mode = PocketAccounterGeneral.EDIT_MODE;
		selected = new boolean[daoSession.getCurrencyDao().loadAll().size()];
		for (int i=0; i<selected.length; i++)
			selected[i] = false;
		toolbarManager.setImageToSecondImage(R.drawable.ic_delete_black);
		Animation fabDown = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_down);
		fabDown.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				fabCurrencyAdd.setVisibility(View.GONE);
			}
		});
		fabCurrencyAdd.startAnimation(fabDown);
		fabCurrencyAdd.setClickable(false);
		refreshList();
	}

	private void setCurrencyListMode() {
		mode = PocketAccounterGeneral.NORMAL_MODE;
		toolbarManager.setImageToSecondImage(R.drawable.pencil);
		Animation fabUp = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_up);
		fabUp.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {	fabCurrencyAdd.setVisibility(View.VISIBLE);}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		fabCurrencyAdd.startAnimation(fabUp);
		fabCurrencyAdd.setClickable(true);
		refreshList();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.fabCurrencyAdd:
				paFragmentManager.displayFragment(new CurrencyChooseFragment());
				break;
			case R.id.ivToolbarMostRight:
				if (daoSession.getCurrencyDao().loadAll().size() == 1) {
					Toast.makeText(getActivity(), getResources().getString(R.string.currency_empty_warning), Toast.LENGTH_SHORT).show();
					return;
				}
				if (mode == PocketAccounterGeneral.NORMAL_MODE)
					setEditMode();
				else {
					boolean selection = false;
					for (int i=0; i<selected.length; i++) {
						if (selected[i]) {
							selection = true;
							break;
						}
					}
					if (!selection) {
						setCurrencyListMode();
						return;
					}
					final WarningDialog dialog = new WarningDialog(getContext());
					dialog.setOnYesButtonListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
							List<Currency> deletingObjects = new ArrayList<>();
							for (int i=0; i<selected.length; i++) {
								if (selected[i]) {
									deletingObjects.add(currencies.get(i));
								}
							}
							setCurrencyListMode();
							int answer = logicManager.deleteCurrency(deletingObjects);
							if (answer == LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT) {
								Toast.makeText(getContext(), R.string.currency_empty_warning, Toast.LENGTH_SHORT).show();
								return;
							}
							refreshList();
							dataCache.updateAllPercents();
							toolbarManager.setSubtitle(getResources().getString(R.string.main_currency) + " " + commonOperations.getMainCurrency().getAbbr());
							dialog.dismiss();
						}
					});
					dialog.setOnNoButtonClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					dialog.show();
				}
				break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (view != null) {
			if (mode == PocketAccounterGeneral.EDIT_MODE) {
				CheckBox chbCurrencyEdit = (CheckBox) view.findViewById(R.id.chbCurrencyEdit);
				chbCurrencyEdit.setChecked(!chbCurrencyEdit.isChecked());
				selected[position] = chbCurrencyEdit.isChecked();
			} else {
				if (daoSession.getCurrencyDao().loadAll().get(position).getMain()) {
					Toast.makeText(getActivity(), getResources().getString(R.string.main_currency_edit), Toast.LENGTH_SHORT).show();
					return;
				}
				paFragmentManager.displayFragment(new CurrencyEditFragment(daoSession.getCurrencyDao().loadAll().get(position)));
			}
		}
	};
	@Override
	void refreshList() {
		CurrencyAdapter adapter = new CurrencyAdapter(getActivity(), daoSession.getCurrencyDao().loadAll(), selected, mode);
		lvCurrency.setAdapter(adapter);
	}
}