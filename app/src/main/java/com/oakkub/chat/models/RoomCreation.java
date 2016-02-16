package com.oakkub.chat.models;

import android.net.Uri;

/**
 * Created by OaKKuB on 2/16/2016.
 */
public class RoomCreation {

    public final Room room;
    public final Uri uriImage;
    public final String absolutePath;

    public RoomCreation(Room room, Uri uriImage, String absolutePath) {
        this.room = room;
        this.uriImage = uriImage;
        this.absolutePath = absolutePath;
    }
}
