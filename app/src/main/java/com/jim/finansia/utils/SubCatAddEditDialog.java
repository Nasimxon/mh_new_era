package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.managers.LogicManager;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by DEV on 29.08.2016.
 */

public class SubCatAddEditDialog extends Dialog {
    private TextView tv;
    private View dialogView;
    private ImageView fabChooseIcon;
    private String subcatIcon;
    private SubCategory subCategory;
    private ImageView ivSubCatClose;
    private TextView ivSubCatSave;
    private String rootCategoryId;
    private EditText etSubCategoryName;
    @Inject
    IconChooseDialog iconsChooseDialog;
    @Inject
    LogicManager logicManager;
    public SubCatAddEditDialog(Context context) {
        super(context);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        dialogView = getLayoutInflater().inflate(R.layout.sub_category_edit_layout, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        fabChooseIcon = (ImageView) dialogView.findViewById(R.id.fabChooseIcon);
        etSubCategoryName = (EditText) dialogView.findViewById(R.id.etSubCategoryName);
    }

    public void setRootCategory(String rootCategoryId) {
        this.rootCategoryId = rootCategoryId;
    }

    public void setSubCat(SubCategory subCategory, final OnSubcategorySavingListener onSubcategorySavingListener) {
        this.subCategory = subCategory;
        Bitmap temp;
        if (subCategory != null) {
            etSubCategoryName.setText(subCategory.getName());
            subcatIcon = subCategory.getIcon();
            int resId = getContext().getResources().getIdentifier(subCategory.getIcon(), "drawable", getContext().getPackageName());
            temp = BitmapFactory.decodeResource(getContext().getResources(), resId);
//            scaled = Bitmap.createScaledBitmap(temp, (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp),
//                    (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp), false);
        } else {
            etSubCategoryName.setText("");
            subcatIcon = "add_icon";
            int resId = getContext().getResources().getIdentifier(subcatIcon, "drawable", getContext().getPackageName());
            temp = BitmapFactory.decodeResource(getContext().getResources(), resId);
//            scaled = Bitmap.createScaledBitmap(temp, (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp),
//                    (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp), false);
        }
        fabChooseIcon.setImageBitmap(temp);
        fabChooseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconsChooseDialog.setSelectedIcon(subcatIcon);
                iconsChooseDialog.setOnIconPickListener(new OnIconPickListener() {
                    @Override
                    public void OnIconPick(String icon) {
                        int resId = getContext().getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
                        Bitmap temp = BitmapFactory.decodeResource(getContext().getResources(), resId);
//                        Bitmap scaled = Bitmap.createScaledBitmap(temp, (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp),
//                                (int) getContext().getResources().getDimension(R.dimen.twentyfive_dp), false);
                        fabChooseIcon.setImageBitmap(temp);
                        subcatIcon = icon;
                        iconsChooseDialog.dismiss();
                    }
                });
                iconsChooseDialog.show();
            }
        });
        ivSubCatClose = (ImageView) dialogView.findViewById(R.id.ivSubCatClose);
        ivSubCatClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ivSubCatSave = (TextView) dialogView.findViewById(R.id.ivSubCatSave);
        ivSubCatSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subcatIcon.equals("add_icon")){
                    Toast.makeText(getContext(), R.string.select_icons_sb, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(etSubCategoryName.getText().toString().length()==0){
                    Toast.makeText(getContext(), R.string.subcategory_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                SubCategory subCategory = null;
                if (SubCatAddEditDialog.this.subCategory == null) {
                    subCategory = new SubCategory();
                    subCategory.setId(UUID.randomUUID().toString());
                }
                else
                    subCategory = SubCatAddEditDialog.this.subCategory;
                subCategory.setParentId(rootCategoryId);
                subCategory.setName(etSubCategoryName.getText().toString());
                subCategory.setIcon(subcatIcon);
                onSubcategorySavingListener
                        .onSubcategorySaving(subCategory);
                etSubCategoryName.setText("");
            }
        });
    }

    public SubCatAddEditDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SubCatAddEditDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
