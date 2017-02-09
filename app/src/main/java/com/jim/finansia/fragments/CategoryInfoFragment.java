package com.jim.finansia.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.utils.OnCheckedChangeListener;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SubcatItemChecker;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.catselector.OnItemSelectedListener;
import com.jim.finansia.utils.catselector.SelectorView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class CategoryInfoFragment extends PABaseInfoFragment {
    @Inject DecimalFormat formatter;
    private WarningDialog warningDialog;
    private RootCategory rootCategory;
    private RecyclerView rvCategoryInfoOperations, rvCatInfoSubcats;
    private TextView tvCategoryInfoTotal;
    private TextView tvCategoryInfoSubcategories;
    private SelectorView svCategorySelector;
    private boolean[] subcatChecked;
    private List<SubCategory> subCategories;
    private PopupMenu popupMenu;

    public static CategoryInfoFragment newInstance(RootCategory category) {
        CategoryInfoFragment fragment = new CategoryInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CategoryFragment.CATEGORY_ID, category.getId());
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            String categoryId = getArguments().getString(CategoryFragment.CATEGORY_ID);
            if (categoryId != null) {
                rootCategory = daoSession.load(RootCategory.class, categoryId);
            }
        }
        final View rootView = inflater.inflate(R.layout.category_info_layout, container, false);
        if (toolbarManager != null) {
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
            toolbarManager.setTitle(getResources().getString(R.string.category));
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitle(rootCategory.getName());
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new CategoryFragment());
                }
            });

            toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOperationsList(v);
                }
            });
        }
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        subCategories = new ArrayList<>();
        SubCategory all = new SubCategory();
        all.setName(getString(R.string.all));
        all.setIcon("all");
        subCategories.add(all);
        subCategories.addAll(rootCategory.getSubCategories());
        subcatChecked = new boolean[subCategories.size()];
        for (int i = 0; i < subcatChecked.length; i++)
            subcatChecked[i] = (i == 0);
        warningDialog = new WarningDialog(getContext());
        rvCategoryInfoOperations = (RecyclerView) rootView.findViewById(R.id.rvCategoryInfoOperations);
        svCategorySelector = (SelectorView) rootView.findViewById(R.id.svCategorySelector);

        final List<RootCategory> rootCategories = daoSession.getRootCategoryDao().queryBuilder().orderAsc(RootCategoryDao.Properties.Name).list();
        CategorySelectorAdapter adapter = new CategorySelectorAdapter(rootCategories);
        int selectedPos = 0;
        for (int i = 0; i < rootCategories.size(); i++) {
            if (rootCategories.get(i).getId().equals(rootCategory.getId())) {
                selectedPos = i;
                break;
            }
        }

        svCategorySelector.setAdapter(adapter);
        svCategorySelector.setSelection(selectedPos);
        svCategorySelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int selectedItemPosition) {
                rootCategory = rootCategories.get(selectedItemPosition);
                toolbarManager.setSubtitle(rootCategory.getName());
                toolbarManager.setSubtitleIconVisibility(View.GONE);
                subCategories = new ArrayList<>();
                SubCategory all = new SubCategory();
                all.setName(getString(R.string.all));
                all.setIcon("all");
                subCategories.add(all);
                subCategories.addAll(rootCategory.getSubCategories());
                subcatChecked = new boolean[subCategories.size()];
                for (int i = 0; i < subcatChecked.length; i++)
                    subcatChecked[i] = (i == 0);
                refreshSubcatItems();
                refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
            }
        });
        rvCatInfoSubcats = (RecyclerView) rootView.findViewById(R.id.rvCatInfoSubcats);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCatInfoSubcats.setLayoutManager(layoutManager);
        refreshSubcatItems();

        rvCategoryInfoOperations = (RecyclerView) rootView.findViewById(R.id.rvCategoryInfoOperations);
        rvCategoryInfoOperations.setLayoutManager(new LinearLayoutManager(getContext()));
        tvCategoryInfoTotal = (TextView) rootView.findViewById(R.id.tvCategoryInfoTotal);
        refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
        return rootView;
    }

    private void refreshSubcatItems() {
        SubcatAdapter subcatAdapter = new SubcatAdapter(subCategories);
        rvCatInfoSubcats.setAdapter(subcatAdapter);
    }

    private void showOperationsList(View v) {
        popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.toolbar_popup);
        MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete: {
                        warningDialog.setText(getResources().getString(R.string.category_delete_warning));
                        warningDialog.setOnNoButtonClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnYesButtonListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<BoardButton> list = daoSession.getBoardButtonDao().queryBuilder().where(BoardButtonDao.Properties.CategoryId.eq(rootCategory.getId())).list();
                                if (!list.isEmpty()) {
                                    int currentPage = 0, countOfButtons = 0;
                                    if (rootCategory.getType() == PocketAccounterGeneral.INCOME) {
                                        currentPage = preferences.getInt("income_current_page", 1);
                                        countOfButtons = 4;
                                    } else {
                                        currentPage = preferences.getInt("expense_current_page", 1);
                                        countOfButtons = 16;
                                    }
                                    for (BoardButton boardButton : list) {
                                        if (currentPage * countOfButtons <= boardButton.getPos()
                                                && (currentPage + 1) * countOfButtons > currentPage * countOfButtons) {
                                            BitmapFactory.Options options = new BitmapFactory.Options();
                                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                                            Bitmap scaled = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
                                            scaled = Bitmap.createScaledBitmap(scaled, (int) getResources().getDimension(R.dimen.thirty_dp),
                                                    (int) getResources().getDimension(R.dimen.thirty_dp), false);
                                            dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
                                        }
                                    }
                                }
                                logicManager.deleteRootCategory(rootCategory);
                                reportManager.clearCache();
                                dataCache.updateAllPercents();
                                paFragmentManager.updateAllFragmentsPageChanges();
                                paFragmentManager.updateTemplatesInVoiceRecognitionFragment();
                                paFragmentManager.getFragmentManager().popBackStack();
                                paFragmentManager.displayFragment(new CategoryFragment());
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.show();
                        break;
                    }
                    case R.id.edit: {
                        paFragmentManager.getFragmentManager().popBackStack();
                        paFragmentManager
                                .displayFragment(RootCategoryEditFragment.newInstance(rootCategory, 0, PocketAccounterGeneral.NO_MODE));
                        break;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void refreshOperationsList(Calendar begin, Calendar end) {
        List<FinanceRecord> objects = reportManager.getCategoryOperations(rootCategory, begin, end);
        if (!subcatChecked[0]) {
            List<SubCategory> selected = new ArrayList<>();
            for (int i = 0; i < subcatChecked.length; i++) {
                if (subcatChecked[i]) {
                    selected.add(subCategories.get(i));
                }
            }
            for (int i = 0; i < objects.size(); i++) {
                boolean notSelected = true;
                for (SubCategory subCategory : selected) {
                    if (subCategory.getId().equals(objects.get(i).getSubCategoryId())) {
                        notSelected = false;
                        break;
                    }
                }
                if (objects.get(i).getSubCategory() == null) notSelected = true;
                if (notSelected) {
                    objects.remove(i);
                    i--;
                }
            }
        }
        CategoryOperationsAdapter accountOperationsAdapter = new CategoryOperationsAdapter(objects);

        rvCategoryInfoOperations.setAdapter(accountOperationsAdapter);
        DecimalFormat format = new DecimalFormat("0.##");
        double total = 0.0d;

        for (FinanceRecord record : objects) {
            if (record.getCategory().getType() == PocketAccounterGeneral.INCOME)
                total += commonOperations.getCost(record);
            else
                total -= commonOperations.getCost(record);
        }
        tvCategoryInfoTotal.setText(getResources().getString(R.string.total) + " " + format.format(total) + commonOperations.getMainCurrency().getAbbr());
    }

    @Override
    void refreshList() {
    }

    private class CategoryOperationsAdapter extends RecyclerView.Adapter<CategoryInfoFragment.ViewHolder> {
        private List<FinanceRecord> result;

        public CategoryOperationsAdapter(List<FinanceRecord> result) {
            this.result = result;
        }

        public int getItemCount() {
            return result.size();
        }

        public void onBindViewHolder(final CategoryInfoFragment.ViewHolder view, final int position) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            view.tvAccountInfoDate.setText(simpleDateFormat.format(result.get(position).getDate().getTime()));
            String text = result.get(position).getCategory().getName();
            if (result.get(position).getSubCategory() != null)
                text += ", " + result.get(position).getSubCategory().getName();
            view.tvAccountInfoName.setText(text);
            String amount = "";
            if (result.get(position).getCategory().getType() == PocketAccounterGeneral.INCOME) {
                amount += "+" + formatter.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr();
                view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
            } else {
                amount += "-" + formatter.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr();
                view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            }
            view.tvAccountInfoAmount.setText(amount);
        }

        public CategoryInfoFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_info_operations, parent, false);
            return new CategoryInfoFragment.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountInfoDate;
        TextView tvAccountInfoName;
        TextView tvAccountInfoAmount;

        public ViewHolder(View view) {
            super(view);
            tvAccountInfoDate = (TextView) view.findViewById(R.id.tvAccountInfoDate);
            tvAccountInfoName = (TextView) view.findViewById(R.id.tvAccountInfoName);
            tvAccountInfoAmount = (TextView) view.findViewById(R.id.tvAccountInfoAmount);
        }
    }


    private class SubcatAdapter extends RecyclerView.Adapter<CategoryInfoFragment.SubcatViewHolder> {
        private List<SubCategory> result;

        public SubcatAdapter(List<SubCategory> result) {
            this.result = result;
        }

        public int getItemCount() {
            return result.size();
        }

        public void onBindViewHolder(final CategoryInfoFragment.SubcatViewHolder view, final int position) {
            if (position == 0) {

                view.sichCatInfo.setSubCategory(result.get(position));
                view.sichCatInfo.setChecked(subcatChecked[position]);
                view.sichCatInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChange(boolean isChecked) {
                        for (int i = 0; i < subcatChecked.length; i++) {
                            View view = rvCatInfoSubcats.getChildAt(i);
                            if (view != null)
                                ((SubcatItemChecker) view.findViewById(R.id.sichCatInfo)).setChecked(false);
                            subcatChecked[i] = false;
                        }
                        subcatChecked[position] = true;
                        View view = rvCatInfoSubcats.getChildAt(position);
                        if (view != null)
                            ((SubcatItemChecker) view.findViewById(R.id.sichCatInfo)).setChecked(true);
                        rvCatInfoSubcats.getAdapter().notifyDataSetChanged();
                        refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
                    }
                });
            } else {
                view.sichCatInfo.setSubCategory(result.get(position));
                view.sichCatInfo.setChecked(subcatChecked[position]);
                view.sichCatInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChange(boolean isChecked) {
                        if (isChecked) {
                            subcatChecked[0] = false;
                        } else {
                            int count = 0;
                            for (int i = 0; i < subcatChecked.length; i++) {
                                if (subcatChecked[i]) count++;
                            }
                            if (count <= 1)
                                subcatChecked[0] = true;
                        }
                        subcatChecked[position] = isChecked;
                        rvCatInfoSubcats.getAdapter().notifyDataSetChanged();
                        refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
                    }
                });
            }
        }

        public CategoryInfoFragment.SubcatViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catinfo_subcat_list_item, parent, false);
            return new CategoryInfoFragment.SubcatViewHolder(view);
        }
    }

    public class SubcatViewHolder extends RecyclerView.ViewHolder {
        SubcatItemChecker sichCatInfo;

        public SubcatViewHolder(View view) {
            super(view);
            sichCatInfo = (SubcatItemChecker) view.findViewById(R.id.sichCatInfo);
        }
    }

    class CategorySelectorAdapter extends SelectorView.SelectorAdapter<RootCategory> {
        public CategorySelectorAdapter(List<RootCategory> list) {
            this.list = list;
        }

        @Override
        protected ViewGroup getInstantiatedView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_selector_item, parent, false);
            ImageView ivCatSelectorItem = (ImageView) view.findViewById(R.id.ivCatSelectorItem);
            int resId = getResources().getIdentifier(list.get(position).getIcon(), "drawable", getContext().getPackageName());
            ivCatSelectorItem.setImageResource(resId);
            TextView tvCatSelectorItem = (TextView) view.findViewById(R.id.tvCatSelectorItem);
            tvCatSelectorItem.setText(list.get(position).getName());
            parent.addView(view);
            return parent;
        }

        @Override
        protected int getCount() {
            return list.size();
        }

    }

}
