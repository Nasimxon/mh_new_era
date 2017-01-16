package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.AccountOperation;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.database.PurposeDao;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.report.FilterSelectable;
import com.jim.finansia.utils.FilterDialog;
import com.jim.finansia.utils.OperationsListDialog;
import com.jim.finansia.utils.PercentView;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.TransferDialog;
import com.jim.finansia.utils.reportfilter.IntervalPickDialog;
import com.jim.finansia.utils.reportfilter.IntervalPickerView;

import org.greenrobot.greendao.query.Query;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by root on 9/7/16.
 */
@SuppressLint("ValidFragment")
public class PurposeInfoFragment extends Fragment implements View.OnClickListener {
    @Inject ToolbarManager toolbarManager;
    @Inject OperationsListDialog operationsListDialog;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject ReportManager reportManager;
    @Inject DaoSession daoSession;
    @Inject TransferDialog transferDialog;
    @Inject CommonOperations commonOperations;
    IntervalPickDialog filterDialog;
    private MyAdapter myAdapter;
    private Purpose purpose;
    private ImageView iconPurpose;
    private ImageView filterOpertions;
    private TextView namePurpose;
    private TextView amountPurpose;
    private LinearLayout cashAdd;
    private LinearLayout cashSend;
    private TextView myLefDate;
    private TextView Allcashes;
    private RecyclerView recyclerView;
    private boolean MODE = false;
    private RelativeLayout forGoneLeftDate;
    private Calendar beginDate;
    private Calendar endDate;
    private PercentView pvPercent;
    private DecimalFormat format = new DecimalFormat("0.##");
    public PurposeInfoFragment(Purpose purpose) {
        this.purpose = purpose;
        if (purpose == null) {
            this.purpose = new Purpose();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rooView = inflater.inflate(R.layout.purpose_info_layout, container, false);
        beginDate = null;
        endDate = null;
        filterDialog = new IntervalPickDialog(getContext());
        forGoneLeftDate = (RelativeLayout) rooView.findViewById(R.id.forGoneLeftDate);
//        deleteOpertions = (ImageView) rooView.findViewById(R.id.ivPurposeInfoDelete);
        filterOpertions = (ImageView) rooView.findViewById(R.id.ivPurposeInfoFilter);
        cashAdd = (LinearLayout) rooView.findViewById(R.id.tvPurposeInfoToCash);
        cashSend = (LinearLayout) rooView.findViewById(R.id.tvPurposeInfoReplanish);
        myLefDate = (TextView) rooView.findViewById(R.id.leftdate);
        Allcashes = (TextView) rooView.findViewById(R.id.totaly);
        pvPercent = (PercentView) rooView.findViewById(R.id.pvPercent);
        pvPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvPercent.animatePercent(0, 1200);
            }
        });
        pvPercent.setPercetStripeVisibility(false);
        toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
        toolbarManager.setOnTitleClickListener(null);
        toolbarManager.setSubtitle(getResources().getString(R.string.information));
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] st = new String[2];
                st[0] = getResources().getString(R.string.to_edit);
                st[1] = getResources().getString(R.string.delete);
                operationsListDialog.setAdapter(st);
                operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            paFragmentManager.displayFragment(new PurposeEditFragment(purpose));
                            operationsListDialog.dismiss();
                        } else {
                            switch (logicManager.deletePurpose(purpose)) {
                                case LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND: {
                                    Toast.makeText(getContext(), "No this purpose", Toast.LENGTH_SHORT).show();
                                    operationsListDialog.dismiss();
                                    break;
                                }
                                case LogicManagerConstants.DELETED_SUCCESSFUL: {
                                    paFragmentManager.getFragmentManager().popBackStack();
                                    paFragmentManager.displayFragment(new PurposeFragment());
                                    operationsListDialog.dismiss();
                                    break;
                                }
                            }
                        }
                    }
                });
                operationsListDialog.show();
            }
        });
        iconPurpose = (ImageView) rooView.findViewById(R.id.ivPurposeinfoIcon);
        namePurpose = (TextView) rooView.findViewById(R.id.tvPurposeInfoName);
        amountPurpose = (TextView) rooView.findViewById(R.id.tvPurposeInfoLeftAmount);
        recyclerView = (RecyclerView) rooView.findViewById(R.id.rvPurposeInfo);
        calculateTotal();
        // ------- LEFT DATE MODUL -------

        if(purpose.getBegin()!=null&&purpose.getEnd()!=null){
            forGoneLeftDate.setVisibility(View.VISIBLE);
        int t[]=InfoCreditFragment.getDateDifferenceInDDMMYYYY(purpose.getBegin().getTime(),purpose.getEnd().getTime());
        if(t[0]*t[1]*t[2]<0&&(t[0]+t[1]+t[2])!=0){
            myLefDate.setText(R.string.ends);
            myLefDate.setTextColor(Color.parseColor("#832e1c"));
        } else {
            String left_date_string = "";
            if (t[0] != 0) {
                if (t[0] > 1) {
                    left_date_string += Integer.toString(t[0]) + " " + getString(R.string.years);
                } else {
                    left_date_string += Integer.toString(t[0]) + " " + getString(R.string.year);
                }

            }
            if(t[1]!=0){
                if(!left_date_string.matches("")){
                    left_date_string+=" ";
                }
                if(t[1]>1){
                    left_date_string+=Integer.toString(t[1])+" "+getString(R.string.moths);
                }
                else{
                    left_date_string+=Integer.toString(t[1])+" "+getString(R.string.moth);
                }
            }
            if(t[2]!=0){
                if(!left_date_string.matches("")){
                    left_date_string+=" ";
                }
                if(t[2]>1){
                    left_date_string+=Integer.toString(t[2])+" "+getString(R.string.days);
                }
                else{
                    left_date_string+=Integer.toString(t[2])+" "+getString(R.string.day);
                }
            }
            if(!left_date_string.equals(""))
                myLefDate.setText(left_date_string);
            else myLefDate.setText(R.string.ends);
        }
        }else {
            forGoneLeftDate.setVisibility(View.GONE);
        }

        iconPurpose = (ImageView) rooView.findViewById(R.id.ivPurposeinfoIcon);
        namePurpose = (TextView) rooView.findViewById(R.id.tvPurposeInfoName);
        amountPurpose = (TextView) rooView.findViewById(R.id.tvPurposeInfoLeftAmount);
        recyclerView = (RecyclerView) rooView.findViewById(R.id.rvPurposeInfo);
        // ---------- icon set start ---------
        int resId = getResources().getIdentifier(purpose.getIcon(), "drawable", getContext().getPackageName());

        iconPurpose.setImageResource(resId);
        // ---------- end icon set ---------
        namePurpose.setText(purpose.getDescription());
     //     deleteOpertions.setOnClickListener(this);
        filterOpertions.setOnClickListener(this);
        cashAdd.setOnClickListener(this);
        cashSend.setOnClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        return rooView;
    }

    private void calculateTotal() {
        double total = 0;
        List<AccountOperation> operationList = reportManager.getAccountOpertions(purpose);
        for (AccountOperation accountOperation : operationList) {
            if (accountOperation.getSourceId().equals(purpose.getId()))
                total -= commonOperations.getCost(Calendar.getInstance(), accountOperation.getCurrency(), purpose.getCurrency(), accountOperation.getAmount());
            else if (accountOperation.getTargetId().equals(purpose.getId()))
                total += commonOperations.getCost(Calendar.getInstance(), accountOperation.getCurrency(), purpose.getCurrency(), accountOperation.getAmount());
        }
        double leftAmount = purpose.getPurpose() - total;
        if (Allcashes != null)
            Allcashes.setText(format.format(total)+purpose.getCurrency().getAbbr());
        if (amountPurpose != null)
            amountPurpose.setText(format.format(leftAmount) + purpose.getCurrency().getAbbr());
        DecimalFormat format = new DecimalFormat("0.##");
        String abbr = commonOperations.getMainCurrency().getAbbr();
        String bottomText;
        if(leftAmount > 0)
            bottomText = getString(R.string.left_acomuleted) + " " + format.format(leftAmount) + abbr;
        else
            bottomText = getString(R.string.left_acomuleted) + " " + 0 + abbr;
        pvPercent.setPercent((((int) (100*total/purpose.getPurpose()))<100)?(int)(100*total/purpose.getPurpose()):100);
        amountPurpose.setText(bottomText);
        pvPercent.animatePercent(0, 1200);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPurposeInfoDelete: {
                if (MODE) {
                    myAdapter.deleteOperation();
                }
                MODE = !MODE;
                myAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.ivPurposeInfoFilter: {
                filterDialog.show();
                filterDialog.setListener(new IntervalPickerView.IntervalPickListener() {
                    @Override
                    public void onIntervalPick(Calendar begin, Calendar end) {
                        beginDate = (Calendar) begin.clone();
                        endDate = (Calendar) end.clone();
                        myAdapter.refreshFilterPurpose();
                        filterDialog.dismiss();
                    }

                    @Override
                    public void onCancelPick() {
                        filterDialog.dismiss();
                    }
                });
                int width = getResources().getDisplayMetrics().widthPixels;
                filterDialog.getWindow().setLayout(8*width/9, ViewGroup.LayoutParams.WRAP_CONTENT);
                filterDialog.show();
                break;
            }
            //TODO Nasimxon pul otkazilgandan kiyin adapter bilan total sumni yam yangilavoriw kere
            case R.id.tvPurposeInfoToCash: {
                transferDialog.show();
                transferDialog.setAccountOrPurpose(purpose.getId(), false);
                transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
                    @Override
                    public void OnTransferDialogSave() {
                        myAdapter = new MyAdapter();
                        recyclerView.setAdapter(myAdapter);
                    }
                });
                break;

            }
            case R.id.tvPurposeInfoReplanish: {
                transferDialog.show();
                transferDialog.setAccountOrPurpose(purpose.getId(), true);
                transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
                    @Override
                    public void OnTransferDialogSave() {
                        myAdapter = new MyAdapter();
                        recyclerView.setAdapter(myAdapter);
                    }
                });
                break;
            }
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<AccountOperation> purposes;
        private ArrayList<AccountOperation> allPurposes;
        private boolean tek[];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        public MyAdapter() {
            allPurposes = (ArrayList<AccountOperation>) reportManager.getAccountOpertions(purpose);
            purposes = (ArrayList<AccountOperation>) allPurposes.clone();
            calculateTotal();
            tek = new boolean[purposes.size()];
        }

        public int getItemCount() {
            return purposes.size();
        }

        public void deleteOperation() {
            for (int i = tek.length - 1; i >= 0; i--) {
                if (tek[i]) {
                    logicManager.deleteAccountOperation(purposes.get(i));
                }
            }
            allPurposes = (ArrayList<AccountOperation>) reportManager.getAccountOpertions(purpose);
            purposes = (ArrayList<AccountOperation>) allPurposes.clone();
            tek = new boolean[purposes.size()];
            refreshFilterPurpose();
        }

        public void refreshFilterPurpose() {
            if (beginDate != null && endDate != null) {
                for (AccountOperation pr : allPurposes) {
                    if (pr.getDate().after(endDate) || pr.getDate().before(beginDate)) {
                        purposes.remove(pr);
                    } else {
                        if (purposes.indexOf(pr) == -1) {
                            purposes.add(pr);
                        }
                    }
                }
            }
            calculateTotal();
        }

        public void onBindViewHolder(final ViewHolder view, final int position) {
            int type = purpose.getId().matches(purposes.get(position).getSourceId())
                    ? PocketAccounterGeneral.EXPENSE : PocketAccounterGeneral.INCOME;

            view.dateOperation.setText(simpleDateFormat.format(purposes.get(position).getDate().getTime()));

            String name = "", sign = "";
            int color = 0;

            if (type == PocketAccounterGeneral.EXPENSE) {
                Query query = daoSession.getAccountDao().queryBuilder()
                        .where(AccountDao.Properties.Id.eq(purposes.get(position).getTargetId())).build();
                if (!query.list().isEmpty()) {
                    name = ((Account) query.list().get(0)).getName();
                } else {
                    query = daoSession.getPurposeDao().queryBuilder()
                            .where(PurposeDao.Properties.Id.eq(purposes.get(position).getTargetId())).build();
                    if (!query.list().isEmpty())
                        name = ((Purpose) query.list().get(0)).getDescription();
                }
                sign = "-";
                color = ContextCompat.getColor(getContext(), R.color.red);
            } else {
                Query query = daoSession.getAccountDao().queryBuilder()
                        .where(AccountDao.Properties.Id.eq(purposes.get(position).getSourceId())).build();
                if (!query.list().isEmpty()) {
                    name = ((Account) query.list().get(0)).getName();
                } else {
                    query = daoSession.getPurposeDao().queryBuilder()
                            .where(PurposeDao.Properties.Id.eq(purposes.get(position).getSourceId())).build();
                    if (!query.list().isEmpty()) {
                        name = ((Purpose) query.list().get(0)).getDescription();
                    }
                }
                sign = "+";
                color = ContextCompat.getColor(getContext(), R.color.green_just);
            }
            view.amount.setTextColor(color);
            view.amount.setText(sign + purposes.get(position).getAmount() + purposes.get(position).getCurrency().getAbbr());
            view.checkBox.setVisibility(View.GONE);
            view.accountName.setText(name);
            view.itemView.setOnClickListener(null);
            if (MODE) {
                view.checkBox.setVisibility(View.VISIBLE);
                view.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        tek[position] = !tek[position];
                    }
                });
                view.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.checkBox.setChecked(!tek[position]);
                    }
                });
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_info_operations, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView dateOperation;
        public TextView accountName;
        public TextView amount;

        public ViewHolder(View view) {
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.ivAccountInfoOpertionDelete);
            dateOperation = (TextView) view.findViewById(R.id.tvAccountInfoDate);
            accountName = (TextView) view.findViewById(R.id.tvAccountInfoName);
            amount = (TextView) view.findViewById(R.id.tvAccountInfoAmount);
        }
    }
}
