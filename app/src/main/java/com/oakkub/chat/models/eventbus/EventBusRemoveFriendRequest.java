package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

/**
 * Created by OaKKuB on 2/24/2016.
 */
public class EventBusRemoveFriendRequest {

    public final UserInfo userInfo;

    public EventBusRemoveFriendRequest(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
