package com.jim.finansia.managers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;
import com.jim.finansia.SettingsActivity;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.ChangeColorOfStyleFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.ReportFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.syncbase.SignInGoogleMoneyHold;
import com.jim.finansia.syncbase.SyncBase;
import com.jim.finansia.utils.CircleImageView;
import com.jim.finansia.utils.GetterAttributColors;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.navdrawer.LeftMenuItem;
import com.jim.finansia.utils.navdrawer.LeftSideDrawer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class DrawerInitializer {
    private PocketAccounter pocketAccounter;
    private LeftSideDrawer drawer;
    private RecyclerView rvLeftMenu;
    private PAFragmentManager fragmentManager;
    TextView userName, userEmail;
    com.jim.finansia.utils.CircleImageView userAvatar;
    SharedPreferences spref;
    public  SyncBase mySync;
    boolean downloadnycCanRest = true;
    Uri imageUri;
    ImageView fabIconFrame;
    LeftMenuAdapter adapter;
    public static final int key_for_restat = 10101;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pocket-accounter.appspot.com");
    DownloadImageTask imagetask;
    public static final String KEY_INIT_POS = "keyinit";
    public static SignInGoogleMoneyHold reg;
    public DrawerInitializer(PocketAccounter pocketAccounter, PAFragmentManager fragmentManager) {
        this.pocketAccounter = pocketAccounter;
        this.fragmentManager = fragmentManager;
        Log.d("sasasas", "DrawerInitializer: ");
        drawer = new LeftSideDrawer(pocketAccounter);
        drawer.setLeftBehindContentView(R.layout.activity_behind_left_simple);
        rvLeftMenu = (RecyclerView) pocketAccounter.findViewById(R.id.rvLeftMenu);
        rvLeftMenu.setLayoutManager(new LinearLayoutManager(pocketAccounter));
        rvLeftMenu.setHasFixedSize(true);
        fillNavigationDrawer();
    }

    public LeftSideDrawer getDrawer() {
        return drawer;
    }
    private void fillNavigationDrawer() {
        String[] drawerMenus = pocketAccounter.getResources().getStringArray(R.array.drawer_menus);
        String[] drawerMenuIcons = pocketAccounter.getResources().getStringArray(R.array.drawer_menu_icons);

        spref = pocketAccounter.getSharedPreferences("infoFirst", pocketAccounter.MODE_PRIVATE);
        mySync = new SyncBase(storageRef, pocketAccounter, PocketAccounterGeneral.CURRENT_DB_NAME);

        userName = (TextView) pocketAccounter.findViewById(R.id.tvToolbarName);
        userEmail = (TextView) pocketAccounter.findViewById(R.id.tvGoogleMail);
        userAvatar = (CircleImageView) pocketAccounter.findViewById(R.id.userphoto);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
            try {
                if (user.getPhotoUrl() != null) {
                    imagetask = new DownloadImageTask(userAvatar);
                    imagetask.execute(user.getPhotoUrl().toString());
                    imageUri = user.getPhotoUrl();
                }
            } catch (Exception o) {

            }
        }
        final ImageView fabIcon = (ImageView) pocketAccounter.findViewById(R.id.btnFirebaseLogin);
//        fabIconFrame = (ImageView) pocketAccounter.findViewById(R.id.iconFrameForAnim);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            fabIcon.setImageResource(R.drawable.savebutdisable);
        } else
            fabIcon.setImageResource(R.drawable.ic_login_symbol);

        reg = new SignInGoogleMoneyHold(pocketAccounter, new SignInGoogleMoneyHold.UpdateSucsess() {
            @Override
            public void updateToSucsess() {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    imagetask = new DownloadImageTask(userAvatar);
                    userName.setText(user.getDisplayName());
                    userEmail.setText(user.getEmail());
                    if (user.getPhotoUrl() != null) {
                        try {
                            imagetask.execute(user.getPhotoUrl().toString());

                        } catch (Exception o) {
                        }
                        imageUri = user.getPhotoUrl();
                    }

                    showProgressDialog(pocketAccounter.getString(R.string.cheking_user));
                    mySync.meta_Message(user.getUid(), new SyncBase.ChangeStateLisMETA() {
                        @Override
                        public void onSuccses(final long inFormat) {
                            hideProgressDialog();
                            Date datee = new Date();
                            fabIcon.setImageResource(R.drawable.savebut);
                            datee.setTime(inFormat);
                            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(pocketAccounter);
                            builder.setMessage(pocketAccounter.getString(R.string.sync_last_data_sign_up) + (new SimpleDateFormat("dd.MM.yyyy kk:mm")).format(datee))
                                    .setPositiveButton(pocketAccounter.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            showProgressDialog(pocketAccounter.getString(R.string.download));
                                            mySync.downloadLast(user.getUid(), new SyncBase.ChangeStateLis() {
                                                @Override
                                                public void onSuccses() {
                                                    pocketAccounter.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            hideProgressDialog();
                                                          fabIcon.setImageResource(R.drawable.savebutdisable);
                                                            if (!drawer.isClosed()) {
                                                                drawer.close();
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailed(String e) {
                                                    hideProgressDialog();
                                                    fabIcon.setImageResource(R.drawable.savebutdisable);
                                                    Toast.makeText(pocketAccounter,R.string.connection_faild,Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }
                                    }).setNegativeButton(pocketAccounter.getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    fabIcon.setImageResource(R.drawable.savebutdisable);
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
                            hideProgressDialog();
                            fabIcon.setImageResource(R.drawable.savebutdisable);
                            Toast.makeText(pocketAccounter,R.string.connection_faild,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void updateToFailed() {
                userName.setText(R.string.try_later);
                userEmail.setText(R.string.err_con);
            }
        });

        fabIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser userim = FirebaseAuth.getInstance().getCurrentUser();
                if (userim != null) {

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(pocketAccounter);
                            builder.setMessage(R.string.sync_message)
                                    .setPositiveButton(R.string.sync_short, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            fabIcon.setImageResource(R.drawable.savebut);
                                            mySync.uploadBASE(userim.getUid(), new SyncBase.ChangeStateLis() {
                                                @Override
                                                public void onSuccses() {
                                                   fabIcon.setImageResource(R.drawable.savebutdisable);
                                                }

                                                @Override
                                                public void onFailed(String e) {
                                                    fabIcon.setImageResource(R.drawable.savebutdisable);
                                                    Toast.makeText(pocketAccounter,R.string.connection_faild,Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    }).setNegativeButton(pocketAccounter.getString(R.string.cancel1), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            builder.create().show();
                        }
                    }, 150);
                } else {
                    drawer.close();
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (spref.getBoolean("FIRSTSYNC", true)) {
                                reg.openDialog();
                            } else
                                reg.regitUser();
                        }
                    }, 150);
                }
            }
        });
        ArrayList<LeftMenuItem> items = new ArrayList<>();
        for (int i = 0; i < drawerMenus.length; i++) {
            int resId = pocketAccounter.getResources().getIdentifier(drawerMenuIcons[i], "drawable", pocketAccounter.getPackageName());
            LeftMenuItem leftMenuItem = new LeftMenuItem(drawerMenus[i], resId);
            items.add(leftMenuItem);
        }
         adapter = new LeftMenuAdapter(items);
        rvLeftMenu.setAdapter(adapter);

    }
    public void onActivResultForDrawerCalls(int requestCode, int resultCode, Intent data){
        if (requestCode == SignInGoogleMoneyHold.RC_SIGN_IN) {
            reg.regitRequstGet(data);
        }
        if (requestCode == key_for_restat && resultCode == RESULT_OK) {
            fragmentManager.displayMainWindow();
            if (!drawer.isClosed()) {
                drawer.close();
            }
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                userAvatar.setImageResource(R.drawable.ic_photo);
                userName.setText(R.string.please_sign);
                userEmail.setText(R.string.and_sync_your_data);
                fabIconFrame.setBackgroundResource(R.drawable.cloud_sign_in);
            }
        }
        if (resultCode != RESULT_OK && requestCode == key_for_restat && resultCode != 1111) {
            fragmentManager.displayMainWindow();
            for (int i = 0; i < fragmentManager.getFragmentManager().getBackStackEntryCount(); i++) {
                fragmentManager.getFragmentManager().popBackStack();
            }
        }
    }

    public static void openGmail(Activity activity, String[] email, String subject, String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        PocketAccounter.openActivity=true;

        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        final PackageManager pm = activity.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        activity.startActivity(emailIntent);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        CircleImageView bmImage;

        public DownloadImageTask(CircleImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            File file = new File(pocketAccounter.getFilesDir(), "userphoto.jpg");
            if (file.exists()) {
                bmImage.setImageURI(Uri.parse(file.getAbsolutePath()));
            }
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;

            for (; true; ) {
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (isCancelled()) break;
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                downloadnycCanRest = false;
                bmImage.setImageBitmap(result);
                File file = new File(pocketAccounter.getFilesDir(), "userphoto.jpg");
                FileOutputStream out = null;

                try {
                    out = new FileOutputStream(file);
                    result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private ProgressDialog mProgressDialog;
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(pocketAccounter);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void onStopSuniy(){
        try {
            if (imagetask != null)
                imagetask.cancel(true);
            if (imagetask != null) {
                imagetask.cancel(true);
                imagetask = null;
            }
        } catch (Exception o) {
            o.printStackTrace();
        }
    }
    public void inits(){
//        oldPosition =  spref.getInt(KEY_OLD_POSITION,0);
        oldPosition = 0;
        adapter.notifyDataSetChanged();
    }
    int oldPosition=0;

    public void setCursorToFragment(int pos){
        oldPosition = pos;
        adapter.notifyDataSetChanged();
    }
    public int getCursorPosition(){
        return oldPosition;
    }
    public class LeftMenuAdapter extends RecyclerView.Adapter<LeftMenuAdapter.ViewHolder>{

        ArrayList<LeftMenuItem> result;

        public LeftMenuAdapter(ArrayList<LeftMenuItem> items) {
            this.result = items;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer, parent, false);
            LeftMenuAdapter.ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            if(position==oldPosition){
                holder.tvTitle.setTextColor(GetterAttributColors.fetchHeadAccedentColor(pocketAccounter));
                holder.ivIcon.setColorFilter(GetterAttributColors.fetchHeadAccedentColor(pocketAccounter));
                holder.llMenuItems.setBackgroundColor(pocketAccounter.getResources().getColor(R.color.credit_white_grey));
                holder.flStroke.setBackgroundColor(GetterAttributColors.fetchHeadAccedentColor(pocketAccounter));
                holder.flStroke.setVisibility(View.VISIBLE);
            }
            else {
                holder.tvTitle.setTextColor(pocketAccounter.getResources().getColor(R.color.menu_item_color));
                holder.ivIcon.setColorFilter(pocketAccounter.getResources().getColor(R.color.menu_item_color));
                holder.llMenuItems.setBackgroundColor(pocketAccounter.getResources().getColor(R.color.white));
                holder.flStroke.setBackgroundColor(pocketAccounter.getResources().getColor(R.color.white));
                holder.flStroke.setVisibility(View.INVISIBLE);

            }
            holder.ivIcon.setImageResource(result.get(position).getIconId());
            holder.tvTitle.setText(result.get(position).getTitleName());
            holder.llMenuItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(oldPosition == position)
                    {
                        drawer.closeLeftSide();
                        return;
                    }

                    if (fragmentManager.getFragmentManager().getBackStackEntryCount() == 0 && position == 0) {
                        if (pocketAccounter.findViewById(R.id.change) != null)
                            pocketAccounter.findViewById(R.id.change).setVisibility(View.VISIBLE);
                    } else {
                        if (pocketAccounter.findViewById(R.id.change) != null)
                            pocketAccounter.findViewById(R.id.change).setVisibility(View.GONE);
                    }

                    oldPosition = position;
                    drawer.closeLeftSide();
                    notifyDataSetChanged();
                    drawer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int count = pocketAccounter.getSupportFragmentManager().getBackStackEntryCount();
                            while (count > 0) {
                                pocketAccounter.getSupportFragmentManager().popBackStack();
                                count--;
                            }
                            switch (position) {
                                case 0:
                                    fragmentManager.displayMainWindow();
                                    break;
                                case 1:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new CurrencyFragment());
                                    break;
                                case 2:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new CategoryFragment());
                                    break;
                                case 3:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new AccountFragment());
                                    break;
                                case 4:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new PurposeFragment());
                                    break;
                                case 5:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new AutoMarketFragment());
                                    break;
                                case 6:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new CreditTabLay());
                                    break;
                                case 7:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new DebtBorrowFragment());
                                    break;
                                case 8:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new SmsParseMainFragment());
                                    break;
                                case 9:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new ReportFragment());
                                    break;
                                case 10:
                                    pocketAccounter.findViewById(R.id.mainWhite).setVisibility(View.VISIBLE);
                                    fragmentManager.displayFragment(new ChangeColorOfStyleFragment());
                                    break;
                                case 11:
                                    Intent zssettings = new Intent(pocketAccounter, SettingsActivity.class);
                                    PocketAccounter.openActivity=true;
                                    for (int i = 0; i < fragmentManager.getFragmentManager().getBackStackEntryCount(); i++) {
                                        fragmentManager.getFragmentManager().popBackStack();
                                    }
                                    pocketAccounter.startActivityForResult(zssettings, key_for_restat);
                                    break;
                                case 12:
                                    pocketAccounter.findViewById(R.id.change).setVisibility(View.VISIBLE);
                                    Intent rate_app_web = new Intent(Intent.ACTION_VIEW);
                                    PocketAccounter.openActivity=true;
                                    rate_app_web.setData(Uri.parse(pocketAccounter.getResources().getString(R.string.rate_app_web)));
                                    pocketAccounter.startActivity(rate_app_web);
                                    break;
                                case 13:
                                    pocketAccounter.findViewById(R.id.change).setVisibility(View.VISIBLE);
                                    Intent Email = new Intent(Intent.ACTION_SEND);
                                    PocketAccounter.openActivity=true;
                                    Email.setType("text/email");
                                    Email.putExtra(Intent.EXTRA_SUBJECT, pocketAccounter.getString(R.string.share_app));
                                    Email.putExtra(Intent.EXTRA_TEXT, pocketAccounter.getString(R.string.share_app_text));
                                    pocketAccounter.startActivity(Intent.createChooser(Email, pocketAccounter.getString(R.string.share_app)));
                                    break;
                                case 14:
                                    pocketAccounter.findViewById(R.id.change).setVisibility(View.VISIBLE);
                                    openGmail(pocketAccounter, new String[]{pocketAccounter.getString(R.string.to_email)},
                                            pocketAccounter.getString(R.string.feedback_subject),
                                            pocketAccounter.getString(R.string.feedback_content));
                                    break;
                            }
                            if(oldPosition>=11){
                                oldPosition = 0;
                                notifyDataSetChanged();
                            }
                        }
                    }, 170);
                }
            });

        }

        @Override
        public int getItemCount() {
            return result.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvTitle;
            LinearLayout llMenuItems;
            FrameLayout flStroke;
            public ViewHolder(View view) {
                super(view);
                ivIcon = (ImageView) view.findViewById(R.id.ivTitleIcon);
                tvTitle = (TextView) view.findViewById(R.id.tvTitleName);
                llMenuItems = (LinearLayout) view.findViewById(R.id.llMenuItems);
                flStroke = (FrameLayout) view.findViewById(R.id.flStroke);
            }


        }


    }


}
