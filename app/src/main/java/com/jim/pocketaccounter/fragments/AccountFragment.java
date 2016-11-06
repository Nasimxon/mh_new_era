package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.utils.FloatingActionButton;
import com.jim.pocketaccounter.utils.TransferAddEditDialog;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@SuppressLint("InflateParams")
public class AccountFragment extends PABaseListFragment {
	private FloatingActionButton fabAccountAdd;
    private RecyclerView recyclerView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.account_layout, container, false);
		rootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(PocketAccounter.keyboardVisible){
					InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);}
			}
		}, 100);

		((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
		toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerInitializer.getDrawer().openLeftSide();
			}
		});
        toolbarManager.setTitle(getResources().getString(R.string.accounts));
        toolbarManager.setSubtitle("");
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
		toolbarManager.setImageToSecondImage(R.drawable.transfer_money);
		toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!daoSession.getAccountOperationDao().loadAll().isEmpty()) {
					final TransferAddEditDialog transferAddEditDialog = new TransferAddEditDialog(getContext());
					int width = getResources().getDisplayMetrics().widthPixels;
					int height = getResources().getDisplayMetrics().heightPixels;
					transferAddEditDialog.getWindow().setLayout(9*width/10, 9*height/9);
					transferAddEditDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							refreshList();
						}
					});
					transferAddEditDialog.show();
				} else
					Toast.makeText(getContext(), R.string.transfer_isnt_done, Toast.LENGTH_SHORT).show();
			}
		});
        toolbarManager.setSpinnerVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvAccounts);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				try {
					onScrolledList(dy > 0);
				} catch (NullPointerException e) {}
			}
		});
        fabAccountAdd = (FloatingActionButton) rootView.findViewById(R.id.fabAccountAdd);
		fabAccountAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				paFragmentManager.displayFragment(new AccountEditFragment(null));
			}
		});
        refreshList();
		return rootView;
	}
	private boolean show = false;
	public void onScrolledList(boolean k) {
		if (k) {
			if (!show)
				fabAccountAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_down));
			show = true;
		} else {
			if (show)
				fabAccountAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_up));
			show = false;
		}
	}
	@Override
	void refreshList() {
		AccountAdapter adapter = new AccountAdapter(daoSession.getAccountDao().loadAll());
		recyclerView.setAdapter(adapter);
	}

	private class AccountAdapter extends RecyclerView.Adapter<ViewHolder> {
		private List<Account> result;
		public AccountAdapter(List<Account> result) {
			this.result = result;
		}

		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final ViewHolder view, final int position) {
			view.tvAccountName.setText(result.get(position).getName());
			int resId = getResources().getIdentifier(result.get(position).getIcon(), "drawable", getContext().getPackageName());
			view.ivIconItem.setImageResource(resId);
			DecimalFormat format = new DecimalFormat("0.##");

			// income expanses balances
			Map<Currency, List<Double>> map = reportManager.getRemain(result.get(position));
			String inc = "";
			String exp = "";
			String bal = "";
			for (Currency currency : map.keySet()) {
				String abbr = currency.getAbbr();
				inc += map.get(currency).get(0) + abbr + "\n";
				exp += map.get(currency).get(1) + abbr + "\n";
				bal += map.get(currency).get(2) + abbr + "\n";
			}

			view.tvAccountIncome.setText(inc);
			view.tvAccountExpanse.setText(exp);
			view.tvAccountBalance.setText(bal);

			view.ivIconItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					paFragmentManager.displayFragment(new AccountInfoFragment(result.get(position)));
				}
			});
			view.ivOtherAccount.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TransferDialog transferDialog = new TransferDialog(getContext());
					transferDialog.setAccountOrPurpose(result.get(position).getId(), false);
					transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
						@Override
						public void OnTransferDialogSave() {
							refreshList();
						}
					});
					transferDialog.show();
				}
			});
			view.ivSendPurpose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (daoSession.getPurposeDao().loadAll().isEmpty()) {
						final WarningDialog warningDialog = new WarningDialog(getContext());
						warningDialog.setText(getString(R.string.purpose_list_is_empty));
						warningDialog.setOnYesButtonListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								paFragmentManager.getFragmentManager().popBackStack();
								paFragmentManager.displayFragment(new PurposeFragment());
								warningDialog.dismiss();
							}
						});
						warningDialog.setOnNoButtonClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								warningDialog.dismiss();
							}
						});
						warningDialog.show();
					} else {
						TransferDialog transferDialog = new TransferDialog(getContext());
						transferDialog.setAccountOrPurpose(result.get(position).getId(), true);
						transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
							@Override
							public void OnTransferDialogSave() {
								refreshList();
							}
						});
						transferDialog.show();
					}
				}
			});
		}

		public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_item_modern, parent, false);
			return new ViewHolder(view);
		}
	}
	public class ViewHolder extends RecyclerView.ViewHolder {
		public ImageView ivIconItem;
		public ImageView ivSendPurpose;
		public ImageView ivOtherAccount;
		public TextView tvAccountName;
		public TextView tvAccountIncome;
		public TextView tvAccountExpanse;
		public TextView tvAccountBalance;

		public ViewHolder(View view) {
			super(view);
			ivIconItem = (ImageView) view.findViewById(R.id.ivAccountItemCurrent);
			ivOtherAccount = (ImageView) view.findViewById(R.id.ivAccountItemExchange);
			ivSendPurpose = (ImageView) view.findViewById(R.id.ivAccountItemSendPurpose);
			tvAccountName = (TextView) view.findViewById(R.id.tvAccountItemName);
			tvAccountIncome = (TextView) view.findViewById(R.id.tvAccountItemIncome);
			tvAccountExpanse = (TextView) view.findViewById(R.id.tvAccountItemExpanse);
			tvAccountBalance = (TextView) view.findViewById(R.id.tvAccountItemBalance);
		}
	}
}
