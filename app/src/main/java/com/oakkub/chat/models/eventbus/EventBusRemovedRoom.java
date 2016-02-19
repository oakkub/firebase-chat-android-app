package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 2/12/2016.
 */
public class EventBusRemovedRoom {

    public final Room room;

    public EventBusRemovedRoom(Room room) {
        this.room = room;
    }

}
