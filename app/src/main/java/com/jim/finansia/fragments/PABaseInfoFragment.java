package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.R;

public abstract class PABaseInfoFragment extends PABaseFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setImageToSecondImage(R.drawable.check_sign);
        toolbarManager.setSubtitleIconVisibility(View.GONE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    abstract void refreshList();
}
