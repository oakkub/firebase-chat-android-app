package com.oakkub.chat.utils;

/**
 * Created by OaKKuB on 10/22/2015.
 */
public class FirebaseUtil {

    public static final String NAMED_ONLINE_USERS = "firebaseConnecting";
    public static final String NAMED_CONNECTION = "firebaseConnection";
    public static final String NAMED_ROOT = "firebaseRoot";
    public static final String NAMED_USERS = "firebaseUser";
    public static final String NAMED_USER_INFO = "firebaseUserInfo";
    public static final String NAMED_USER_FRIENDS = "firebaseUserFriends";
    public static final String NAMED_USER_ROOMS = "firebaseUserRooms";
    public static final String NAMED_ROOMS = "firebaseRooms";
    public static final String NAMED_MESSAGES = "firebaseMessages";
    public static final String NAMED_ROOMS_INFO = "firebaseRoomsInfo";
    public static final String NAMED_ROOMS_MEMBERS = "firebaseRoomsMembers";

    public static final String FIREBASE_URL = "https://oakkub-chat.firebaseio.com/";
    public static final String FIREBASE_USER_URL = FIREBASE_URL + "users/";
    public static final String FIREBASE_CONNECTION_URL = FIREBASE_URL + ".info/connected";

    public static final String KEY_USERS = "users";
    public static final String KEY_USERS_USER_INFO = "userInfo";
    public static final String KEY_USERS_USER_FRIENDS = "userFriends";
    public static final String KEY_USERS_USER_ROOMS = "userRooms";
    public static final String KEY_USERS_USER_GROUP_ROOMS = "userGroupRooms";
    public static final String KEY_ONLINE_USER = "onlineUsers";
    public static final String KEY_TYPING_USER = "typingUsers";
    public static final String KEY_CONNECTION = ".info/connected";
    public static final String KEY_FRIENDS_OF_USER = "friendUsers";
    public static final String KEY_ROOMS = "rooms";
    public static final String KEY_ROOMS_INFO = "roomsInfo";
    public static final String KEY_ROOMS_MEMBERS = "roomsMembers";
    public static final String KEY_MESSAGES = "messages";

    public static final String CHILD_ONLINE = "online";
    public static final String CHILD_LAST_ONLINE = "lastOnline";
    public static final String CHILD_REGISTERED_DATE = "registeredDate";
    public static final String CHILD_DISPLAY_NAME = "displayName";
    public static final String CHILD_PROFILE_IMAGE_URL = "profileImageURL";
    public static final String CHILD_USER_PRIVATE_ROOMS = "userPrivateRooms";
    public static final String CHILD_USER_GROUP_ROOMS = "userGroupRooms";
    public static final String CHILD_ROOM_CREATED = "created";
    public static final String CHILD_ROOM_MEMBERS = "members";
    public static final String CHILD_ROOM_TYPE = "type";

    public static final String CHILD_SENT_WHEN = "sentWhen";
    public static final String CHILD_ROOM_ID = "roomId";
    public static final String CHILD_MESSAGE = "message";
    public static final String CHILD_MESSAGE_IMAGE_PATH = "imagePath";

    public static final String CHILD_LATEST_MESSAGE = "latestMessage";
    public static final String CHILD_LATEST_MESSAGE_USER = "latestMessageUser";
    public static final String CHILD_LATEST_MESSAGE_TIME = "latestMessageTime";

    public static final String VALUE_ROOM_TYPE_PRIVATE = "private";
    public static final String VALUE_ROOM_TYPE_GROUP = "group";

    public static final String ROOM_ID_STARTER = "chat_";

    public static final String PROVIDER_EMAIL = "email";
    public static final String PROVIDER_PROFILE_IMAGE = "profileImageURL";
    public static final String PROVIDER_DISPLAY_NAME = "displayName";

    public static boolean isFirebaseLogin(String provider) {
        return isEmailLogin(provider) && !(isFacebookLogin(provider) && isGoogleLogin(provider));
    }

    public static boolean isFacebookLogin(String provider) {
        return provider.equals(TextUtil.FACEBOOK_PROVIDER);
    }

    public static boolean isGoogleLogin(String provider) {
        return provider.equals(TextUtil.GOOGLE_PROVIDER);
    }

    public static boolean isEmailLogin(String provider) {
        return provider.equals(TextUtil.EMAIL_PROVIDER);
    }

    public static String privateRoomFriendKey(String myId, String roomKey) {
        // example of room id : chat_facebook:232194892384_google:4545646456445
        String[] splitString = roomKey.split("_");

        for (int i = 1, size = splitString.length; i < size; i++) {
            if (!splitString[i].equals(myId)) {
                return splitString[i];
            }
        }

        return "";
    }
}
