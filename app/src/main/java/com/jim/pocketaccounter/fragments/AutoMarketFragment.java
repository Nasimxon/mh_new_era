package com.jim.pocketaccounter.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.AutoMarket;
import com.jim.pocketaccounter.database.AutoMarketDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.debt.DebtBorrowFragment;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;

import org.w3c.dom.Text;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by root on 9/15/16.
 */
public class AutoMarketFragment extends Fragment implements View.OnClickListener {
    @Inject
    DaoSession daoSession;
    @Inject
    LogicManager logicManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private AutoMarketDao autoMarketDao;
    private AutoAdapter autoAdapter;
    private TextView ifListEmpty;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        autoMarketDao = daoSession.getAutoMarketDao();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(false);
        View rootView = inflater.inflate(R.layout.auto_market_layout, container, false);
        ifListEmpty = (TextView) rootView.findViewById(R.id.ifListEmpty);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvAutoMarketFragment);
        autoAdapter = new AutoAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(autoAdapter);
        toolbarManager.setToolbarIconsVisibility(View.GONE,View.GONE,View.GONE);
        toolbarManager.setTitle(getResources().getString(R.string.auto_operations));
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setSubtitle("");
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fbAutoMarketAdd);
        floatingActionButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fbAutoMarketAdd: {
                paFragmentManager.getFragmentManager().popBackStack();
                paFragmentManager.displayFragment(new AddAutoMarketFragment());
                break;
            }
        }
    }

    private class AutoAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<AutoMarket> list;

        public AutoAdapter() {
            list = (ArrayList<AutoMarket>) autoMarketDao.loadAll();
            if(list.size()==0){
                ifListEmpty.setVisibility(View.VISIBLE);
                ifListEmpty.setText(R.string.auto_op_is_empty);
            }
            else {
                ifListEmpty.setVisibility(View.GONE);
            }
        }

        public int getItemCount() {
            return list.size();
        }

        public void onBindViewHolder(final ViewHolder view, final int position) {
            view.catName.setText(list.get(position).getRootCategory().getName());
            view.subCatName.setText(list.get(position).getSubCategory() != null ?
                    list.get(position).getSubCategory().getName() : "no sub categry");
            if(list.get(position).getSubCategory()==null)
            view.catIcon.setImageResource(getResources().getIdentifier(list.get(position).getRootCategory().getIcon(), "drawable", getActivity().getPackageName()));
            else             view.catIcon.setImageResource(getResources().getIdentifier(list.get(position).getSubCategory() .getIcon(), "drawable", getActivity().getPackageName()));

            if (list.get(position).getAmount() == (int) list.get(position).getAmount()) {
                view.amount.setText("" + ((int) list.get(position).getAmount()) + list.get(position).getCurrency().getAbbr());
            } else {
                view.amount.setText("" + list.get(position).getAmount() + list.get(position).getCurrency().getAbbr());
            }
            view.account.setText(list.get(position).getAccount().getName());
            String dates [] = list.get(position).getDates().split(",");
            int count = dates.length;
            String date = "";
            if (count >= 8) {
                for (int i = 0; i < dates.length; i++) {
                    date += count/2 == i ? ("\n" + dates[i] + ",") : dates[i] + ",";
                }
            } else {
                date = list.get(position).getDates();
            }
            view.period.setText(date);
            view.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddAutoMarketFragment addAutoMarketFragment = new AddAutoMarketFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("key", list.get(position).getId());
                    addAutoMarketFragment.setArguments(bundle);
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(addAutoMarketFragment);
                }
            });
            view.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getResources().getString(R.string.delete))
                            .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            logicManager.deleteAutoMarket(list.get(position));
                            list.remove(position);
                            notifyItemRemoved(position);
                        }
                    });
                    builder.create().show();
                }
            });
//            view.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    paFragmentManager.getFragmentManager().popBackStack();
//                    InfoAutoMarketFragment  infoAutoMarketFragment = new InfoAutoMarketFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("key", list.get(position).getId());
//                    infoAutoMarketFragment.setArguments(bundle);
//                    paFragmentManager.displayFragment(infoAutoMarketFragment);
//                }
//            });
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auto_market_item_layout_modern, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView catIcon;
        public TextView catName;
        public TextView subCatName;
        public TextView amount;
        public TextView period;
        public TextView edit;
        public TextView delete;
        public TextView account;

        public ViewHolder(View view) {
            super(view);
            catIcon = (ImageView) view.findViewById(R.id.ivItemAutoMarketCategory);
            catName = (TextView) view.findViewById(R.id.tvItemAutoMarketCatName);
            amount = (TextView) view.findViewById(R.id.tvItemAutoMarketAmount);
            subCatName = (TextView) view.findViewById(R.id.tvItemAutoMarketSubCatName);
            period = (TextView) view.findViewById(R.id.tvItemAutoMarketPerType);
            edit = (TextView) view.findViewById(R.id.tvAutoMarketItemEdit);
            delete = (TextView) view.findViewById(R.id.tvAutoMarketItemDelete);
            account = (TextView) view.findViewById(R.id.tvNal);
        }
    }
}