package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 2/19/2016.
 */
public class EventBusDeleteGroupRoom {

    public final Room room;

    public EventBusDeleteGroupRoom(Room room) {
        this.room = room;
    }
}
