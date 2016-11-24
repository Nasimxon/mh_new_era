package com.jim.pocketaccounter.fragments;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.AutoMarket;
import com.jim.pocketaccounter.database.AutoMarketDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.debt.DebtBorrowFragment;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import jxl.format.VerticalAlignment;

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
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
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
            Collections.sort(list, new Comparator<AutoMarket>() {
                @Override
                public int compare(AutoMarket autoMarket, AutoMarket t1) {
                    return autoMarket.getCreateDay().compareTo(t1.getCreateDay());
                }
            });
            if (list.size() == 0) {
                ifListEmpty.setVisibility(View.VISIBLE);
                ifListEmpty.setText(R.string.auto_op_is_empty);
            } else {
                ifListEmpty.setVisibility(View.GONE);
            }
        }

        public int getItemCount() {
            return list.size();
        }

        private DaysAdapter daysAdapter;
        private RecordAdapter recordAdapter;

        public void onBindViewHolder(final ViewHolder view, final int position) {
            view.catName.setText(list.get(position).getRootCategory().getName());
            view.recyclerView.setVisibility(View.GONE);
            view.llAutoMarketItemSwitchMode.setVisibility(View.GONE);
            view.llAutoMarketItemDefaultMode.setVisibility(View.VISIBLE);
            view.subCatName.setText(list.get(position).getSubCategory() != null ?
                    list.get(position).getSubCategory().getName() : "no sub categry");
            if (list.get(position).getSubCategory() == null)
                view.catIcon.setImageResource(getResources().getIdentifier(list.get(position).getRootCategory().getIcon(), "drawable", getActivity().getPackageName()));
            else
                view.catIcon.setImageResource(getResources().getIdentifier(list.get(position).getSubCategory().getIcon(), "drawable", getActivity().getPackageName()));

            if (list.get(position).getAmount() == (int) list.get(position).getAmount()) {
                view.amount.setText("" + ((int) list.get(position).getAmount()) + list.get(position).getCurrency().getAbbr());
            } else {
                view.amount.setText("" + list.get(position).getAmount() + list.get(position).getCurrency().getAbbr());
            }
            view.account.setText(list.get(position).getAccount().getName());
            view.llAutoMarketItemDays.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // todo marked days introduction
                    recordAdapter = null;
                    if (view.recyclerView.getVisibility() == View.GONE || daysAdapter == null) {
                        daysAdapter = new DaysAdapter(list.get(position));
                        view.recyclerView.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemSwitchMode.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemDefaultMode.setVisibility(View.GONE);
                        view.rvTitle.setText("switch days");
                        RecyclerView.LayoutManager layoutManager;
                        if (list.get(position).getType()) {
                            // todo It's numbers
                            layoutManager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);
                        } else {
                            // todo It's weeks
                            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                        }
                        view.recyclerView.setLayoutManager(layoutManager);
                        view.recyclerView.setAdapter(daysAdapter);
                    } else {
                        list.get(position).setPosDays(daysAdapter.posDays());
                        list.get(position).setDates(sequence);
                        daoSession.getAutoMarketDao().insertOrReplace(list.get(position));
                        daysAdapter = null;
                        view.recyclerView.setVisibility(View.GONE);
                        view.llAutoMarketItemDefaultMode.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemSwitchMode.setVisibility(View.GONE);
                    }
                }
            });
            view.llAutoMarketItemNextDays.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // todo left next operation day
                    daysAdapter = null;
                    if (view.llAutoMarketItemDefaultMode.getVisibility() == View.VISIBLE || recordAdapter == null) {
                        view.rvTitle.setText("operation lists");
                        view.recyclerView.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemSwitchMode.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemDefaultMode.setVisibility(View.GONE);
                        recordAdapter = new RecordAdapter(list.get(position));
                        RecyclerView.LayoutManager layoutManager =
                                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        view.recyclerView.setLayoutManager(layoutManager);
                        view.recyclerView.setAdapter(recordAdapter);
                        view.deleteOperationItem.setVisibility(View.VISIBLE);
                        view.deleteOperationItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // todo delete operations
                                if (recordAdapter.getMode()) {
                                    recordAdapter.deleteItems();
                                } else {
                                    recordAdapter.setMode(true);
                                    recordAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } else {
                        recordAdapter = null;
                        view.llAutoMarketItemDefaultMode.setVisibility(View.VISIBLE);
                        view.llAutoMarketItemSwitchMode.setVisibility(View.GONE);
                        view.recyclerView.setVisibility(View.GONE);
                        view.deleteOperationItem.setVisibility(View.GONE);
                    }
                }
            });

            view.editDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(getContext(), view);
                    popupMenu.inflate(R.menu.toolbar_popup);
                    MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), view);
                    menuHelper.setForceShowIcon(true);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete: {
                                    final WarningDialog warningDialog = new WarningDialog(getContext());
                                    warningDialog.setText(getContext().getResources().getString(R.string.do_you_want_to_delete));
                                    warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            daoSession.getAutoMarketDao().delete(list.get(position));
                                            autoAdapter = new AutoAdapter();
                                            recyclerView.setAdapter(autoAdapter);
                                            warningDialog.dismiss();
                                        }
                                    });
                                    warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            warningDialog.dismiss();
                                        }
                                    });
                                    warningDialog.show();
                                    break;
                                }
                                case R.id.edit: {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("key", list.get(position).getId());
                                    AddAutoMarketFragment addAutoMarketFragment = new AddAutoMarketFragment();
                                    addAutoMarketFragment.setArguments(bundle);
                                    paFragmentManager.displayFragment(addAutoMarketFragment);
                                    break;
                                }
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });

//            String dates[] = list.get(position).getDates().split(",");
//            int count = dates.length;
//            String date = "";
//            if (count >= 8) {
//                for (int i = 0; i < dates.length; i++) {
//                    date += count / 2 == i ? ("\n" + dates[i] + ",") : dates[i] + ",";
//                }
//            } else {
//                date = list.get(position).getDates();
//            }

//            view.period.setText(date);
//            view.edit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    AddAutoMarketFragment addAutoMarketFragment = new AddAutoMarketFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("key", list.get(position).getId());
//                    addAutoMarketFragment.setArguments(bundle);
//                    paFragmentManager.getFragmentManager().popBackStack();
//                    paFragmentManager.displayFragment(addAutoMarketFragment);
//                }
//            });
//            view.delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setMessage(getResources().getString(R.string.delete))
//                            .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            }).setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                            logicManager.deleteAutoMarket(list.get(position));
//                            list.remove(position);
//                            notifyItemRemoved(position);
//                        }
//                    });
//                    builder.create().show();
//                }
//            });
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auto_market_item_layout, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout llAutoMarketItemDays;
        public LinearLayout llAutoMarketItemNextDays;
        public LinearLayout llAutoMarketItemDefaultMode;
        public LinearLayout llAutoMarketItemSwitchMode;
        public RecyclerView recyclerView;
        public ImageView catIcon;
        public ImageView editDelete;
        public ImageView deleteOperationItem;
        public TextView catName;
        public TextView subCatName;
        public TextView amount;
        public TextView account;
        public TextView nextDay;
        public TextView rvTitle;

        public ViewHolder(View view) {
            super(view);
            catIcon = (ImageView) view.findViewById(R.id.ivItemAutoMarketCategoryIcon);
            catName = (TextView) view.findViewById(R.id.tvItemAutoMarketCatName);
            subCatName = (TextView) view.findViewById(R.id.tvItemAutoMarketSubCatName);
            amount = (TextView) view.findViewById(R.id.tvAutoMarketItemAmount);
            account = (TextView) view.findViewById(R.id.tvAutoMarketItemAccountName);
            nextDay = (TextView) view.findViewById(R.id.tvAutoMarketItemNextDay);
            llAutoMarketItemDays = (LinearLayout) view.findViewById(R.id.llAutoMarketOperationDays);
            llAutoMarketItemNextDays = (LinearLayout) view.findViewById(R.id.llAutoMarketNextOperationDay);
            recyclerView = (RecyclerView) view.findViewById(R.id.rvAutoMarketItemDays);
            llAutoMarketItemDefaultMode = (LinearLayout) view.findViewById(R.id.llAutoMarketItemDefaultMode);
            rvTitle = (TextView) view.findViewById(R.id.tvItemAutoMarketRvTitle);
            llAutoMarketItemSwitchMode = (LinearLayout) view.findViewById(R.id.llAutoMarketItemSwitchMode);
            editDelete = (ImageView) view.findViewById(R.id.ivAutoMarketItemDelete);
            deleteOperationItem = (ImageView) view.findViewById(R.id.ivItemAutoMarketOperationDelete);
        }
    }

    String sequence = "";

    private class DaysAdapter extends RecyclerView.Adapter<ViewHolderDialog> {
        private String[] days;
        private boolean tek[];
        private AutoMarket autoMarket;

        public DaysAdapter(AutoMarket autoMarket) {
            sequence = "";
            this.autoMarket = autoMarket;
            if (!autoMarket.getType()) {
                days = getResources().getStringArray(R.array.week_day_auto);
            } else {
                days = new String[31];
                for (int i = 0; i < days.length; i++) {
                    days[i] = i < 9 ? "" + (i + 1) : "" + (i + 1);
                }
            }
            tek = new boolean[days.length];
            String[] dates = autoMarket.getDates().split(",");
            for (int i = 0; i < days.length; i++) {
                for (String date : dates) {
                    if (days[i].matches(date)) {
                        tek[i] = true;
                        break;
                    }
                }
            }
        }

        public String posDays() {
            String posDay = "";
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    posDay += i + ",";
                    if (autoMarket.getType()) {
                        sequence += i + 1 + ",";
                    } else {
                        sequence += days[i] + ",";
                    }
                }
            }
            return posDay;
        }

        @Override
        public int getItemCount() {
            return days.length;
        }

        public void onBindViewHolder(final ViewHolderDialog view, final int position) {
            if (position % 7 == 0) {
                view.frameLayout.setVisibility(View.GONE);
            }
            view.day.setText(days[position]);
            if (tek[position]) {
                view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                view.day.setTypeface(null, Typeface.BOLD);

            } else {
                view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.black_for_secondary_text));
                view.day.setTypeface(null, Typeface.NORMAL);

            }
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tek[position]) {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                        view.day.setTypeface(null, Typeface.BOLD);

                    } else {
                        view.day.setTextColor(ContextCompat.getColor(getContext(), R.color.black_for_secondary_text));
                        view.day.setTypeface(null, Typeface.NORMAL);

                    }
                    tek[position] = !tek[position];
                }
            });
        }

        public ViewHolderDialog onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_month_layout, parent, false);
            return new ViewHolderDialog(view);
        }
    }

    public class ViewHolderDialog extends RecyclerView.ViewHolder {
        public TextView day;
        public FrameLayout frameLayout;
        public View itemView;

        public ViewHolderDialog(View view) {
            super(view);
            itemView = view;
            day = (TextView) view.findViewById(R.id.tvItemDay);
            frameLayout = (FrameLayout) view.findViewById(R.id.flItemDay);
        }
    }

    private class RecordAdapter extends RecyclerView.Adapter<ViewHolderRecord> {
        private List<FinanceRecord> financeRecordList;
        private AutoMarket autoMarket;
        private SimpleDateFormat simpleDate = new SimpleDateFormat("dd.MM.yyyy");
        private boolean mode = false;
        private boolean tek[];

        public RecordAdapter(AutoMarket autoMarket) {
            this.autoMarket = autoMarket;
            financeRecordList = daoSession.getFinanceRecordDao().queryBuilder()
                    .where(FinanceRecordDao.Properties.RecordId.like("auto"),
                            FinanceRecordDao.Properties.CategoryId.eq(autoMarket.getCatId()),
                            FinanceRecordDao.Properties.SubCategoryId.eq(autoMarket.getCatSubId()))
                    .list();
            Collections.sort(financeRecordList, new Comparator<FinanceRecord>() {
                @Override
                public int compare(FinanceRecord financeRecord, FinanceRecord t1) {
                    return financeRecord.getDate().compareTo(t1.getDate());
                }
            });
            if (financeRecordList.size() > 7) {
                financeRecordList = financeRecordList.subList(0, 7);
            }
            tek = new boolean[financeRecordList.size()];
        }

        public boolean getMode () {
            return mode;
        }

        public void setMode (boolean mode) {
            this.mode = mode;
        }

        @Override
        public int getItemCount() {
            return financeRecordList.size() + 1;
        }

        @Override
        public void onBindViewHolder(final ViewHolderRecord view, final int position) {
            if (financeRecordList.size() == position) {
                Calendar nextOperation = Calendar.getInstance();
                String[] days = autoMarket.getPosDays().split(",");
                if (autoMarket.getType()) {
                    int current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    for (String day : days) {
                        if (current < Integer.parseInt(day) + 1) {
                            current = Integer.parseInt(day) + 1;
                            break;
                        }
                    }
                    if (current == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        if (days.length == 1) {
                            // left one month
                            current = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                            nextOperation.add(Calendar.MONTH, 1);
                        } else {
                            current = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                                    - current + Integer.parseInt(days[0]) + 1;
                            nextOperation.add(Calendar.MONTH, 1);
                            nextOperation.set(Calendar.DAY_OF_MONTH, Integer.parseInt(days[0]) + 1);
                        }
                    } else {
                        current = current - Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        nextOperation.set(Calendar.DAY_OF_MONTH, current);
                    }
                } else {
                    int current = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    for (String day : days) {
                        if (current < Integer.parseInt(day) + 1) {
                            current = Integer.parseInt(day) + 1;
                            break;
                        }
                    }
                    if (current == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                        if (days.length == 1) {
                            // left one month
                            current = 7;
                            nextOperation.add(Calendar.WEEK_OF_MONTH, 1);
                        } else {
                            current = 7 - Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + Integer.parseInt(days[0]);
                            nextOperation.set(Calendar.DAY_OF_WEEK, Integer.parseInt(days[0]));
                        }
                    } else {
                        current = current - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                        nextOperation.set(Calendar.DAY_OF_WEEK, current);
                    }
                }
                view.tvDate.setText(simpleDate.format(nextOperation.getTime()));
                view.tvAmount.setText("" + autoMarket.getAmount() + autoMarket.getCurrency().getAbbr());
                view.tvIsSuccess.setText("Waiting");
            } else {
                view.tvDate.setText(simpleDate.format(financeRecordList.get(position).getDate().getTime()));
                view.tvAmount.setText("" + financeRecordList.get(position).getAmount());
                view.tvIsSuccess.setText("Success");
                if (mode) {
                    view.checkBox.setVisibility(View.VISIBLE);
                    tek[position] = !tek[position];
                    view.checkBox.setChecked(tek[position]);
                } else {
                    // todo delete finance records
                    view.checkBox.setVisibility(View.GONE);
                    tek = new boolean[financeRecordList.size()];
                }
            }
        }

        public ViewHolderRecord onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto_market_opetion, parent, false);
            return new ViewHolderRecord(view);
        }

        public void deleteItems() {
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    daoSession.getFinanceRecordDao().delete(financeRecordList.get(i));
                }
            }
            mode = false;
            notifyDataSetChanged();
        }
    }

    public class ViewHolderRecord extends RecyclerView.ViewHolder {
        public TextView tvDate;
        public TextView tvAmount;
        public TextView tvIsSuccess;
        public CheckBox checkBox;

        public ViewHolderRecord(View view) {
            super(view);
            tvDate = (TextView) view.findViewById(R.id.tvItemAutoMarketOperationDate);
            tvAmount = (TextView) view.findViewById(R.id.tvItemAutoMarketOperationAmount);
            tvIsSuccess = (TextView) view.findViewById(R.id.tvItemAutoMarketOperationIsSuccess);
            checkBox = (CheckBox) view.findViewById(R.id.chbAutoMarketOperation);
        }
    }
}