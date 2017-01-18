package com.jim.finansia.utils.catselector;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;

import java.util.Iterator;
import java.util.Map;


public class SelectorView extends DrawingSelectorView{

    private OnItemSelectedListener listener;
    private Animation inFromLeft, inFromRight, outToLeft, outToRight;
    private View last, current;
    private boolean right = false;
    private float oldX = 0;
    private boolean moved = false;

    public SelectorView(Context context) {
        super(context);
        init();
    }

    public SelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SelectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inFromLeft = AnimationUtils.loadAnimation(getContext(), R.anim.in_from_left);
        inFromRight = AnimationUtils.loadAnimation(getContext(), R.anim.in_from_right);
        outToLeft = AnimationUtils.loadAnimation(getContext(), R.anim.out_to_left);
        outToRight = AnimationUtils.loadAnimation(getContext(), R.anim.out_to_right);
        ivPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count <= 1) return;
                right = false;
                decPosition();
                if (listener != null)
                    listener.onItemSelected(position);
                doTransition();
            }
        });
        ivNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count <= 1) return;
                right = true;
                incPosition();
                if (listener != null)
                    listener.onItemSelected(position);
                doTransition();
            }
        });
        scene.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    oldX = event.getX();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (oldX + 200 < event.getX()) {
                        if (count <= 1) return false;
                        right = false;
                        decPosition();
                        if (listener != null)
                            listener.onItemSelected(position);
                        doTransition();
                    } else if (oldX - 200 > event.getX()) {
                        if(count <= 1) return false;
                        right = true;
                        incPosition();
                        if (listener != null)
                            listener.onItemSelected(position);
                        doTransition();
                    }
                }
                return true;
            }
        });
    }

    private void doTransition() {
        last = stack.get(lastPosition);
        current = stack.get(position);
        scene.addView(current);
        if (!right) {
            last.startAnimation(outToRight);
            current.startAnimation(inFromLeft);
            current.setVisibility(VISIBLE);
            last.setVisibility(GONE);
            scene.removeView(last);
        }
        else {
            last.startAnimation(outToLeft);
            current.startAnimation(inFromRight);
            current.setVisibility(VISIBLE);
            last.setVisibility(GONE);
            scene.removeView(last);
        }
        int leftBorder = position - 2;
        int rightBorder = position + 2;
        if (rightBorder > count-1) rightBorder = rightBorder%count;
        else if (leftBorder < 0) leftBorder = count + leftBorder;
        if (stack.get(leftBorder) == null ||
                stack.get(rightBorder) == null)
            instantiateStack();
    }

    public void setSelection(int position) {
        scene.removeAllViews();
        this.position = position;
        instantiateStack();
        if (stack.get(this.position) == null) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout oneScreen = new LinearLayout(getContext());
            oneScreen.setLayoutParams(lp);
            stack.put(position, adapter.getInstantiatedView(position, oneScreen));
        }
        scene.addView(stack.get(this.position));
    }

    protected void instantiateStack() {
        if (count <= 10 ) return;
        Iterator it = stack.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ViewGroup> item = (Map.Entry) it.next();
            if (item.getKey() != position)
                it.remove();
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int i = position+1; i <= position+drawnNeighborAmount; i++) {
            int realPos = i%count;
            LinearLayout oneScreen = new LinearLayout(getContext());
            oneScreen.setLayoutParams(lp);
            ViewGroup viewGroup = adapter.getInstantiatedView(realPos, oneScreen);
            stack.put(realPos, viewGroup);
        }
        for (int i = position-drawnNeighborAmount; i < position; i++) {
            int realPos = i;
            if (i > count-1) realPos = i%count;
            else if (i < 0) realPos = count+i;
            LinearLayout oneScreen = new LinearLayout(getContext());
            oneScreen.setLayoutParams(lp);
            ViewGroup viewGroup = adapter.getInstantiatedView(realPos, oneScreen);
            stack.put(realPos, viewGroup);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setAnimations(Animation inFromLeft, Animation inFromRight, Animation outToLeft, Animation outToRight) {
        this.inFromLeft = inFromLeft;
        this.inFromRight = inFromRight;
        this.outToLeft = outToLeft;
        this.outToRight = outToRight;
    }
}
