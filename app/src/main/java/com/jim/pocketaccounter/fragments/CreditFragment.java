package com.jim.pocketaccounter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.credit.AdapterCridet;
import com.jim.pocketaccounter.credit.AdapterCridetArchive;
import com.jim.pocketaccounter.credit.LinearManagerWithOutEx;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.SearchResultConten;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

public class CreditFragment extends Fragment {
    @Inject
    DaoSession daoSession;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;

    private CreditDetialsDao creditDetialsDao;
    RecyclerView crRV;
    AdapterCridet crAdap;
    Context This;
    TextView ifListEmpty;
    CreditTabLay.SvyazkaFragmentov svyaz;
    private CreditTabLay creditTabLay;

    public AdapterCridetArchive.GoCredFragForNotify getInterfaceNotify(){
        return new AdapterCridetArchive.GoCredFragForNotify() {
            @Override
            public void notifyCredFrag() {
                crAdap.notifyDataSetChanged();
            }
        };
    }

    public CreditFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        creditDetialsDao = daoSession.getCreditDetialsDao();
        This=getActivity();
    }
    public  CreditTabLay.ForFab getEvent(){
        return new CreditTabLay.ForFab() {
            @Override
            public void pressedFab() {
                openFragment(new AddCreditFragment(),AddCreditFragment.OPENED_TAG);
            }
        };
    }
    public void setSvyaz(CreditTabLay.SvyazkaFragmentov A){
        svyaz=A;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        toolbarManager.setTitle(getResources().getString(R.string.cred_managment));
        toolbarManager.setSubtitle("");
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setOnTitleClickListener(null);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
        View V=inflater.inflate(R.layout.fragment_credit, container, false);
        ifListEmpty=(TextView) V.findViewById(R.id.ifListEmpty);
        if(creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.Key_for_archive.eq(false)).orderDesc(CreditDetialsDao.Properties.MyCredit_id).build().list().size()==0){
            ifListEmpty.setVisibility(View.VISIBLE);
            ifListEmpty.setText(getResources().getString(R.string.credit_are_empty));
        }
        else ifListEmpty.setVisibility(View.GONE);
        crRV=(RecyclerView) V.findViewById(R.id.my_recycler_view);
        LinearManagerWithOutEx llm = new LinearManagerWithOutEx(This);
        crRV.setLayoutManager(llm);

        crAdap=new AdapterCridet(This,svyaz);
        crRV.setAdapter(crAdap);

        crRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                creditTabLay.onScrolledList(dy > 0);
            }
        });
        return V;
    }

    public void setCreditTabLay (CreditTabLay creditTabLay) {
        this.creditTabLay = creditTabLay;
    }

    public void openFragment(Fragment fragment,String tag) {
        if (fragment != null) {
            if(tag.matches("Addcredit"))
                ((AddCreditFragment)fragment).addEventLis(new EventFromAdding() {
                    @Override
                    public void addedCredit() {
                        updateToFirst();
                    }

                    @Override
                    public void canceledAdding() {
                    }
                });
//            paFragmentManager.getFragmentManager().popBackStack();
//            paFragmentManager.displayFragment(fragment);
            final android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(tag).setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.add(R.id.flMain, fragment,tag);
            ft.commit();
        }
    }
    public void updateToFirst(){
        Log.d("checkInterfaces", (crAdap==null)?"ADDING - AdapterIsNull":"ADDING - AdapterIsNotNull");
         ifListEmpty.setVisibility(View.GONE);
        crAdap.updateList();
        try{
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                   crAdap.notifyItemInserted(0);
                }
            }, 50);
            try {
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      crRV.scrollToPosition(0);
                    }
                }, 100);
            }
            catch (Exception o){}
        }
        catch (Exception o){}
    }

    public void sortListFromDate(ArrayList<CreditDetials> crList){

        Collections.sort(crList, new Comparator<CreditDetials>() {
            @Override
            public int compare(CreditDetials con1, CreditDetials con2)
            {
                return  con1.getMyCredit_id()<con2.getMyCredit_id()?1:-1;
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public interface EventFromAdding{
        void addedCredit();
        void canceledAdding();
    }
}