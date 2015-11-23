package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 11/13/2015.
 */
public class Message {

    String messageKey;
    String roomId;
    String message;
    String sentBy;
    String imagePath;
    String thumbnailPath;
    long sentWhen = System.currentTimeMillis();

    public Message() {
    }

    public Message(String roomId, String message, String sentBy) {
        this.roomId = roomId;
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getMessage() {
        return message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public long getSentWhen() {
        return sentWhen;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return messageKey.equals(message.messageKey);
    }

    @Override
    public int hashCode() {
        return messageKey.hashCode();
    }
}
