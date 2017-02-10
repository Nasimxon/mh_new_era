package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingDao;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.database.SmsParseSuccessDao;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class DetailedSmsSuccessesFragment extends Fragment {
    private Calendar date;
    private RecyclerView rvRecordDetail;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private List<SmsParseSuccess> smses;
    private boolean[] selections;
    Context context;
    @Inject DaoSession daoSession;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject DataCache dataCache;
    @Inject FinansiaFirebaseAnalytics analytics;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private RecyclerView rvDetailedSms;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_sms_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        analytics.sendText("User enters " + getClass().getName());
        if (toolbarManager != null)
        {
            toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
            toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int size = 0;
                    size = paFragmentManager.getFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < size; i++)
                        paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayMainWindow();
                }
            });
        }
        date = Calendar.getInstance();
        if (getArguments() != null) {
            String tempDate = getArguments().getString(RecordDetailFragment.DATE);
            try {
                date.setTime(format.parse(tempDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        rvDetailedSms = (RecyclerView) rootView.findViewById(R.id.rvDetailedSms);
        rvDetailedSms.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshList();
        return rootView;
    }

    public void refreshList() {
        smses = daoSession
                .queryBuilder(SmsParseSuccess.class)
                .where(SmsParseSuccessDao.Properties.Date.eq(format.format(date.getTime())),
                        SmsParseSuccessDao.Properties.IsSuccess.eq(true))
                .list();
        DetailedDebtBorrowsAdapter adapter = new DetailedDebtBorrowsAdapter(smses);
        if (smses.isEmpty()) {
            rvDetailedSms.setVisibility(View.GONE);
        } else {
            rvDetailedSms.setVisibility(View.VISIBLE);
        }
        rvDetailedSms.setAdapter(adapter);
    }

    public class DetailedDebtBorrowsAdapter extends RecyclerView.Adapter<DetailedDebtBorrowsAdapter.DetailViewHolder>{
        List<SmsParseSuccess> result;
        Context context;
        int mode = PocketAccounterGeneral.NORMAL_MODE;
        public DetailedDebtBorrowsAdapter(List<SmsParseSuccess> result){
            this.result = result;
        }
        @Override
        public void onBindViewHolder(final DetailedDebtBorrowsAdapter.DetailViewHolder holder, final int position) {
            String sign = "";
            holder.tvDetailedSmsName.setText(result.get(position).getNumber());
            holder.tvSmsParsingDate.setText(format.format(result.get(position).getDate().getTime()));
            if (result.get(position).getType() == PocketAccounterGeneral.EXPENSE) {
                holder.tvSmsAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.record_red));
                sign = "-";
            }
            else {
                holder.tvSmsAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.record_green));
                sign = "+";
            }
            holder.tvSmsAmount.setText(sign + (result.get(position).getAmount()) + " " + result.get(position).getCurrency().getAbbr());
            holder.tvSmsAccount.setText(result.get(position).getAccount().getName());
        }

        @Override
        public int getItemCount() {
            return result.size();
        }
        @Override
        public DetailedDebtBorrowsAdapter.DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_sms_list_item, parent, false);
            DetailedDebtBorrowsAdapter.DetailViewHolder viewHolder = new DetailedDebtBorrowsAdapter.DetailViewHolder(v);
            return viewHolder;
        }

        public class DetailViewHolder extends RecyclerView.ViewHolder {
            TextView tvDetailedSmsName;
            TextView tvSmsAmount;
            TextView tvSmsParsingDate;
            TextView tvSmsAccount;
            View view;
            public DetailViewHolder(View view) {
                super(view);
                tvDetailedSmsName = (TextView) view.findViewById(R.id.tvDetailedSmsName);
                tvSmsAmount = (TextView) view.findViewById(R.id.tvSmsAmount);
                tvSmsParsingDate = (TextView) view.findViewById(R.id.tvSmsParsingDate);
                tvSmsAccount = (TextView) view.findViewById(R.id.tvSmsAccountName);
                this.view = view;
            }
        }
    }
}