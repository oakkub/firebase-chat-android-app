package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 11/10/2015.
 */
@Parcel
public class Room {

    String roomId;
    String name;
    String imagePath;
    String latestMessage;
    String latestMessageUser;
    String type;
    long latestMessageTime;
    long created;

    public Room() {
    }

    public Room(String type) {
        this.type = type;
        created = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setRoomName(String name) {
        this.name = name;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public String getLatestMessageUser() {
        return latestMessageUser;
    }

    public String getType() {
        return type;
    }

    public long getCreated() {
        return created;
    }

    public long getLatestMessageTime() {
        return latestMessageTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setRoomImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return roomId.equals(room.roomId);

    }

    @Override
    public int hashCode() {
        return roomId.hashCode();
    }
}
