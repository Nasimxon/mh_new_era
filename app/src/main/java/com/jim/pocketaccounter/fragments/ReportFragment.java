package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.jim.pocketaccounter.RollBehindView;
import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("ValidFragment")
public class ReportFragment extends Fragment {
    @Inject ReportManager reportManager;
    @Inject DataCache dataCache;
    @Inject CommonOperations commonOperations;
    @Inject ToolbarManager toolbarManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat simpleDateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject @Named(value = "begin") Calendar begin;
    @Inject @Named(value = "end") Calendar end;
    @Inject SharedPreferences preferences;
    RollBehindView rbView;
    Bitmap bitmap;
    ScrollView svScroll;
    float thirty = 0.0f;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        thirty = getResources().getDimension(R.dimen.thirtyfive_dp);
        rbView = (RollBehindView) rootView.findViewById(R.id.rbView);
        svScroll = (ScrollView) rootView.findViewById(R.id.svScroll);
        final FrameLayout root = (FrameLayout) rootView.findViewById(R.id.rtView);
        root.post(new Runnable() {
            @Override
            public void run() {
                root.setDrawingCacheEnabled(true);
                root.buildDrawingCache();
                bitmap = root.getDrawingCache();
                Bitmap temp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), (int) thirty);
                rbView.setBitmap(temp);
            }
        });
        svScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = svScroll.getScrollY(); // For ScrollView
                int scrollX = svScroll.getScrollX(); // For HorizontalScrollView
                if (scrollY < 0) return;
                if (bitmap != null && !bitmap.isRecycled()) {
                    Log.d("sss", "y: " + scrollY + " bitmap: " + bitmap.getHeight());
                    Bitmap temp = Bitmap.createBitmap(bitmap, 0, scrollY, bitmap.getWidth(), (int) thirty);
                    rbView.setBitmap(temp);
                } else {
                    root.setDrawingCacheEnabled(true);
                    root.buildDrawingCache();
                    bitmap = root.getDrawingCache();
                    Log.d("sss", "y: " + scrollY + " bitmap: " + bitmap.getHeight());

                    Bitmap temp = Bitmap.createBitmap(bitmap, 0, scrollY, bitmap.getWidth(), (int) thirty);
                    rbView.setBitmap(temp);
                }

                // DO SOMETHING WITH THE SCROLL COORDINATES
            }
        });

        return rootView;
    }
}
