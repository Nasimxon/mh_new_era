package com.jim.finansia.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.SubCategory;

import javax.inject.Inject;

public class SubcatItemChecker extends LinearLayout {
    private int color;
    private ImageView imageView;
    private TextView textView;
    private boolean checked = false;
    private OnCheckedChangeListener onCheckedChangeListener;
    private SubCategory subCategory;
    @Inject SharedPreferences preferences;
    public SubcatItemChecker(Context context) {
        super(context);
        init(context);
    }
    public SubcatItemChecker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public SubcatItemChecker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public SubcatItemChecker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        LayoutInflater.from(context).inflate(R.layout.sub_cat_item_cheker, this, true);
        imageView = (ImageView) findViewById(R.id.ivSubcatChecker);
        textView = (TextView) findViewById(R.id.tvSubcatChecker);
        color = GetterAttributColors.fetchHeadAccedentColor(context);
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checked = !checked;
                toggle();
                invalidate();
                if (onCheckedChangeListener != null)
                    onCheckedChangeListener.onCheckedChange(checked);
            }
        });
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
        int resId = getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
        imageView.setImageDrawable(null);
        imageView.setImageResource(resId);
        textView.setText(subCategory.getName());
        invalidate();
    }

    private void toggle() {
        if (checked){
            imageView.setColorFilter(color);
            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_myagkiy_glavniy));
        }
        else{
            imageView.setColorFilter(ContextCompat.getColor(getContext(),R.color.black_for_maykiy_secodary_category));
            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.black_for_maykiy_secodary_category));
        }
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        toggle();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

}
