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
    String ratio;
    String isSuccessfullySent;
    String languageRes;
    int readTotal;
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

    public String getKey() {
        return messageKey;
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

    public long getSentWhen() {
        return sentWhen;
    }

    public void setKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public void successfullySent() {
        this.isSuccessfullySent = "";
    }

    public String getIsSuccessfullySent() {
        return isSuccessfullySent;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public int getReadTotal() {
        return readTotal;
    }

    public void setReadTotal(int readTotal) {
        this.readTotal = readTotal;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguageRes() {
        return languageRes;
    }

    public void setLanguageRes(String languageRes) {
        this.languageRes = languageRes;
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

    @Override
    public String toString() {
        return "Message{" +
                "messageKey='" + messageKey + '\'' +
                ", roomId='" + roomId + '\'' +
                ", message='" + message + '\'' +
                ", sentBy='" + sentBy + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", ratio='" + ratio + '\'' +
                ", isSuccessfullySent='" + isSuccessfullySent + '\'' +
                ", languageRes='" + languageRes + '\'' +
                ", readTotal=" + readTotal +
                ", sentWhen=" + sentWhen +
                '}';
    }
}
