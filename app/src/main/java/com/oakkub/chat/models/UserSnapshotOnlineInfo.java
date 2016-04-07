package com.oakkub.chat.models;

import com.firebase.client.ServerValue;

import java.util.Map;

/**
 * Created by OaKKuB on 1/6/2016.
 */
public class UserSnapshotOnlineInfo {

    private boolean online;
    private Map<String, String> lastOnline;

    public UserSnapshotOnlineInfo() {}

    public UserSnapshotOnlineInfo(boolean online) {
        this.online = online;
        this.lastOnline = ServerValue.TIMESTAMP;
    }

    public boolean isOnline() {
        return online;
    }

    public Map<String, String> getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Map<String, String> lastOnline) {
        this.lastOnline = lastOnline;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
