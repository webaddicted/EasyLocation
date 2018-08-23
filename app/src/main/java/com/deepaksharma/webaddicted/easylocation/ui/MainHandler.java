package com.deepaksharma.webaddicted.easylocation.ui;

import android.view.View;

/**
 * Created by deepaksharma on 23/8/18.
 */

public class MainHandler {
    MainListener mMainListener;

    public MainHandler(MainListener mainListener) {
        this.mMainListener = mainListener;
    }

    public void onBaseLocation(View v){
        mMainListener.onBaseListener();
    }

    public void onCustomLocation(View v){
        mMainListener.onCustomListener();
    }
}
