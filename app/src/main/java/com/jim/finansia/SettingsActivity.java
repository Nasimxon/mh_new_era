package com.jim.finansia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.DrawerInitializer;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterApplicationModule;
import com.jim.finansia.syncbase.SyncBase;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.widget.WidgetKeys;
import com.jim.finansia.widget.WidgetProvider;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static android.graphics.Color.RED;


/**
 * Created by ismoi on 6/18/2016.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final int PERMISSION_READ_STORAGE = 0;
    @Inject DaoSession daoSession;
    @Inject SharedPreferences sharedPreferences;
    @Inject PocketAccounterApplicationModule pocketAccounterApplicationModule;
    @Inject DataCache dataCache;
    @Inject ReportManager reportManager;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pocket-accounter.appspot.com");

    public SyncBase mySync;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((PocketAccounterApplication) this.getApplicationContext()).component().inject(this);
        setTheme(R.style.BlueTheme);
        mySync = new SyncBase(storageRef, this, PocketAccounterGeneral.CURRENT_DB_NAME);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);
        ListPreference language = (ListPreference) findPreference("language");
        if (language.getValue().matches(getResources().getString(R.string.language_default))) {
            language.setValue(Locale.getDefault().getLanguage());
        }
        language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent intent = new Intent(SettingsActivity.this, PocketAccounter.class);
                startActivity(intent);
                setResult(1111);
                SettingsActivity.this.finish();
                return true;
            }
        });
        updatePrefs("language");
        Preference save = (Preference) findPreference("save");
        save.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int permission = ContextCompat.checkSelfPermission(SettingsActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((SettingsActivity.this),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                .setTitle("Permission required");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(SettingsActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_READ_STORAGE);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                } else {
                    File direct = new File(Environment.getExternalStorageDirectory() + "/Finansia");
                    if(!direct.exists())
                    {
                        if(direct.mkdir())
                        {
                            exportDB();
                        }
                    } else {
                        exportDB();
                    }
                }
                return true;
            }
        });
        Preference load = (Preference) findPreference("load");
        load.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                int permission = ContextCompat.checkSelfPermission(SettingsActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((SettingsActivity.this),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                                .setTitle("Permission required");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(SettingsActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_READ_STORAGE);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READ_STORAGE);
                    }
                } else {
                    importDB();
                }
                return false;
            }
        });
        Preference googleBackup = (Preference) findPreference("backup");
        FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
        if(auth==null){googleBackup.setEnabled(false); }
        else
        googleBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                lastUpload();
                return false;
            }
        });
        Preference googleLogout = (Preference) findPreference("logout");
        if(auth==null){googleLogout.setEnabled(false);  }
        else
        googleLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage(R.string.are_you_sure_for_log_out)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DrawerInitializer.reg.revokeAccess();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }) .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return false;
            }
        });

        ((CheckBoxPreference) findPreference("securewidget")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sPref;
                        sPref = getSharedPreferences("infoFirst", MODE_PRIVATE);
                        int WidgetID = sPref.getInt(WidgetKeys.SPREF_WIDGET_ID, -1);
                        if (WidgetID >= 0) {
                            if (AppWidgetManager.INVALID_APPWIDGET_ID != WidgetID)
                                WidgetProvider.updateWidget(SettingsActivity.this, AppWidgetManager.getInstance(SettingsActivity.this),
                                        WidgetID);
                        }
                    }
                })).start();

                return true;
            }
        });
        SharedPreferences sPref;
        sPref = getSharedPreferences("infoFirst", MODE_PRIVATE);
        int WidgetID = sPref.getInt(WidgetKeys.SPREF_WIDGET_ID, -1);
        if (WidgetID >= 0) {
            if (AppWidgetManager.INVALID_APPWIDGET_ID != WidgetID)
                WidgetProvider.updateWidget(SettingsActivity.this, AppWidgetManager.getInstance(SettingsActivity.this),
                        WidgetID);
        }

        Preference sbrosdannix = (Preference) findPreference("sbros");
        sbrosdannix.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage(R.string.default_settings)
                        .setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }) .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DrawerInitializer.reg.revokeAccess();
                        String DB_PATH;
                        for(AbstractDao abstractDao : daoSession.getAllDaos()) {
                            abstractDao.deleteAll();
                            abstractDao.detachAll();
                        }
                        CommonOperations.createDefaultDatas(sharedPreferences,SettingsActivity.this,daoSession);
                        dataCache.clearAllCaches();
                        reportManager.clearCache();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                        prefs.edit().clear().apply();
                        sharedPreferences.edit().clear().apply();
                        getSharedPreferences("infoFirst", MODE_PRIVATE).edit().clear().apply();
                        setResult(1111);
                        finish();
                    }
                });
                builder.create().show();

                return false;
            }
        });

        Preference mainWindow=findPreference("mainwind");
        mainWindow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Dialog dialog = new Dialog(SettingsActivity.this);
                final View dialogView = getLayoutInflater().inflate(R.layout.main_window_pages_set, null);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogView);

                final EditText topWindow=(EditText)dialogView.findViewById(R.id.firstPassword);
                final EditText bottomWindow=(EditText)dialogView.findViewById(R.id.secondPassword);
                final TextView tvTop=(TextView)dialogView.findViewById(R.id.passwordTextShould);
                final TextView tvBottom=(TextView)dialogView.findViewById(R.id.passwordRepiat);
                final TextView Titlee=(TextView)dialogView.findViewById(R.id.idtitle);
                topWindow.setText(""+Integer.toString(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getInt("key_for_window_top",4)));
                bottomWindow.setText(""+Integer.toString(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getInt("key_for_window_bottom",4)));
                dialogView.findViewById(R.id.okbuttt).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isMojno=true;
                        if(Integer.parseInt(topWindow.getText().toString()) < 1 ||
                                Integer.parseInt(topWindow.getText().toString()) > 10){
                            tvTop.setText(getString(R.string.limit_page));
                            tvTop.setTextColor(Color.RED);
                            isMojno=false;
                        }
                        else {
                            tvTop.setText(R.string.in_window_expense_top_window);
                            tvTop.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));
                        }
                        if(Integer.parseInt(bottomWindow.getText().toString())<1 ||
                                Integer.parseInt(bottomWindow.getText().toString())>10){
                            tvBottom.setText(getString(R.string.limit_page));
                            tvBottom.setTextColor(Color.RED);
                            isMojno=false;
                        }
                        else {
                            tvBottom.setText(R.string.in_windows_incomes_bottom_window);
                            tvBottom.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));
                        }
                        if(isMojno){
                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putInt("key_for_window_top",Integer.parseInt(topWindow.getText().toString())).apply();
                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putInt("key_for_window_bottom",Integer.parseInt(bottomWindow.getText().toString())).apply();
                            if (PreferenceManager
                                    .getDefaultSharedPreferences(SettingsActivity.this)
                                    .getInt("income_current_page", 0) >= Integer.parseInt(bottomWindow.getText().toString())) {
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                                        .edit()
                                        .putInt("income_current_page", 0).apply();
                            }
                            if (PreferenceManager
                                    .getDefaultSharedPreferences(SettingsActivity.this)
                                    .getInt("expense_current_page", 0) >= Integer.parseInt(topWindow.getText().toString())) {
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                                        .edit()
                                        .putInt("expense_current_page", 0).apply();
                            }
                            dialog.dismiss();
                        }
                    }
                });
                DisplayMetrics dm = getResources().getDisplayMetrics();
                int width = dm.widthPixels;
                dialog.getWindow().setLayout(7*width/8, SlidingPaneLayout.LayoutParams.WRAP_CONTENT);
                dialog.show();
                return false;
            }
        });

        CheckBoxPreference checkkSecure=(CheckBoxPreference)findPreference("secure");
        checkkSecure.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((Boolean)newValue&&PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getBoolean("firstclick",true)) {
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    final View dialogView = getLayoutInflater().inflate(R.layout.password_layout_create, null);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(dialogView);

                    final EditText myPassword1=(EditText)dialogView.findViewById(R.id.firstPassword);
                    final EditText myPassword2=(EditText)dialogView.findViewById(R.id.secondPassword);
                    final TextView myFourNumbers=(TextView)dialogView.findViewById(R.id.passwordTextShould);
                    final TextView myRepiatPassword=(TextView)dialogView.findViewById(R.id.passwordRepiat);
                    final TextView Titlee=(TextView)dialogView.findViewById(R.id.idtitle);
                    dialogView.findViewById(R.id.okbuttt).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myRepiatPassword.setText(getString(R.string.repiat_yout_password));
                            myRepiatPassword.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));

                            if(myPassword1.getText().toString().length()!=4){
                                myPassword1.setText("");
                                myFourNumbers.setTextColor(RED);
                                return;
                            }
                            else
                                myFourNumbers.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));

                            if (myPassword2.getText().toString().length()!=4){
                                myPassword2.setText("");
                                myRepiatPassword.setTextColor(RED);
                                return;

                            }
                            else
                                myRepiatPassword.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));

                            if (myPassword1.getText().toString().matches(myPassword2.getText().toString())){

                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putBoolean("firstclick",false  ).apply();
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putString("password",myPassword2.getText().toString()  ).apply();

                                ((CheckBoxPreference) findPreference("secure")).setChecked(true);
                                dialog.dismiss();
                            }
                            else  {
                                myPassword2.setText("");
                                myRepiatPassword.setText(R.string.please_repait_correct);
                                myRepiatPassword.setTextColor(RED);
                                return;

                            }
                        }
                    });
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    int width = dm.widthPixels;
                    dialog.getWindow().setLayout(7*width/8, SlidingPaneLayout.LayoutParams.WRAP_CONTENT);
                    dialog.show();

                return false;
                }
                else if(!(Boolean)newValue)
                {
                    Log.d("keeee", "onPreferenceChange: fasleee");
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    final View dialogView = getLayoutInflater().inflate(R.layout.password_layout_turn_off, null);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(dialogView);
                     final EditText myPassword1=(EditText)dialogView.findViewById(R.id.confirmpasword);
                    final  TextView myFourNumbers=(TextView) dialogView.findViewById(R.id.passwordTextShouldRepiat);
                    final Button okBut=(Button) dialogView.findViewById(R.id.okbuttt);
                    okBut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(myPassword1.getText().toString().length()!=4){
                                myPassword1.setText("");
                                myFourNumbers.setText(R.string.was_four_numbers);
                                myFourNumbers.setTextColor(RED);
                                return;
                            }
                            else
                                myFourNumbers.setTextColor(ContextCompat.getColor(SettingsActivity.this,R.color.black_for_secondary_text));

                            if (myPassword1.getText().toString().matches(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("password",""))){
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putBoolean("secure",false  ).apply();
                                ((CheckBoxPreference) findPreference("secure")).setChecked(false);
                                dialog.dismiss();
                            }
                            else  {
                                myPassword1.setText("");
                                myFourNumbers.setText(R.string.try_one_more);
                                myFourNumbers.setTextColor(RED);
                                return;

                            }
                        }
                    });
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    int width = dm.widthPixels;
                    dialog.getWindow().setLayout(7*width/8, SlidingPaneLayout.LayoutParams.WRAP_CONTENT);
                    dialog.show();


                    return false;
                }
                else
                return true;
            }
        });







    }

    private void lastUpload(){

        final FirebaseUser userik= FirebaseAuth.getInstance().getCurrentUser();

        if(userik!=null){
            if(!SyncBase.isNetworkAvailable(this)){
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage(R.string.connection_faild)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                               dialog.dismiss();
                                return;
                            }
                        });
                builder.create().show();
                return;
            }
            showProgressDialog(getString(R.string.cheking_user));
            mySync.meta_Message(userik.getUid(), new SyncBase.ChangeStateLisMETA() {
                @Override
                public void onSuccses(final long inFormat) {
                    hideProgressDialog();
                    Date datee=new Date();
                    datee.setTime(inFormat);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage(getString(R.string.sync_want_from_data) + (new SimpleDateFormat("dd.MM.yyyy kk:mm")).format(datee))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                       showProgressDialog(getString(R.string.download));
                                    mySync.downloadLast(userik.getUid(), new SyncBase.ChangeStateLis() {
                                        @Override
                                        public void onSuccses() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pocketAccounterApplicationModule.updateDaoSession();
                                                    hideProgressDialog();
                                                    setResult(RESULT_OK);
                                                    finish();
                                                }
                                            });
                                        }
                                        @Override
                                        public void onFailed(String e) {
                                            hideProgressDialog();
                                            Toast.makeText(SettingsActivity.this, R.string.toast_error_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }) .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            hideProgressDialog();
                            dialog.cancel();
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            hideProgressDialog();
                        }
                    });
                    builder.create().show();
                }
                @Override
                public void onFailed(Exception e) {
                }
            });
        }


        //google backup
    }

    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data  = Environment.getDataDirectory();


            if (sd.canWrite()) {
                String  currentDBPath;
                currentDBPath = "//data//" + getPackageName().toString()
                        + "//databases//" + PocketAccounterGeneral.CURRENT_DB_NAME;
                String backupDBPath  = "/Finansia/" + PocketAccounterGeneral.OLD_DB_NAME;
                File   currentDB= new File(data, currentDBPath);
                File  backupDB = new File(sd, backupDBPath);

                if(!backupDB.exists()){
                    backupDBPath  = "/Finansia/" + PocketAccounterGeneral.CURRENT_DB_NAME;
                    backupDB = new File(sd,backupDBPath);
                    if(!backupDB.exists())
                        return;
                }

                final SQLiteDatabase current = SQLiteDatabase.openDatabase(currentDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                SQLiteDatabase received = SQLiteDatabase.openDatabase(backupDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                if (current.getVersion() > received.getVersion()) {
                    DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, PocketAccounterGeneral.CURRENT_DB_NAME, null);
                    Database sqLiteDatabase= helper.getWritableDb();
                    DaoMaster daoMaster= new DaoMaster(sqLiteDatabase);
                    daoSession= daoMaster.newSession();
                    daoMaster.dropAllTables(sqLiteDatabase, true);
                    daoMaster.createAllTables(sqLiteDatabase, true);
                    CommonOperations.migrateDatabase(this,backupDB.getAbsolutePath(), daoSession, sharedPreferences);
                    for (AbstractDao abstractDao : daoSession.getAllDaos())
                        abstractDao.detachAll();

                }
                else {
                    File currentDB1 = new File(backupDB.getAbsolutePath());
                    File backupDB1 = new File(currentDB.getAbsolutePath());
                    FileChannel src = null, dst = null;
                    try {
                        src = new FileInputStream(currentDB1).getChannel();
                        dst = new FileOutputStream(backupDB1).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                dataCache.getBoardBitmapsCache().evictAll();
                dataCache.updateAllPercents();
                dataCache.clearAllCaches();
                pocketAccounterApplicationModule.updateDaoSession();
                Toast.makeText(this,R.string.sync_suc,Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + getPackageName().toString()+ "//databases//" + PocketAccounterGeneral.CURRENT_DB_NAME;
                String backupDBPath  = "/Finansia/"+PocketAccounterGeneral.CURRENT_DB_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
    }

    public boolean mListStyled;



    @Override
    public void onResume() {
        super.onResume();
        if (!mListStyled) {
            View rootView = findViewById(android.R.id.content).getRootView();
            if (rootView != null) {
                ListView list = (ListView) rootView.findViewById(android.R.id.list);
                list.setPadding(0, 0, 0, 0);
                list.setDivider(null);
                //any other styling call
                mListStyled = true;
            }
        }
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PocketAccounter.openActivity=false;
    }

    private void updatePrefs(String key) {

        if (key.matches("language")) {
            ListPreference preference = (ListPreference) findPreference("language");
            CharSequence entry = ((ListPreference) preference).getEntry();
            preference.setTitle(entry);
        }
        if (key.matches("planningNotif")) {
            final ListPreference planningNotif = (ListPreference) findPreference("planningNotif");
            planningNotif.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    return false;
                }
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        updatePrefs(key);
        if (key.matches("language")) {
            ListPreference preference = (ListPreference) findPreference("language");
            CharSequence entry = ((ListPreference) preference).getEntry();
            preference.setTitle(entry);
            setLocale((String) entry);
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        setResult(RESULT_OK);
        finish();
    }
    private ProgressDialog mProgressDialog;


    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.download));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
