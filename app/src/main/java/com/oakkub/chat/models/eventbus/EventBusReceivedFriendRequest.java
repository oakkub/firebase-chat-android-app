package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

/**
 * Created by OaKKuB on 2/22/2016.
 */
public class EventBusReceivedFriendRequest {

    public final UserInfo userInfo;

    public EventBusReceivedFriendRequest(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
