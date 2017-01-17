package com.jim.finansia.utils;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jim.finansia.R;
import com.jim.finansia.finance.IconAdapterCategory;

/**
 * Created by DEV on 29.08.2016.
 */

public class IconChooseDialog extends Dialog {
    private RecyclerView rvCategoryIcons;
    private String[] icons;
    private String selectedIcon = "icons_1";
    private View dialogView;
    private IconsAdapters adapter;
    private OnIconPickListener onIconPickListener;
    IconsAdapters.OnClickListnerForBack onClickListnerForBack;
    public IconChooseDialog(Context context ) {
        super(context);
        dialogView = getLayoutInflater().inflate(R.layout.cat_icon_select, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        onClickListnerForBack = new IconsAdapters.OnClickListnerForBack() {
            @Override
            public void onClick(String s) {
                onIconPickListener.OnIconPick(s);
            }
        };
        rvCategoryIcons = (RecyclerView) dialogView.findViewById(R.id.gvCategoryIcons);
        icons = context.getResources().getStringArray(R.array.icons);
        adapter = new IconsAdapters(context, icons, selectedIcon, onClickListnerForBack);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context,4);
        rvCategoryIcons.setLayoutManager(layoutManager);
        rvCategoryIcons.setAdapter(adapter);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        getWindow().setLayout(width, ActionBar.LayoutParams.MATCH_PARENT);
    }

    public IconChooseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected IconChooseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnIconPickListener(final OnIconPickListener onIconPickListener) {
        this.onIconPickListener = onIconPickListener;
    }

    public String[] getIcons() {
        return icons;
    }

    public void setSelectedIcon(String selectedIcon) {
        this.selectedIcon = selectedIcon;
        adapter = new IconsAdapters(getContext(), icons, selectedIcon, new IconsAdapters.OnClickListnerForBack() {
            @Override
            public void onClick(String s) {
                onIconPickListener.OnIconPick(s);
            }
        });
        rvCategoryIcons.setAdapter(adapter);
    }
}
