package com.jim.finansia.fragments;

import android.content.Context;
import android.view.View;

import com.jim.finansia.R;

/**
 * Created by vosit on 26.10.16.
 */

public abstract class PABaseListFragment extends PABaseFragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
        toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerInitializer.getDrawer().openLeftSide();
            }
        });
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setSubtitleIconVisibility(View.GONE);
    }
    abstract void refreshList();
}
