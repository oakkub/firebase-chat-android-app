package com.oakkub.chat.models.eventbus;

import com.oakkub.chat.models.Room;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 2/2/2016.
 */
public class EventBusPublicRoom {

    public final ArrayList<Room> rooms;

    public EventBusPublicRoom(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

}
