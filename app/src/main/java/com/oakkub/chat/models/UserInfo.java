package com.oakkub.chat.models;

import org.parceler.Parcel;

/**
 * Created by OaKKuB on 10/12/2015.
 */
@Parcel
public class UserInfo {

    String email;
    String displayName;
    String profileImageURL;
    String userKey;
    String instanceID;
    long registeredDate;

    public UserInfo() {}

    public UserInfo(String email, String displayName, String profileImageURL) {
        this.email = email;
        this.displayName = displayName;
        this.profileImageURL = profileImageURL;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFirstDisplayName() {
        return displayName.split(" ")[0];
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getKey() {
        return userKey;
    }

    public void setKey(String userKey) {
        this.userKey = userKey;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public void setRegisteredDate(long registeredDate) {
        this.registeredDate = registeredDate;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        return getKey().equals(userInfo.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", profileImageURL='" + profileImageURL + '\'' +
                ", userKey='" + userKey + '\'' +
                ", instanceID='" + instanceID + '\'' +
                ", registeredDate=" + registeredDate +
                '}';
    }
}
