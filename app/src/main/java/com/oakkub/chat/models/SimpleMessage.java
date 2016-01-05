package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 12/31/2015.
 */
public class SimpleMessage {

    public final String message;
    public final long messageTime;
    public final String user;

    public SimpleMessage(String message, long messageTime, String user) {
        this.message = message;
        this.messageTime = messageTime;
        this.user = user;
    }
}
