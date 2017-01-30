package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.InfoDebtBorrowFragment;
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

public class DetailedDebtBorrowsFragment extends Fragment{
    private Calendar date;
    private RecyclerView rvRecordDetail;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private List<DebtBorrow> debtBorrows;
    private List<Recking> reckings;
    private boolean[] selections;
    Context context;
    @Inject DaoSession daoSession;
    @Inject ToolbarManager toolbarManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject DataCache dataCache;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private RecyclerView rvDetailedDebtBorrows;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_debt_borrows_fragment, container, false);
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
        list.addAll(debtBorrows);
        list.addAll(reckings);
        rvDetailedDebtBorrows = (RecyclerView) rootView.findViewById(R.id.rvDetailedDebtBorrows);
        rvDetailedDebtBorrows.setLayoutManager(new LinearLayoutManager(getContext()));
        DetailedDebtBorrowsAdapter adapter = new DetailedDebtBorrowsAdapter(list);
        rvDetailedDebtBorrows.setAdapter(adapter);
        return rootView;
    }

    private void initDatas() {
        debtBorrows = daoSession
                .queryBuilder(DebtBorrow.class)
                .where(DebtBorrowDao.Properties.TakenDate.eq(format.format(date.getTime())), DebtBorrowDao.Properties.Calculate.eq(true))
                .list();
        reckings = daoSession.queryBuilder(Recking.class)
                .where(ReckingDao.Properties.PayDate.eq(format.format(date.getTime())),
                        ReckingDao.Properties.AccountId.isNotNull(),
                        ReckingDao.Properties.AccountId.notEq(""))
                .list();
    }

    public void onResume() {
        super.onResume();
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
            toolbarManager.setTitle(getResources().getString(R.string.records));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd,LLL yyyy");
            toolbarManager.setSubtitle(dateFormat.format(date.getTime()));
        }
    }

    public class DetailedDebtBorrowsAdapter extends RecyclerView.Adapter<DetailedDebtBorrowsAdapter.DetailViewHolder>{
        List<Object> result;
        Context context;
        int mode = PocketAccounterGeneral.NORMAL_MODE;
        public DetailedDebtBorrowsAdapter(List<Object> result){
            this.result = result;
        }
        @Override
        public void onBindViewHolder(final DetailedDebtBorrowsAdapter.DetailViewHolder holder, final int position) {
            if (result.get(position).getClass().getName().equals(Recking.class.getName())) {
                final Recking recking = (Recking) result.get(position);
                holder.tvDetailedDebtBorrowsName.setText("Recking: " + format.format(recking.getPayDate().getTime()));
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, recking.getDebtBorrowsId());
                        bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.DETAIL);
                        InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
                        fragment.setArguments(bundle);
                        paFragmentManager.displayFragment(fragment);
                    }
                });
            } else {
                final DebtBorrow debtBorrow = (DebtBorrow) result.get(position);
                holder.tvDetailedDebtBorrowsName.setText("Debt borrow: " + debtBorrow.getPerson().getName() + ", " + format.format(debtBorrow.getTakenDate().getTime()));
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, debtBorrow.getId());
                        bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.DETAIL);
                        InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
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
        public DetailedDebtBorrowsAdapter.DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_debt_borrows_list_item, parent, false);
            DetailedDebtBorrowsAdapter.DetailViewHolder viewHolder = new DetailedDebtBorrowsAdapter.DetailViewHolder(v);
            return viewHolder;
        }

        public class DetailViewHolder extends RecyclerView.ViewHolder {
            TextView tvDetailedDebtBorrowsName;
            View view;
            public DetailViewHolder(View view) {
                super(view);
                tvDetailedDebtBorrowsName = (TextView) view.findViewById(R.id.tvDetailedDebtBorrowsName);
                this.view = view;
            }
        }
    }
}