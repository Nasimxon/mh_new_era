package com.jim.pocketaccounter.modulesandcomponents.modules;



import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.database.TemplateVoice;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.DrawerInitializer;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.SettingsManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.CashDialog;
import com.jim.pocketaccounter.utils.DatePicker;
import com.jim.pocketaccounter.utils.FilterDialog;
import com.jim.pocketaccounter.utils.IconChooseDialog;
import com.jim.pocketaccounter.utils.OperationsListDialog;
import com.jim.pocketaccounter.utils.SubCatAddEditDialog;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by DEV on 27.08.2016.
 */
@Module
public class PocketAccounterActivityModule {
    private PAFragmentManager paFragmentManager;
    private PocketAccounter pocketAccounter;
    private ToolbarManager toolbarManager;
    private DrawerInitializer drawerInitializer;
    private Toolbar toolbar;
    private LogicManager logicManager;

    public PocketAccounterActivityModule(PocketAccounter pocketAccounter, Toolbar toolbar) {
        this.pocketAccounter = pocketAccounter;
        paFragmentManager = new PAFragmentManager(pocketAccounter);
        this.toolbar = toolbar;
        toolbarManager = new ToolbarManager(pocketAccounter, toolbar);
        drawerInitializer = new DrawerInitializer(this.pocketAccounter, paFragmentManager);
        logicManager = new LogicManager(pocketAccounter);
        Log.d("sss", toolbar == null ? "null toolbar" : toolbar.getClass().getName());
    }

    @Provides
    public LogicManager getLogicManager() {
        if (logicManager == null)
            logicManager = new LogicManager(pocketAccounter);
        return logicManager;
    }

    @Provides
    public PAFragmentManager getPaFragmentManager() {
        if (paFragmentManager == null)
            paFragmentManager = new PAFragmentManager(pocketAccounter);
        return paFragmentManager;
    }
    @Provides
    public ToolbarManager getToolbarManager() {
        if (toolbarManager == null)
            toolbarManager = new ToolbarManager(pocketAccounter, toolbar);
        return toolbarManager;
    }
    @Provides
    public Context getContext() {
        return pocketAccounter;
    }
    @Provides
    public SettingsManager getSettingsManager() {
        return new SettingsManager(pocketAccounter);
    }

    @Provides
    public DrawerInitializer getDrawerInitializer() {
        if (drawerInitializer == null)
            drawerInitializer = new DrawerInitializer(this.pocketAccounter, paFragmentManager);
        return drawerInitializer;
    }
    @Provides
    public WarningDialog getWarningDialog() {
        return new WarningDialog(pocketAccounter);
    }
    @Provides
    public IconChooseDialog getIconsChooseDialog() {
        return new IconChooseDialog(pocketAccounter);
    }

    @Provides
    public DatePicker getDatePicker() {
        return new DatePicker(pocketAccounter);
    }
    @Provides
    public OperationsListDialog operationsListDialog() {
        return new OperationsListDialog(pocketAccounter);
    }
    @Provides
    public FilterDialog filterDialog() {
        return new FilterDialog(pocketAccounter);
    }
    @Provides
    public SubCatAddEditDialog subCatAddEditDialog() {
        return new SubCatAddEditDialog(pocketAccounter);
    }

    @Provides
    public TransferDialog transferDialog() {
        return new TransferDialog(pocketAccounter);
    }

}
