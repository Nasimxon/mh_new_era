package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.database.TemplateVoice;
import com.jim.pocketaccounter.finance.SubCategoryAdapter;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.FABIcon;
import com.jim.pocketaccounter.utils.IconChooseDialog;
import com.jim.pocketaccounter.utils.OnIconPickListener;
import com.jim.pocketaccounter.utils.OnSubcategorySavingListener;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.SubCatAddEditDialog;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.regex.RegexBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

@SuppressLint({"InflateParams", "ValidFragment"})
public class RootCategoryEditFragment extends PABaseInfoFragment implements OnClickListener {
    private EditText etCatEditName;
    private CheckBox chbCatEditExpanse, chbCatEditIncome;
    private ImageView ivCatEdit;
    private ImageView ivSubCatAdd, ivSubCatDelete;
    private RecyclerView rvSubcats;
    private RootCategory category;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private String selectedIcon = "add_icon";
    private boolean[] selected;
    private String[] icons;
    private List<SubCategory> subCategories;
    private String categoryId;
    private int editMode, pos;

    public RootCategoryEditFragment(RootCategory rootCategory, int mode, int pos, Calendar date) {
        category = rootCategory;
        editMode = mode;
        this.pos = pos;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cat_edit_layout, container, false);
        toolbarManager.setTitle(getResources().getString(R.string.category));
        toolbarManager.setSubtitle(getResources().getString(R.string.edit));
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setOnSecondImageClickListener(this);

        toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (subCategories != null) {
                    for (int i = 0; i < subCategories.size(); i++) {
                        if (subCategories.get(i) == null) {
                            subCategories.remove(i);
                            i--;
                        }
                    }
                }
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (editMode == PocketAccounterGeneral.NO_MODE) {
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayFragment(new CategoryFragment());
                        } else {
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayMainWindow();
                        }
                    }
                }, 50);
            }
        });
        etCatEditName = (EditText) rootView.findViewById(R.id.etAccountEditName);
        chbCatEditExpanse = (CheckBox) rootView.findViewById(R.id.chbCatEditExpanse);
        chbCatEditIncome = (CheckBox) rootView.findViewById(R.id.chbCatEditIncome);

        if (editMode == PocketAccounterGeneral.EXPANSE_MODE) {
            chbCatEditExpanse.setChecked(true);
            chbCatEditIncome.setChecked(false);

        }
        if (editMode == PocketAccounterGeneral.INCOME_MODE) {
            chbCatEditExpanse.setChecked(false);
            chbCatEditIncome.setChecked(true);
        }
        chbCatEditExpanse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (editMode == PocketAccounterGeneral.NO_MODE) {
                    chbCatEditExpanse.setChecked(isChecked);
                    chbCatEditIncome.setChecked(!isChecked);
                }
                if (editMode == PocketAccounterGeneral.EXPANSE_MODE) {
                    chbCatEditExpanse.setChecked(true);
                    chbCatEditIncome.setChecked(false);
                }
                if (editMode == PocketAccounterGeneral.INCOME_MODE) {
                    chbCatEditExpanse.setChecked(false);
                    chbCatEditIncome.setChecked(true);
                }
            }
        });
        chbCatEditIncome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (editMode == PocketAccounterGeneral.NO_MODE) {
                    chbCatEditExpanse.setChecked(!isChecked);
                    chbCatEditIncome.setChecked(isChecked);
                }
                if (editMode == PocketAccounterGeneral.EXPANSE_MODE) {
                    chbCatEditExpanse.setChecked(true);
                    chbCatEditIncome.setChecked(false);
                }
                if (editMode == PocketAccounterGeneral.INCOME_MODE) {
                    chbCatEditExpanse.setChecked(false);
                    chbCatEditIncome.setChecked(true);
                }
            }
        });
        ivCatEdit = (ImageView) rootView.findViewById(R.id.ivCatEdit);
        ivCatEdit.setOnClickListener(this);
        ivSubCatDelete = (ImageView) rootView.findViewById(R.id.ivSubCatDelete);
        ivSubCatDelete.setOnClickListener(this);
        rvSubcats = (RecyclerView) rootView.findViewById(R.id.rvAccountHistory);
        rvSubcats.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryId = UUID.randomUUID().toString();
        subCategories = new ArrayList<>();
        if (category != null) {
            etCatEditName.setText(category.getName());
            chbCatEditIncome.setChecked(false);
            chbCatEditExpanse.setChecked(false);
            switch (category.getType()) {
                case PocketAccounterGeneral.INCOME:
                    chbCatEditIncome.setChecked(true);
                    break;
                case PocketAccounterGeneral.EXPENSE:
                    chbCatEditExpanse.setChecked(true);
                    break;
            }
            categoryId = category.getId();
            selectedIcon = category.getIcon();
            subCategories = category.getSubCategories();
            refreshSubCatList();
        }
        mode = PocketAccounterGeneral.NORMAL_MODE;
        setMode(mode);
        int resId = getResources().getIdentifier(selectedIcon, "drawable", getContext().getPackageName());
              ivCatEdit.setImageResource(resId);
        return rootView;
    }

    private void refreshSubCatList() {
        SubcatAdapter adapter = new SubcatAdapter(subCategories);
        rvSubcats.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("nnn", "ishladi");
        if (subCategories != null) {
            for (int i = 0; i < subCategories.size(); i++) {
                if (subCategories.get(i) == null) {
                    subCategories.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        switch (v.getId()) {
            case R.id.ivCatEdit:
                final IconChooseDialog iconChooseDialog = new IconChooseDialog(getContext());
                iconChooseDialog.setSelectedIcon(selectedIcon);
                iconChooseDialog.setOnIconPickListener(new OnIconPickListener() {
                    @Override
                    public void OnIconPick(String icon) {
                        selectedIcon = icon;
                        Bitmap temp, scaled;
                        int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                        temp = BitmapFactory.decodeResource(getResources(), resId);
                        scaled = Bitmap.createScaledBitmap(temp, (int) getResources().getDimension(R.dimen.twentyfive_dp),
                                (int) getResources().getDimension(R.dimen.twentyfive_dp), false);
                        ivCatEdit.setImageBitmap(scaled);
                        iconChooseDialog.dismiss();
                    }
                });
                iconChooseDialog.show();
                break;
            case R.id.ivSubCatDelete:
                if (mode == PocketAccounterGeneral.NORMAL_MODE) {
                    mode = PocketAccounterGeneral.EDIT_MODE;
                    setMode(mode);
                } else {
                    mode = PocketAccounterGeneral.NORMAL_MODE;
                    boolean isAnySelected = false;
                    for (int i = 0; i < selected.length; i++) {
                        if (selected[i]) {
                            isAnySelected = true;
                            break;
                        }
                    }
                    if (isAnySelected) {
                        final WarningDialog warningDialog = new WarningDialog(getContext());
                        warningDialog.setText(getResources().getString(R.string.subcat_delete_warning));
                        warningDialog.setOnYesButtonListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<SubCategory> deleting = new ArrayList<>();
                                for (int i = 0; i < selected.length; i++)
                                    if (selected[i]) {
                                        deleting.add(subCategories.get(i));
                                        subCategories.set(i, null);
                                    }
                                logicManager.deleteSubcategories(deleting);
                                for (int i = 0; i < subCategories.size(); i++) {
                                    if (subCategories.get(i) == null) {
                                        subCategories.remove(i);
                                        i--;
                                    }
                                }
                                refreshSubCatList();
                                dataCache.updateAllPercents();
                                mode = PocketAccounterGeneral.NORMAL_MODE;
                                setMode(mode);
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnNoButtonClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.show();
                    } else
                        setMode(mode);
                }
                break;
            case R.id.ivToolbarMostRight:
                if (etCatEditName.getText().toString().matches("")) {
                    etCatEditName.setError(getResources().getString(R.string.category_name_error));
                    return;
                }
                if (!chbCatEditIncome.isChecked() && !chbCatEditExpanse.isChecked()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.cat_type_not_choosen), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectedIcon.equals("add_icon")){
                    Toast.makeText(getActivity(), R.string.select_icon_category, Toast.LENGTH_SHORT).show();
                    return;
                }

                RootCategory rootCategory = null;
                if (category == null)
                    rootCategory = new RootCategory();
                else
                    rootCategory = category;
                rootCategory.setName(etCatEditName.getText().toString());
                if (chbCatEditIncome.isChecked()) {
                    if (category != null && rootCategory.getType() == PocketAccounterGeneral.EXPENSE) {
                        List<BoardButton> list = daoSession.getBoardButtonDao()
                                .queryBuilder()
                                .where(BoardButtonDao.Properties.CategoryId.eq(categoryId),
                                        BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.EXPENSE))
                                .list();
                        for (BoardButton boardButton : list) {
                            logicManager.changeBoardButton(boardButton.getTable(), boardButton.getPos(), null);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap scaled = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
                            scaled = Bitmap.createScaledBitmap(scaled, (int) getResources().getDimension(R.dimen.thirty_dp),
                                    (int) getResources().getDimension(R.dimen.thirty_dp), false);
                            dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
                        }

                    }
                    rootCategory.setType(PocketAccounterGeneral.INCOME);
                } else {
                    if (category != null && rootCategory.getType() == PocketAccounterGeneral.INCOME) {
                        List<BoardButton> list = daoSession.getBoardButtonDao()
                                .queryBuilder()
                                .where(BoardButtonDao.Properties.CategoryId.eq(categoryId),
                                        BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.INCOME))
                                .list();
                        for (BoardButton boardButton : list) {
                            logicManager.changeBoardButton(boardButton.getTable(), boardButton.getPos(), null);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap scaled = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
                            scaled = Bitmap.createScaledBitmap(scaled, (int) getResources().getDimension(R.dimen.thirty_dp),
                                    (int) getResources().getDimension(R.dimen.thirty_dp), false);
                            dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
                        }
                    }
                    rootCategory.setType(PocketAccounterGeneral.EXPENSE);
                }
                rootCategory.setIcon(selectedIcon);
                for (int i = 0; i < subCategories.size(); i++) {
                    if (subCategories.get(i) == null) {
                        subCategories.remove(i);
                        i--;
                    }
                }
                if (subCategories != null && logicManager.insertSubCategory(subCategories) == LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS) {
                    Toast.makeText(getContext(), R.string.such_subcat_exist, Toast.LENGTH_SHORT).show();
                    return;
                }
                rootCategory.setSubCategories(subCategories);
                rootCategory.setId(categoryId);
                if (category != null) {
                    logicManager.insertRootCategory(rootCategory);
                    List<BoardButton> list = daoSession
                            .getBoardButtonDao()
                            .queryBuilder()
                            .where(BoardButtonDao.Properties.CategoryId.eq(category.getId()))
                            .list();
                    if (!list.isEmpty()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        for (BoardButton boardButton : list) {
                            int resId = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), false);
                            dataCache.getBoardBitmapsCache().put(boardButton.getId(), bitmap);
                        }
                    }
                }
                else {
                    if (logicManager.insertRootCategory(rootCategory) == LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS) {
                        Toast.makeText(getContext(), R.string.category_name_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (editMode == PocketAccounterGeneral.NO_MODE) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    paFragmentManager.displayFragment(new CategoryFragment());
                } else {
                    logicManager.changeBoardButton(rootCategory.getType(),
                            pos, categoryId);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    int resId = getResources().getIdentifier(rootCategory.getIcon(), "drawable", getContext().getPackageName());
                    Bitmap scaled = BitmapFactory.decodeResource(getResources(), resId, options);
                    scaled = Bitmap.createScaledBitmap(scaled, (int) getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), true);
                    Long id = null;
                    if (!daoSession.getBoardButtonDao().queryBuilder()
                            .where(BoardButtonDao.Properties.CategoryId.eq(categoryId))
                            .build().list().isEmpty()) {
                        id = daoSession.getBoardButtonDao().queryBuilder()
                                .where(BoardButtonDao.Properties.CategoryId.eq(categoryId))
                                .build().list().get(0).getId();
                    }
                    if (id != null)
                        dataCache.getBoardBitmapsCache().put(id, scaled);
                    paFragmentManager.displayMainWindow();
                    paFragmentManager.getFragmentManager().popBackStack();
                }
                break;
        }
    }


    private void setMode(int mode) {
        if (mode == PocketAccounterGeneral.NORMAL_MODE) {
            subCategories.add(0, null);
            ivSubCatDelete.setImageResource(R.drawable.subcat_delete);
            selected = null;
        } else {
            for (int i = 0; i < subCategories.size(); i++) {
                if (subCategories.get(i) == null) {
                    subCategories.remove(i);
                    i--;
                }
            }
            ivSubCatDelete.setImageResource(R.drawable.ic_cat_trash);
            if (subCategories != null)
                selected = new boolean[subCategories.size()];
        }
        if (rvSubcats.getAdapter() != null)
            rvSubcats.getAdapter().notifyDataSetChanged();
        refreshSubCatList();
    }

    @Override
    void refreshList() {

    }

    private class SubcatAdapter extends RecyclerView.Adapter<RootCategoryEditFragment.ViewHolder> {
        private List<SubCategory> result;
        public SubcatAdapter(List<SubCategory> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final RootCategoryEditFragment.ViewHolder view, final int position) {
            if (position == 0)
                view.ivTopStripe.setBackgroundColor(Color.WHITE);
            if (mode == PocketAccounterGeneral.NORMAL_MODE) {
                if (result.get(position) == null) {
                    view.tvSubCatName.setText(getResources().getString(R.string.add));
                    view.ivSubCategoryIcon.setImageResource(R.drawable.add_green);
                    view.view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final SubCatAddEditDialog subCatAddEditDialog = new SubCatAddEditDialog(getContext());
                            subCatAddEditDialog.setRootCategory(categoryId);
                            subCatAddEditDialog.setSubCat(null, new OnSubcategorySavingListener() {
                                @Override
                                public void onSubcategorySaving(SubCategory subCategory) {
                                    if (subCategories != null) {
                                        for (SubCategory subcat : subCategories) {
                                            if (subcat == null) continue;
                                            if (subcat.getName().equals(subCategory.getName())) {
                                                Toast.makeText(getContext(), R.string.such_subcat_exist, Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }

                                    } else
                                        subCategories = new ArrayList<>();
                                    subCategories.add(subCategory);
                                    List<SubCategory> saving = new ArrayList<>();
                                    for (int i = 0; i < subCategories.size(); i++) {
                                        if (subCategories.get(i) != null) {
                                            saving.add(subCategories.get(i));
                                        }
                                    }
                                    logicManager.insertSubCategory(saving);
                                    refreshSubCatList();
                                    subCatAddEditDialog.dismiss();
                                }
                            });
                            subCatAddEditDialog.show();
                        }
                    });
                }
                else {
                    view.tvSubCatName.setText(result.get(position).getName());
                    int resId = getResources().getIdentifier(result.get(position).getIcon(), "drawable", getContext().getPackageName());
                    view.ivSubCategoryIcon.setImageResource(resId);
                    view.view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final SubCatAddEditDialog subCatAddEditDialog = new SubCatAddEditDialog(getContext());
                            subCatAddEditDialog.setRootCategory(categoryId);
                            subCatAddEditDialog.setSubCat(result.get(position), new OnSubcategorySavingListener() {
                                @Override
                                public void onSubcategorySaving(SubCategory subCategory) {
                                    if (subCategories == null) return;
                                    for (SubCategory s : subCategories) {
                                        if (s == null) continue;
                                        if (s.getName().equals(subCategory.getName()) && !s.getId().matches(subCategory.getId())) {
                                            Toast.makeText(getContext(), R.string.such_subcat_exist, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    for (int i = 1; i < subCategories.size(); i++) {
                                        if (subCategories.get(i).getId().matches(subCategory.getId())) {
                                            subCategories.set(i, subCategory);
                                            break;
                                        }
                                    }
                                    refreshSubCatList();
                                    subCatAddEditDialog.dismiss();
                                }
                            });
                            subCatAddEditDialog.show();
                        }
                    });
                }
                view.chbSubCat.setVisibility(View.GONE);
            }
            else {
                view.tvSubCatName.setText(result.get(position).getName());
                int resId = getResources().getIdentifier(result.get(position).getIcon(), "drawable", getContext().getPackageName());
                view.ivSubCategoryIcon.setImageResource(resId);
                view.chbSubCat.setVisibility(View.VISIBLE);
                view.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.chbSubCat.setChecked(!view.chbSubCat.isChecked());
                    }
                });
                view.chbSubCat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        selected[position] = b;
                    }
                });
            }
        }

        public RootCategoryEditFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subcat_item, parent, false);
            return new RootCategoryEditFragment.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubCatName;
        ImageView ivSubCategoryIcon, ivTopStripe;
        CheckBox chbSubCat;
        View view;
        public ViewHolder(View view) {
            super(view);
            tvSubCatName = (TextView) view.findViewById(R.id.tvSubCatName);
            ivSubCategoryIcon = (ImageView) view.findViewById(R.id.ivSubCategoryIcon);
            chbSubCat = (CheckBox) view.findViewById(R.id.chbSubCat);
            ivTopStripe = (ImageView) view.findViewById(R.id.ivTopStripe);
            this.view = view;
        }
    }
}