package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 10/12/2015.
 */
@Parcel
public class UserInfo {

    public static final int ME = 0;
    public static final int FRIEND = 1;
    public static final int ADD_FRIEND = 2;

    public static final String EMAIL = "email";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PROFILE_IMAGE_URL = "profileImageURL";
    public static final String REGISTERED_DATE = "registeredDate";

    String email;
    String displayName;
    String profileImageURL;
    String userKey;
    String instanceID;
    long registeredDate;
    int type;

    public UserInfo() {}

    public UserInfo(String email, String displayName, String profileImageURL) {
        this.email = email;
        this.displayName = displayName;
        this.profileImageURL = profileImageURL;
        this.type = ME;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        return getUserKey().equals(userInfo.getUserKey());
    }

    @Override
    public int hashCode() {
        return getUserKey().hashCode();
    }
}
