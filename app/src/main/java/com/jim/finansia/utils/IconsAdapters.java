package com.jim.finansia.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.R;
import com.jim.finansia.credit.AdapterCridet;

/**
 * Created by developer on 17.01.2017.
 */

public class IconsAdapters extends RecyclerView.Adapter<IconsAdapters.myViewHolder>  {
    private String[] result;
    private String selectedItem;
    private Context context;
    private OnClickListnerForBack onClickListnerForBack;
    public interface OnClickListnerForBack{
        void onClick(String s);
    }
    public IconsAdapters (Context context, String[] result, String selectedItem,OnClickListnerForBack onClickListnerForBack){
        this.result = result;
        this.selectedItem = selectedItem;
        this.context=context;
        this.onClickListnerForBack=onClickListnerForBack;
    }

    @Override
    public IconsAdapters.myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_contener_dialog, parent, false);
        myViewHolder vh = new myViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(IconsAdapters.myViewHolder holder,final int position) {
        if (!result[position].matches(selectedItem))
            holder.imageView.setBackgroundResource(R.drawable.unselected_icon);
        else
            holder.imageView.setBackgroundResource(R.drawable.selected_icon);

        int resId = context.getResources().getIdentifier(result[position], "drawable", context.getPackageName());
        holder.imageView.setImageResource(resId);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListnerForBack.onClick(result[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return result.length;
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public myViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.imageViewContent);
        }
    }
}
