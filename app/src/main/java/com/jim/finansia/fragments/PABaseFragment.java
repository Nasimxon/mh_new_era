package com.jim.finansia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.DrawerInitializer;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.cache.DataCache;

import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by vosit on 26.10.16.
 */

public abstract class PABaseFragment extends Fragment {
    @Inject DaoSession daoSession;
    @Inject ToolbarManager toolbarManager;
    @Inject LogicManager logicManager;
    @Inject CommonOperations commonOperations;
    @Inject PAFragmentManager paFragmentManager;
    @Inject DrawerInitializer drawerInitializer;
    @Inject DataCache dataCache;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject ReportManager reportManager;
    @Inject SharedPreferences preferences;
    @Inject FinansiaFirebaseAnalytics analytics;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
    }
}
