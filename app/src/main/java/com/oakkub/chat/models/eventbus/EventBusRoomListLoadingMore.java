package com.oakkub.chat.models.eventbus;

/**
 * Created by OaKKuB on 3/4/2016.
 */
public class EventBusRoomListLoadingMore {

    public final long oldestTime;

    public EventBusRoomListLoadingMore(long oldestTime) {
        this.oldestTime = oldestTime;
    }
}
