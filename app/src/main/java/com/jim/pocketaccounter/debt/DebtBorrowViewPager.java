package com.jim.pocketaccounter.debt;

import android.content.Context;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by vosit on 09.11.16.
 */

public class DebtBorrowViewPager extends ViewPager {
    FragmentStatePagerAdapter mPagerAdapter;

    public DebtBorrowViewPager(Context context) {
        super(context);
    }

    public DebtBorrowViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPagerAdapter != null) {
            super.setAdapter(mPagerAdapter);
//            mPageIndicator.setViewPager(this);
        }
    }

    public void storeAdapter(FragmentStatePagerAdapter pagerAdapter) {
        mPagerAdapter = pagerAdapter;
    }


}
