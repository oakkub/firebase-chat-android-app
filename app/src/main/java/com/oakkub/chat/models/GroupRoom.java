package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 11/15/2015.
 */
public class GroupRoom extends Room {

    String roomImage;

    public GroupRoom() {
    }

    public GroupRoom(String roomName, String roomImage, String type) {
        super(type);

        this.name = roomName;
        this.roomImage = roomImage;
    }

    public String getRoomImage() {
        return roomImage;
    }

}
