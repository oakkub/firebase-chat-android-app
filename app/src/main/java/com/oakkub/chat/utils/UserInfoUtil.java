package com.oakkub.chat.utils;

import com.firebase.client.DataSnapshot;
import com.oakkub.chat.models.UserInfo;

import java.util.HashMap;

/**
 * Created by OaKKuB on 2/2/2016.
 */
public class UserInfoUtil {

    public static final int ME = 0;
    public static final int FRIEND = 1;
    public static final int ADD_FRIEND = 2;

    public static final String EMAIL = "email";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PROFILE_IMAGE_URL = "profileImageURL";
    public static final String REGISTERED_DATE = "registeredDate";
    public static final String INSTANCE_ID = "instanceID";

    public static UserInfo get(String itemKey, HashMap<String, Object> itemMap) {
        UserInfo userInfo = new UserInfo(itemMap.get(EMAIL).toString(),
                itemMap.get(DISPLAY_NAME).toString(),
                itemMap.get(PROFILE_IMAGE_URL).toString());
        userInfo.setKey(itemKey);
        userInfo.setInstanceID(itemMap.get(INSTANCE_ID).toString());
        userInfo.setRegisteredDate((Long) itemMap.get(REGISTERED_DATE));

        return userInfo;
    }

    public static UserInfo get(String itemKey, DataSnapshot dataSnapshot) {
        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
        userInfo.setKey(itemKey);

        return userInfo;
    }

}
