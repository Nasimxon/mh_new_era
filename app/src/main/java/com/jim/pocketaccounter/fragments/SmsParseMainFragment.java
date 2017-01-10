package com.jim.pocketaccounter.fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.SmsParseObject;
import com.jim.pocketaccounter.database.SmsParseSuccess;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.database.TemplateSms;
import com.jim.pocketaccounter.utils.billing.PurchaseImplementation;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by root on 9/29/16.
 */

public class SmsParseMainFragment extends Fragment implements View.OnClickListener {
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;
    @Inject PurchaseImplementation purchaseImplementation;
    @Inject SharedPreferences preferences;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private TextView ifListEmpty;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms_tab_lay, container, false);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        toolbarManager.setTitle(getResources().getString(R.string.sms_parse));
        toolbarManager.setSubtitle("");
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvSmsParseAllList);
        ifListEmpty = (TextView) rootView.findViewById(R.id.ifListEmpty);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fbDebtBorrowFragment);
        floatingActionButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fbDebtBorrowFragment: {
                int accessCount = preferences.getInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SMS_PARSING_COUNT_KEY, 1);
                int smsCount = (int) daoSession.getSmsParseObjectDao().count();
                if (smsCount < accessCount) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new AddSmsParseFragment(null));
                }
                else {
                    purchaseImplementation.buySms();
                }
                break;
            }
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<SmsParseMainFragment.ViewHolder> {
        private List<SmsParseObject> smsParseObjects;

        public MyAdapter() {
            this.smsParseObjects = daoSession.getSmsParseObjectDao().loadAll();
            if(smsParseObjects.size()==0){
                ifListEmpty.setVisibility(View.VISIBLE);
                ifListEmpty.setText(R.string.sms_pars_list_empty);
            }
            else ifListEmpty.setVisibility(View.GONE);
        }

        public int getItemCount() {
            return smsParseObjects.size();
        }

        public void onBindViewHolder(final SmsParseMainFragment.ViewHolder view, final int position) {
            view.tvNumber.setText(smsParseObjects.get(position).getNumber());
            view.tvAccount.setText(smsParseObjects.get(position).getAccount().getName());
            view.tvMessagesCount.setText("" + smsParseObjects.get(position).getSuccessList().size());
            int count = 0;
            for (SmsParseSuccess smsParseSuccess : smsParseObjects.get(position).getSuccessList()) {
                if (!smsParseSuccess.getIsSuccess()) count ++;
            }
            if (count != 0) {
                if(count>1)
                view.tvNotReadCount.setText( count +" "+ getString(R.string.mes));
                else
                    view.tvNotReadCount.setText( count+" " + getString(R.string.mes2));

                view.tvNotReadCount.setTextColor(ContextCompat.getColor(getContext(),R.color.red));
            }
            else {
                view.tvNotReadCount.setText( getString(R.string.no_message));
                view.tvNotReadCount.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
            }
            if (smsParseObjects.get(position).getSuccessList() != null) {
                double incomeSumm = 0;
                double expanceSumm = 0;
                for (SmsParseSuccess parseSuccess : smsParseObjects.get(position).getSuccessList()) {
                    if (parseSuccess.getType() == PocketAccounterGeneral.INCOME) {
                        incomeSumm += parseSuccess.getAmount();
                    } else
                        expanceSumm += parseSuccess.getAmount();
                }
                view.tvAllExpense.setText("" + expanceSumm + smsParseObjects.get(position).getCurrency().getAbbr());
                view.tvAllIncomes.setText("" + incomeSumm + smsParseObjects.get(position).getCurrency().getAbbr());
            }
            view.info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (view.llInfoGone.getVisibility() == View.GONE) {
                        view.llInfoGone.setVisibility(View.VISIBLE);
                        view.ivInfoIcon.setImageResource(R.drawable.info_pastga);
                    } else {
                        view.llInfoGone.setVisibility(View.GONE);
                        view.ivInfoIcon.setImageResource(R.drawable.info_open);
                    }
                }
            });
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new SMSParseInfoFragment(smsParseObjects.get(position)));
                }
            });
            view.addKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(getActivity());
                    View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_sms_key_words, null);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(dialogView);
                    final EditText etIncome = (EditText) dialogView.findViewById(R.id.etIncomeAdd);
                    final EditText etAmount = (EditText) dialogView.findViewById(R.id.etAmountAdd);
                    final TextView tvIncome = (TextView) dialogView.findViewById(R.id.tvIncome);
                    final ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
                    final ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
                    final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.chbIncome);
                    if (checkBox.isChecked()) {
                        tvIncome.setText(getResources().getString(R.string.income));
                    } else {
                        tvIncome.setText(getResources().getString(R.string.expanse));
                    }
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                tvIncome.setText(getResources().getString(R.string.income));
                            } else {
                                tvIncome.setText(getResources().getString(R.string.expanse));
                            }
                        }
                    });
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<String> incomeList = new LinkedList<>();
                            List<String> expanceList = new LinkedList<>();
                            if (checkBox.isChecked())
                                incomeList.add(etIncome.getText().toString());
                            else
                                expanceList.add(etIncome.getText().toString());
                            List<String> amountList = new LinkedList<>();
                            incomeList.add(etAmount.getText().toString());

                            for (TemplateSms smsTemalate : commonOperations.generateSmsTemplateList(new LinkedList<String>(),
                                    0, 0, incomeList, expanceList, amountList)) {
                                smsParseObjects.get(position).getTemplates().add(smsTemalate);
                                smsTemalate.setParseObjectId(smsParseObjects.get(position).getId());
                                smsTemalate.setType(checkBox.isChecked() ? PocketAccounterGeneral.INCOME
                                        : PocketAccounterGeneral.EXPENSE);
                            }

                            dialog.dismiss();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    int width = getResources().getDisplayMetrics().widthPixels;
                    dialog.getWindow().setLayout(9 * width / 10, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                    dialog.show();
                }
            });
        }

        public SmsParseMainFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_adapter_root, parent, false);
            return new SmsParseMainFragment.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNumber;
        public TextView tvNotReadCount;
        public TextView tvAccount;
        public TextView tvAllIncomes;
        public TextView tvAllExpense;
        public TextView tvMessagesCount;
        public LinearLayout info;
        public LinearLayout llInfoGone;
        public ImageView addKeys;
        public ImageView ivInfoIcon;

        public ViewHolder(View view) {
            super(view);
            tvNumber = (TextView) view.findViewById(R.id.tvSmsSenderNumber);
            tvNotReadCount = (TextView) view.findViewById(R.id.tvNotMessageReadCount);
            tvAccount = (TextView) view.findViewById(R.id.tvAccount);
            tvAllExpense = (TextView) view.findViewById(R.id.tvAllExpense);
            tvAllIncomes = (TextView) view.findViewById(R.id.tvAllIncomes);
            tvMessagesCount = (TextView) view.findViewById(R.id.tvProcessedCout);
            info = (LinearLayout) view.findViewById(R.id.rlSmsAdapterInfo);
            llInfoGone = (LinearLayout) view.findViewById(R.id.llItemInfoView);
            addKeys = (ImageView) view.findViewById(R.id.imageView7);
            ivInfoIcon = (ImageView) view.findViewById(R.id.ivInfoIcon);
        }
    }
}
