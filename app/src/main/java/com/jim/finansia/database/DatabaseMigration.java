package com.jim.finansia.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jim.finansia.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.database.Database;

/**
 * Created by DEV on 27.08.2016.
 */

public class DatabaseMigration extends DaoMaster.DevOpenHelper{
    private SharedPreferences preferences;
    public DatabaseMigration(Context context, String name) {
        super(context, name);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        Log.d("sss", "onCreating db");
        preferences.edit().putBoolean(PocketAccounterGeneral.DB_ONCREATE_ENTER, true).commit();
        Log.d("sss", preferences.getBoolean(PocketAccounterGeneral.DB_ONCREATE_ENTER, false) + "after");
    }

}
