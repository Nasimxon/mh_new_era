package com.jim.finansia.modulesandcomponents.modules;



import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.managers.DrawerInitializer;
import com.jim.finansia.managers.FinansiaFirebaseAnalytics;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.SettingsManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.DatePicker;
import com.jim.finansia.utils.FilterDialog;
import com.jim.finansia.utils.IconChooseDialog;
import com.jim.finansia.utils.OperationsListDialog;
import com.jim.finansia.utils.SubCatAddEditDialog;
import com.jim.finansia.utils.TransferDialog;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.billing.PurchaseImplementation;

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
    private PurchaseImplementation purchaseImplementation;
    private FinansiaFirebaseAnalytics finansiaFiregbaseAnalytics;
    public PocketAccounterActivityModule(PocketAccounter pocketAccounter, Toolbar toolbar) {
        this.pocketAccounter = pocketAccounter;
        this.toolbar = toolbar;
        toolbarManager = new ToolbarManager(pocketAccounter, toolbar);
        paFragmentManager = new PAFragmentManager(pocketAccounter);
        drawerInitializer = new DrawerInitializer(this.pocketAccounter, paFragmentManager);
        logicManager = new LogicManager(pocketAccounter);
        finansiaFiregbaseAnalytics = new FinansiaFirebaseAnalytics(pocketAccounter);
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

    @Provides
    public PurchaseImplementation getPurchaseImplementation() {
        if (purchaseImplementation == null)
            purchaseImplementation = new PurchaseImplementation(pocketAccounter, paFragmentManager);
        return purchaseImplementation;

    }
    @Provides
    public FinansiaFirebaseAnalytics getFinansiaFiregbaseAnalytics() {
        if (finansiaFiregbaseAnalytics == null)
            finansiaFiregbaseAnalytics = new FinansiaFirebaseAnalytics(pocketAccounter);
        return finansiaFiregbaseAnalytics;

    }
}
