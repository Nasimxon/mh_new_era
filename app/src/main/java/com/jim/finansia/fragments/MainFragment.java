package com.jim.finansia.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.helper.MyVerticalViewPager;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import javax.inject.Inject;



public class MainFragment extends Fragment {
    public static final String DATE = "date";
    public static final String POSITION = "position";
    private VerticalViewPagerAdapter adapter;
    private MyVerticalViewPager vpVertical;
    @Inject DataCache dataCache;
    @Inject PAFragmentManager paFragmentManager;
    @Inject SharedPreferences preferences;
    @Inject FinansiaFirebaseAnalytics analytics;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        Log.d("sss", "onCreateView:");
        vpVertical = (MyVerticalViewPager) rootView.findViewById(R.id.vpVertical);
        adapter = new VerticalViewPagerAdapter(paFragmentManager.getFragmentManager());
        vpVertical.setOnTouchListener(null);
        vpVertical.setAdapter(adapter);
        int mainSelectedPage = preferences.getInt(PocketAccounterGeneral.VERTICAL_SELECTED_PAGE, 1);
        vpVertical.setCurrentItem(mainSelectedPage, false);
        vpVertical.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                preferences
                        .edit()
                        .putInt(PocketAccounterGeneral.VERTICAL_SELECTED_PAGE, position)
                        .commit();
                if (position == 0) {
                    analytics.sendText("User enters to voice recognition fragment");
                    ((PocketAccounter) getContext()).setToToolbarVoiceMode();
                }
                else {
                    analytics.sendText("User enters to manual input mode fragment");
                    ((PocketAccounter) getContext()).setToToolbarManualEnterMode();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return rootView;
    }

    class VerticalViewPagerAdapter extends FragmentStatePagerAdapter{
        public VerticalViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0){
                VoiceRecognizerFragment voiceRecognizerFragment = new VoiceRecognizerFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(VoiceRecognizerFragment.DATA_CACHE, dataCache.getEndDate().getTimeInMillis());
                voiceRecognizerFragment.setArguments(bundle);
                fragment = voiceRecognizerFragment;

            }
            else {
                fragment = new ManualEnterFragment();
            }
            return fragment;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }

}