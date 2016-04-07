package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 3/21/2016.
 */
public class EventBusRemovePublicChat {

    public final Room room;

    public EventBusRemovePublicChat(Room room) {
        this.room = room;
    }
}
