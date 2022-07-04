package com.wlj.butterknife;


import androidx.annotation.UiThread;

/**
 * Created by hcDarren on 2017/9/9.
 */

public interface Unbinder {
    @UiThread
    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() {
        }
    };
}
