package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.fragments.ReportByCategoryRootCategoryFragment;
import com.jim.finansia.managers.CommonOperations;

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
    ImageView ivToLeftButton,ivToRightButton;

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
    public void toNext(){
        if (countAccounts-1!=vpCategorySlider.getCurrentItem()){
            keyAnimIsEnded  = false;
            vpCategorySlider.setCurrentItem(vpCategorySlider.getCurrentItem()+1,true);
        }
    }
    public void toPrev(){
        if (vpCategorySlider.getCurrentItem()!=0){
            keyAnimIsEnded  = false;
            vpCategorySlider.setCurrentItem(vpCategorySlider.getCurrentItem()-1,true);
        }
    }
    boolean keyAnimIsEnded = true;
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.category_slider_layout, this, true);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        vpCategorySlider = (ViewPager) findViewById(R.id.vpCategorySlider);
        ivToLeftButton = (ImageView) findViewById(R.id.ivToLeftButton);
        ivToRightButton = (ImageView) findViewById(R.id.ivToRightButton);
        ivToLeftButton.setVisibility(INVISIBLE);
        initDataForViewPager();
        adapter = new CategoryAdapter(((PocketAccounter)context).getSupportFragmentManager());
        vpCategorySlider.setAdapter(adapter);
        vpCategorySlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (listener != null) {
                    listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position, true);
                }

            }

            @Override
            public void onPageSelected(int position) {
                CategorySliding.this.position = position;
                if (listener != null) {
                    listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position, true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && lastPosition != position) {
                    if (listener != null) {
                        listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position, false);
                    }
                    lastPosition = position;
                    if(lastPosition==0){
                        ivToLeftButton.setVisibility(INVISIBLE);
                    }
                    else ivToLeftButton.setVisibility(VISIBLE);
                    if(lastPosition==countAccounts-1){
                        ivToRightButton.setVisibility(INVISIBLE);
                    }
                    else ivToRightButton.setVisibility(VISIBLE);
                    keyAnimIsEnded=true;
                }
            }
        });
        ivToLeftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keyAnimIsEnded){
                    toPrev();
                }
//                CommonOperations.buttonClickCustomAnimation(0.85f,ivToLeftButton, new CommonOperations.AfterAnimationEnd() {
//                    @Override
//                    public void onAnimoationEnd() {
//
//
//                    }
//                });
            }
        });
        ivToRightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keyAnimIsEnded){
                    toNext();

                }
//                CommonOperations.buttonClickCustomAnimation(0.85f,ivToRightButton, new CommonOperations.AfterAnimationEnd() {
//                    @Override
//                    public void onAnimoationEnd() {
//
//
//                    }
//                });
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
                listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position, false);
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
            listener.onSlide(allCategories.get(position), allColors.get(allCategories.get(position)), position, false);
        }
    }

    private Map<String, Integer> initColors(int position) {
        Map<String, Integer> colors = new HashMap<>();
        colors.put("null", colorsCode[0]);
        RootCategory category = daoSession.load(RootCategory.class, allCategories.get(position));
        if (category.getSubCategories() != null &&category.getSubCategories().size()!=0) {
            double dlinaShaga = 11 / category.getSubCategories().size() ;
            double polushag = dlinaShaga/2;
            for (int i = 0; i < category.getSubCategories().size(); i++) {
                colors.put(category.getSubCategories().get(i).getId(), colorsCode[(int)Math.round(polushag+(dlinaShaga*i))]);
            }
        }
        return colors;
    }
    int countAccounts = 0;
    private void initDataForViewPager() {
        if (allCategories == null) {
            allCategories = new ArrayList<>();
            List<RootCategory> categories = daoSession.loadAll(RootCategory.class);
            countAccounts = categories.size();
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
    int colorCount = 0;
    private int[] colorsCode = {
            Color.parseColor("#0d3c55"),
            Color.parseColor("#0f5b78"),
            Color.parseColor("#117899"),
            Color.parseColor("#1395ba"),
            Color.parseColor("#5ca793"),
            Color.parseColor("#a2b86c"),
            Color.parseColor("#ebc844"),
            Color.parseColor("#ecaa38"),
            Color.parseColor("#ef8b2c"),
            Color.parseColor("#f16c20"),
            Color.parseColor("#d94e1f"),
            Color.parseColor("#c02e1d")
    };
}
