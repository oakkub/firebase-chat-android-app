package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 11/10/2015.
 */
@Parcel
public class Room {

    String roomId;
    String name;
    String description;
    String tag;
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

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public void setLatestMessageUser(String latestMessageUser) {
        this.latestMessageUser = latestMessageUser;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatestMessageTime(long latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tag='" + tag + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", latestMessage='" + latestMessage + '\'' +
                ", latestMessageUser='" + latestMessageUser + '\'' +
                ", type='" + type + '\'' +
                ", latestMessageTime=" + latestMessageTime +
                ", created=" + created +
                '}';
    }
}
