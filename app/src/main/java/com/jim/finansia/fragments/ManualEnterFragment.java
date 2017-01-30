package com.jim.finansia.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.utils.cache.DataCache;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class ManualEnterFragment extends Fragment {
    private ViewPager lvpMain;
    private int lastPos = 5000, idleLast = 5000;
    private Boolean direction = null;
    @Inject DataCache dataCache;
    @Inject FinansiaFirebaseAnalytics analytics;
    @Inject PAFragmentManager paFragmentManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.manual_enter_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        lvpMain = (ViewPager) rootView.findViewById(R.id.lvpMain);
        initialize();
        String fragmentName = getClass().getName();
        analytics.sendText("User entered: " + fragmentName);
        return rootView;
    }

    public void initialize() {
        FragmentStatePagerAdapter adapter = new LVPAdapter(getFragmentManager());
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
                MainPageFragment page = null;
                List<Fragment> fragments = paFragmentManager.getFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()) &&
                            ((MainPageFragment)fragment).getPosition() == position) {
                        page = (MainPageFragment)fragment;
                    }
                }
                MainPageFragment prevFragment = null;
                if (lastPos>position && !direction) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()) &&
                                ((MainPageFragment)fragment).getPosition() == position + 1) {
                            prevFragment = (MainPageFragment)fragment;
                        }
                    }
                    if (prevFragment != null && prevFragment.getDay().compareTo(page.getDay()) <= 0) {
                        Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, -1);
                        page.setDay(day);
                    }
                }
                if (lastPos<position && direction) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()) &&
                                ((MainPageFragment)fragment).getPosition() == position - 1) {
                            prevFragment = (MainPageFragment)fragment;
                        }
                    }
                    if (prevFragment.getDay().compareTo(page.getDay()) >= 0) {
                        final Calendar day = (Calendar) prevFragment.getDay().clone();
                        day.add(Calendar.DAY_OF_MONTH, 1);
                        page.setDay(day);
                    }
                }
                if (page != null) {
                    page.swipingUpdate();
                    dataCache.setEndDate(page.getDay());
                    dataCache.updatePercentsWhenSwiping();
                    paFragmentManager.updateVoiceRecognizePage(page.getDay());
                }
                lastPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (idleLast == lastPos) return;
                    for (int i = 0; i < getFragmentManager().getFragments().size(); i++) {
                        if (getFragmentManager().getFragments().get(i) != null &&
                                getFragmentManager().getFragments().get(i).getClass().getName().equals(MainPageFragment.class.getName())) {
                            ((MainPageFragment) getFragmentManager().getFragments().get(i)).hideClouds();
                        }
                    }
                    MainPageFragment page = null;
                    List<Fragment> fragments = paFragmentManager.getFragmentManager().getFragments();
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment.getClass().getName().equals(MainPageFragment.class.getName()) &&
                                ((MainPageFragment)fragment).getPosition() == lastPos) {
                            page = (MainPageFragment)fragment;
                        }
                    }
                    if (page != null)
                        page.startAnimation();
                    idleLast = lastPos;
                }
            }
        });
    }

    class LVPAdapter extends FragmentStatePagerAdapter {
        private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        public LVPAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Calendar end = Calendar.getInstance();
            end.add(Calendar.DAY_OF_MONTH, position - 5000);
            Bundle bundle = new Bundle();
            bundle.putString(MainFragment.DATE, format.format(end.getTime()));;
            bundle.putInt(MainFragment.POSITION, position);
            Fragment fragment = new MainPageFragment();
            fragment.setArguments(bundle);
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
    public void setCurrentPage(int page) {
        if (lvpMain != null)
            lvpMain.setCurrentItem(page, false);
    }
}
