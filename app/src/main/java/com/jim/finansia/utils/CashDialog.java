package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.database.PurposeDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by root on 9/13/16.
 */
public class CashDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    @Inject
    DaoSession daoSession;
    private AccountDao accountDao;
    private PurposeDao purposeDao;
    private TextView tvEqual;
    private ImageView close;
    private ImageView save;
    private Spinner transferFirst;
    private Spinner transferSecond;
    private EditText amount;
    private Spinner accounts;
    private ArrayAdapter<String> accountAdarper;
    private ArrayAdapter<String> transferFirstAdarper;
    private ArrayAdapter<String> transferSecondAdarper;

    private int selectOne = 0;
    private int selectTwo = 1;

    public CashDialog(Context context) {
        super(context);
    }

    public CashDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CashDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_transfer_dialog);
        tvEqual = (TextView) findViewById(R.id.tvEqual);
        close = (ImageView) findViewById(R.id.ivAccountManClose);
        save = (ImageView) findViewById(R.id.ivAccountManSave);
        transferFirst = (Spinner) findViewById(R.id.spTransferFirst);
        transferSecond = (Spinner) findViewById(R.id.spTransferSecond);
        amount = (EditText) findViewById(R.id.etAccountEditName);
        accounts = (Spinner) findViewById(R.id.spAccManDialog);
        close.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivAccountManClose: {
                dismiss();
                break;
            }
            case R.id.ivAccountManSave: {
                // ------------ Save dialog -------

                break;
            }
        }
    }

    public void setOnType(int type) {
        List<String> list = new ArrayList<>();
        if (type == 0) {
            for (Account ac : accountDao.loadAll()) {
                list.add(ac.getName());
            }

            transferFirstAdarper = new ArrayAdapter<String>(getContext()
            ,android.R.layout.simple_list_item_1, list);
            transferSecondAdarper = new ArrayAdapter<String>(getContext()
            ,android.R.layout.simple_list_item_1, list);

        } else {
            for (Purpose pr : purposeDao.loadAll()) {
                list.add(pr.getDescription());
            }
        }
        transferFirst.setAdapter(transferFirstAdarper);
        transferSecond.setAdapter(transferSecondAdarper);
        transferFirst.setOnItemSelectedListener(this);
        transferSecond.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (view.getId()) {
            case R.id.spTransferFirst: {
                if (position == selectTwo) {
                    transferSecond.setSelection(selectOne);
                }
                break;
            }
            case R.id.spTransferSecond: {
                if (position == selectOne) {
                    transferFirst.setSelection(selectTwo);
                }
                break;
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
