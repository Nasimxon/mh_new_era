package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.Currency;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.TransferAddEditDialog;
import com.jim.finansia.utils.TransferDialog;
import com.jim.finansia.utils.WarningDialog;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@SuppressLint("InflateParams")
public class AccountFragment extends PABaseListFragment {
	@Inject
	DecimalFormat formatter;
	private FloatingActionButton fabAccountAdd;
    private RecyclerView recyclerView;
	boolean isReportOpen = true;
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

	@Override
	public void onResume() {
		super.onResume();
		if (toolbarManager != null) {
			toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawerInitializer.getDrawer().openLeftSide();
				}
			});
			toolbarManager.setTitle(getResources().getString(R.string.accounts));
			toolbarManager.setSubtitle("");
			toolbarManager.setOnTitleClickListener(null);
			toolbarManager.setSubtitleIconVisibility(View.GONE);
			toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE, View.VISIBLE);
			toolbarManager.setImageToSecondImage(R.drawable.ic_info_outline_black_48dp);
			toolbarManager.setImageToFirstImage(R.drawable.ic_history_black_48dp);

			toolbarManager.setOnFirstImageClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!daoSession.getAccountOperationDao().loadAll().isEmpty()) {
						final TransferAddEditDialog transferAddEditDialog = new TransferAddEditDialog(getContext());
						int width = getResources().getDisplayMetrics().widthPixels;
						int height = getResources().getDisplayMetrics().heightPixels;
						transferAddEditDialog.getWindow().setLayout(12*width/13, 9*height/10);
						transferAddEditDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
                                reportManager.clearCache();
                                refreshList();
							}
						});
						transferAddEditDialog.show();
					} else
						Toast.makeText(getContext(), R.string.transfer_isnt_done, Toast.LENGTH_SHORT).show();
				}
			});
			isReportOpen = preferences.getBoolean(PocketAccounterGeneral.ACCOUNT_INFO_ENABLED_KEY, true);
			toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(isReportOpen){
						isReportOpen=false	;
						refreshList();
					}
					else {
						isReportOpen=true;
						refreshList();
					}
					preferences.edit().putBoolean(PocketAccounterGeneral.ACCOUNT_INFO_ENABLED_KEY, isReportOpen).commit();
				}
			});
		}
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

			// income expanses balances
			Map<Currency, List<Double>> map = reportManager.getRemain(result.get(position));
			String inc = "";
			String exp = "";
			String bal = "";
			if(isReportOpen){
				view.infoOpen.setVisibility(View.VISIBLE);
				view.withGone.setVisibility(View.GONE);
				for (Currency currency : map.keySet()) {
					String abbr = currency.getAbbr();
					inc += formatter.format(map.get(currency).get(0)) + abbr + "\n";
					exp += formatter.format(map.get(currency).get(1)) + abbr + "\n";
					bal += formatter.format(map.get(currency).get(2)) + abbr + "\n";
				}
				if(inc.length()!=0&&exp.length()!=0&&bal.length()!=0) {
					view.tvAccountIncome.setText(inc.substring(0, inc.length() - 1));
					view.tvAccountExpanse.setText(exp.substring(0, exp.length() - 1));
					view.tvAccountBalance.setText(bal.substring(0, bal.length() - 1));
				}
				else {
					view.tvAccountIncome.setText("0"+commonOperations.getMainCurrency().getAbbr());
					view.tvAccountExpanse.setText("0"+commonOperations.getMainCurrency().getAbbr());
					view.tvAccountBalance.setText("0"+commonOperations.getMainCurrency().getAbbr());
				}
			} else {
				view.infoOpen.setVisibility(View.GONE);
				view.withGone.setVisibility(View.VISIBLE);
			}

			view.mainViewF.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					paFragmentManager.displayFragment(new AccountInfoFragment(result.get(position)));
				}
			});
			view.llAccountItemOther.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (daoSession.getAccountDao().loadAll().size() == 1){
                        final WarningDialog warningDialog = new WarningDialog(getContext());
                        warningDialog.setText(getString(R.string.account_list));
                        warningDialog.setOnYesButtonListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                paFragmentManager.getFragmentManager().popBackStack();
                                paFragmentManager.displayFragment(new AccountEditFragment(null));
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
					transferDialog.setAccountOrPurpose(result.get(position).getId(), false);
					transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
						@Override
						public void OnTransferDialogSave() {
							refreshList();
						}
					});
					transferDialog.getWindow().setLayout(9 * getResources().getDisplayMetrics().widthPixels/10, RelativeLayout.LayoutParams.WRAP_CONTENT);
					transferDialog.show();
					}
				}
			});
			view.llAccountItemPurpose.setOnClickListener(new OnClickListener() {
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
						transferDialog.getWindow().setLayout(9 * getResources().getDisplayMetrics().widthPixels/10, RelativeLayout.LayoutParams.WRAP_CONTENT);
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
		ImageView ivIconItem;
		TextView tvAccountName;
		TextView tvAccountIncome;
		TextView tvAccountExpanse;
		TextView tvAccountBalance;
		LinearLayout infoOpen;
		FrameLayout withGone;
		RelativeLayout mainViewF;
		LinearLayout llAccountItemPurpose;
		LinearLayout llAccountItemOther;
		public ViewHolder(View view) {
			super(view);
			ivIconItem = (ImageView) view.findViewById(R.id.ivAccountItemCurrent);
			tvAccountName = (TextView) view.findViewById(R.id.tvAccountItemName);
			tvAccountIncome = (TextView) view.findViewById(R.id.tvAccountItemIncome);
			tvAccountExpanse = (TextView) view.findViewById(R.id.tvAccountItemExpanse);
			tvAccountBalance = (TextView) view.findViewById(R.id.tvAccountItemBalance);
			infoOpen = (LinearLayout) view.findViewById(R.id.infoOpen);
			withGone = (FrameLayout) view.findViewById(R.id.withGone);
			mainViewF = (RelativeLayout) view.findViewById(R.id.mainViewF);
			llAccountItemOther = (LinearLayout) view.findViewById(R.id.llAccountItemOther);
			llAccountItemPurpose = (LinearLayout) view.findViewById(R.id.llAccountItemPurpose);
		}
	}
}