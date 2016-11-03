package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountOperation;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.DrawerInitializer;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.FABIcon;
import com.jim.pocketaccounter.utils.StyleSetter;
import com.jim.pocketaccounter.utils.Styleable;
import com.jim.pocketaccounter.utils.TransferAddEditDialog;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.FloatingActionButton;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.ScrollDirectionListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("InflateParams")
public class AccountFragment extends PABaseListFragment {
	@Styleable(colorLayer = PocketAccounterGeneral.HEAD_COLOR)
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
		},100);
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
				}
				else
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
				} catch (NullPointerException e) {

				}
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
		new StyleSetter(this, preferences).set();
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
            view.tvAccountListName.setText(result.get(position).getName());
			int resId = getResources().getIdentifier(result.get(position).getIcon(), "drawable", getContext().getPackageName());
            view.ivAccountListIcon.setImageResource(resId);
			DecimalFormat format = new DecimalFormat("0.##");
			String startMoney = "";
			if (result.get(position).getAmount() == 0)
            	startMoney = getResources().getString(R.string.start_amount) + ": "+0+"\n";
			else
				startMoney = getResources().getString(R.string.start_amount) + ": "+format.format(result.get(position).getAmount())+result.get(position).getStartMoneyCurrency().getAbbr()+"\n";
			if (result.get(position).getNoneMinusAccount())
				startMoney += getString(R.string.none_minusable_account);
			else
				startMoney += getString(R.string.minusable_account);
			view.tvAccStartMoney.setText(startMoney);
			Map<Currency, Double> map = reportManager.getRemain(result.get(position));
			String text = "";
			boolean isFirst = true;
			for (Currency currency : map.keySet()){
				if(isFirst){
					text = text + currency.getName() + ": " + format.format(map.get(currency).doubleValue()) + " "+currency.getAbbr();
					isFirst = false;
				}
				else
					text = text + "\n" + currency.getName() + ": " + format.format(map.get(currency).doubleValue()) + " "+currency.getAbbr();

			}
			view.view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					paFragmentManager.displayFragment(new AccountInfoFragment(result.get(position)));
				}
			});
			if(!text.equals(""))
				view.tvContent.setText(text);
			else
				view.tvContent.setVisibility(View.INVISIBLE);
				view.pay.setOnClickListener(new OnClickListener() {
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
				view.earn.setOnClickListener(new OnClickListener() {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item, parent, false);
            return new ViewHolder(view);
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAccountListIcon;
        TextView tvAccountListName;
        TextView tvAccStartMoney;
        TextView tvContent;
		TextView pay;
		TextView earn;
		View view;
        public ViewHolder(View view) {
            super(view);
            ivAccountListIcon = (ImageView) view.findViewById(R.id.ivAccountListIcon);
            tvAccStartMoney = (TextView) view.findViewById(R.id.tvAccStartMoney);
            tvAccountListName = (TextView) view.findViewById(R.id.tvAccountListName);
            tvContent = (TextView) view.findViewById(R.id.tvContent);
			pay = (TextView) view.findViewById(R.id.tvAccountPay);
			earn = (TextView) view.findViewById(R.id.tvAccountToEarn);
            this.view = view;
        }
    }
}
