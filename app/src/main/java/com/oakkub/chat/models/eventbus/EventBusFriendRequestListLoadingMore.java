package com.oakkub.chat.models.eventbus;

/**
 * Created by OaKKuB on 3/2/2016.
 */
public class EventBusFriendRequestListLoadingMore {

    public final long lastRegisteredDate;

    public EventBusFriendRequestListLoadingMore(long lastRegisteredDate) {
        this.lastRegisteredDate = lastRegisteredDate;
    }
}
