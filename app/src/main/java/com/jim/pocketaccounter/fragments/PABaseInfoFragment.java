package com.jim.pocketaccounter.fragments;

import android.content.Context;
import android.view.View;

import com.jim.pocketaccounter.R;

public abstract class PABaseInfoFragment extends PABaseFragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setSubtitleIconVisibility(View.GONE);
    }
    abstract void refreshList();
}
