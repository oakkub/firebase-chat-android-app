package com.oakkub.chat.managers;

/**
 * Created by OaKKuB on 2/17/2016.
 */
public class BitmapManager {

    private static BitmapManager bitmapManager;

    public static BitmapManager getInstance() {
        if (bitmapManager == null) bitmapManager = new BitmapManager();
        return bitmapManager;
    }

    private BitmapManager() {}


}
