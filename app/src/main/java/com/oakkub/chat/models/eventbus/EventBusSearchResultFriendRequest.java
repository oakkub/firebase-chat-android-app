package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 2/25/2016.
 */
public class EventBusSearchResultFriendRequest {

    public final ArrayList<UserInfo> userInfoResultList;

    public EventBusSearchResultFriendRequest(ArrayList<UserInfo> userInfoResultList) {
        this.userInfoResultList = userInfoResultList;
    }
}
