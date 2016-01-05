package com.oakkub.chat.models;

import java.util.List;

/**
 * Created by OaKKuB on 12/9/2015.
 */
public class EventBusFriendListInfo {

    public final List<UserInfo> friendListInfo;

    public EventBusFriendListInfo(List<UserInfo> friendListInfo) {
        this.friendListInfo = friendListInfo;
    }

}
