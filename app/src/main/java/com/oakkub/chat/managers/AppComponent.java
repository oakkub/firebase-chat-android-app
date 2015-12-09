package com.oakkub.chat.managers;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import com.firebase.client.Firebase;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.activities.PrivateChatRoomActivity;
import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.fragments.AddFriendActivityFragment;
import com.oakkub.chat.fragments.AuthenticationActivityFragment;
import com.oakkub.chat.fragments.EmailLoginFragment;
import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.fragments.PrivateChatRoomActivityFragment;
import com.oakkub.chat.fragments.RoomListFetchingFragment;
import com.oakkub.chat.fragments.RoomListFragment;
import com.oakkub.chat.modules.AnimationModule;
import com.oakkub.chat.modules.AppControllerModule;
import com.oakkub.chat.modules.NetworkModule;
import com.oakkub.chat.modules.StorageModule;
import com.oakkub.chat.modules.SystemServiceModule;
import com.oakkub.chat.utils.AnimateUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;

import dagger.Component;


/**
 * Created by OaKKuB on 10/22/2015.
 */
@PerApp
@Component(
        modules = {
                AppControllerModule.class,
                AnimationModule.class,
                NetworkModule.class,
                StorageModule.class,
                SystemServiceModule.class
        }
)
public interface AppComponent {

    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);

    void inject(PrivateChatRoomActivity privateChatRoomActivity);

    void inject(FriendDetailActivity friendDetailActivity);
    void inject(AuthenticationActivityFragment authenticationActivityFragment);
    void inject(EmailLoginFragment emailLoginActivityFragment);
    void inject(FriendsFragment friendsFragment);
    void inject(AddFriendActivityFragment addFriendActivityFragment);

    void inject(PrivateChatRoomActivityFragment privateChatRoomActivityFragment);

    void inject(RoomListFragment roomListFragment);

    void inject(RoomListFetchingFragment roomListFetchingFragment);

    Application application();
    Context context();

    InputMethodManager inputMethodManager();
    ConnectivityManager connectivityManager();

    NotificationManager notificationManager();

    @Named(AnimateUtil.ALPHA)
    Animation alphaAnimation();

    @Named(AnimateUtil.SCALE_ALPHA)
    Animation scaleAlphaAnimation();

    @Named(AnimateUtil.SCALE_UP)
    Animation scaleUpAnimation();

    @Named(AnimateUtil.SCALE_DOWN)
    Animation scaleDownAnimation();

    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase();

    OkHttpClient okHttpClient();

    SharedPreferences sharedPreferences();

    SharedPreferences.Editor sharedPreferencesEditor();

    DefaultItemAnimator defaultItemAnimator();
}
