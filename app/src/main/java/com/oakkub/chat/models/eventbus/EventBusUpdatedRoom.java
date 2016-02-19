package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

/**
 * Created by OaKKuB on 12/9/2015.
 */
public class EventBusUpdatedRoom {

    public final Room room;
    public final long latestActiveTime;

    public EventBusUpdatedRoom(Room room, long latestActiveTime) {
        this.room = room;
        this.latestActiveTime = latestActiveTime;
    }

}
