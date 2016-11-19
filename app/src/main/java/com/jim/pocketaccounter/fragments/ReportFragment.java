package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jim.pocketaccounter.MirrorView;
import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.billing.LockViewButtonClickListener;
import com.jim.pocketaccounter.utils.billing.MainPageLockView;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.record.BalanceStripe;
import com.jim.pocketaccounter.utils.record.BoardView;
import com.jim.pocketaccounter.utils.record.PageChangeListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.jim.pocketaccounter.PocketAccounter.PRESSED;

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
    MirrorView mvReport;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.report_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        final FrameLayout root = (FrameLayout) rootView.findViewById(R.id.rootView);
        root.post(new Runnable() {
            @Override
            public void run() {
                root.setDrawingCacheEnabled(true);
                root.buildDrawingCache();
                Bitmap bitmap = root.getDrawingCache();

            }
        });
        return rootView;
    }
}
