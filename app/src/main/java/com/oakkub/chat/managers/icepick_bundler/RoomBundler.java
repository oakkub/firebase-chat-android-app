package com.oakkub.chat.managers.icepick_bundler;

import android.os.Bundle;

import com.oakkub.chat.models.Room;

import org.parceler.Parcels;

import icepick.Bundler;

/**
 * Created by OaKKuB on 3/17/2016.
 */
public class RoomBundler implements Bundler<Room> {

    @Override
    public void put(String key, Room room, Bundle bundle) {
        bundle.putParcelable(key, Parcels.wrap(room));
    }

    @Override
    public Room get(String key, Bundle bundle) {
        return Parcels.unwrap(bundle.getParcelable(key));
    }
}
