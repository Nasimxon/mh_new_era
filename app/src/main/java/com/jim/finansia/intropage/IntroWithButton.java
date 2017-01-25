package com.jim.finansia.intropage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;

import static android.content.Context.MODE_PRIVATE;

public class IntroWithButton extends Fragment {
    TextView textLets;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;

    public IntroWithButton() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V=inflater.inflate(R.layout.fragment_intro_with_button, container, false);
        sPref=getActivity().getSharedPreferences("infoFirst", MODE_PRIVATE);
        ed=sPref.edit();

        textLets=(TextView) V.findViewById(R.id.textView8) ;
//        Typeface fontBlack = Typeface.createFromAsset(getActivity().getAssets(), "ralewayBlack.ttf");
//        textLets.setTypeface(fontBlack);

        return  V;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
