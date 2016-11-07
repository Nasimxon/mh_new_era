package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.report.FilterSelectable;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.FABIcon;
import com.jim.pocketaccounter.utils.FilterDialog;
import com.jim.pocketaccounter.utils.OperationsListDialog;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.catselector.OnItemSelectedListener;
import com.jim.pocketaccounter.utils.catselector.SelectorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint({"InflateParams", "ValidFragment"})
public class AccountInfoFragment extends Fragment {
    @Inject LogicManager logicManager;
    @Inject ToolbarManager toolbarManager;
    @Inject DaoSession daoSession;
	@Inject ReportManager reportManager;
	@Inject	@Named(value = "display_formatter")	SimpleDateFormat dateFormat;
	@Inject	CommonOperations commonOperations;
	@Inject	PAFragmentManager paFragmentManager;
	@Inject	DataCache dataCache;
	private Account account;
//	private FABIcon fabAccountIcon;
	private TextView tvAccountNameInfo;
	private TextView tvAccountConfigurationInfo;
	private RecyclerView rvAccountDetailsInfo;
//	private ImageView ivAccountInfoOperationsFilter;
//	private TextView getPay;
//	private TextView sendPay;
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
				showOperationsList();
			}
		});

//		getPay = (TextView) rootView.findViewById(R.id.tvAccountInfoReplanish);
//		sendPay = (TextView) rootView.findViewById(R.id.tvAccountInfoToCash);

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

//		fabAccountIcon = (FABIcon) rootView.findViewById(R.id.fabAccountIcon);
//		tvAccountNameInfo = (TextView) rootView.findViewById(R.id.tvAccountNameInfo);
//		tvAccountConfigurationInfo = (TextView) rootView.findViewById(R.id.tvAccountConfigurationInfo);
		rvAccountDetailsInfo = (RecyclerView) rootView.findViewById(R.id.rvAccountDetailsInfo);
//		int resId = getContext().getResources().getIdentifier(account.getIcon(), "drawable", getContext().getPackageName());
//		Bitmap temp = BitmapFactory.decodeResource(getResources(), resId);
//		Bitmap bitmap = Bitmap.createScaledBitmap(temp, (int) getResources().getDimension(R.dimen.twentyfive_dp),
//				(int) getResources().getDimension(R.dimen.twentyfive_dp), false);
//		fabAccountIcon.setImageBitmap(bitmap);
//		tvAccountNameInfo.setText(account.getName());
		String info = "";
		if (account.getAmount() == 0)
			info += getResources().getString(R.string.start_amount) + ": " + 0 + "\n";
		else
			info += getResources().getString(R.string.start_amount) + ": " +account.getAmount() + " "+account.getStartMoneyCurrency().getAbbr() + "\n";
		if (account.getNoneMinusAccount())
			info += getResources().getString(R.string.none_minusable_account);
		else
			info += getResources().getString(R.string.minusable_account);
//		tvAccountConfigurationInfo.setText(info);
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
		return rootView;
	}

	private void showOperationsList() {
		String[] ops = new String[2];
		ops[0] = getResources().getString(R.string.to_edit);
		ops[1] = getResources().getString(R.string.delete);
		final OperationsListDialog operationsListDialog = new OperationsListDialog(getContext());
		operationsListDialog.setAdapter(ops);
		operationsListDialog.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch(position) {
					case 0:
						paFragmentManager.getFragmentManager().popBackStack();
						paFragmentManager.displayFragment(new AccountEditFragment(account));
						break;
					case 1:
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
				operationsListDialog.dismiss();
			}
		});
		operationsListDialog.show();
	}

	private void refreshOperationsList() {
		List<ReportObject> objects = reportManager.getAccountOperations(account, account.getCalendar(), Calendar.getInstance());
		AccountOperationsAdapter accountOperationsAdapter = new AccountOperationsAdapter(objects);
		rvAccountDetailsInfo.setAdapter(accountOperationsAdapter);
	}

	private void refreshOperationsList(Calendar begin, Calendar end) {
		List<ReportObject> objects = reportManager.getAccountOperations(account, begin, end);
		AccountOperationsAdapter accountOperationsAdapter = new AccountOperationsAdapter(objects);
		rvAccountDetailsInfo.setAdapter(accountOperationsAdapter);
	}

	private class AccountOperationsAdapter extends RecyclerView.Adapter<AccountInfoFragment.ViewHolder> {
		private List<ReportObject> result;
		public AccountOperationsAdapter(List<ReportObject> result) {
			this.result = result;
		}
		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final AccountInfoFragment.ViewHolder view, final int position) {
			view.tvAccountInfoDate.setText(dateFormat.format(result.get(position).getDate().getTime()));
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

	class CategorySelectorAdapter extends SelectorView.SelectorAdapter<Account> {
		public CategorySelectorAdapter(List<Account> list) {
			this.list = list;
		}

		@Override
		protected ViewGroup getInstantiatedView(int position, ViewGroup parent) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_selector_item, parent, false);
			ImageView ivCatSelectorItem = (ImageView) view.findViewById(R.id.ivCatSelectorItem);
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
