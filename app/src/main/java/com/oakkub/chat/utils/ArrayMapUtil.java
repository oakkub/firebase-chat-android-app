package com.oakkub.chat.utils;

import android.support.v4.util.ArrayMap;

import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;

import java.util.Map;

import static com.oakkub.chat.utils.FirebaseUtil.KEY_MESSAGES;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_MESSAGES_LIST;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_ADMIN_MEMBERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_INFO;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_MEMBERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_PRESERVED_MEMBERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS_USER_GROUP_ROOMS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS_USER_PUBLIC_ROOMS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS_USER_ROOMS;

/**
 * Created by OaKKuB on 1/12/2016.
 */
public class ArrayMapUtil {

    public static void mapPublicRoomList(ArrayMap<String, Object> map, Room room) {
        map.put(TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_PUBLIC, room.getRoomId()), room.getCreated());
    }

    public static void mapPublicRoomList(ArrayMap<String, Object> map, String roomId, long time) {
        map.put(TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_PUBLIC, roomId), time);
    }

    public static void mapRoom(ArrayMap<String, Object> groupRoomMap, Room room, String roomKey) {
        if (room == null) {
            putRoomInfo(groupRoomMap, roomKey, "name", null);
            putRoomInfo(groupRoomMap, roomKey, "imagePath", null);
            putRoomInfo(groupRoomMap, roomKey, "latestMessage", null);
            putRoomInfo(groupRoomMap, roomKey, "latestMessageUser", null);
            putRoomInfo(groupRoomMap, roomKey, "description", null);
            putRoomInfo(groupRoomMap, roomKey, "tag", null);
            putRoomInfo(groupRoomMap, roomKey, "latestMessageTime", null);
            putRoomInfo(groupRoomMap, roomKey, "type", null);
            putRoomInfo(groupRoomMap, roomKey, "created", null);
            return;
        }

        String name = room.getName();
        String imagePath = room.getImagePath();
        String latestMessage = room.getLatestMessage();
        String latestMessageUser = room.getLatestMessageUser();
        String description = room.getDescription();
        String tag = room.getTag();

        if (name != null) {
            putRoomInfo(groupRoomMap, roomKey, "name", name);
        }

        if (imagePath != null) {
            putRoomInfo(groupRoomMap, roomKey, "imagePath", imagePath);
        }

        if (latestMessage != null) {
            putRoomInfo(groupRoomMap, roomKey, "latestMessage", latestMessage);
        }

        if (latestMessageUser != null) {
            putRoomInfo(groupRoomMap, roomKey, "latestMessageUser", latestMessageUser);
        }

        if (description != null) {
            putRoomInfo(groupRoomMap, roomKey, "description", description);
        }

        if (tag != null) {
            putRoomInfo(groupRoomMap, roomKey, "tag", tag);
        }

        putRoomInfo(groupRoomMap, roomKey, "latestMessageTime", room.getLatestMessageTime());
        putRoomInfo(groupRoomMap, roomKey, "type", room.getType());
        putRoomInfo(groupRoomMap, roomKey, "created", room.getCreated());
    }

    private static void putRoomInfo(ArrayMap<String, Object> roomMap, String roomKey, String key, Object value) {
        roomMap.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_INFO, roomKey, key), value);
    }

    public static void mapUsersRoom(ArrayMap<String, Object> map, String[] usersKey, String roomKey, long roomCreated) {
        for (String userKey : usersKey) {
            // put room to user rooms node
            mapUserRoom(map, userKey, roomKey, roomCreated);
        }
    }

    public static void mapNewUsersRoom(ArrayMap<String, Object> map, UserInfo[] usersInfo, String roomKey, long roomCreated, boolean isPrivateRoom, boolean newRoom) {
        for (UserInfo userInfo : usersInfo) {
            String userKey = userInfo.getKey();

            mapUserRoom(map, userKey, roomKey, roomCreated);

            if (!isPrivateRoom) {
                mapUserGroupRoom(map, userKey, roomKey, roomCreated);
            }
        }
    }

    public static void mapUserRoom(ArrayMap<String, Object> map, String userKey, String roomKey, Object roomCreated) {
        map.put(TextUtil.getPath(KEY_USERS, KEY_USERS_USER_ROOMS, userKey, roomKey), roomCreated);
    }

    public static void mapUsersGroupRoom(ArrayMap<String, Object> map, UserInfo[] usersInfo, String roomKey, long roomCreated) {
        for (UserInfo userInfo : usersInfo) {
            mapUserGroupRoom(map, userInfo.getKey(), roomKey, roomCreated);
        }
    }

    public static void mapUserGroupRoom(ArrayMap<String, Object> map, String userKey, String roomKey, Object roomCreated) {
        mapUserRoomMember(map, userKey, roomKey, roomCreated);

        // put room to user group rooms node
        map.put(TextUtil.getPath(KEY_USERS, KEY_USERS_USER_GROUP_ROOMS, userKey, roomKey), roomCreated);
    }

    public static void mapUserPreservedMemberRoom(ArrayMap<String, Object> map, String userKey, String roomKey, Object roomCreated) {
        map.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_PRESERVED_MEMBERS, roomKey, userKey), roomCreated);
    }

    public static void mapUserPublicRoom(ArrayMap<String, Object> map, String userKey, String roomKey, Object roomCreated) {
        mapUserRoomMember(map, userKey, roomKey, roomCreated);
        // put room to user group rooms node
        map.put(TextUtil.getPath(KEY_USERS, KEY_USERS_USER_PUBLIC_ROOMS, userKey, roomKey), roomCreated);
    }

    public static void mapUserRoomMember(ArrayMap<String, Object> map, String userKey, String roomKey, Object when) {
        // put member to Rooms Members node
        map.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_MEMBERS, roomKey, userKey), when);
    }

    public static void mapUserRoomAdminMember(ArrayMap<String, Object> map, String userKey, String roomKey, Object when) {
        map.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_ADMIN_MEMBERS, roomKey, userKey), when);
    }

    public static void mapMessage(ArrayMap<String, Object> map, String messageKey, String roomKey, Message messageRoom) {
        checkMessageMapKey(map, roomKey, messageKey, "imagePath", messageRoom.getImagePath());
        checkMessageMapKey(map, roomKey, messageKey, "ratio", messageRoom.getRatio());
        checkMessageMapKey(map, roomKey, messageKey, "languageRes", messageRoom.getLanguageRes());

        putMessageInfo(map, roomKey, messageKey, "roomId", messageRoom.getRoomId());
        putMessageInfo(map, roomKey, messageKey, "message", messageRoom.getMessage());
        putMessageInfo(map, roomKey, messageKey, "sentBy", messageRoom.getSentBy());
        putMessageInfo(map, roomKey, messageKey, "sentWhen", messageRoom.getSentWhen());
        putMessageInfo(map, roomKey, messageKey, "imagePath", messageRoom.getImagePath());
        putMessageInfo(map, roomKey, messageKey, "ratio", messageRoom.getRatio());
        putMessageInfo(map, roomKey, messageKey, "languageRes", messageRoom.getLanguageRes());
    }

    private static void putMessageInfo(ArrayMap<String, Object> map, String roomKey, String messageKey, String key, Object value) {
        if (value != null) {
            map.put(TextUtil.getPath(KEY_MESSAGES, KEY_MESSAGES_LIST, roomKey, messageKey, key), value);
        }
    }

    private static void checkMessageMapKey(ArrayMap<String, Object> map, String roomKey, String messageKey, String child, String value) {
        if (map.containsKey(TextUtil.getPath(KEY_MESSAGES, KEY_MESSAGES_LIST, messageKey, roomKey, child))
                && value == null) {
            map.remove(TextUtil.getPath(KEY_MESSAGES, KEY_MESSAGES_LIST, messageKey, roomKey, child));
        }
    }

    public static void mapRoomMessage(Map<String, Object> map, Message message, String roomKey) {
        map.put(getRoomInfoPath(roomKey, FirebaseUtil.CHILD_LATEST_MESSAGE), message.getMessage());
        map.put(getRoomInfoPath(roomKey, FirebaseUtil.CHILD_LATEST_MESSAGE_USER), message.getSentBy());
        map.put(getRoomInfoPath(roomKey, FirebaseUtil.CHILD_LATEST_MESSAGE_TIME), message.getSentWhen());
    }

    private static String getRoomInfoPath(String roomKey, String childRoomKey) {
        return TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_INFO, roomKey, childRoomKey);
    }

    public static void mapMessageUserRoom(ArrayMap<String, Object> map, UserInfo[] usersInfo, String roomKey, long time) {
        for (UserInfo userInfo : usersInfo) {
            map.put(getMessageUserRoomPath(userInfo.getKey(), roomKey), time);
        }
    }

    private static String getMessageUserRoomPath(String userId, String roomKey) {
        return TextUtil.getPath(FirebaseUtil.KEY_USERS, FirebaseUtil.KEY_USERS_USER_ROOMS, userId, roomKey);
    }

}
