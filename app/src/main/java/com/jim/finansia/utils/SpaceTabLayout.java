package com.jim.finansia.utils;

/*
 * Copyright (c) 2016 Lung Razvan
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.utils.Utils;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.debt.BorrowFragment;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.managers.CommonOperations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jxl.read.biff.Record;

import static com.jim.finansia.fragments.RecordDetailFragment.SELECTED_POSITION;


public class SpaceTabLayout extends RelativeLayout {

    private Context context;

    private TabLayout tabLayout;

    private TabLayout.Tab tabOne;
    private TabLayout.Tab tabTwo;
    private TabLayout.Tab tabThree;
    private TabLayout.Tab tabFour;
    private TabLayout.Tab tabFive;

    private RelativeLayout parentLayout;
    private RelativeLayout selectedTabLayout;
    private FloatingActionButton actionButton;
    private ImageView backgroundImage;
    private ImageView backgroundImage2;

    private TextView tabOneTextView;
    private TextView tabTwoTextView;
    private TextView tabThreeTextView;

    private String text_one;
    private String text_two;
    private String text_three;

    private ImageView tabOneImageView;
    private ImageView tabTwoImageView;
    private ImageView tabThreeImageView;
    private ImageView tabFourImageView;
    private ImageView tabFiveImageView;

    private List<TabLayout.Tab> tabs = new ArrayList<>();
    private List<Integer> tabSize = new ArrayList<>();

    private int numberOfTabs = 3;
    private int currentPosition;
    private int startingPosition;

    private OnClickListener tabOneOnClickListener;
    private OnClickListener tabTwoOnClickListener;
    private OnClickListener tabThreeOnClickListener;
    private OnClickListener tabFourOnClickListener;
    private OnClickListener tabFiveOnClickListener;


    private Drawable defaultTabOneButtonIcon;
    private Drawable defaultTabTwoButtonIcon;
    private Drawable defaultTabThreeButtonIcon;
    private Drawable defaultTabFourButtonIcon;
    private Drawable defaultTabFiveButtonIcon;

    private int defaultTextColor;
    private int defaultButtonColor;
    private int defaultTabColor;

    private boolean iconOnly = false;

    private boolean SCROLL_STATE_DRAGGING = false;

    private static final String CURRENT_POSITION_SAVE_STATE = "buttonPosition";
    public SpaceTabLayout(Context context) {
        super(context);
        this.context = context;
        init();

    }

    public SpaceTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        initArrts(attrs);
    }

    public SpaceTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initArrts(attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(eu.long1.spacetablayout.R.layout.three_tab_space_layout, this);

        parentLayout = (RelativeLayout) findViewById(eu.long1.spacetablayout.R.id.tabLayout);

        selectedTabLayout = (RelativeLayout) findViewById(eu.long1.spacetablayout.R.id.selectedTabLayout);

        backgroundImage = (ImageView) findViewById(eu.long1.spacetablayout.R.id.backgroundImage);
        backgroundImage2 = (ImageView) findViewById(eu.long1.spacetablayout.R.id.backgroundImage2);

        actionButton = (FloatingActionButton) findViewById(eu.long1.spacetablayout.R.id.fab);

        tabLayout = (TabLayout) findViewById(eu.long1.spacetablayout.R.id.spaceTab);

        defaultTabOneButtonIcon = ContextCompat.getDrawable(getContext(), eu.long1.spacetablayout.R.drawable.ic_tab_one);
        defaultTabTwoButtonIcon = context.getResources().getDrawable(eu.long1.spacetablayout.R.drawable.ic_tab_two);
        defaultTabThreeButtonIcon = context.getResources().getDrawable(eu.long1.spacetablayout.R.drawable.ic_tab_three);
        defaultTabFourButtonIcon = context.getResources().getDrawable(eu.long1.spacetablayout.R.drawable.ic_tab_four);
        defaultTabFiveButtonIcon = context.getResources().getDrawable(eu.long1.spacetablayout.R.drawable.ic_tab_five);

        defaultTextColor = ContextCompat.getColor(context, android.R.color.primary_text_dark);

        defaultButtonColor = ContextCompat.getColor(context, eu.long1.spacetablayout.R.color.colorAccent);

        defaultTabColor = ContextCompat.getColor(context, eu.long1.spacetablayout.R.color.colorPrimary);
    }

    private void initArrts(AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, eu.long1.spacetablayout.R.styleable.SpaceTabLayout);
        for (int i = 0; i < a.getIndexCount(); ++i) {
            int attr = a.getIndex(i);
            if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_number_of_tabs) {
                numberOfTabs = a.getInt(attr, 3);
                if (numberOfTabs == 0) {
                    numberOfTabs = 3;
                    iconOnly = true;
                }
            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_starting_position) {
                startingPosition = a.getInt(attr, 0);
                int test = startingPosition + 1;
                if (test > numberOfTabs) {
                    switch (numberOfTabs) {
                        case 3:
                            startingPosition = 1;
                            actionButton.setImageDrawable(defaultTabTwoButtonIcon);
                            actionButton.setOnClickListener(tabTwoOnClickListener);
                            break;
                        case 4:
                            startingPosition = 0;
                            actionButton.setImageDrawable(defaultTabOneButtonIcon);
                            actionButton.setOnClickListener(tabOneOnClickListener);
                            break;
                        case 5:
                            startingPosition = 2;
                            actionButton.setImageDrawable(defaultTabThreeButtonIcon);
                            actionButton.setOnClickListener(tabThreeOnClickListener);
                            break;
                    }
                } else {
                    switch (startingPosition) {
                        case 0:
                            actionButton.setImageDrawable(defaultTabOneButtonIcon);
                            actionButton.setOnClickListener(tabOneOnClickListener);
                            break;
                        case 1:
                            actionButton.setImageDrawable(defaultTabTwoButtonIcon);
                            actionButton.setOnClickListener(tabTwoOnClickListener);
                            break;
                        case 2:
                            actionButton.setImageDrawable(defaultTabThreeButtonIcon);
                            actionButton.setOnClickListener(tabThreeOnClickListener);
                            break;
                        case 3:
                            actionButton.setImageDrawable(defaultTabFourButtonIcon);
                            actionButton.setOnClickListener(tabFourOnClickListener);
                            break;
                        case 4:
                            actionButton.setImageDrawable(defaultTabFiveButtonIcon);
                            actionButton.setOnClickListener(tabFiveOnClickListener);
                            break;
                    }
                }
            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_tab_color) {
                defaultTabColor = a.getColor(attr, context.getResources().getIdentifier("colorAccent", "color", context.getPackageName()));

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_button_color) {
                defaultButtonColor = a.getColor(attr, context.getResources().getIdentifier("colorPrimary", "color", context.getPackageName()));

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_text_color) {
                defaultTextColor = a.getColor(attr, ContextCompat.getColor(context, android.R.color.primary_text_dark));

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_icon_one) {
                defaultTabOneButtonIcon = a.getDrawable(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_icon_two) {
                defaultTabTwoButtonIcon = a.getDrawable(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_icon_three) {
                defaultTabThreeButtonIcon = a.getDrawable(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_icon_four) {
                if (numberOfTabs > 3)
                    defaultTabFourButtonIcon = a.getDrawable(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_icon_five) {
                if (numberOfTabs > 4)
                    defaultTabFiveButtonIcon = a.getDrawable(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_text_one) {
                text_one = a.getString(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_text_two) {
                text_two = a.getString(attr);

            } else if (attr == eu.long1.spacetablayout.R.styleable.SpaceTabLayout_text_three) {
                text_three = a.getString(attr);

            }
        }
        a.recycle();
    }

    /**
     * This will initialize the View and the ViewPager to properly display
     * the fragments from the list.
     *
     * @param viewPager          where you want the fragments to show
     * @param fragmentManager    needed for the fragment transactions
     * @param fragments          fragments to be displayed
     * @param savedInstanceState used to save the current position
     */
    public void initialize(ViewPager viewPager, FragmentManager fragmentManager, List<Fragment> fragments, final Bundle savedInstanceState) {
        if (numberOfTabs < fragments.size() || numberOfTabs > fragments.size())
            throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
        viewPager.setAdapter(new TabLayoutAdapter(fragments, fragmentManager));

        tabLayout.setupWithViewPager(viewPager);

        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tabSize.add(tabOne.getCustomView().getWidth());
                        tabSize.add(getWidth());
                        tabSize.add(backgroundImage.getWidth());

                        moveTab(tabSize, startingPosition);

                        if (currentPosition == 0) {
                            currentPosition = startingPosition;
                            tabs.get(startingPosition).getCustomView().setAlpha(0);
                        }
                        if (savedInstanceState != null) {
                            currentPosition = savedInstanceState.getInt(CURRENT_POSITION_SAVE_STATE);
                            moveTab(tabSize, currentPosition);
                        }


                        if (Build.VERSION.SDK_INT < 16)
                            getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        else getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (SCROLL_STATE_DRAGGING) {
                    tabs.get(position).getCustomView().setAlpha(positionOffset);
                    if (position < numberOfTabs - 1) {
                        tabs.get(position + 1).getCustomView().setAlpha(1 - positionOffset);
                    }

                    if (!tabSize.isEmpty()) {
                        if (position < currentPosition) {
                            final float endX = -tabSize.get(2) / 2 + getX(position, tabSize) + 42;
                            final float startX = -tabSize.get(2) / 2 + getX(currentPosition, tabSize) + 42;

                            if (!tabSize.isEmpty()) {
                                float a = endX - (positionOffset * (endX - startX));
                                selectedTabLayout.setX(a);
                            }

                        } else {
                            position++;
                            final float endX = -tabSize.get(2) / 2 + getX(position, tabSize) + 42;
                            final float startX = -tabSize.get(2) / 2 + getX(currentPosition, tabSize) + 42;

                            float a = startX + (positionOffset * (endX - startX));
                            selectedTabLayout.setX(a);
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                for (TabLayout.Tab t : tabs) t.getCustomView().setAlpha(1);
                tabs.get(position).getCustomView().setAlpha(0);
                moveTab(tabSize, position);
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                SCROLL_STATE_DRAGGING = state == ViewPager.SCROLL_STATE_DRAGGING;

                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    for (TabLayout.Tab t : tabs) t.getCustomView().setAlpha(1);
                    tabs.get(currentPosition).getCustomView().setAlpha(0);
                    moveTab(tabSize, currentPosition);
                }
            }
        });

        tabOne = tabLayout.getTabAt(0);
        tabTwo = tabLayout.getTabAt(1);
        tabThree = tabLayout.getTabAt(2);
        if (numberOfTabs > 3) tabFour = tabLayout.getTabAt(3);
        if (numberOfTabs > 4) tabFive = tabLayout.getTabAt(4);

        if (numberOfTabs == 3 && !iconOnly) {
            tabOne.setCustomView(eu.long1.spacetablayout.R.layout.icon_text_tab_layout);
            tabTwo.setCustomView(eu.long1.spacetablayout.R.layout.icon_text_tab_layout);
            tabThree.setCustomView(eu.long1.spacetablayout.R.layout.icon_text_tab_layout);

            tabs.add(tabOne);
            tabs.add(tabTwo);
            tabs.add(tabThree);

            tabOneTextView = (TextView) tabOne.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabTextView);
            tabTwoTextView = (TextView) tabTwo.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabTextView);
            tabThreeTextView = (TextView) tabThree.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabTextView);

            tabOneImageView = (ImageView) tabOne.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
            tabTwoImageView = (ImageView) tabTwo.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
            tabThreeImageView = (ImageView) tabThree.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);

        } else {

            tabOne.setCustomView(eu.long1.spacetablayout.R.layout.icon_tab_layout);
            tabTwo.setCustomView(eu.long1.spacetablayout.R.layout.icon_tab_layout);
            tabThree.setCustomView(eu.long1.spacetablayout.R.layout.icon_tab_layout);
            if (numberOfTabs > 3) tabFour.setCustomView(eu.long1.spacetablayout.R.layout.icon_tab_layout);
            if (numberOfTabs > 4) tabFive.setCustomView(eu.long1.spacetablayout.R.layout.icon_tab_layout);

            tabs.add(tabOne);
            tabs.add(tabTwo);
            tabs.add(tabThree);
            if (numberOfTabs > 3) tabs.add(tabFour);
            if (numberOfTabs > 4) tabs.add(tabFive);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) Utils.convertDpToPixel(getResources().getDimension(R.dimen.twelve_dp)), (int) Utils.convertDpToPixel(getResources().getDimension(R.dimen.twelve_dp)),
                    (int) Utils.convertDpToPixel(getResources().getDimension(R.dimen.twelve_dp)),(int) Utils.convertDpToPixel(getResources().getDimension(R.dimen.twelve_dp)));

            tabOneImageView = (ImageView) tabOne.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);

            tabOneImageView.setLayoutParams(lp);
            tabTwoImageView = (ImageView) tabTwo.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
            tabTwoImageView.setLayoutParams(lp);
            tabThreeImageView = (ImageView) tabThree.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
            tabThreeImageView.setLayoutParams(lp);
            if (numberOfTabs > 3){
                tabFourImageView = (ImageView) tabFour.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
                tabFourImageView.setLayoutParams(lp);
            }
            if (numberOfTabs > 4)
                tabFiveImageView = (ImageView) tabFive.getCustomView().findViewById(eu.long1.spacetablayout.R.id.tabImageView);
        }


        selectedTabLayout.bringToFront();
        tabLayout.setSelectedTabIndicatorHeight(0);
        setAttrs();
    }


    private void setAttrs() {
        setTabColor(defaultTabColor);

        setButtonColor(defaultButtonColor);

        if (numberOfTabs == 3 && !iconOnly) {
            if (text_one != null) setTabOneText(text_one);
            if (text_two != null) setTabTwoText(text_two);
            if (text_three != null) setTabThreeText(text_three);

            setTabOneTextColor(defaultTextColor);
            setTabTwoTextColor(defaultTextColor);
            setTabThreeTextColor(defaultTextColor);
        }

        setTabOneIcon(defaultTabOneButtonIcon);
        setTabTwoIcon(defaultTabTwoButtonIcon);
        setTabThreeIcon(defaultTabThreeButtonIcon);
        if (numberOfTabs > 3) setTabFourIcon(defaultTabFourButtonIcon);
        if (numberOfTabs > 4) setTabFiveIcon(defaultTabFiveButtonIcon);


    }

    public void saveState(Bundle outState) {
        outState.putInt(CURRENT_POSITION_SAVE_STATE, currentPosition);
    }

    private void moveTab(List<Integer> tabSize, int position) {
        if (!tabSize.isEmpty()) {
            float backgroundX = -tabSize.get(2) / 2 + getX(position, tabSize) + 42;
            selectedTabLayout.setX(backgroundX);
            switch (position) {
                case 0:
                    actionButton.setImageDrawable(defaultTabOneButtonIcon);
                    actionButton.setOnClickListener(tabOneOnClickListener);
                    break;
                case 1:
                    actionButton.setImageDrawable(defaultTabTwoButtonIcon);
                    actionButton.setOnClickListener(tabTwoOnClickListener);
                    break;
                case 2:
                    actionButton.setImageDrawable(defaultTabThreeButtonIcon);
                    actionButton.setOnClickListener(tabThreeOnClickListener);
                    break;
                case 3:
                    actionButton.setImageDrawable(defaultTabFourButtonIcon);
                    actionButton.setOnClickListener(tabFourOnClickListener);
                    break;
                case 4:
                    actionButton.setImageDrawable(defaultTabFiveButtonIcon);
                    actionButton.setOnClickListener(tabFiveOnClickListener);
                    break;

            }
        }
    }

    private void moveTabAnimate(List<Integer> tabSize, int position) {
        if (!tabSize.isEmpty()) {
            float backgroundX = -tabSize.get(2) / 2 + getX(position, tabSize) + 42;
            selectedTabLayout.animate().x(backgroundX).setDuration(200);
            switch (position) {
                case 0:
                    actionButton.setImageDrawable(defaultTabOneButtonIcon);
                    actionButton.setOnClickListener(tabOneOnClickListener);
                    break;
                case 1:
                    actionButton.setImageDrawable(defaultTabTwoButtonIcon);
                    actionButton.setOnClickListener(tabTwoOnClickListener);
                    break;
                case 2:
                    actionButton.setImageDrawable(defaultTabThreeButtonIcon);
                    actionButton.setOnClickListener(tabThreeOnClickListener);
                    break;
                case 3:
                    actionButton.setImageDrawable(defaultTabFourButtonIcon);
                    actionButton.setOnClickListener(tabFourOnClickListener);
                    break;
                case 4:
                    actionButton.setImageDrawable(defaultTabFiveButtonIcon);
                    actionButton.setOnClickListener(tabFiveOnClickListener);
                    break;
            }
        }
    }

    private float getX(int position, List<Integer> sizesList) {
        if (!sizesList.isEmpty()) {
            float tabWidth = sizesList.get(0);

            float numberOfMargins = 2 * numberOfTabs;
            float itemsWidth = (int) (numberOfTabs * tabWidth);
            float marginsWidth = sizesList.get(1) - itemsWidth;

            float margin = marginsWidth / numberOfMargins;

            //TODO: this is a magic number
            float half = 42;


            float x = 0;
            switch (position) {
                case 0:
                    x = tabWidth / 2 + margin - half;
                    break;
                case 1:
                    x = tabWidth * 3 / 2 + 3 * margin - half;
                    break;
                case 2:
                    x = tabWidth * 5 / 2 + 5 * margin - half;
                    break;
                case 3:
                    x = tabWidth * 7 / 2 + 7 * margin - half;
                    break;
                case 4:
                    x = tabWidth * 9 / 2 + 9 * margin - half;
                    break;
            }
            return x;
        }
        return 0;
    }

    //TODO: get this working :))
    private void setCurrentPosition(int currentPosition) {

    }

    /**
     * You can use it, for example, if want to set the same listener to all
     * buttons and you can use a switch using this method to identify the
     * button that was pressed.
     *
     * @return the current tab position
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Used to set the same OnClickListener to each position of the button.
     *
     * @param l the listener you want to set
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        setTabOneOnClickListener(l);
        setTabTwoOnClickListener(l);
        setTabThreeOnClickListener(l);
        if (numberOfTabs > 3) setTabFourOnClickListener(l);
        if (numberOfTabs > 4) setTabFiveOnClickListener(l);
    }


    /***********************************************************************************************
     * getters and setters for the OnClickListeners of each tab position
     **********************************************************************************************/
    public OnClickListener getTabOneOnClickListener() {
        return tabOneOnClickListener;
    }

    public OnClickListener getTabTwoOnClickListener() {
        return tabTwoOnClickListener;
    }

    public OnClickListener getTabThreeOnClickListener() {
        return tabThreeOnClickListener;
    }

    public OnClickListener getTabFourOnClickListener() {
        return tabFourOnClickListener;
    }

    public OnClickListener getTabFiveOnClickListener() {
        return tabFiveOnClickListener;
    }

    public void setTabOneOnClickListener(OnClickListener l) {
        tabOneOnClickListener = l;
    }

    public void setTabTwoOnClickListener(OnClickListener l) {
        tabTwoOnClickListener = l;
    }

    public void setTabThreeOnClickListener(OnClickListener l) {
        tabThreeOnClickListener = l;
    }

    public void setTabFourOnClickListener(OnClickListener l) {
        if (numberOfTabs > 3) tabFourOnClickListener = l;
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFiveOnClickListener(OnClickListener l) {
        if (numberOfTabs > 4) tabFiveOnClickListener = l;
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    /***********************************************************************************************
     * Tab and Views getters, setters and customization for them
     **********************************************************************************************/
    public View getTabLayout() {
        return parentLayout;
    }

    public void setTabColor(@ColorInt int backgroundColor) {
        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        Drawable image = ContextCompat.getDrawable(context, eu.long1.spacetablayout.R.drawable.background);
        Drawable image2 = ContextCompat.getDrawable(context, eu.long1.spacetablayout.R.drawable.background2);
        image.setColorFilter(porterDuffColorFilter);
        image2.setColorFilter(porterDuffColorFilter);

        backgroundImage.setImageDrawable(image);
        backgroundImage2.setImageDrawable(image2);
    }

    public FloatingActionButton getButton() {
        return actionButton;
    }

    public void setButtonColor(@ColorInt int backgroundColor) {
        actionButton.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
    }


    public View getTabOneView() {
        return tabOne.getCustomView();
    }

    public View getTabTwoView() {
        return tabTwo.getCustomView();
    }

    public View getTabThreeView() {
        return tabThree.getCustomView();
    }

    public View getTabFourView() {
        if (numberOfTabs > 3) return tabFour.getCustomView();
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public View getTabFiveView() {
        if (numberOfTabs > 4) return tabFive.getCustomView();
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }


    public void setTabOneView(View tabOneView) {
        tabOne.setCustomView(tabOneView);
    }

    public void setTabOneView(@LayoutRes int tabOneLayout) {
        tabOne.setCustomView(tabOneLayout);
    }

    public void setTabTwoView(View tabTwoView) {
        tabTwo.setCustomView(tabTwoView);
    }

    public void setTabTwoView(@LayoutRes int tabTwoLayout) {
        tabOne.setCustomView(tabTwoLayout);
    }

    public void setTabThreeView(View tabThreeView) {
        tabThree.setCustomView(tabThreeView);
    }

    public void setTabThreeView(@LayoutRes int tabThreeLayout) {
        tabThree.setCustomView(tabThreeLayout);
    }

    public void setTabFourView(View tabFourView) {
        if (numberOfTabs > 3) tabFour.setCustomView(tabFourView);
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFourView(@LayoutRes int tabFourLayout) {
        if (numberOfTabs > 3) tabFour.setCustomView(tabFourLayout);
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFiveView(View tabFiveView) {
        if (numberOfTabs > 4) tabFour.setCustomView(tabFiveView);
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFiveView(@LayoutRes int tabFiveLayout) {
        if (numberOfTabs > 4) tabFour.setCustomView(tabFiveLayout);
        else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }


    public void setTabOneIcon(@DrawableRes int tabOneIcon) {
        defaultTabOneButtonIcon = context.getResources().getDrawable(tabOneIcon);
        tabOneImageView.setImageResource(tabOneIcon);
    }

    public void setTabOneIcon(Drawable tabOneIcon) {
        defaultTabOneButtonIcon = tabOneIcon;
        tabOneImageView.setImageDrawable(tabOneIcon);
    }

    public void setTabTwoIcon(@DrawableRes int tabTwoIcon) {
        defaultTabTwoButtonIcon = context.getResources().getDrawable(tabTwoIcon);
        tabTwoImageView.setImageResource(tabTwoIcon);
    }

    public void setTabTwoIcon(Drawable tabTwoIcon) {
        defaultTabTwoButtonIcon = tabTwoIcon;
        tabTwoImageView.setImageDrawable(tabTwoIcon);
    }

    public void setTabThreeIcon(@DrawableRes int tabThreeIcon) {
        defaultTabThreeButtonIcon = context.getResources().getDrawable(tabThreeIcon);
        tabThreeImageView.setImageResource(tabThreeIcon);
    }

    public void setTabThreeIcon(Drawable tabThreeIcon) {
        defaultTabThreeButtonIcon = tabThreeIcon;
        tabThreeImageView.setImageDrawable(tabThreeIcon);
    }

    public void setTabFourIcon(@DrawableRes int tabFourIcon) {
        if (numberOfTabs > 3) {
            defaultTabFourButtonIcon = context.getResources().getDrawable(tabFourIcon);
            tabFourImageView.setImageResource(tabFourIcon);
        } else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFourIcon(Drawable tabFourIcon) {
        if (numberOfTabs > 3) {
            defaultTabFourButtonIcon = tabFourIcon;
            tabFourImageView.setImageDrawable(tabFourIcon);
        } else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFiveIcon(@DrawableRes int tabFiveIcon) {
        if (numberOfTabs > 4) {
            defaultTabFiveButtonIcon = context.getResources().getDrawable(tabFiveIcon);
            tabFiveImageView.setImageResource(tabFiveIcon);
        } else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    public void setTabFiveIcon(Drawable tabFiveIcon) {
        if (numberOfTabs > 4) {
            defaultTabFiveButtonIcon = tabFiveIcon;
            tabFiveImageView.setImageDrawable(tabFiveIcon);
        } else throw new IllegalArgumentException("You have " + numberOfTabs + " tabs.");
    }

    /***********************************************************************************************
     * Customization for the TextViews when you have three tabs
     **********************************************************************************************/
    public void setTabOneText(String tabOneText) {
        if (!iconOnly) tabOneTextView.setText(tabOneText);
        else throw new IllegalArgumentException("You selected icons only.");
    }

    public void setTabOneText(@StringRes int tabOneText) {
        if (!iconOnly) tabOneTextView.setText(tabOneText);
        else throw new IllegalArgumentException("You selected icons only.");
    }

    public void setTabTwoText(String tabTwoText) {
        if (!iconOnly) tabTwoTextView.setText(tabTwoText);
        else throw new IllegalArgumentException("You selected icons only.");
    }

    public void setTabTwoText(@StringRes int tabTwoText) {
        if (!iconOnly) tabTwoTextView.setText(tabTwoText);
        else throw new IllegalArgumentException("You selected icons only.");
    }

    public void setTabThreeText(String tabThreeText) {
        if (!iconOnly) tabThreeTextView.setText(tabThreeText);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabThreeText(@StringRes int tabThreeText) {
        if (!iconOnly) tabThreeTextView.setText(tabThreeText);
        else throw new IllegalArgumentException("You selected icons only.");
    }


    public void setTabOneTextColor(@ColorInt int tabOneTextColor) {
        if (!iconOnly) tabOneTextView.setTextColor(tabOneTextColor);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabOneTextColor(ColorStateList colorStateList) {
        if (!iconOnly) tabOneTextView.setTextColor(colorStateList);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabTwoTextColor(@ColorInt int tabTwoTextColor) {
        if (!iconOnly) tabTwoTextView.setTextColor(tabTwoTextColor);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabTwoTextColor(ColorStateList colorStateList) {
        if (!iconOnly) tabTwoTextView.setTextColor(colorStateList);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabThreeTextColor(@ColorInt int tabThreeTextColor) {
        if (!iconOnly) tabThreeTextView.setTextColor(tabThreeTextColor);
        else throw new IllegalArgumentException("You selected icons only.");

    }

    public void setTabThreeTextColor(ColorStateList colorStateList) {
        if (!iconOnly) tabThreeTextView.setTextColor(colorStateList);
        else throw new IllegalArgumentException("You selected icons only.");


    }

    private class TabLayoutAdapter extends FragmentStatePagerAdapter {
        List<Fragment> list;

        public TabLayoutAdapter(List<Fragment> list, FragmentManager fm) {
            super(fm);
            this.list = list;
        }

        public Fragment getItem(int position) {
            return list.get(position);
        }

        public int getCount() {
            return list.size();
        }
    }

}
