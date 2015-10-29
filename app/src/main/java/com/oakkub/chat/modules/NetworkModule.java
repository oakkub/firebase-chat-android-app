package com.oakkub.chat.modules;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

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

    @Named(FirebaseUtil.NAMED_CURRENT_USER)
    @Provides
    Firebase provideFirebaseCurrentUser(@Named (FirebaseUtil.NAMED_USERS) Firebase firebaseUser, AuthData authData) {
        return firebaseUser.child(FirebaseUtil.KEY_USERS_USER_INFO).child(authData.getUid());
    }

    @Named(FirebaseUtil.NAMED_FRIENDS_OF_USER)
    @Provides
    Firebase provideFirebaseFriends(@Named (FirebaseUtil.NAMED_USERS) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_USERS_USER_FRIENDS);
    }

    @Named(FirebaseUtil.NAMED_USER_ONLINE)
    @Provides
    Firebase provideFirebaseUserOnline(@Named (FirebaseUtil.NAMED_ROOT) Firebase firebase, AuthData authData) {
        return firebase.child(FirebaseUtil.KEY_ONLINE_USER).child(authData.getUid());
    }

    @Named(FirebaseUtil.NAMED_CONNECTION)
    @Provides
    Firebase provideFirebaseConnection(@Named (FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.child(FirebaseUtil.KEY_CONNECTION);
    }

    @Provides
    AuthData provideAuthData(@Named (FirebaseUtil.NAMED_ROOT) Firebase firebase) {
        return firebase.getAuth();
    }

}
