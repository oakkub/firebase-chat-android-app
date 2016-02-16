package com.oakkub.chat.utils;

import android.content.Context;

import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;

import java.util.HashMap;

/**
 * Created by OaKKuB on 1/14/2016.
 */
public class RoomUtil {

    public static final String KEY_NAME = "name";
    public static final String KEY_TAG = "tag";

    public static Room get(HashMap<String, Object> roomMap, String roomKey) {
        Room room = new Room();
        room.setRoomId(roomKey);

        String roomName = roomMap.get("name") == null ? null : roomMap.get("name").toString();
        String tag = roomMap.get("tag") == null ? null : roomMap.get("tag").toString();
        String type = roomMap.get("type") == null ? null : roomMap.get("type").toString();
        String description = roomMap.get("description") == null ? null : roomMap.get("description").toString();
        String latestMessage = roomMap.get("latestMessage") == null ? null : roomMap.get("latestMessage").toString();
        String latestMessageUser = roomMap.get("latestMessageUser") == null ? null : roomMap.get("latestMessageUser").toString();
        String imagePath = roomMap.get("imagePath") == null ? null : roomMap.get("imagePath").toString();
        long latestMessageTime = roomMap.get("latestMessageTime") == null ? 0 : (long) roomMap.get("latestMessageTime");
        long created = roomMap.get("created") == null ? 0 : (long) roomMap.get("created");

        if (roomName != null) {
            room.setName(roomName);
        }

        if (tag != null) {
            room.setTag(tag);
        }

        if (type != null) {
            room.setType(type);
        }

        if (description != null) {
            room.setDescription(description);
        }

        if (latestMessage != null) {
            room.setLatestMessage(latestMessage);
        }

        if (latestMessageUser != null) {
            room.setLatestMessageUser(latestMessageUser);
        }

        if (imagePath != null) {
            room.setImagePath(imagePath);
        }

        room.setCreated(created);
        room.setLatestMessageTime(latestMessageTime);

        return room;
    }

    public static String getPrivateRoomKey(Context context, final String userId, final String friendId) {
        return userId.compareTo(friendId) <= 0 ?
                context.getString(R.string.private_room_key, userId, friendId)
                :
                context.getString(R.string.private_room_key, friendId, userId);
    }

    public static String findFriendIdFromPrivateRoom(String myId, String roomId) {
        // find image for private room
        final String[] key = roomId.split("_");
        final String[] users = new String[]{
                key[1], key[2]
        };

        for (String userId : users) {
            if (!userId.equals(myId)) {
                return userId;
            }
        }
        return "";
    }

}
