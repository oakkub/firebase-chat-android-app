package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 10/26/2015.
 */
@Parcel
public class Friend {

    String displayName;
    String profileImageURL;

    public Friend() {}

    public Friend(String displayName, String profileImageURL) {
        this.displayName = displayName;
        this.profileImageURL = profileImageURL;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }
}
