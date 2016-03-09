package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 2/20/2016.
 */
public class EventBusUpdatedGroupRoom {

    public final Room room;

    public EventBusUpdatedGroupRoom(Room room) {
        this.room = room;
    }
}
