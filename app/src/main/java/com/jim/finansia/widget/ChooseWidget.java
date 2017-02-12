package com.jim.finansia.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.R;
//import com.jim.pocketaccounter.finance.FinanceManager;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;

public class ChooseWidget extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private SharedPreferences sPref;

    private RecyclerView rvCategories;
    private CheckBox chbCatIncomes, chbCatExpanses;
    private TextView tvSubcategory;
    private boolean[] selected;
    private String BUTTON_ID;
    int mAppWidgetId;
    String GOBACK="s";
    List<RootCategory> listCategory;
    DaoSession daoSession;
    Database db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = prefs.getString(PocketAccounterGeneral.CHOOSEN_THEME_NAME_KEY, PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.BLUE_THEME);
        int themeId = getResources().getIdentifier(themeName, "style", getPackageName());
        setTheme(themeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_widget);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, PocketAccounterGeneral.CURRENT_DB_NAME);
         db = helper.getReadableDb();
        daoSession = new DaoMaster(db).newSession();
        listCategory=daoSession.getRootCategoryDao().loadAll();
        sPref = getSharedPreferences("infoFirst", MODE_PRIVATE);
        mAppWidgetId= AppWidgetManager.INVALID_APPWIDGET_ID;


            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Log.d(WidgetKeys.TAG, "bundle_not_null");
                mAppWidgetId = getIntent().getIntExtra(
                        WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                BUTTON_ID=getIntent().getStringExtra(WidgetKeys.KEY_FOR_INTENT);
            }
        try {
        GOBACK=getIntent().getStringExtra(WidgetKeys.INTENT_FOR_BACK_SETTINGS);
        }
        catch (Exception o){

        }
        rvCategories = (RecyclerView) findViewById(R.id.rvAccounts);
        rvCategories.setLayoutManager(new GridLayoutManager(getApplicationContext(),3));
        rvCategories.setHasFixedSize(true);
        chbCatIncomes = (CheckBox) findViewById(R.id.chbCatIncomes);
        chbCatIncomes.setOnCheckedChangeListener(this);
        chbCatExpanses = (CheckBox) findViewById(R.id.chbCatExpanses);
        chbCatExpanses.setOnCheckedChangeListener(this);
        tvSubcategory = (TextView) findViewById(R.id.tvSubcategory);

        refreshList();


    }

    private void refreshList() {
        ArrayList<RootCategory> categories = new ArrayList<RootCategory>();
        for (int i = 0; i< listCategory.size(); i++) {
            if (chbCatIncomes.isChecked()) {
                if (listCategory.get(i).getType() == PocketAccounterGeneral.INCOME)
                    categories.add(listCategory.get(i));
            }
            if(chbCatExpanses.isChecked()) {
                if (listCategory.get(i).getType() == PocketAccounterGeneral.EXPENSE)
                    categories.add(listCategory.get(i));
            }
        }
        CategoryAdapter adapter = new CategoryAdapter(categories);
        rvCategories.setAdapter(adapter);
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        refreshList();
    }

    @Override
    public void onStop() {
        super.onStop();

    }
    boolean backedToCategory = false;
    @Override
    public void onBackPressed(){
        chbCatExpanses.setVisibility(View.VISIBLE);
        chbCatIncomes.setVisibility(View.VISIBLE);
        tvSubcategory.setVisibility(View.GONE);
        if (backedToCategory){
            backedToCategory = false;
            refreshList();
        }
        else {
        try{

            if(GOBACK!=null){
                Intent aint=new Intent(ChooseWidget.this,SettingsWidget.class);
                aint.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_set);
                aint.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                        mAppWidgetId);
                startActivity(aint);
            }
        }
        finally {
            super.onBackPressed();
        }}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class CategoryAdapter extends RecyclerView.Adapter<ChooseWidget.ViewHolder> {
        private List<Object> result;

        public CategoryAdapter(List<RootCategory> result) {

            this.result = new ArrayList<>();
            for(RootCategory rootCategory:result){
                this.result.add(rootCategory);
            }

        }

        public int getItemCount() {
            return result.size();
        }

        public void onBindViewHolder(final ChooseWidget.ViewHolder holder, final int position) {
            if(!backedToCategory){

                final RootCategory rootCategory = (RootCategory) result.get(position);
                holder.tvCategoryListName.setText(rootCategory.getName());
            final int resId = getResources().getIdentifier(rootCategory.getIcon(),"drawable", getApplicationContext().getPackageName());
                holder.ivCategoryListIcon.setImageResource(resId);

                holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                            chbCatExpanses.setVisibility(View.GONE);
                            chbCatIncomes.setVisibility(View.GONE);
                            tvSubcategory.setVisibility(View.VISIBLE);
                            if(rootCategory.getSubCategories().size()==0) {
                                sPref.edit().putString(BUTTON_ID, rootCategory.getId()).apply();
                                setResult(RESULT_OK);
                                if (AppWidgetManager.INVALID_APPWIDGET_ID != mAppWidgetId) {
                                    (new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            WidgetProvider.updateWidget(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),
                                                    mAppWidgetId);
                                        }
                                    })).start();
                                }
                                if (GOBACK != null) {
                                    Intent aint = new Intent(ChooseWidget.this, SettingsWidget.class);
                                    aint.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_set);
                                    aint.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                                            mAppWidgetId);
                                    startActivity(aint);
                                }
                                finish();
                            }
                            else {
                                result = new ArrayList<>();
                                result.add(rootCategory);
                                for(SubCategory subCategory:rootCategory.getSubCategories()){
                                    result.add(subCategory);
                                }
                                notifyDataSetChanged();
                                backedToCategory = true;
                            }


                }
            });
            }
            else if(backedToCategory){
                if(position == 0){
                    holder.tvCategoryListName.setText("No subcategory");
                    holder.ivCategoryListIcon.setImageResource(R.drawable.category_not_selected);
                    holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chbCatExpanses.setVisibility(View.VISIBLE);
                            chbCatIncomes.setVisibility(View.VISIBLE);
                            tvSubcategory.setVisibility(View.GONE);
                            sPref.edit().putString(BUTTON_ID, ((RootCategory) result.get(position)).getId()).apply();
                            setResult(RESULT_OK);
                            if (AppWidgetManager.INVALID_APPWIDGET_ID != mAppWidgetId) {
                                (new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        WidgetProvider.updateWidget(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),
                                                mAppWidgetId);
                                    }
                                })).start();
                            }
                            if (GOBACK != null) {
                                Intent aint = new Intent(ChooseWidget.this, SettingsWidget.class);
                                aint.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_set);
                                aint.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                                        mAppWidgetId);
                                startActivity(aint);
                            }
                            finish();
                        }
                    });
                    return;
                }

                final SubCategory subCategory = (SubCategory) result.get(position);
                holder.tvCategoryListName.setText(subCategory.getName());
                final int resId = getResources().getIdentifier(subCategory.getIcon(),"drawable", getApplicationContext().getPackageName());
                holder.ivCategoryListIcon.setImageResource(resId);
                holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sPref.edit().putString(BUTTON_ID, subCategory.getId()).apply();
                        setResult(RESULT_OK);
                        if (AppWidgetManager.INVALID_APPWIDGET_ID != mAppWidgetId) {
                            (new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    WidgetProvider.updateWidget(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),
                                            mAppWidgetId);
                                }
                            })).start();
                        }
                        if (GOBACK != null) {
                            Intent aint = new Intent(ChooseWidget.this, SettingsWidget.class);
                            aint.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_set);
                            aint.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                                    mAppWidgetId);
                            startActivity(aint);
                        }
                        finish();
                    }
                });
            }

                    }
        public ChooseWidget.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item_old, parent, false);
            return new ChooseWidget.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryListIcon;
        TextView tvCategoryListName;
        LinearLayout llCategoryItems;
        public ViewHolder(View view) {
            super(view);
            llCategoryItems = (LinearLayout) view.findViewById(R.id.llCategoryItems);
            ivCategoryListIcon = (ImageView) view.findViewById(R.id.ivAccountListIcon);
            tvCategoryListName = (TextView) view.findViewById(R.id.tvAccountListName);
        }
    }

    }
