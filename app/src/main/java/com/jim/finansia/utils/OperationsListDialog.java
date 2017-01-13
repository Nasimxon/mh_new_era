package com.jim.finansia.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jim.finansia.R;

/**
 * Created by DEV on 29.08.2016.
 */

public class OperationsListDialog extends Dialog {
    private ListView listView;
    View dialogView;
    public OperationsListDialog(Context context) {
        super(context);
        dialogView = getLayoutInflater().inflate(R.layout.listview, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        listView = (ListView) dialogView.findViewById(R.id.lvOperations);
    }
    public OperationsListDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
    protected OperationsListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    public void setAdapter(String[] list){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        listView.setOnItemClickListener(onItemClickListener);
    }
}
