package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.report.ReportObject;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.catselector.OnItemSelectedListener;
import com.jim.finansia.utils.catselector.SelectorView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

@SuppressLint("InflateParams")
public class AccountInfoFragment extends PABaseInfoFragment {
	private DecimalFormat formatter = new DecimalFormat("0.##");
	private Account account;
	private RecyclerView rvAccountDetailsInfo;
	private TextView firstPay;
	private TextView canbeNegative;
	private TextView totalAmount;
	private SelectorView svCategorySelector;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View rootView = inflater.inflate(R.layout.account_info_modern_layout, container, false);
		analytics.sendText("User enters " + getClass().getName());
		if (getArguments() != null) {
			String accountId = getArguments().getString(AccountFragment.ACCOUNT_ID);
			if (accountId != null) {
				account = daoSession.load(Account.class, accountId);
			}
		}
		if (toolbarManager != null) {
			toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
			toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
			toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
			toolbarManager.setTitle(getResources().getString(R.string.accounts));
			toolbarManager.setOnTitleClickListener(null);
			toolbarManager.setSubtitle(account.getName());
			toolbarManager.setSubtitleIconVisibility(View.GONE);
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
		}
		if (getArguments() != null) {
			String accountId = getArguments().getString(AccountFragment.ACCOUNT_ID);
			if (accountId != null) {
				account = daoSession.load(Account.class, accountId);
			}
		}
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
				toolbarManager.setSubtitleIconVisibility(View.GONE);
				refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
			}
		});

		rvAccountDetailsInfo = (RecyclerView) rootView.findViewById(R.id.rvAccountInfoOperations);
 		firstPay = (TextView) rootView.findViewById(R.id.tvAccountInfoFirstPay);
		canbeNegative = (TextView) rootView.findViewById(R.id.tvAccountInfoCanBeNegative);
		if (account.getAmount() == 0) {
			firstPay.setText(getResources().getString(R.string.start_amount) + " " + 0 );
		}
		else {
			firstPay.setText(getResources().getString(R.string.start_amount) + " " + formatter.format(account.getAmount()) + " "+account.getStartMoneyCurrency().getAbbr());
		}
		if (account.getNoneMinusAccount()) {
			canbeNegative.setText(getResources().getString(R.string.none_minusable_account)); ;
		}
		else {
			canbeNegative.setText(getResources().getString(R.string.minusable_account));
		}

		rvAccountDetailsInfo = (RecyclerView) rootView.findViewById(R.id.rvAccountInfoOperations);
		rvAccountDetailsInfo.setLayoutManager(new LinearLayoutManager(getContext()));
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		rvAccountDetailsInfo.setLayoutManager(layoutManager);
		refreshOperationsList();
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
						final WarningDialog dialog = new WarningDialog(getContext());
						dialog.setText(getResources().getString(R.string.account_delete_warning));
						dialog.setOnYesButtonListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								List<Account> accounts = new ArrayList<>();
								accounts.add(account);
								if (LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT == logicManager.deleteAccount(accounts)){
									Toast.makeText(getContext(), R.string.account_deleting_error, Toast.LENGTH_SHORT).show();
								} else {
									paFragmentManager.getFragmentManager().popBackStack();
									paFragmentManager.displayFragment(new AccountFragment());
								}
								reportManager.clearCache();
								dataCache.updateAllPercents();
								paFragmentManager.updateAllFragmentsPageChanges();
								paFragmentManager.updateTemplatesInVoiceRecognitionFragment();
								dialog.dismiss();
							}
						});
						dialog.setOnNoButtonClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								dialog.dismiss();
							}
						});
						dialog.show();
						break;
					}
					case R.id.edit: {
						paFragmentManager.getFragmentManager().popBackStack();
						Bundle bundle = new Bundle();
						bundle.putString(AccountFragment.ACCOUNT_ID, account.getId());
						AccountEditFragment fragment = new AccountEditFragment();
						fragment.setArguments(bundle);
						paFragmentManager.displayFragment(fragment);
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
			if (reportObject.getType() == PocketAccounterGeneral.INCOME)
				total += commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
			else
				total -= commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
		}
		totalAmount.setText(getResources().getString(R.string.balance) + ": " + format.format(total) + commonOperations.getMainCurrency().getAbbr());
		rvAccountDetailsInfo.setAdapter(accountOperationsAdapter);
	}

	private void refreshOperationsList(Calendar b, Calendar e) {
		Calendar begin = (Calendar) b.clone();
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		Calendar end = (Calendar) e.clone();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 59);
		List<ReportObject> objects = reportManager.getAccountOperations(account, begin, end);
		AccountOperationsAdapter accountOperationsAdapter = new AccountOperationsAdapter(objects);
		double total = 0.0d;
		for (ReportObject reportObject : objects) {
			if (reportObject.getType() == PocketAccounterGeneral.INCOME)
				total += commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
			else
				total -= commonOperations.getCost(reportObject.getDate(), reportObject.getCurrency(), reportObject.getAmount());
		}
		totalAmount.setText(getResources().getString(R.string.balance) + ": " + formatter.format(total) + commonOperations.getMainCurrency().getAbbr());
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
				amount += "+"+formatter.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
			}
			else {
				amount += "-"+formatter.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
			}
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