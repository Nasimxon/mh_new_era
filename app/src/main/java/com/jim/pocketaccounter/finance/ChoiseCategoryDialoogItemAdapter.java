package com.jim.pocketaccounter.finance;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.widget.ChooseWidget;
import com.jim.pocketaccounter.widget.SettingsWidget;
import com.jim.pocketaccounter.widget.WidgetKeys;
import com.jim.pocketaccounter.widget.WidgetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by developer on 09.01.2017.
 */

public class ChoiseCategoryDialoogItemAdapter extends RecyclerView.Adapter<ChoiseCategoryDialoogItemAdapter.ViewHolder> {
    private List<Object> result;
    boolean backedToCategory = false;
    private Context context;
    private OnItemSelected onItemSelected;
    public void setListForRefresh(List<RootCategory> listForRefresh){
        this.result = new ArrayList<>();
        for(RootCategory rootCategory:listForRefresh){
            this.result.add(rootCategory);
        }
    }
    public ChoiseCategoryDialoogItemAdapter(List<Object> result, Context context, OnItemSelected onItemSelected) {
        this.context=context;
        this.onItemSelected = onItemSelected;
        this.result = result;

    }


    public interface OnItemSelected{
        void itemPressed(String itemID);
    }

    public interface OnItemSelectedForSubCatDialog{
        void itemPressed(String itemID);
    }
    public void toBackedToCategory(boolean backedToCategory){
        this.backedToCategory = backedToCategory;
        notifyDataSetChanged();
    }
    public int getItemCount() {
        return result.size();
    }

    public void onBindViewHolder(final ChoiseCategoryDialoogItemAdapter.ViewHolder holder, final int position) {
        if(!backedToCategory){

            final RootCategory rootCategory = (RootCategory) result.get(position);
            holder.tvCategoryListName.setText(rootCategory.getName());
            final int resId = context.getResources().getIdentifier(rootCategory.getIcon(),"drawable", context.getPackageName());
            holder.ivCategoryListIcon.setImageResource(resId);

            holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(rootCategory.getSubCategories().size()==0) {
                    //// TODO: 09.01.2017 VIBOR CAT
                        onItemSelected.itemPressed(rootCategory.getId());
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
                holder.tvCategoryListName.setText(context.getString(R.string.no_category_name));
                holder.ivCategoryListIcon.setImageResource(R.drawable.category_not_selected);
                holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                //// TODO: 09.01.2017 VIBOR NO_SUB
                onItemSelected.itemPressed(((RootCategory) result.get(position)).getId());
                    }
                });
                return;
            }


            final SubCategory subCategory = (SubCategory) result.get(position);
            if(subCategory == null){
                holder.tvCategoryListName.setText(context.getString(R.string.add_subcategory));
                holder.ivCategoryListIcon.setImageResource(R.drawable.no_category);
                holder.ivCategoryListIcon.setColorFilter(ContextCompat.getColor(context,R.color.black_for_myagkiy_glavniy));
                holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //// TODO: 09.01.2017 VIBOR Add_Sub
                        onItemSelected.itemPressed("");
                    }
                });
                return;
            }
            holder.tvCategoryListName.setText(subCategory.getName());
            final int resId = context.getResources().getIdentifier(subCategory.getIcon(),"drawable", context.getApplicationContext().getPackageName());
            holder.ivCategoryListIcon.setImageResource(resId);
            holder.llCategoryItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
            //// TODO: 09.01.2017 VIBOR SUB

                    onItemSelected.itemPressed(subCategory.getId());
                }
            });
        }

    }
    public ChoiseCategoryDialoogItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item_dialog, parent, false);
        return new ChoiseCategoryDialoogItemAdapter.ViewHolder(view);
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
