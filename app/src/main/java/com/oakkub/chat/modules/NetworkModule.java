package com.oakkub.chat.modules;

import com.firebase.client.Firebase;
import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Module
public class NetworkModule {

    @Named(FirebaseUtil.NAMED_ROOT)
    @Provides
    Firebase provideFirebase() {
        return new Firebase(FirebaseUtil.FIREBASE_URL);
    }

    @Named(FirebaseUtil.NAMED_USERS)
    @Provides
    Firebase provideFirebaseUser(@Named (FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS);
    }

    @Named(FirebaseUtil.NAMED_USER_INFO)
    @Provides
    Firebase provideFirebaseUserInfo(@Named (FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_INFO);
    }

    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    @Provides
    Firebase provideFirebaseFriends(@Named(FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_FRIENDS);
    }

    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    @Provides
    Firebase provideFirebaseUserRooms(@Named(FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_ROOMS);
    }

    @Named(FirebaseUtil.NAMED_USER_GROUPS)
    @Provides
    Firebase provideFirebaseUserGroups(@Named(FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_GROUP_ROOMS);
    }

    @Named(FirebaseUtil.NAMED_USER_PUBLIC)
    @Provides
    Firebase provideFirebaseUserPublic(@Named(FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_PUBLIC_ROOMS);
    }

    @Named(FirebaseUtil.NAMED_ONLINE_USERS)
    @Provides
    Firebase provideFirebaseUserOnline(@Named(FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ONLINE_USER);
    }

    @Named(FirebaseUtil.NAMED_CONNECTION)
    @Provides
    Firebase provideFirebaseConnection(@Named (FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_CONNECTION);
    }

    @Named(FirebaseUtil.NAMED_ROOMS)
    @Provides
    Firebase provideFirebaseRooms(@Named(FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS);
    }

    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    @Provides
    Firebase provideFirebasePrivateRooms(@Named(FirebaseUtil.NAMED_ROOMS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS_INFO);
    }

    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    @Provides
    Firebase provideFirebaseGroupRooms(@Named(FirebaseUtil.NAMED_ROOMS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS_MEMBERS);
    }

    @Named(FirebaseUtil.NAMED_ROOMS_ADMIN_MEMBERS)
    @Provides
    Firebase provideFirebaseAdminMembers(@Named(FirebaseUtil.NAMED_ROOMS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS_ADMIN_MEMBERS);
    }

    @Named(FirebaseUtil.NAMED_ROOMS_PRESERVED_MEMBERS)
    @Provides
    Firebase provideFirebasePreservedMembers(@Named(FirebaseUtil.NAMED_ROOMS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS_PRESERVED_MEMBERS);
    }

    @Named(FirebaseUtil.NAMED_ROOMS_PUBLIC)
    @Provides
    Firebase provideFirebasePublicRooms(@Named(FirebaseUtil.NAMED_ROOMS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_ROOMS_PUBLIC);
    }

    @Named(FirebaseUtil.NAMED_MESSAGES)
    @Provides
    Firebase provideFirebaseMessages(@Named(FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_MESSAGES);
    }

    @Named(FirebaseUtil.NAMED_MESSAGES_LIST)
    @Provides
    Firebase provideFirebaseMessagesList(@Named(FirebaseUtil.NAMED_MESSAGES) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_MESSAGES_LIST);
    }

    @Named(FirebaseUtil.NAMED_MESSAGES_READ_GROUP_ROOM)
    @Provides
    Firebase provideFirebaseTotalReadGroupRoom(@Named(FirebaseUtil.NAMED_MESSAGES) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_MESSAGES_READ_GROUP_ROOM);
    }

    @Named(FirebaseUtil.NAMED_MESSAGES_TYPING)
    @Provides
    Firebase provideFirebaseMessagesTyping(@Named(FirebaseUtil.NAMED_MESSAGES) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_MESSAGES_TYPING);
    }

    @PerApp
    @Provides
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient().newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
