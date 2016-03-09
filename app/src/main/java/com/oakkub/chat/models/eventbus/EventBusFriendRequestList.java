package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.UserInfo;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 2/23/2016.
 */
public class EventBusFriendRequestList {

    public final ArrayList<UserInfo> userInfoList;
    public final int totalFetched;

    public EventBusFriendRequestList(ArrayList<UserInfo> userInfoList, int totalFetched) {
        this.userInfoList = userInfoList;
        this.totalFetched = totalFetched;
    }
}
