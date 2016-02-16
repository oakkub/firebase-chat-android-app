package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 1/6/2016.
 */
public class UserOnlineInfo {

    private boolean online;
    private long lastOnline;

    public UserOnlineInfo() {}

    public UserOnlineInfo(boolean online, long lastOnline) {
        this.online = online;
        this.lastOnline = lastOnline;
    }

    public boolean isOnline() {
        return online;
    }

    public long getLastOnline() {
        return lastOnline;
    }
}
