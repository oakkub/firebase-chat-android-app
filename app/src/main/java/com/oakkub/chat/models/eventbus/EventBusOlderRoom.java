package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 12/9/2015.
 */
public class EventBusOlderRoom {

    public final Room room;
    public final long latestActiveTime;

    public EventBusOlderRoom(Room room, long latestActiveTime) {
        this.room = room;
        this.latestActiveTime = latestActiveTime;
    }

}
