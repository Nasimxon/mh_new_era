package com.jim.pocketaccounter.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.DrawerInitializer;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ReportManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.cache.DataCache;

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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
    }
}
