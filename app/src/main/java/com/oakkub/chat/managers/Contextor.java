package com.oakkub.chat.managers;

import android.content.Context;

/**
 * Created by OaKKuB on 2/5/2016.
 */
public class Contextor {

    private static Contextor contextor;
    private Context context;

    public static Contextor getInstance() {
        if (contextor == null) contextor = new Contextor();
        return contextor;
    }

    private Contextor() {}

    public Context getContext() {
        return context;
    }

    public void init(Context context) {
        this.context = context;
    }

}
