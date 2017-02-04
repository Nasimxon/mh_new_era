package com.jim.finansia.utils;

import com.transitionseverywhere.Transition;

/**
 * Created by developer on 03.02.2017.
 */

public class TransactionEnd implements Transition.TransitionListener {
    public interface Listner{
        void onTransitionEnd();
    }
    Listner listner;
    public TransactionEnd(Listner listner){
        this.listner = listner;
    }
    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
        listner.onTransitionEnd();
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }
}
