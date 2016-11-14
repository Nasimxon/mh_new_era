package com.jim.pocketaccounter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by root on 11/14/16.
 */

public class AfterSavedVoiceFragment extends PABaseFragment {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.after_saved_voice_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvAfterSavedVoice);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MyAfterSavedAdapter());
        return rootView;
    }

    private class MyAfterSavedAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<FinanceRecord> financeRecords;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        public MyAfterSavedAdapter() {
            financeRecords = daoSession.getFinanceRecordDao().queryBuilder()
                    .orderAsc(FinanceRecordDao.Properties.Date).list();
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.itemCatName.setText(financeRecords.get(position).getCategory().getName());
            holder.itemAmount.setText("" + financeRecords.get(position).getAmount() +
                    financeRecords.get(position).getCurrency().getAbbr());
            holder.itemDate.setText(simpleDateFormat.format(financeRecords.get(position).getDate().getTime()));
        }

        @Override
        public int getItemCount() {
            return financeRecords.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_after_saved_fragment, parent, false);
            return new MyViewHolder(view);
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView itemDate;
        public TextView itemCatName;
        public TextView itemAmount;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemDate = (TextView) itemView.findViewById(R.id.tvItemAfterSavedDate);
            itemAmount = (TextView) itemView.findViewById(R.id.tvItemAfterSavedAmount);
            itemCatName = (TextView) itemView.findViewById(R.id.tvItemAfterSavedCatName);
        }
    }
}