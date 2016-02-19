package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

import java.util.List;

/**
 * Created by OaKKuB on 12/25/2015.
 */
public class EventBusNewMessagesFriendInfo {

    public final List<UserInfo> friendListInfo;

    public EventBusNewMessagesFriendInfo(List<UserInfo> friendListInfo) {
        this.friendListInfo = friendListInfo;
    }

}
