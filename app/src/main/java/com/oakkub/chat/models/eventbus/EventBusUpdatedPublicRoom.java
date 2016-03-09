package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 2/20/2016.
 */
public class EventBusUpdatedPublicRoom {

    public final Room room;

    public EventBusUpdatedPublicRoom(Room room) {
        this.room = room;
    }
}
