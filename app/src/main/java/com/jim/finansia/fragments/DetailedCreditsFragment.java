package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.ReckingCreditDao;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.photocalc.PhotoAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class DetailedCreditsFragment extends Fragment {
    private Calendar date;
    private RecyclerView rvRecordDetail;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private List<CreditDetials> credits;
    private List<ReckingCredit> reckings;
    private boolean[] selections;
    Context context;
    @Inject DaoSession daoSession;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject DataCache dataCache;
    @Inject DecimalFormat formatter;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private RecyclerView rvDetailedCredits;
    private AccountDao accountDao;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_credits_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        date = Calendar.getInstance();
        if (getArguments() != null) {
            String tempDate = getArguments().getString(RecordDetailFragment.DATE);
            try {
                date.setTime(format.parse(tempDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        initDatas();
        List<Object> list = new ArrayList<>();
        list.addAll(credits);
        list.addAll(reckings);
        rvDetailedCredits = (RecyclerView) rootView.findViewById(R.id.rvDetailedCredits);
        rvDetailedCredits.setLayoutManager(new LinearLayoutManager(getContext()));
        DetailedCreditsAdapter adapter = new DetailedCreditsAdapter(list);
        rvDetailedCredits.setAdapter(adapter);
        return rootView;
    }

    private void initDatas() {
        credits = daoSession
                .queryBuilder(CreditDetials.class)
                .where(CreditDetialsDao.Properties.Take_time.eq(format.format(date.getTime())), CreditDetialsDao.Properties.Key_for_include.eq(true))
                .list();
        reckings = daoSession.queryBuilder(ReckingCredit.class)
                .where(ReckingCreditDao.Properties.PayDate.eq(format.format(date.getTime())),
                        ReckingCreditDao.Properties.AccountId.isNotNull(),
                        ReckingCreditDao.Properties.AccountId.notEq(""))
                .list();

    }

    public void onResume() {
        super.onResume();
    }

    public class DetailedCreditsAdapter extends RecyclerView.Adapter<DetailedCreditsAdapter.DetailViewHolder>{
        List<Object> result;
        Context context;
        int mode = PocketAccounterGeneral.NORMAL_MODE;
        List<Currency> currencies;
        List<Account> allAccounts;
        public DetailedCreditsAdapter(List<Object> result){
            this.result = result;
            currencies = daoSession.getCurrencyDao().loadAll();
            allAccounts = daoSession.loadAll(Account.class);
            context = getContext();
        }
        @Override
        public void onBindViewHolder(final DetailedCreditsAdapter.DetailViewHolder holder, final int position) {

            if (result.get(position).getClass().getName().equals(ReckingCredit.class.getName())) {
                ReckingCredit reckingCredit = (ReckingCredit) result.get(position);
                final CreditDetials creditDetials = daoSession.load(CreditDetials.class,reckingCredit.getMyCredit_id());

                if (reckingCredit.getComment()==null||((ReckingCredit) result.get(position)).getComment().matches("")){
                    holder.visibleIfCommentHaveCredit.setVisibility(View.GONE);
                }
                else {
                    holder.tvCreditComment.setText(reckingCredit.getComment());
                    holder.visibleIfCommentHaveCredit.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < allAccounts.size(); i++) {
                    if (allAccounts.get(i).getId().equals(reckingCredit.getAccountId()))
                    {
                        holder.tvCreditAccount.setText(allAccounts.get(i).getName());
                    }
                }

                String cur = null;
                for (int i = 0; i < currencies.size(); i++) {
                    if (currencies.get(i).getId().equals(creditDetials.getCurrencyId()))
                    {
                        cur = currencies.get(i).getAbbr();
                    }
                }
                if(creditDetials!=null){
                    int resId = context.getResources().getIdentifier(creditDetials.getIcon_ID(), "drawable", context.getPackageName());
                    holder.ivCreditImage.setImageResource(resId);
                }
                holder.tvCreditAmount.setTextColor(ContextCompat.getColor(context, R.color.record_red));
                holder.tvCreditAmount.setText("- " + formatter.format(reckingCredit.getAmount()) + cur);
                holder.tvDetailedCreditsName.setText(getString(R.string.payment_to)+creditDetials.getCredit_name());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putLong(CreditTabLay.CREDIT_ID, creditDetials.getMyCredit_id());
                        bundle.putInt(CreditTabLay.MODE, PocketAccounterGeneral.DETAIL);
                        InfoCreditFragment fragment = new InfoCreditFragment();
                        fragment.setArguments(bundle);
                        paFragmentManager.displayFragment(fragment);
                    }
                });

            } else {
                final CreditDetials creditDetials = (CreditDetials) result.get(position);
                String cur = null;
                for (int i = 0; i < currencies.size(); i++) {
                    if (currencies.get(i).getId().equals(creditDetials.getCurrencyId()))
                    {
                        cur = currencies.get(i).getAbbr();
                    }
                }
                for (int i = 0; i < allAccounts.size(); i++) {
                    if (allAccounts.get(i).getId().equals(creditDetials.getAccountID()))
                    {
                        holder.tvCreditAccount.setText(allAccounts.get(i).getName());
                    }
                }
                holder.tvCreditAmount.setTextColor(ContextCompat.getColor(context, R.color.record_green));
                holder.tvCreditAmount.setText("+ " + formatter.format(creditDetials.getValue_of_credit()) + cur);
                holder.tvDetailedCreditsName.setText(creditDetials.getCredit_name());
                int resId = context.getResources().getIdentifier(creditDetials.getIcon_ID(), "drawable", context.getPackageName());
                holder.ivCreditImage.setImageResource(resId);
                holder.visibleIfCommentHaveCredit.setVisibility(View.VISIBLE);
                holder.tvCreditComment.setText(R.string.credit_info);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putLong(CreditTabLay.CREDIT_ID, creditDetials.getMyCredit_id());
                        bundle.putInt(CreditTabLay.MODE, PocketAccounterGeneral.DETAIL);
                        InfoCreditFragment fragment = new InfoCreditFragment();
                        fragment.setArguments(bundle);
                        paFragmentManager.displayFragment(fragment);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return result.size();
        }
        @Override
        public DetailedCreditsAdapter.DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_credits_list_item, parent, false);
            DetailedCreditsAdapter.DetailViewHolder viewHolder = new DetailedCreditsAdapter.DetailViewHolder(v);
            return viewHolder;
        }

        public class DetailViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCreditImage;
            TextView tvCreditAccount;
            TextView tvCreditAmount;
            TextView tvCreditComment;
            TextView tvDetailedCreditsName;
            LinearLayout visibleIfCommentHaveCredit;
            View view;
            public DetailViewHolder(View view) {
                super(view);
                tvDetailedCreditsName = (TextView) view.findViewById(R.id.tvDetailedCreditsName);
                tvCreditAccount = (TextView) view.findViewById(R.id.tvCreditAccount);
                tvCreditAmount = (TextView) view.findViewById(R.id.tvCreditAmount);
                tvCreditComment = (TextView) view.findViewById(R.id.tvCreditComment);
                ivCreditImage = (ImageView) view.findViewById(R.id.ivCreditDetail);
                visibleIfCommentHaveCredit = (LinearLayout) view.findViewById(R.id.visibleIfCommentHaveCredit);
                this.view = view;
            }
        }
    }
}