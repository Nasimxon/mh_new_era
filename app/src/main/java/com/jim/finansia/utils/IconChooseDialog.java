package com.jim.finansia.utils;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
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
    private GridView gvCategoryIcons;
    private String[] icons;
    private String selectedIcon = "icons_1";
    private View dialogView;
    private IconAdapterCategory adapter;

    public IconChooseDialog(Context context) {
        super(context);
        dialogView = getLayoutInflater().inflate(R.layout.cat_icon_select, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        gvCategoryIcons = (GridView) dialogView.findViewById(R.id.gvCategoryIcons);
        icons = context.getResources().getStringArray(R.array.icons);
        adapter = new IconAdapterCategory(context, icons, selectedIcon);
        gvCategoryIcons.setAdapter(adapter);
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
        gvCategoryIcons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onIconPickListener.OnIconPick(icons[position]);
            }
        });
    }

    public String[] getIcons() {
        return icons;
    }

    public void setSelectedIcon(String selectedIcon) {
        this.selectedIcon = selectedIcon;
        adapter = new IconAdapterCategory(getContext(), icons, selectedIcon);
        gvCategoryIcons.setAdapter(adapter);
    }
}
