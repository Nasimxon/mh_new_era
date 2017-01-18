package com.jim.finansia.utils.catselector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class DrawingSelectorView extends AbstractSelectorView {
    protected RelativeLayout scene;
    protected ImageView ivPrevious;
    protected ImageView ivNext;
    protected SelectorAdapter adapter;
    protected Map<Integer, ViewGroup> stack;
    protected int drawnNeighborAmount = 2;

    public DrawingSelectorView(Context context) {
        super(context);
        init(context);
    }

    public DrawingSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawingSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DrawingSelectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.drawing_selector_view, this);
        ivPrevious = (ImageView) findViewById(R.id.ivPrevious);
        ivPrevious.setRotation(180.0f);
        ivNext = (ImageView) findViewById(R.id.ivNext);
        scene = (RelativeLayout) findViewById(R.id.scene);
    }

    public void setAdapter(SelectorAdapter adapter) {
        this.adapter = adapter;
        count = adapter.getCount();
        initStack();
        scene.addView(stack.get(position));
    }

    protected void initStack() {
        stack = new HashMap<>();
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (count > 10) {
            for (int i = 0; i <= drawnNeighborAmount; i++) {
                LinearLayout oneScreen = new LinearLayout(getContext());
                oneScreen.setLayoutParams(lp);
                ViewGroup viewGroup = adapter.getInstantiatedView(i, oneScreen);
                stack.put(i, viewGroup);
            }
            for (int i = count - 1; i >= count - drawnNeighborAmount; i--) {
                LinearLayout oneScreen = new LinearLayout(getContext());
                oneScreen.setLayoutParams(lp);
                ViewGroup viewGroup = adapter.getInstantiatedView(i, oneScreen);
                stack.put(i, viewGroup);
            }
        }
        else {
            for (int i = 0; i < count; i++) {
                LinearLayout oneScreen = new LinearLayout(getContext());
                oneScreen.setLayoutParams(lp);
                ViewGroup viewGroup = adapter.getInstantiatedView(i, oneScreen);
                stack.put(i, viewGroup);
            }
        }
    }


    public static abstract class SelectorAdapter<T> {
        protected List<T> list;
        protected abstract ViewGroup getInstantiatedView(int position, ViewGroup parent);
        protected abstract int getCount();
        public List<T> getList() { return list; }
    }
}
