package com.jim.pocketaccounter.fragments;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.BuildConfig;
import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.billing.PurchaseImplementation;
import com.jim.pocketaccounter.utils.record.BoardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class ChangeColorOfStyleFragment extends Fragment {
    private String[] colors = {BuildConfig.BLUE_THEME, BuildConfig.YELLOW_THEME, BuildConfig.FIOLA_THEME};
    private RelativeLayout rlColorChangeFragment;
    private RecyclerView rvColorChangeFragment;
    private int selectedColorPos = 0;
    @Inject PurchaseImplementation purchaseImplementation;
    @Inject SharedPreferences preferences;
    @Inject ToolbarManager toolbarManager;
    private BoardView boardView;
    private List<AdjustColors> colorDatas;
    private LinearLayout llBuyColorButton;
    private TextView tvColorChooseSetText, tvColorChooseButtonPaymentAmount;
    private ImageView ivColorChooseButtonThinStripe;
    private int buyButtonBgId = R.drawable.color_choose_bottom_button_border_with_bg,
                setColorBgId = R.drawable.color_choose_bottom_button_border_no_bg;
    private String choosenThemeName;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.change_color_of_style_fragment, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        choosenThemeName = preferences.getString(PocketAccounterGeneral.CHOOSEN_THEME_NAME_KEY,
                PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.BLUE_THEME);
        llBuyColorButton = (LinearLayout) rootView.findViewById(R.id.llBuyColorButton);
        tvColorChooseSetText = (TextView) rootView.findViewById(R.id.tvColorChooseSetText);
        tvColorChooseButtonPaymentAmount = (TextView) rootView.findViewById(R.id.tvColorChooseButtonPaymentAmount);
        ivColorChooseButtonThinStripe = (ImageView) rootView.findViewById(R.id.ivColorChooseButtonThinStripe);
        rlColorChangeFragment = (RelativeLayout) rootView.findViewById(R.id.rlColorChangeFragment);
        rvColorChangeFragment = (RecyclerView) rootView.findViewById(R.id.rvColorChangeFragment);
        boardView = new BoardView(getContext(), PocketAccounterGeneral.EXPENSE, Calendar.getInstance());
        boardView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        boardView.setDrawIndicator(false);
        rlColorChangeFragment.addView(boardView);
        boardView.hideText();
        LinearLayout layout = new LinearLayout(getContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setClickable(true);
        rlColorChangeFragment.addView(layout);
        generateColorDatas();
        rvColorChangeFragment.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvColorChangeFragment.setAdapter(new ColorChangeListAdapter(colorDatas));
        defineMode();
        return rootView;
    }



    private void generateColorDatas() {
        colorDatas = new ArrayList<>();
        for (String color : colors) {
            AdjustColors c = new AdjustColors();
            c.setThemeName(color);
            colorDatas.add(c);
        }
    }

    private void defineMode() {
        boolean isActive = preferences.getBoolean(choosenThemeName, choosenThemeName.equals(BuildConfig.BLUE_THEME));
        if (isActive) {
            tvColorChooseSetText.setText("Set color to theme");
            tvColorChooseSetText.setTextColor(Color.parseColor("#414141"));
            ivColorChooseButtonThinStripe.setVisibility(View.GONE);
            tvColorChooseButtonPaymentAmount.setVisibility(View.GONE);
            llBuyColorButton.setBackground(null);
            llBuyColorButton.setBackground(ContextCompat.getDrawable(getContext(), setColorBgId));
        }
        else {
            tvColorChooseSetText.setText("Buy color for theme");
            tvColorChooseSetText.setTextColor(Color.WHITE);
            ivColorChooseButtonThinStripe.setVisibility(View.VISIBLE);
            tvColorChooseButtonPaymentAmount.setVisibility(View.VISIBLE);
            llBuyColorButton.setBackground(null);
            llBuyColorButton.setBackground(ContextCompat.getDrawable(getContext(), buyButtonBgId));
        }
    }

    private class ColorChangeListAdapter extends RecyclerView.Adapter<ChangeColorOfStyleFragment.ViewHolder> {
        private List<AdjustColors> result;
        public ColorChangeListAdapter(List<AdjustColors> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final ChangeColorOfStyleFragment.ViewHolder view, final int position) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.color_chooser_listview_item);
            drawable.setColorFilter(new PorterDuffColorFilter(result.get(position).getColor(),PorterDuff.Mode.MULTIPLY));
            view.llColorChooserBackground.setBackground(null);
            view.llColorChooserBackground.setBackground(drawable);
            if (choosenThemeName.equals(result.get(position).themeName))
                scaleUp(view.rlChangingColorItem);
            else
                scaleDown(view.rlChangingColorItem);
            if (result.get(position).isAvailable())
                view.ivColorChooserLock.setVisibility(View.GONE);
            else
                view.ivColorChooserLock.setVisibility(View.VISIBLE);
            if (position == 0) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.view.getLayoutParams();
                lp.setMargins((int) getResources().getDimension(R.dimen.ten_dp), 0, 0, 0);
                view.view.setLayoutParams(lp);
            } else if (position == result.size()-1) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.view.getLayoutParams();
                lp.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.ten_dp));
                view.view.setLayoutParams(lp);
            }
            view.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boardView.setBackgroundColor(result.get(position).getColor());
                    toolbarManager.setBackgroundColor(result.get(position).getColor());
                    choosenThemeName = result.get(position).getThemeName();
                    defineMode();
                    notifyDataSetChanged();
                }
            });
        }

        public void scaleUp(View view) {
            view.setScaleX(1.1f);
            view.setScaleY(1.1f);
        }

        public void scaleDown(View view) {
            view.setScaleX(1.0f);
            view.setScaleY(1.0f);
        }

        public ChangeColorOfStyleFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changing_color_item, parent, false);
            return new ChangeColorOfStyleFragment.ViewHolder(view);
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlChangingColorItem;
        View view;
        ImageView ivColorChooserLock;
        LinearLayout llColorChooserBackground;

        public ViewHolder(View view) {
            super(view);
            rlChangingColorItem = (RelativeLayout) view.findViewById(R.id.llChangingColorItem);
            ivColorChooserLock = (ImageView) view.findViewById(R.id.ivColorChooserLock);
            llColorChooserBackground = (LinearLayout) view.findViewById(R.id.llColorChooserBackground);
            this.view = view;
        }
    }

    class AdjustColors {
        private int color;
        private String themeName;
        int[] attrs = {R.attr.headColor};
        private boolean isAvailable = false;
        public int getColor() { return color; }
        public void setThemeName(String themeName) {
            this.themeName = themeName;
            int id = getResources().getIdentifier(themeName, "style", getContext().getPackageName());
            TypedArray ta = getContext().obtainStyledAttributes(id, attrs);
            color = ta.getColor(0, Color.BLACK);
            boolean defaultAvailable = themeName.equals(BuildConfig.BLUE_THEME);
            isAvailable = preferences.getBoolean(themeName, defaultAvailable);
        }
        public boolean isAvailable() {
            return isAvailable;
        }
        public String getThemeName() {
            return themeName;
        }
    }
}
