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

    public static String EMAIL = "email";
    public static String DISPLAY_NAME = "displayName";
    public static String PROFILE_IMAGE_URL = "profileImageURL";
    public static String REGISTERED_DATE = "registeredDate";

    String email;
    String displayName;
    String profileImageURL;
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

    public int getType() {
        return type;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }

    public void setType(int type) {
        this.type = type;
    }
}
