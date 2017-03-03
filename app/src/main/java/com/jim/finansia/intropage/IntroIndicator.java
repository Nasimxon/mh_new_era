package com.jim.finansia.intropage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;

import java.util.ArrayList;
import java.util.List;

public class IntroIndicator extends AppCompatActivity {
    ViewPager pager;
    PagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TextView forskip;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    ImageView ivToNextBotton;
    ImageView miniIcon;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref=getSharedPreferences("infoFirst",MODE_PRIVATE);
        ed=sPref.edit();
        if (!sPref.getBoolean("FIRST_KEY", true)) {
            Intent goMain=new Intent(IntroIndicator.this, PocketAccounter.class);
            startActivity(goMain);
            finish();
        }
        setTheme(R.style.BlueTheme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro_indicator);

        forskip = (TextView) findViewById(R.id.forskip);
        ivToNextBotton = (ImageView) findViewById(R.id.ivToNextBotton);
        miniIcon = (ImageView) findViewById(R.id.miniIcon);

        forskip = (TextView) findViewById(R.id.forskip);
        pager = (ViewPager) findViewById(R.id.pager);
        initFrags();
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        forskip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                   Intent goMain=new Intent(IntroIndicator.this, PocketAccounter.class);
                   startActivity(goMain);
//                   ed.putBoolean("FIRST_KEY", false);
//                   ed.commit();
               }
               finally {
                   finish();
               }
            }
        });

        PageIndicator mIndicator= (PageCircleIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(pager);


        pager.setPageTransformer(true,new ZoomOutTranformer());
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 5) {
//                    miniIcon.setImageResource(R.drawable.right_password);
                    miniIcon.setImageResource(R.drawable.right_password);
                }
                else miniIcon.setImageResource(R.drawable.right_password);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mp = MediaPlayer.create(this, R.raw.revhiti  );
        mp.setVolume(0.1f,0.1f);
        ivToNextBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pager.getCurrentItem()!=5){
                    pager.setCurrentItem(pager.getCurrentItem()+1,true);
                    mp.start();

                }
                else {
                    try{
                        Intent togoB=new Intent(IntroIndicator.this, PocketAccounter.class);
                        startActivity(togoB);
//                        ed.putBoolean("FIRST_KEY",false);
//                        ed.commit();
                    }
                    finally {
                        finish();
                    }
                }
            }
        });
    }
    private void initFrags(){
        fragments=new ArrayList<>();

        //Sozdaniya i otpravka data v fragmentam
        IntroFirstFrame appInfo=new IntroFirstFrame();
        appInfo.shareData(new DataIntro(getString(R.string.appInfoTitel),getString(R.string.introAppInfo),R.drawable.bagicon));
        fragments.add(appInfo);

        IntroFrame smsIntro=new IntroFrame();
        smsIntro.shareData(new DataIntro(getString(R.string.safeInfo),getString(R.string.secureInfo),R.drawable.mobilesms));

        IntroFrame voiceint=new IntroFrame();
        voiceint.shareData(new DataIntro(getString(R.string.quickInfp),getString(R.string.quickInfo),R.drawable.voice_intro));


        IntroFrame debtintro=new IntroFrame();
        debtintro.shareData(new DataIntro(getString(R.string.designInfo),getString(R.string.clear_desgn),R.drawable.debt_intro));


        IntroFrame reportint=new IntroFrame();
        reportint.shareData(new DataIntro(getString(R.string.flexInfo),getString(R.string.flexiinfo),R.drawable.report_intro));

        fragments.add(voiceint);
        fragments.add(debtintro);
        fragments.add(smsIntro);
        fragments.add(reportint);

        fragments.add(new IntroWithButton());

    }
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
