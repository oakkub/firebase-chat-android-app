package com.oakkub.chat.receivers;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by OaKKuB on 10/18/2015.
 */
@SuppressLint("ParcelCreator")
public class GoogleResultReceiver extends ResultReceiver {

    public static final String TAG = GoogleResultReceiver.class.getSimpleName();

    private Receiver receiver;

    public GoogleResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

}
