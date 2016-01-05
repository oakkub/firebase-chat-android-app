package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 11/13/2015.
 */
@Parcel
public class Message {

    String messageKey;
    String roomId;
    String message;
    String sentBy;
    String imagePath;
    String thumbnailPath;
    boolean showImage = true;
    long sentWhen = System.currentTimeMillis();

    public Message() {
    }

    public Message(String roomId, String message, String sentBy) {
        this.roomId = roomId;
        this.message = message;
        this.sentBy = sentBy;
    }

    public Message(String roomId, String message, String sentBy, long sentWhen) {
        this(roomId, message, sentBy);
        this.sentWhen = sentWhen;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
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

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }

    public long getSentWhen() {
        return sentWhen;
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
