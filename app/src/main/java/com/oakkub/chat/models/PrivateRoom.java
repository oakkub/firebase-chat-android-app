package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 11/17/2015.
 */
public class PrivateRoom extends Room {

    String user1;
    String user2;

    public PrivateRoom() {
    }

    public PrivateRoom(String user1, String user2, String type) {
        super(type);

        this.user1 = user1;
        this.user2 = user2;
    }

    public String getUser1() {
        return user1;
    }

    public String getUser2() {
        return user2;
    }

}
