package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.catselector.OnItemSelectedListener;
import com.jim.pocketaccounter.utils.catselector.SelectorView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint({"InflateParams", "ValidFragment"})
public class AccountInfoFragment extends PABaseInfoFragment {
	private Account account;
	private RecyclerView rvAccountDetailsInfo;
	private TextView firstPay;
	private TextView canbeNegative;
	private TextView totalAmount;
	SelectorView svCategorySelector;

	@SuppressLint("ValidFragment")
	public AccountInfoFragment(Account account) {
		this.account = account;
	}


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.account_info_modern_layout, container, false);
		((PocketAccounter)getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
		toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
		toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
		toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
		toolbarManager.setTitle(getResources().getString(R.string.accounts));
		toolbarManager.setSubtitle(account.getName());
		toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				paFragmentManager.getFragmentManager().popBackStack();
				paFragmentManager.displayFragment(new AccountFragment());
			}
		});
		toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showOperationsList(v);
			}
		});

		totalAmount = (TextView) rootView.findViewById(R.id.tvAccountInfoTotal);
		svCategorySelector = (SelectorView) rootView.findViewById(R.id.svAccountSelector);

		final List<Account> rootAccounts = daoSession.getAccountDao().queryBuilder().orderAsc(AccountDao.Properties.Name).list();
		CategorySelectorAdapter adapter = new CategorySelectorAdapter(rootAccounts);
		int selectedPos = 0;
		for (int i = 0; i < rootAccounts.size(); i++) {
			if (rootAccounts.get(i).getId().equals(account.getId())) {
				selectedPos = i;
				break;
			}
		}

		svCategorySelector.setAdapter(adapter);
		svCategorySelector.setSelection(selectedPos);
		svCategorySelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(int selectedItemPosition) {
				account = rootAccounts.get(selectedItemPosition);
				toolbarManager.setSubtitle(account.getName());
				refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
			}
		});

		rvAccountDetailsInfo = (RecyclerView) rootView.findViewById(R.id.rvAccountDetailsInfo);
 		firstPay = (TextView) rootView.findViewById(R.id.tvAccountInfoFirstPay);
		canbeNegative = (TextView) rootView.findViewById(R.id.tvAccountInfoCanBeNegative);
		if (account.getAmount() == 0) {
			firstPay.setText(getResources().getString(R.string.start_amount) + " " + 0 );
		}
		else {
			firstPay.setText(getResources().getString(R.string.start_amount) + " " +account.getAmount() + " "+account.getStartMoneyCurrency().getAbbr());
		}
		if (account.getNoneMinusAccount()) {
			canbeNegative.setText(getResources().getString(R.string.none_minusable_account)); ;
		}
		else {
			canbeNegative.setText(getResources().getString(R.string.minusable_account));
		}

		rvAccountDetailsInfo = (RecyclerView) rootView.findViewById(R.id.rvAccountInfoOperations);
		rvAccountDetailsInfo.setLayoutManager(new LinearLayoutManager(getContext()));
//		ivAccountInfoOperationsFilter = (ImageView) rootView.findViewById(R.id.ivAccountInfoOperationsFilter);
//		ivAccountInfoOperationsFilter.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				FilterDialog filterDialog = new FilterDialog(getContext());
//				filterDialog.setOnDateSelectedListener(new FilterSelectable() {
//					@Override
//					public void onDateSelected(Calendar begin, Calendar end) {
//						refreshOperationsList(begin, end);
//					}
//				});
//				filterDialog.show();
//			}
//		});
//		sendPay.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (daoSession.getPurposeDao().loadAll().isEmpty()) {
//					final WarningDialog warningDialog = new WarningDialog(getContext());
//					warningDialog.setText(getString(R.string.purpose_list_is_empty));
//					warningDialog.setOnYesButtonListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							paFragmentManager.getFragmentManager().popBackStack();
//							paFragmentManager.displayFragment(new PurposeFragment());
//							warningDialog.dismiss();
//						}
//					});
//					warningDialog.setOnNoButtonClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							warningDialog.dismiss();
//						}
//					});
//					warningDialog.show();
//				} else {
//					final TransferDialog transferDialog = new TransferDialog(getContext());
//					transferDialog.setAccountOrPurpose(account.getId(), true);
//					transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
//						@Override
//						public void OnTransferDialogSave() {
//							refreshOperationsList();
//							transferDialog.dismiss();
//						}
//					});
//					transferDialog.show();
//				}
//			}
//		});
//		getPay.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final TransferDialog transferDialog = new TransferDialog(getContext());
//				transferDialog.show();
//				transferDialog.setAccountOrPurpose(account.getId(), false);
//				transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
//					@Override
//					public void OnTransferDialogSave() {
//						refreshOperationsList();
//						transferDialog.dismiss();
//					}
//				});
//			}
//		});
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		rvAccountDetailsInfo.setLayoutManager(layoutManager);
		refreshOperationsList();

//		ivCatSelectorItem.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if (event.getAction() == MotionEvent.ACTION_DOWN) {
//					int pos = rootAccounts.indexOf(account);
////					if (pos ++ >= rootAccounts.size() - 1) {
////						pos = 0;
////					}
//					pos ++;
//					svCategorySelector.move(pos);
//					return true;
//				}
//				return false;
//			}
//		});

		return rootView;
	}

	private void showOperationsList(View v) {
		PopupMenu popupMenu = new PopupMenu(getContext(), v);
		popupMenu.inflate(R.menu.toolbar_popup);
		MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
		menuHelper.setForceShowIcon(true);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.delete: {
						List<Account> accounts = new ArrayList<>();
						accounts.add(account);
						if (LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT == logicManager.deleteAccount(accounts)){
							Toast.makeText(getContext(), R.string.account_deleting_error, Toast.LENGTH_SHORT).show();
						} else {
							paFragmentManager.getFragmentManager().popBackStack();
							paFragmentManager.displayFragment(new AccountFragment());
						}
						dataCache.updateAllPercents();
						break;
					}
					case R.id.edit: {
						paFragmentManager.getFragmentManager().popBackStack();
						paFragmentManager.displayFragment(new AccountEditFragment(account));
						break;
					}
				}
				return false;
			}
		});
		popupMenu.show();
	}

	private void refreshOperationsList() {
		List<ReportObject> objects = reportManager.getAccountOperations(account, account.getCalendar(), Calendar.getInstance());
		AccountOperationsAdapter accountOperationsAdapter = new AccountOperationsAdapter(objects);
		DecimalFormat format = new DecimalFormat("0.##");
		double total = 0.0d;
		for (ReportObject reportObject : objects) {
			if (reportObject.getAccount().getId().matches(account.getId()))
				total += commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
			else
				total -= commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
		}
		totalAmount.setText(getResources().getString(R.string.total) + " " + format.format(total) + commonOperations.getMainCurrency().getAbbr());
		rvAccountDetailsInfo.setAdapter(accountOperationsAdapter);
	}

	private void refreshOperationsList(Calendar begin, Calendar end) {
		List<ReportObject> objects = reportManager.getAccountOperations(account, begin, end);
		AccountOperationsAdapter accountOperationsAdapter = new AccountOperationsAdapter(objects);
		DecimalFormat format = new DecimalFormat("0.##");
		double total = 0.0d;
		for (ReportObject reportObject : objects) {
			if (reportObject.getAccount().getId() == account.getId())
				total += commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAccount().getAmount());
			else
				total -= commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAccount().getAmount());
		}
		totalAmount.setText(getResources().getString(R.string.total) + " " + format.format(total) + commonOperations.getMainCurrency().getAbbr());
		rvAccountDetailsInfo.setAdapter(accountOperationsAdapter);
	}

	@Override
	void refreshList() {

	}

	private class AccountOperationsAdapter extends RecyclerView.Adapter<AccountInfoFragment.ViewHolder> {
		private List<ReportObject> result;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

		public AccountOperationsAdapter(List<ReportObject> result) {
			this.result = result;
		}
		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final AccountInfoFragment.ViewHolder view, final int position) {
			view.tvAccountInfoDate.setText(simpleDateFormat.format(result.get(position).getDate().getTime()));
			view.tvAccountInfoName.setText(result.get(position).getDescription());
			String amount = "";
			if (result.get(position).getType() == PocketAccounterGeneral.INCOME) {
				amount += "+"+result.get(position).getAmount() + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
			}
			else {
				amount += "-"+result.get(position).getAmount() + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
			}
			Log.d("nnn", " " + amount.matches("\\s?[0-9]*[.,]?[0]?\\s?"));
			view.tvAccountInfoAmount.setText(amount);
		}

		public AccountInfoFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_info_operations, parent, false);
			return new AccountInfoFragment.ViewHolder(view);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvAccountInfoDate;
		TextView tvAccountInfoName;
		TextView tvAccountInfoAmount;
		public ViewHolder(View view) {
			super(view);
			tvAccountInfoDate = (TextView) view.findViewById(R.id.tvAccountInfoDate);
			tvAccountInfoName = (TextView) view.findViewById(R.id.tvAccountInfoName);
			tvAccountInfoAmount = (TextView) view.findViewById(R.id.tvAccountInfoAmount);
		}
	}

	private ImageView ivCatSelectorItem;

	class CategorySelectorAdapter extends SelectorView.SelectorAdapter<Account> {
		public CategorySelectorAdapter(List<Account> list) {
			this.list = list;
		}

		@Override
		protected ViewGroup getInstantiatedView(int position, ViewGroup parent) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_selector_item, parent, false);
			ivCatSelectorItem = (ImageView) view.findViewById(R.id.ivCatSelectorItem);
			int resId = getResources().getIdentifier(list.get(position).getIcon(), "drawable", getContext().getPackageName());
			ivCatSelectorItem.setImageResource(resId);
			TextView tvCatSelectorItem = (TextView) view.findViewById(R.id.tvCatSelectorItem);
			tvCatSelectorItem.setText(list.get(position).getName());
			parent.addView(view);
			return parent;
		}
		@Override
		protected int getCount() {
			return list.size();
		}
	}
}