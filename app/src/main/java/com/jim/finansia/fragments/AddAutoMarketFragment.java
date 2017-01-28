package com.jim.finansia.fragments;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.AutoMarket;
import com.jim.finansia.database.AutoMarketDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.finance.ChoiseCategoryDialoogItemAdapter;
import com.jim.finansia.finance.IconAdapterCategory;
import com.jim.finansia.finance.RecordCategoryAdapter;
import com.jim.finansia.finance.RecordSubCategoryAdapter;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SubCatAddEditDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by root on 9/15/16.
 */
public class AddAutoMarketFragment extends Fragment {
    @Inject
    DaoSession daoSession;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    LogicManager logicManager;
    @Inject
    SubCatAddEditDialog subCatAddEditDialog;
    @Inject
    CommonOperations commonOperations;

    private AccountDao accountDao;
    private CurrencyDao currencyDao;

    private AutoMarketDao autoMarketDao;
    private EditText amount;
    private Spinner spCurrency, account_sp;
    private ImageView ivCategory;
    private TextView categoryName;
    private TextView subCategoryName;
    ChoiseCategoryDialoogItemAdapter choiseCategoryDialoogItemAdapter;
    private int selectCategory = -1;
    private int selectSubCategory = -1;
    private AutoMarket autoMarket;
    private boolean type = false;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Account> accounts;
    String[] accs;
    private RecyclerView rvDays;
    private DaysAdapter daysAdapter;
    private RadioGroup radioGroup;
    List<RootCategory> categoryList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        accountDao = daoSession.getAccountDao();
        currencyDao = daoSession.getCurrencyDao();
        autoMarketDao = daoSession.getAutoMarketDao();
        try {
            this.autoMarket = autoMarketDao.load(null == getArguments().getString("key") ? "" : getArguments().getString("key"));
        } catch (NullPointerException e) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_auto_market_layout_modern, container, false);
        amount = (EditText) rootView.findViewById(R.id.etAddAutoMarketAmount);
        ivCategory = (ImageView) rootView.findViewById(R.id.ivAddAutoMarketCategory);
        spCurrency = (Spinner) rootView.findViewById(R.id.spAddAutoMarketCurrency);
        account_sp = (Spinner) rootView.findViewById(R.id.acountSpinner);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.rgMonthWeek);
        categoryName = (TextView) rootView.findViewById(R.id.tvAddAutoMarketCatName);
        subCategoryName = (TextView) rootView.findViewById(R.id.tvAddAutoMarketSubCatName);
        rvDays = (RecyclerView) rootView.findViewById(R.id.rvAddAutoMarketPerItems);

        final List<String> curs = new ArrayList<>();
        for (Currency cr : currencyDao.loadAll()) {
            curs.add(cr.getAbbr());
        }

        accounts = (ArrayList<Account>) accountDao.queryBuilder().list();
        accs = new String[accounts.size()];
        for (int i = 0; i < accounts.size(); i++) {
            accs[i] = accounts.get(i).getName();
        }
        ArrayAdapter<String> adapter_scet = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, accs);
        ArrayAdapter<String> curAdapter = new ArrayAdapter<String>(getActivity()
                , R.layout.spiner_gravity_right, curs);

        account_sp.setAdapter(adapter_scet);
        spCurrency.setAdapter(curAdapter);
        int posMain = 0;
        for (int i = 0; i < curs.size(); i++) {
            if (curs.get(i).equals(commonOperations.getMainCurrency().getAbbr())) {
                posMain = i;
            }
        }
        spCurrency.setSelection(posMain);
        List<String> acNames = new ArrayList<>();
        for (Account ac : accountDao.loadAll()) {
            acNames.add(ac.getId());
        }


        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daysAdapter.getResult();
                try {
                    Double.parseDouble(amount.getText().toString());
                    amount.setError(null);
                } catch (Exception e) {
                    amount.setError(getString(R.string.wrong_input_type));
                    return;
                }
                if (amount.getText().toString().isEmpty()) {
                    amount.setError(getResources().getString(R.string.enter_amount_error));
                } else if (sequence.isEmpty()) {
                    amount.setError(null);
                    Toast.makeText(getContext(), R.string.dates_not_choosen_error, Toast.LENGTH_SHORT).show();
                } else if (autoMarket != null) {
                    amount.setError(null);
                    if (category_item != null && subCategory != null) {
                        autoMarket.setRootCategory(category_item);
                        autoMarket.setSubCategory(subCategory);
                    }
                    autoMarket.setAmount(Double.parseDouble(amount.getText().toString()));
                    autoMarket.setCurrency(currencyDao.queryBuilder().where(CurrencyDao.Properties.Abbr.eq(curs.get(spCurrency.getSelectedItemPosition()))).list().get(0));
                    autoMarket.setAccount(accountDao.loadAll().get(account_sp.getSelectedItemPosition()));
                    autoMarket.setType(type);
                    autoMarket.setDates(sequence.substring(0, sequence.length() - 1));
                    autoMarket.setPosDays(daysAdapter.posDays());
                    daoSession.getAutoMarketDao().insertOrReplace(autoMarket);
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new AutoMarketFragment());
                } else if (selectCategory == -1 && selectSubCategory == -1 && category_item == null) {
                    Toast.makeText(getContext(), R.string.select_category_error, Toast.LENGTH_SHORT).show();
                } else {
                    amount.setError(null);
                    AutoMarket autoMarket = new AutoMarket();
                    autoMarket.__setDaoSession(daoSession);
                    autoMarket.setAmount(Double.parseDouble(amount.getText().toString()));

                    autoMarket.setRootCategory(category_item);
                    autoMarket.setSubCategory(subCategory);

                    autoMarket.setCurrency(currencyDao.queryBuilder().where(CurrencyDao.Properties.Abbr.eq(curs.get(spCurrency.getSelectedItemPosition()))).list().get(0));
                    autoMarket.setAccount(accountDao.loadAll().get(account_sp.getSelectedItemPosition()));
                    autoMarket.setType(type);
                    autoMarket.setPosDays(daysAdapter.posDays());

                    autoMarket.setCreateDay(Calendar.getInstance());
                    autoMarket.setDates(sequence.substring(0, sequence.length() - 1));
                    switch (logicManager.insertAutoMarket(autoMarket)) {
                        case LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS: {
                            Toast.makeText(getContext(), R.string.illegal_name_error, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case LogicManagerConstants.SAVED_SUCCESSFULL: {
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayFragment(new AutoMarketFragment());
                            break;
                        }
                    }
                }
            }
        });

        ivCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(getActivity());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);
                View vr = dialog.getWindow().getDecorView();
                vr.setBackgroundResource(android.R.color.transparent);
                categoryList = daoSession.getRootCategoryDao().loadAll();
                final TextView tvAllView = (TextView)  dialogView.findViewById(R.id.tvAllView);
                final TextView tvExpenseView = (TextView)  dialogView.findViewById(R.id.tvExpenseView);
                final TextView tvIncomeView = (TextView)  dialogView.findViewById(R.id.tvIncomeView);
                RecyclerView rvCategoryChoose = (RecyclerView) dialogView.findViewById(R.id.lvCategoryChoose);
                ArrayList<Object> tempForCastToObject = new ArrayList<>();
                for(int t=0;t<categoryList.size();t++){
                    tempForCastToObject.add(categoryList.get(t));
                }

                choiseCategoryDialoogItemAdapter = new ChoiseCategoryDialoogItemAdapter(tempForCastToObject, getContext(), new ChoiseCategoryDialoogItemAdapter.OnItemSelected() {
                    @Override
                    public void itemPressed(String itemID) {
                        boolean keyBroker = false;
                        for (RootCategory rootCategory : categoryList) {
                            if (rootCategory.getId().equals(itemID)) {
                                category_item = rootCategory;
                                subCategory = null;
                                ivCategory.setImageResource(getResources().getIdentifier(category_item.getIcon(), "drawable", getActivity().getPackageName()));
                                categoryName.setText(category_item.getName());
                                subCategoryName.setText((category_item.getType() == PocketAccounterGeneral.INCOME) ? "Income category" : "Expanse category");

                                break;
                            }
                            for (SubCategory subCategoryTemp : rootCategory.getSubCategories()) {
                                if (subCategoryTemp.getId().equals(itemID)) {
                                    category_item = rootCategory;
                                    subCategory = subCategoryTemp;
                                    categoryName.setText(category_item.getName());
                                    subCategoryName.setText(subCategory.getName());
                                    ivCategory.setImageResource(getResources().getIdentifier(subCategory.getIcon(), "drawable", getActivity().getPackageName()));

                                    keyBroker = true;
                                    break;
                                }
                            }
                            if (keyBroker)
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                tvAllView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));

                        categoryList.clear();
                        categoryList = daoSession.getRootCategoryDao().loadAll();
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);
                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();



                    }
                });
                tvExpenseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));

                        categoryList.clear();
                        for (RootCategory rootCategory:daoSession.getRootCategoryDao().loadAll()){
                            if(rootCategory.getType() == PocketAccounterGeneral.EXPENSE)
                                categoryList.add(rootCategory);
                        }
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);

                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();

                    }
                });
                tvIncomeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvAllView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvExpenseView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_secondary_text));
                        tvIncomeView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));

                        categoryList.clear();
                        for (RootCategory rootCategory:daoSession.getRootCategoryDao().loadAll()){
                            if(rootCategory.getType() == PocketAccounterGeneral.INCOME)
                                categoryList.add(rootCategory);
                        }
                        choiseCategoryDialoogItemAdapter.setListForRefresh(categoryList);
                        choiseCategoryDialoogItemAdapter.toBackedToCategory(false);
                        choiseCategoryDialoogItemAdapter.notifyDataSetChanged();

                    }
                });
                dialogView.findViewById(R.id.ivInfoDebtBorrowCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                rvCategoryChoose.setLayoutManager(new GridLayoutManager(getContext(),3));
                rvCategoryChoose.setHasFixedSize(true);
                rvCategoryChoose.setAdapter(choiseCategoryDialoogItemAdapter);
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int hieght = displayMetrics.heightPixels;
                dialog.getWindow().setLayout(9 * width / 10, (int) (8.2*hieght/10));
                dialog.show();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbtnAddAutoMarketMonth) {
                    daysAdapter = new DaysAdapter(1);
                    layoutManager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);
                } else {
                    daysAdapter = new DaysAdapter(0);
                    layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                }
                type = !type;
                rvDays.setLayoutManager(layoutManager);
                rvDays.setAdapter(daysAdapter);
            }
        });

        if (autoMarket != null) {
            categoryName.setText(autoMarket.getRootCategory().getName());
            subCategoryName.setText(autoMarket.getSubCategory().getName());
            ivCategory.setImageResource(getResources().getIdentifier(autoMarket.getRootCategory().getIcon(), "drawable", getActivity().getPackageName()));
            amount.setText("" + autoMarket.getAmount());
            type = autoMarket.getType();
            for (int i = 0; i < curs.size(); i++) {
                if (curs.get(i).matches(autoMarket.getCurrency().getAbbr())) {
                    spCurrency.setSelection(i);
                    break;
                }
            }

            if (autoMarket.getType()) {
                daysAdapter = new DaysAdapter(1);
                layoutManager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);
                radioGroup.check(R.id.rbtnAddAutoMarketMonth);
            } else {
                daysAdapter = new DaysAdapter(0);
                layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                radioGroup.check(R.id.rbtnAddAutoMarketWeek);
            }

        } else {
            daysAdapter = new DaysAdapter(0);
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
            radioGroup.check(R.id.rbtnAddAutoMarketWeek);
        }
        rvDays.setLayoutManager(layoutManager);
        rvDays.setAdapter(daysAdapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbarManager != null) {
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setTitle(getResources().getString(R.string.addedit));
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        }
    }

    private IconAdapterCategory adapter;
    private boolean catSelected = false;

    String sequence = "";
    private boolean inc = false;

    RootCategory category_item;

    private void openCategoryDialog(final ArrayList<RootCategory> categories) {
        final Dialog dialog = new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvCategoryChoose = (ListView) dialogView.findViewById(R.id.lvCategoryChoose);
        RecordCategoryAdapter adapter = new RecordCategoryAdapter(getContext(), categories);
        lvCategoryChoose.setAdapter(adapter);
        lvCategoryChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                category_item = categories.get(position);
                selectCategory = position;
                openSubCategoryDialog();
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(8 * width / 9, ActionBarOverlayLayout.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    SubCategory subCategory;

    private void openSubCategoryDialog() {
        final Dialog dialog = new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvCategoryChoose = (ListView) dialogView.findViewById(R.id.lvCategoryChoose);
        final ArrayList<SubCategory> subCategories = new ArrayList<SubCategory>();
        SubCategory noSubCategory = new SubCategory();
        noSubCategory.setIcon("category_not_selected");
        noSubCategory.setName(getResources().getString(R.string.no_category_name));
        noSubCategory.setId(getResources().getString(R.string.no_category));
        subCategories.add(noSubCategory);
        for (int i = 0; i < category_item.getSubCategories().size(); i++)
            subCategories.add(category_item.getSubCategories().get(i));

        RecordSubCategoryAdapter adapter = new RecordSubCategoryAdapter(getContext(), subCategories);
        lvCategoryChoose.setAdapter(adapter);
        lvCategoryChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (subCategories.get(position) == null) {
                    subCategory = null;
                    ivCategory.setImageResource(getResources().getIdentifier(subCategory.getIcon(), "drawable", getActivity().getPackageName()));
                    categoryName.setText(category_item.getName());
                    subCategoryName.setText((category_item.getType() == PocketAccounterGeneral.INCOME) ? "Income category" : "Expanse category");

                } else if (subCategories.get(position).getId().matches(getResources().getString(R.string.no_category))) {
                    subCategory = null;
                    ivCategory.setImageResource(getResources().getIdentifier(category_item.getIcon(), "drawable", getActivity().getPackageName()));
                    categoryName.setText(category_item.getName());
                    subCategoryName.setText((category_item.getType() == PocketAccounterGeneral.INCOME) ? "Income category" : "Expanse category");

                } else if (subCategories.get(position) != null) {
                    subCategory = subCategories.get(position);
                    selectSubCategory = position;
                    categoryName.setText(category_item.getName());
                    subCategoryName.setText(subCategory.getName());
                    ivCategory.setImageResource(getResources().getIdentifier(subCategory.getIcon(), "drawable", getActivity().getPackageName()));

                }
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(8 * width / 9, ActionBarOverlayLayout.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    private class DaysAdapter extends RecyclerView.Adapter<ViewHolderDialog> {
        private String[] days;
        private boolean tek[];

        public DaysAdapter(int type) {
            sequence = "";
            if (type == 0) {
                days = getResources().getStringArray(R.array.week_day_auto);
            } else {
                days = new String[31];
                for (int i = 0; i < days.length; i++) {
                    days[i] = i < 9 ? "" + (i + 1) : "" + (i + 1);
                }
            }
            tek = new boolean[days.length];
            if (autoMarket != null) {
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
        }

        public void getResult() {
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    sequence = sequence + days[i] + ",";
                }
            }
        }

        public String posDays() {
            String posDay = "";
            for (int i = 0; i < tek.length; i++) {
                if (tek[i]) {
                    posDay += i + ",";
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
}