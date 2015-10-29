package com.oakkub.chat.utils;

/**
 * Created by OaKKuB on 10/22/2015.
 */
public class FirebaseUtil {

    public static final String NAMED_USER_ONLINE = "firebaseConnecting";
    public static final String NAMED_CONNECTION = "firebaseConnection";
    public static final String NAMED_CURRENT_USER = "firebaseLoggedInUser";
    public static final String NAMED_ROOT = "firebaseRoot";
    public static final String NAMED_USERS = "firebaseUser";
    public static final String NAMED_USER_INFO = "firebaseUserInfo";
    public static final String NAMED_FRIENDS_OF_USER = "firebaseUserFriends";

    public static final String FIREBASE_URL = "https://oakkub-chat.firebaseio.com/";
    public static final String FIREBASE_USER_URL = FIREBASE_URL + "users/";
    public static final String FIREBASE_CONNECTION_URL = FIREBASE_URL + ".info/connected";

    public static final String KEY_USERS = "users";
    public static final String KEY_USERS_USER_INFO = "userInfo";
    public static final String KEY_USERS_USER_FRIENDS = "userFriends";
    public static final String KEY_USERS_USER_ROOMS = "userRooms";
    public static final String KEY_ROOMS = "rooms";
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_ONLINE_USER = "onlineUsers";
    public static final String KEY_TYPING_USER = "typingUsers";
    public static final String KEY_CONNECTION = ".info/connected";
    public static final String KEY_FRIENDS_OF_USER = "friendUsers";

    public static final String CHILD_ONLINE = "online";
    public static final String CHILD_LAST_ONLINE = "lastOnline";
    public static final String CHILD_REGISTERED_DATE = "registeredDate";

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
}
