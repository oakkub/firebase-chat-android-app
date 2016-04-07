package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 3/21/2016.
 */
public class EventBusRemoveGroup {

    public final Room room;

    public EventBusRemoveGroup(Room room) {
        this.room = room;
    }
}
