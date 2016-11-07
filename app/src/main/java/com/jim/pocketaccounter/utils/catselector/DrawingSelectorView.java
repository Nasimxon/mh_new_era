package com.jim.pocketaccounter.utils.catselector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.utils.GetterAttributColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class DrawingSelectorView extends AbstractSelectorView {
    @Inject SharedPreferences preferences;
    private Bitmap prevButtonBitmap;
    private Bitmap nextButtonBitmap;
    private LinearLayout left, right;
    protected RelativeLayout scene;
    protected ImageView ivPrevious;
    protected ImageView ivNext;
    private int arrowLayoutDefaultWidth;
    protected SelectorAdapter adapter;
    protected Map<Integer, ViewGroup> stack;
    protected int drawnNeighborAmount = 2;

    public DrawingSelectorView(Context context) {
        super(context);
        init();
    }

    public DrawingSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawingSelectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ((PocketAccounterApplication) getContext().getApplicationContext()).component().inject(this);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        prevButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.selector_arrow, options);
        nextButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.selector_arrow, options);
        arrowLayoutDefaultWidth = (int) getResources().getDimension(R.dimen.thirtyfive_dp);
        int margin = (int) getResources().getDimension(R.dimen.fifteen_dp);

        setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams neighborLp = new LinearLayout.LayoutParams(arrowLayoutDefaultWidth, arrowLayoutDefaultWidth);
        neighborLp.gravity = Gravity.CENTER_VERTICAL;
        neighborLp.setMargins(margin, 0, margin, 0);

        ivPrevious = new ImageView(getContext());
        ivPrevious.setLayoutParams(neighborLp);
        ivPrevious.setImageBitmap(prevButtonBitmap);
        ivPrevious.setColorFilter( GetterAttributColors.fetchHeadAccedentColor(getContext()));
        ivPrevious.setRotation(180.0f);

        ivNext = new ImageView(getContext());
        ivNext.setLayoutParams(neighborLp);
        ivNext.setColorFilter( GetterAttributColors.fetchHeadAccedentColor(getContext()));
        ivNext.setImageBitmap(nextButtonBitmap);

        left = new LinearLayout(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        left.setLayoutParams(lp);
        left.addView(ivPrevious);

        right = new LinearLayout(getContext());
        right.setLayoutParams(lp);
        right.addView(ivNext);

        RelativeLayout.LayoutParams sceneLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sceneLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        scene = new RelativeLayout(getContext());
        scene.setLayoutParams(sceneLp);

        addView(left);
        addView(scene);
        addView(right);
        invalidate();
    }
    public ViewGroup getLeftLayout() { return left; }
    public ViewGroup getRightLayout() { return right; }
    public ViewGroup getSceneLayout() { return scene; }

    public void setDrawnNeighborAmount(int drawnNeighborAmount) {
        if (drawnNeighborAmount >= 2)
            this.drawnNeighborAmount = drawnNeighborAmount;
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
