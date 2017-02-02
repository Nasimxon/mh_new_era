package com.jim.finansia.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.Currency;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
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
    @Inject DecimalFormat formatter;
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
        rvDetailedDebtBorrows = (RecyclerView) rootView.findViewById(R.id.rvDetailedDebtBorrows);
        rvDetailedDebtBorrows.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshList();
        return rootView;
    }

    public void refreshList() {
        debtBorrows = daoSession
                .queryBuilder(DebtBorrow.class)
                .where(DebtBorrowDao.Properties.TakenDate.eq(format.format(date.getTime())), DebtBorrowDao.Properties.Calculate.eq(true))
                .list();
        reckings = daoSession.queryBuilder(Recking.class)
                .where(ReckingDao.Properties.PayDate.eq(format.format(date.getTime())),
                        ReckingDao.Properties.AccountId.isNotNull(),
                        ReckingDao.Properties.AccountId.notEq(""))
                .list();
        List<Object> list = new ArrayList<>();
        list.addAll(debtBorrows);
        list.addAll(reckings);
        DetailedDebtBorrowsAdapter adapter = new DetailedDebtBorrowsAdapter(list);
        if (list.isEmpty()) {
            rvDetailedDebtBorrows.setVisibility(View.GONE);
        } else {
            rvDetailedDebtBorrows.setVisibility(View.VISIBLE);
        }
        rvDetailedDebtBorrows.setAdapter(adapter);
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
        }
    }

    public class DetailedDebtBorrowsAdapter extends RecyclerView.Adapter<DetailedDebtBorrowsAdapter.DetailViewHolder>{
        List<Object> result;
        Context context;
        List<Currency> currencies;
        List<Account> allAccounts;
        int mode = PocketAccounterGeneral.NORMAL_MODE;
        public DetailedDebtBorrowsAdapter(List<Object> result){
            this.result = result;
            context = getContext();
            currencies = daoSession.getCurrencyDao().loadAll();
            allAccounts = daoSession.loadAll(Account.class);
        }
        @Override
        public void onBindViewHolder(final DetailedDebtBorrowsAdapter.DetailViewHolder holder, final int position) {
            if (result.get(position).getClass().getName().equals(Recking.class.getName())) {
                final Recking recking = (Recking) result.get(position);
                DebtBorrow debtBorrow = daoSession.load(DebtBorrow.class, recking.getDebtBorrowsId());
                String sign = "";
                String cur = null;
                if (debtBorrow != null) {
                    holder.tvDetailedDebtBorrowsName.setText(debtBorrow.getPerson().getName());
                    if (debtBorrow.getType() == PocketAccounterGeneral.EXPENSE) {
                        holder.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.record_red));
                        sign = "-";
                        holder.tvDebtName.setText(R.string.i_returned);
                    } else {
                        holder.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.record_green));
                        sign = "+";
                        holder.tvDebtName.setText(R.string.returned_debt);
                    }
                    if(!debtBorrow.getPerson().getPhoto().isEmpty() && !debtBorrow.getPerson().getPhoto().equals("0")){
                        try {
                            holder.ivDebtImage.setImageBitmap(queryContactImage(Integer.parseInt(debtBorrow.getPerson().getPhoto())));
                        } catch (Exception e) {
                            holder.ivDebtImage.setImageBitmap(decodeFile(new File(debtBorrow.getPerson().getPhoto())));
                        }
                    } else {
                        holder.ivDebtImage.setImageResource(R.drawable.no_photo);
                    }
                    for (int i = 0; i < currencies.size(); i++) {
                        if (currencies.get(i).getId().equals(debtBorrow.getCurrencyId()))
                        {
                            cur = currencies.get(i).getAbbr();
                        }
                    }
                }
                for (int i = 0; i < allAccounts.size(); i++) {
                    if (allAccounts.get(i).getId().equals(recking.getAccountId()))
                    {
                        holder.tvDebtAccount.setText(allAccounts.get(i).getName());
                    }
                }
                if (recking.getComment()==null||((Recking) result.get(position)).getComment().matches("")){
                    holder.visibleIfCommentHaveDebt.setVisibility(View.GONE);
                }
                else {
                    holder.tvDebtComment.setText(recking.getComment());
                    holder.visibleIfCommentHaveDebt.setVisibility(View.VISIBLE);
                }
                holder.tvDebtAmount.setText(sign + " " + formatter.format(recking.getAmount()) + cur);
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
                holder.tvDetailedDebtBorrowsName.setText(debtBorrow.getPerson().getName());
                String sign = "";
                if (debtBorrow.getType() == PocketAccounterGeneral.EXPENSE) {
                    holder.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.record_green));
                    sign = "+";
                    holder.tvDebtName.setText(R.string.took_from);
                }
                else {
                    holder.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.record_red));
                    sign = "-";
                    holder.tvDebtName.setText(R.string.debtor);
                }
                for (int i = 0; i < allAccounts.size(); i++) {
                    if (allAccounts.get(i).getId().equals(debtBorrow.getAccountId()))
                    {
                        holder.tvDebtAccount.setText(allAccounts.get(i).getName());
                    }
                }

                String cur = null;
                for (int i = 0; i < currencies.size(); i++) {
                    if (currencies.get(i).getId().equals(debtBorrow.getCurrencyId()))
                    {
                        cur = currencies.get(i).getAbbr();
                    }
                }
                holder.tvDebtAmount.setText(sign + " " + formatter.format(debtBorrow.getAmount()) + cur);
                if(!debtBorrow.getPerson().getPhoto().isEmpty() && !debtBorrow.getPerson().getPhoto().equals("0")){
                    try {
                        holder.ivDebtImage.setImageBitmap(queryContactImage(Integer.parseInt(debtBorrow.getPerson().getPhoto())));
                    } catch (Exception e) {
                        holder.ivDebtImage.setImageBitmap(decodeFile(new File(debtBorrow.getPerson().getPhoto())));
                    }
                } else {
                    holder.ivDebtImage.setImageResource(R.drawable.no_photo);
                }
                holder.tvDebtComment.setText(R.string.taken_debt);
                holder.visibleIfCommentHaveDebt.setVisibility(View.VISIBLE);
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
            TextView tvDebtAmount;
            TextView tvDebtAccount;
            TextView tvDebtComment;
            TextView tvDebtName;
            ImageView ivDebtImage;
            LinearLayout visibleIfCommentHaveDebt;
            View view;
            public DetailViewHolder(View view) {
                super(view);
                tvDetailedDebtBorrowsName = (TextView) view.findViewById(R.id.tvDetailedDebtBorrowsName);
                tvDebtAmount = (TextView) view.findViewById(R.id.tvDebtAmount);
                tvDebtAccount = (TextView) view.findViewById(R.id.tvDebtAccountName);
                tvDebtComment = (TextView) view.findViewById(R.id.tvDebtComment);
                tvDebtName = (TextView) view.findViewById(R.id.tvDebtName);
                ivDebtImage = (ImageView) view.findViewById(R.id.ivDebtImage);
                visibleIfCommentHaveDebt = (LinearLayout) view.findViewById(R.id.visibleIfCommentHaveDebt);
                this.view = view;
            }
        }
    }
    private Bitmap queryContactImage(int imageDataRow) {
        Cursor c = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                ContactsContract.CommonDataKinds.Photo.PHOTO
        }, ContactsContract.Data._ID + "=?", new String[]{
                Integer.toString(imageDataRow)
        }, null);
        byte[] imageBytes = null;
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0);
            }
            c.close();
        }
        if (imageBytes != null) {
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }
    private Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            final int REQUIRED_SIZE = 128;
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}