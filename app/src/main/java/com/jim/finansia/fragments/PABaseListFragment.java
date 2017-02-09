package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jim.finansia.R;

/**
 * Created by vosit on 26.10.16.
 */

public abstract class PABaseListFragment extends PABaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
        toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerInitializer.getDrawer().openLeftSide();
            }
        });
        toolbarManager.setSubtitleIconVisibility(View.GONE);

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    abstract void refreshList();
}
