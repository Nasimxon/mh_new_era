package com.jim.finansia.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.AccountOperation;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.DrawerInitializer;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PercentView;
import com.jim.finansia.utils.TransferDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by DEV on 06.09.2016.
 */

public class PurposeFragment extends Fragment{
    private RecyclerView rvPurposes;
    private FloatingActionButton fabPurposesAdd;
    public static final String PURPOSE_ID = "purpose_id";
    TextView ifListEmpty;
    @Inject ToolbarManager toolbarManager;
    @Inject DrawerInitializer drawerInitializer;
    @Inject DaoSession daoSession;
    @Inject LogicManager logicManager;
    @Inject PAFragmentManager paFragmentManager;
    @Inject TransferDialog transferDialog;
    @Inject ReportManager reportManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject CommonOperations commonOperations;
    @Inject FinansiaFirebaseAnalytics analytics;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.purpose_layout, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if (toolbarManager != null)
        {
            toolbarManager.setTitle(getString(R.string.purposes));
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
            toolbarManager.setSubtitle("");
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
            toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerInitializer.getDrawer().openLeftSide();
                }
            });
        }
        analytics.sendText("User enters to purpose fragment");
        ifListEmpty = (TextView) rootView.findViewById(R.id.ifListEmpty);
        rvPurposes = (RecyclerView) rootView.findViewById(R.id.rvPurposes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPurposes.setLayoutManager(layoutManager);
        fabPurposesAdd = (FloatingActionButton) rootView.findViewById(R.id.fabPurposesAdd);
        fabPurposesAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paFragmentManager.displayFragment(new PurposeEditFragment());
            }
        });
        refreshList();
        rvPurposes.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        return  rootView;
    }

    private void refreshList() {
        List<Purpose> purposes = daoSession.getPurposeDao().loadAll();
        String temp = Locale.getDefault().getCountry() + " ";
        for (Purpose purpose : purposes) {
            temp += purpose.getDescription() + ", ";
        }
        analytics.sendText(temp);
        PurposeAdapter adapter = new PurposeAdapter(purposes);
        if(daoSession.getPurposeDao().loadAll().size()==0){
            ifListEmpty.setVisibility(View.VISIBLE);
            ifListEmpty.setText(R.string.purpose_list_empty);
        }
        else ifListEmpty.setVisibility(View.GONE);
        rvPurposes.setAdapter(adapter);
    }


    private boolean show = false;
    public void onScrolledList(boolean k) {
        if (k) {
            if (!show)
                fabPurposesAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_down));
            show = true;
        } else {
            if (show)
                fabPurposesAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_up));
            show = false;
        }
    }


    private class PurposeAdapter extends RecyclerView.Adapter<PurposeFragment.ViewHolder> {
        private List<Purpose> result;
        public PurposeAdapter(List<Purpose> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final PurposeFragment.ViewHolder view, final int position) {
            final Purpose item = result.get(position);
            view.tvPurposeName.setText(item.getDescription());
            final int resId = getResources().getIdentifier(item.getIcon(), "drawable", getContext().getPackageName());
            view.ivPurposeItem.setImageResource(resId);
            view.tvPurposeName.setText(item.getDescription());
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    Bundle bundle = new Bundle();
                    bundle.putString(PurposeFragment.PURPOSE_ID, item.getId());;
                    PurposeInfoFragment fragment = new PurposeInfoFragment();
                    fragment.setArguments(bundle);
                    paFragmentManager.displayFragment(fragment);
                }
            });
            view.llPurposePayIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transferDialog.show();
                    transferDialog.setAccountOrPurpose(item.getId(), false);
                    transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
                        @Override
                        public void OnTransferDialogSave() {
                            notifyItemChanged(position);
                            transferDialog.dismiss();
                        }
                    });
                }
            });
            view.llPurposeSpend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transferDialog.show();
                    transferDialog.setAccountOrPurpose(item.getId(), true);
                    transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
                        @Override
                        public void OnTransferDialogSave() {
                            notifyItemChanged(position);
                            transferDialog.dismiss();
                        }
                    });
                }
            });
            double leftAmmountdb = lefAmmount(item);
            double allAmmount = item.getPurpose();
            double paid = allAmmount - leftAmmountdb;
            view.tvTotalAmount.setText(getResources().getString(R.string.purpose_ammount) + " "+parseToWithoutNull(allAmmount) + result.get(position).getCurrency().getAbbr());
            DecimalFormat format = new DecimalFormat("0.##");
            String abbr = result.get(position).getCurrency().getAbbr();
            String topText = getString(R.string.saved_money) + " " + format.format(paid) + abbr;
            String bottomText;
            if(leftAmmountdb >= 0)
                bottomText = getString(R.string.left_acomuleted) + " " + format.format(leftAmmountdb) + abbr;
            else
                bottomText = getString(R.string.left_acomuleted) + " " + 0 + abbr;
            view.pvPercent.setPercent((((int) (100*paid/allAmmount))<100)?((int)(100*paid/allAmmount)):100);
            view.pvPercent.setTopText(topText);
            view.pvPercent.setBottomText(bottomText);
            view.pvPercent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.pvPercent.animatePercent(0, 1200);
                }
            });
            view.pvPercent.animatePercent(0, 1200);
            view.pvPercent.setBowArrowVisibility(false);
        }

        public PurposeFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purpose_list_item, parent, false);
            return new PurposeFragment.ViewHolder(view);
        }
    }
    public String parseToWithoutNull(double A) {
        if (A == (int) A) {
            return Integer.toString((int) A);
        } else
            return dateFormat.format(A);
    }

    public Double lefAmmount(Purpose purpose){
        double qoldiq = 0;
        for (AccountOperation accountOperation: reportManager.getAccountOpertions(purpose)) {
            if (accountOperation.getTargetId().equals(purpose.getId()))
            qoldiq += commonOperations.getCost(accountOperation.getDate(), accountOperation.getCurrency(), purpose.getCurrency(),accountOperation.getAmount());
            else qoldiq -= commonOperations.getCost(accountOperation.getDate(), accountOperation.getCurrency(),purpose.getCurrency(), accountOperation.getAmount());
        }
        return purpose.getPurpose() - qoldiq;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llPurposePayIn, llPurposeSpend;
        ImageView ivPurposeItem;
        TextView tvPurposeName, tvTotalAmount;
        PercentView pvPercent;
        View view;
        public ViewHolder(View view) {
            super(view);
            llPurposePayIn = (LinearLayout) view.findViewById(R.id.llPurposePayIn);
            llPurposeSpend = (LinearLayout) view.findViewById(R.id.llPurposeSpend);
            ivPurposeItem = (ImageView) view.findViewById(R.id.ivPurposeItem);
            tvPurposeName = (TextView) view.findViewById(R.id.tvPurposeName);
            tvTotalAmount = (TextView) view.findViewById(R.id.tvTotalAmount);
            pvPercent = (PercentView) view.findViewById(R.id.pvPercent);
            this.view = view;
        }
    }
}
