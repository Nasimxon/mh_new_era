package com.jim.pocketaccounter.utils.calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CategoryAndSubcategoryChooserView extends RelativeLayout {
    private ArcBitmapChooserView abchvCategoryChooser, abchvSubcategoryChooser;
    @Inject DaoSession daoSession;
    private List<RootCategory> categories;
    private CategoryAndSubcategoryChooseListener listener;
    private RootCategory selectedCategory = null;
    private SubCategory selectedSubcategory = null;
    public CategoryAndSubcategoryChooserView(Context context) {
        super(context);
        init(context);
    }

    public CategoryAndSubcategoryChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CategoryAndSubcategoryChooserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public CategoryAndSubcategoryChooserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.category_and_subcategory_chooser_view, this, true);
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        abchvCategoryChooser = (ArcBitmapChooserView) findViewById(R.id.abchvCategoryChooser);
        abchvCategoryChooser.setListener(new ArcBitmapChooserView.OnCategoryChooseListener() {
            @Override
            public void onCategoryChoose(int position) {
                if (position == ArcBitmapChooserView.NOTHING_SELECTED) return;
                if (categories == null)
                    categories = daoSession.loadAll(RootCategory.class);
                selectedCategory = categories.get(position);
                if (selectedCategory.getSubCategories() != null && !selectedCategory.getSubCategories().isEmpty()) {
                    selectedSubcategory = selectedCategory.getSubCategories().get(0);
                    List<Bitmap> subcategoryBitmaps = new ArrayList<>();
                    for (SubCategory subCategory : selectedCategory.getSubCategories()) {
                        int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.twentyfive_dp), (int) getResources().getDimension(R.dimen.twentyfive_dp), false);
                        subcategoryBitmaps.add(bitmap);
                    }
                    abchvSubcategoryChooser.setBitmaps(subcategoryBitmaps);
                }
                else
                    selectedSubcategory = null;
                if (listener != null)
                    listener.onCategoryAndSubcategoryChoose(selectedCategory, selectedSubcategory);
            }
        });
        abchvSubcategoryChooser = (ArcBitmapChooserView) findViewById(R.id.abchvSubcategoryChooser);
        abchvSubcategoryChooser.setListener(new ArcBitmapChooserView.OnCategoryChooseListener() {
            @Override
            public void onCategoryChoose(int position) {
                if (position == ArcBitmapChooserView.NOTHING_SELECTED) return;
                selectedSubcategory = selectedCategory.getSubCategories().get(position);
                if (listener != null)
                    listener.onCategoryAndSubcategoryChoose(selectedCategory, selectedSubcategory);
            }
        });
        categories = daoSession.loadAll(RootCategory.class);
        if (categories != null && !categories.isEmpty()) {
            selectedCategory = categories.get(0);
            if (selectedCategory.getSubCategories() != null && !selectedCategory.getSubCategories().isEmpty())
                selectedSubcategory = selectedCategory.getSubCategories().get(0);
            else
                selectedSubcategory = null;
            List<Bitmap> categoryBitmaps = new ArrayList<>();
            for (RootCategory category : categories) {
                int resId = getResources().getIdentifier(category.getIcon(), "drawable", getContext().getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.twentyfive_dp), (int) getResources().getDimension(R.dimen.twentyfive_dp), false);
                categoryBitmaps.add(bitmap);
            }
            abchvCategoryChooser.setBitmaps(categoryBitmaps);
            if (selectedCategory.getSubCategories() != null && !selectedCategory.getSubCategories().isEmpty()) {
                List<Bitmap> subcategoryBitmaps = new ArrayList<>();
                for (SubCategory subCategory : selectedCategory.getSubCategories()) {
                    int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.twentyfive_dp), (int) getResources().getDimension(R.dimen.twentyfive_dp), false);
                    subcategoryBitmaps.add(bitmap);
                }
                abchvSubcategoryChooser.setBitmaps(subcategoryBitmaps);
            }
        }
        else
            setVisibility(GONE);
    }

    public void selectCategory(RootCategory category) {
        selectedCategory = category;
        if (categories == null)
            categories = daoSession.loadAll(RootCategory.class);
        for (int i = 0; i < categories.size(); i++) {
            if (category.getId().equals(categories.get(i).getId())) {
                abchvSubcategoryChooser.setSelectedPosition(i);
                break;
            }
        }
    }

    public void selectSubCategory(SubCategory subCategory) {
        selectedSubcategory = subCategory;
        if (categories == null)
            categories = daoSession.loadAll(RootCategory.class);
        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < categories.get(i).getSubCategories().size(); i++) {
                if (categories.get(i).getSubCategories().get(j).equals(subCategory.getId())) {
                    abchvSubcategoryChooser.setSelectedPosition(j);
                    break;
                }
            }
        }
    }

    public void setListener(CategoryAndSubcategoryChooseListener listener) {
        this.listener = listener;
    }

    public interface CategoryAndSubcategoryChooseListener {
        public void onCategoryAndSubcategoryChoose(RootCategory category, SubCategory subCategory);
    }
}
