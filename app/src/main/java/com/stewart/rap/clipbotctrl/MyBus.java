package com.stewart.rap.clipbotctrl;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class MyBus extends Bus {

    private Handler handler = new Handler(Looper.getMainLooper());

    public MyBus() { super(ThreadEnforcer.ANY); }

    @Override public void post(final Object event) {

        if (Looper.myLooper() == Looper.getMainLooper())
            super.post(event);
        else
            handler.post(() -> { MyBus.super.post(event); });
    }
}