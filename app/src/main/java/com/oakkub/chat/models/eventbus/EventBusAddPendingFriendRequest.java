package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

/**
 * Created by OaKKuB on 2/23/2016.
 */
public class EventBusAddPendingFriendRequest {

    public final UserInfo userInfo;

    public EventBusAddPendingFriendRequest(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
