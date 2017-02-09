package com.jim.finansia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        return super.onCreateView(inflater, container, savedInstanceState);

    }
}
