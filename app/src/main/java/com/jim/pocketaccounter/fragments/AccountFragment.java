package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.FloatingActionButton;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.ScrollDirectionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("InflateParams")
public class AccountFragment extends Fragment {
	private FABIcon fabAccountAdd;
    private RecyclerView recyclerView;
	@Inject
	WarningDialog warningDialog;
    @Inject
    LogicManager logicManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    DaoSession daoSession;
	@Inject
	ReportManager reportManager;
	@Inject
	@Named(value = "display_formatter")
	SimpleDateFormat dateFormat;
	@Inject
	CommonOperations commonOperations;
	@Inject
	PAFragmentManager paFragmentManager;
	@Inject
	DrawerInitializer drawerInitializer;
	@Inject
	TransferDialog transferDialog;
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
		toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
		toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerInitializer.getDrawer().openLeftSide();
			}
		});
        toolbarManager.setTitle(getResources().getString(R.string.accounts));
        toolbarManager.setSubtitle("");
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        toolbarManager.setSpinnerVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvAccounts);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fabAccountAdd = (FABIcon) rootView.findViewById(R.id.fabAccountAdd);
		fabAccountAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				paFragmentManager.displayFragment(new AccountEditFragment(null));

			}
		});
        refreshList();
		return rootView;
	}

	private void refreshList() {
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
			int resId = getResources().getIdentifier(result.get(position).getIcon(),"drawable", getContext().getPackageName());
            view.ivAccountListIcon.setImageResource(resId);
			String startMoney = "";
			if (result.get(position).getAmount() == 0)
            	startMoney = getResources().getString(R.string.start_amount) + ": "+0+"\n";
			else
				startMoney = getResources().getString(R.string.start_amount) + ": "+result.get(position).getAmount()+result.get(position).getStartMoneyCurrency().getAbbr()+"\n";
			if (result.get(position).getNoneMinusAccount())
				startMoney += getString(R.string.none_minusable_account);
			else
				startMoney += getString(R.string.minusable_account);
			view.tvAccStartMoney.setText(startMoney);
			Map<Currency, Double> map = reportManager.getRemain(result.get(position));
			String text = "";
			for (Currency currency : map.keySet())
				text = text + currency.getName() + ": " + map.get(currency).doubleValue() + " "+currency.getAbbr()+"\n";
			view.view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					paFragmentManager.displayFragment(new AccountInfoFragment(result.get(position)));
				}
			});
			view.tvContent.setText(text);
			view.pay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					transferDialog.show();
					transferDialog.setAccountOrPurpose(result.get(position).getId(), false);
					transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
						@Override
						public void OnTransferDialogSave() {
							Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			view.earn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (daoSession.getPurposeDao().loadAll().isEmpty()) {
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
						transferDialog.show();
						transferDialog.setAccountOrPurpose(result.get(position).getId(), true);
						transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
							@Override
							public void OnTransferDialogSave() {

							}
						});
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
