package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

/**
 * Created by OaKKuB on 2/23/2016.
 */
public class EventBusExistsFriendRequest {

    public final UserInfo userInfo;

    public EventBusExistsFriendRequest(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
