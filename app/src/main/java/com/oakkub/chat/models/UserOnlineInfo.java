package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 1/6/2016.
 */
public class UserOnlineInfo {

    private boolean online;
    private long lastOnline;

    public UserOnlineInfo() {}

    public boolean isOnline() {
        return online;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
