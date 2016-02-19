package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 2/19/2016.
 */
public class EventBusDeletePublicChat {

    public final Room room;

    public EventBusDeletePublicChat(Room room) {
        this.room = room;
    }
}
