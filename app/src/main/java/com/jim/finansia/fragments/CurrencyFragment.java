package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;
import com.jim.finansia.database.Currency;
import com.jim.finansia.finance.CurrencyAdapter;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("InflateParams")
public class CurrencyFragment extends PABaseListFragment implements OnClickListener {
	private FloatingActionButton fabCurrencyAdd;
	private RecyclerView lvCurrency;
	private int mode = PocketAccounterGeneral.NORMAL_MODE;
	private boolean[] selected;
	public static final String CURRENCY_ID = "currency_id";
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View rootView = inflater.inflate(R.layout.currency_fragment, container, false);
		analytics.sendText("User enters " + getClass().getName());
		if (toolbarManager != null)
		{
			toolbarManager.setTitle(getResources().getString(R.string.currencies));
			toolbarManager.setOnTitleClickListener(null);
			toolbarManager.setSubtitle(getResources().getString(R.string.main_currency)+" "+ commonOperations.getMainCurrency().getAbbr());
			toolbarManager.setSubtitleIconVisibility(View.GONE);
			toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
			toolbarManager.setImageToSecondImage(R.drawable.pencil);
			toolbarManager.setOnSecondImageClickListener(this);
		}
		rootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(PocketAccounter.keyboardVisible){
					InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);}
			}
		},100);

		fabCurrencyAdd = (FloatingActionButton) rootView.findViewById(R.id.fabCurrencyAdd);
		fabCurrencyAdd.setOnClickListener(this);
		lvCurrency = (RecyclerView) rootView.findViewById(R.id.lvCurrency);
		refreshList();
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

	public void updateToolbar() {
		if (toolbarManager != null)
		{
			toolbarManager.setTitle(getResources().getString(R.string.currencies));
			toolbarManager.setOnTitleClickListener(null);
			toolbarManager.setSubtitle(getResources().getString(R.string.main_currency)+" "+ commonOperations.getMainCurrency().getAbbr());
			toolbarManager.setSubtitleIconVisibility(View.GONE);
			toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
			toolbarManager.setImageToSecondImage(R.drawable.pencil);
			toolbarManager.setOnSecondImageClickListener(this);
			toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
			toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawerInitializer.getDrawer().openLeftSide();
				}
			});
		}
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
	public void refreshList() {
		CurrencyAdapter adapter = new CurrencyAdapter(getActivity(), daoSession.getCurrencyDao().loadAll(), selected, mode);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		lvCurrency.setLayoutManager(layoutManager);
		lvCurrency.setAdapter(adapter);
	}
}