package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 1/14/2016.
 */
public class EventBusNewPrivateRoomMessage {

    public final Room room;

    public EventBusNewPrivateRoomMessage(Room room) {
        this.room = room;
    }

}
