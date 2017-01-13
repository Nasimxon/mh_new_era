package com.jim.finansia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.AdapterCridetArchive;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DaoSession;

import javax.inject.Inject;

public class CreditArchiveFragment extends Fragment {
    RecyclerView crRV;
    AdapterCridetArchive crAdap;
    Context This;
    @Inject
    DaoSession daoSession;
    TextView ifListEmpty;
    AdapterCridetArchive.GoCredFragForNotify svyazForNotifyFromArchAdap;
    public void setSvyazToAdapter(AdapterCridetArchive.GoCredFragForNotify goNotify){
        svyazForNotifyFromArchAdap=goNotify;
    }
    public CreditArchiveFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         This=getActivity();
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
    }
    public CreditTabLay.SvyazkaFragmentov getSvyaz(){
        return new CreditTabLay.SvyazkaFragmentov() {
            @Override
            public void itemInsertedToArchive() {
                updateList();
                ifListEmpty.setVisibility(View.GONE);
                Log.d("checkInterfaces", "ARCHIVE - updateList();");
            }
        };
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("checkInterfaces", "onCreatView : " );
        View V=inflater.inflate(R.layout.fragment_credit, container, false);
        ifListEmpty=(TextView) V.findViewById(R.id.ifListEmpty);
        if(daoSession.getCreditDetialsDao().queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(true)).build().list().size()==0){
            ifListEmpty.setVisibility(View.VISIBLE);
            ifListEmpty.setText(getResources().getString(R.string.credit_arcive_are_empty));
        }
        else {
            ifListEmpty.setVisibility(View.GONE);
        }
        crRV=(RecyclerView) V.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(This);
        crRV.setLayoutManager(llm);
        crAdap=new AdapterCridetArchive(This);
        crAdap.setSvyazToAdapter(svyazForNotifyFromArchAdap);
        crRV.setAdapter(crAdap);
        return V;
    }

    public void updateList(){
        Log.d("checkInterfaces", (crAdap==null)?"AdapterIsNull":"AdapterIsNotNull");
        crAdap.updateBase();
        crAdap.notifyDataSetChanged();
        if(crRV.getChildCount()>0);
        crRV.scrollToPosition(0);
    }
}
