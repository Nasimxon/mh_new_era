package com.jim.pocketaccounter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class ManualEnterFragment extends Fragment {
    private ViewPager lvpMain;
    private int lastPos = 5000, idleLast = 5000;
    private Boolean direction = null;
    private MainPageFragment nextPage;
    @Inject
    DataCache dataCache;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.manual_enter_fragment, container, false);
        ((PocketAccounterApplication) getContext().getApplicationContext()).component().inject(this);
        lvpMain = (ViewPager) rootView.findViewById(R.id.lvpMain);
        initialize();
        return rootView;
    }

    public void initialize() {
        FragmentPagerAdapter adapter = new LVPAdapter(getFragmentManager());
        lvpMain.setAdapter(adapter);
        lvpMain.setCurrentItem(5000, false);
        lvpMain.post(new Runnable() {
            @Override
            public void run() {
                lvpMain.setCurrentItem(5000, false);
            }
        });
        lvpMain.setOffscreenPageLimit(0);
        lvpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (lastPos != position && direction == null) {
                    direction = lastPos<position;
                }
                final MainPageFragment page = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+position);
                MainPageFragment prevFragment;
                if (lastPos>position && !direction) {
                    prevFragment = ((MainPageFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(position+1)));
                    if (prevFragment.getDay().compareTo(page.getDay()) <= 0) {
                        Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, -1);
                        page.setDay(day);
                    }
                }
                if (lastPos<position && direction) {
                    prevFragment = ((MainPageFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+(position-1)));
                    if (prevFragment.getDay().compareTo(page.getDay()) >= 0) {
                        final Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, 1);
                        page.setDay(day);
                    }
                }
                if (page != null) {
                    page.update();
                    dataCache.setEndDate(page.getDay());
                    dataCache.updatePercentsWhenSwiping();

                }
                lastPos = position;
                Log.d("sss", "page selected "+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (idleLast == lastPos) return;
                    for (int i = 0; i < getFragmentManager().getFragments().size(); i++) {
                        if (getFragmentManager().getFragments().get(i) != null &&
                                getFragmentManager().getFragments().get(i).getClass().getName().equals(MainPageFragment.class.getName()) &&
                                !getFragmentManager().getFragments().get(i).getTag().equals("android:switcher:"+R.id.lvpMain+":"+lastPos)) {
                            ((MainPageFragment) getFragmentManager().getFragments().get(i)).hideClouds();
                        }
                    }
                    final MainPageFragment page = (MainPageFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.lvpMain+":"+lastPos);
                    if (page != null)
                        page.startAnimation();
                    idleLast = lastPos;
                }
            }
        });
    }
    class LVPAdapter extends FragmentPagerAdapter {
        public LVPAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Calendar end = Calendar.getInstance();
            end.add(Calendar.DAY_OF_MONTH, position - 5000);
            Fragment fragment = new MainPageFragment(getContext(), end);
            nextPage = (MainPageFragment) fragment;
            return fragment;
        }
        @Override
        public int getCount() {
            return 10000;
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
    public ViewPager getLvpMain() {
        return lvpMain;
    }
}
