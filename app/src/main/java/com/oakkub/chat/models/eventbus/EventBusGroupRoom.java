package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class EventBusGroupRoom {

    public final ArrayList<Room> roomList;

    public EventBusGroupRoom(ArrayList<Room> roomList) {
        this.roomList = roomList;
    }

}
