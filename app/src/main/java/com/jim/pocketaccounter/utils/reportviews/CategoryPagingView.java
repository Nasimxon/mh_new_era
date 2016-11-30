package com.jim.pocketaccounter.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.RootCategory;

import java.util.List;

public class CategoryPagingView extends RelativeLayout {
    private CategoryPagingPageChangeListener listener;
    private ViewPager vpCategoryPaging;
    private int position = 0;
    private List<RootCategory> categories;
    public CategoryPagingView(Context context) {
        super(context);
        init(context);
    }

    public CategoryPagingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CategoryPagingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @SuppressLint("NewApi")
    public CategoryPagingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.category_paging_layout, this, true);
        vpCategoryPaging = (ViewPager) findViewById(R.id.vpCategoryPaging);
        vpCategoryPaging.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) { CategoryPagingView.this.position = position; }
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && listener != null && categories != null && !categories.isEmpty()) {
                    listener.onPageChange(categories.get(CategoryPagingView.this.position));
                }
            }
        });
    }

    public void setCategoryPagingPageChangeListener(CategoryPagingPageChangeListener listener) {
        this.listener = listener;
    }
}
