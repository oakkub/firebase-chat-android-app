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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getLatestMessageUser() {
        return latestMessageUser;
    }

    public void setLatestMessageUser(String latestMessageUser) {
        this.latestMessageUser = latestMessageUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLatestMessageTime() {
        return latestMessageTime;
    }

    public void setLatestMessageTime(long latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return roomId.equals(room.roomId);
    }

    public boolean fullEquals(Object o) {
        if (!equals(o)) return false;

        Room room = (Room) o;

        if (latestMessageTime != room.latestMessageTime) return false;
        if (created != room.created) return false;
        if (roomId != null ? !roomId.equals(room.roomId) : room.roomId != null) return false;
        if (name != null ? !name.equals(room.name) : room.name != null) return false;
        if (description != null ? !description.equals(room.description) : room.description != null)
            return false;
        if (tag != null ? !tag.equals(room.tag) : room.tag != null) return false;
        if (imagePath != null ? !imagePath.equals(room.imagePath) : room.imagePath != null)
            return false;
        if (latestMessage != null ? !latestMessage.equals(room.latestMessage) : room.latestMessage != null)
            return false;
        if (latestMessageUser != null ? !latestMessageUser.equals(room.latestMessageUser) : room.latestMessageUser != null)
            return false;
        return type != null ? type.equals(room.type) : room.type == null;
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
