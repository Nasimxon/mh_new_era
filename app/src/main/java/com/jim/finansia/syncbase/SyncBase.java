package com.jim.finansia.syncbase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.SettingsActivity;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterApplicationModule;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * Created by DEV on 10.06.2016.
 */

public class SyncBase {
    private static String DB_PATH;
    private static String DB_NAME;
    private static String PATH_FOR_INPUT;
    private static String META_KEY="CreatAT";
    StorageReference refStorage;
    Context context;
    private String DATABASE_FIREBASE = "FinansiaBase";
    @Inject
    DaoSession daoSession;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    PocketAccounterApplicationModule pocketAccounterApplicationModule;
    @Inject
    ReportManager reportManager;
    @Inject
    DataCache dataCache;
    void SyncBase(){

    }
    public SyncBase(StorageReference refStorage, Context context,String databsename) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        this.refStorage = refStorage;
        this.context = context;
        String packageName = context.getPackageName();
        DB_PATH = String.format("//data//data//%s//databases//", packageName);
        DB_NAME=databsename;
        PATH_FOR_INPUT=DB_PATH+DB_NAME;
        }
    public void uploadBASE(String auth_uid, final ChangeStateLis even){
        if(!isNetworkAvailable()){
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setMessage(R.string.connection_faild)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
            even.onFailed("NotNetworkAvailable");
            return;
        }
        try {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("sqlite/db")
                    .setCustomMetadata(META_KEY, Long.toString(System.currentTimeMillis()))
//                    .setCustomMetadata(FINANSIA_META_KEY,FINANSIA_META_KEY_VALUE)
                    .build();
            InputStream stream = new FileInputStream(new File(PATH_FOR_INPUT));
            refStorage.child(auth_uid + "/" + DATABASE_FIREBASE).putStream(stream, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    even.onSuccses();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    even.onFailed(e.getMessage());

                }
            });
        }
        catch (Exception o){
            even.onFailed(o.getMessage());
        }



    }
   public boolean downloadLast(final String auth_uid, final ChangeStateLis even){

       final ProgressDialog A1=new ProgressDialog(context);
       A1.setMessage(context.getString(R.string.please_wait));
       A1.show();

       try {
           final File file = new File(PATH_FOR_INPUT);
           final File fileDirectory = new File(context.getFilesDir(),DB_NAME) ;

           refStorage.child(auth_uid+"/"+DATABASE_FIREBASE).getFile(fileDirectory).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {



                       File currentDB = new File(fileDirectory.getAbsolutePath());
                       File backupDB = new File(file.getAbsolutePath());
                       FileChannel src = null, dst = null;
                       try {
                           src = new FileInputStream(currentDB).getChannel();
                           dst = new FileOutputStream(backupDB).getChannel();
                           dst.transferFrom(src, 0, src.size());
                           src.close();
                           dst.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }

                   if (!context.getClass().getName().equals(SettingsActivity.class.getName())) {
                       even.onSuccses();

                   } else {
                       ((SettingsActivity) context).setResult(RESULT_OK);
                       ((SettingsActivity) context).finish();
                   }
                   A1.dismiss();
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {

               }
           });

       } catch (Exception e) {
           even.onFailed(e.getMessage());
           e.printStackTrace();
           A1.dismiss();
       }
       return false;
   }
    public void meta_Message(String auth_uid, final ChangeStateLisMETA even){
         refStorage.child(auth_uid+"/"+DATABASE_FIREBASE).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
             @Override
             public void onSuccess(StorageMetadata storageMetadata) {
                 even.onSuccses(Long.parseLong(storageMetadata.getCustomMetadata(META_KEY)));
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 even.onFailed(e);
             }
         });
    }

    public interface ChangeStateLis {
        void onSuccses();
        void onFailed(String e);


    }
    public interface ChangeStateLisMETA {
        void onSuccses(long inFormat);
        void onFailed(Exception e);


    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
