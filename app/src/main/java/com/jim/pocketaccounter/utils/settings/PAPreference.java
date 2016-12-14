package com.jim.pocketaccounter.utils.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jim.pocketaccounter.R;

public class PAPreference extends Preference {
    private TextView tvSettingsPreferenceTitle,
                     tvSettingsPreferenceSubtitle;

    @SuppressLint("NewApi")
    public PAPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public PAPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PAPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PAPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.setWidgetLayoutResource(R.layout.settings_preference);
    }



    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        tvSettingsPreferenceTitle = (TextView) view.findViewById(R.id.tvSettingsPreferenceTitle);
        tvSettingsPreferenceSubtitle = (TextView) view.findViewById(R.id.tvSettingsPreferenceSubtitle);

    }
}
