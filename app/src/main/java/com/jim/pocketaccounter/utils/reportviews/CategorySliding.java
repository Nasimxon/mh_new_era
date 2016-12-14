package com.jim.pocketaccounter.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.fragments.ReportByCategoryRootCategoryFragment;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class CategorySliding extends LinearLayout {
    private CategorySlidingInterface listener;
    private ViewPager vpCategorySlider;
    private List<String> allCategories;
    private int position = 0, lastPosition = 0;
    private Map<String, Map<String, Integer>> allColors;
    private CategoryAdapter adapter;
    private Calendar begin, end;
    @Inject DaoSession daoSession;
    public CategorySliding(Context context) {
        super(context);
        init(context);
    }
    public CategorySliding(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CategorySliding(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @SuppressLint("NewApi")
    public CategorySliding(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.category_slider_layout, this, true);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        vpCategorySlider = (ViewPager) findViewById(R.id.vpCategorySlider);
        initDataForViewPager();
        adapter = new CategoryAdapter(((PocketAccounter)context).getSupportFragmentManager());
        vpCategorySlider.setAdapter(adapter);
        vpCategorySlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CategorySliding.this.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && lastPosition != position) {
                    if (listener != null) {
                        listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position);
                    }
                    lastPosition = position;
                }
            }
        });
        vpCategorySlider.setCurrentItem(0);
        generateAllColors();
    }

    public void setInterval(Calendar begin, Calendar end) {
        this.begin = (Calendar) begin.clone();
        this.end = (Calendar) end.clone();
        init(getContext());
        if (vpCategorySlider != null) {
            vpCategorySlider.setCurrentItem(position);
            if (listener != null) {
                listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position);
            }
        }
    }

    private void generateAllColors() {
        allColors = new HashMap<>();
        for (int i = 0; i < allCategories.size(); i++) {
            allColors.put(allCategories.get(i), initColors(i));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (listener != null) {
            listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position);
        }
    }

    private Map<String, Integer> initColors(int position) {
        Map<String, Integer> colors = new HashMap<>();
        colors.put("null", generateColor());
        RootCategory category = daoSession.load(RootCategory.class, allCategories.get(position));
        if (category.getSubCategories() != null) {
            for (int i = 0; i < category.getSubCategories().size(); i++) {
                colors.put(category.getSubCategories().get(i).getId(), generateColor());
            }
        }
        return colors;
    }

    private void initDataForViewPager() {
        if (allCategories == null) {
            allCategories = new ArrayList<>();
            List<RootCategory> categories = daoSession.loadAll(RootCategory.class);
            for (RootCategory category : categories)
                allCategories.add(category.getId());
        }
    }

    public void setListener(CategorySlidingInterface listener) {
        this.listener = listener;
    }
    class CategoryAdapter extends FragmentStatePagerAdapter {
        public CategoryAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            String id = allCategories.get(position);
            ReportByCategoryRootCategoryFragment fragment = new ReportByCategoryRootCategoryFragment(id, allColors.get(id));
            fragment.setInterval(begin, end);
            return fragment;
        }
        @Override
        public int getCount() {
            return allCategories.size();
        }
    }
    //----------------- util methods ----------------------
    private int generateColor() {
        return Color.rgb(RandomUtils.nextInt(0, 192),
                         RandomUtils.nextInt(0, 192),
                         RandomUtils.nextInt(0, 192)
        );
    }
}
