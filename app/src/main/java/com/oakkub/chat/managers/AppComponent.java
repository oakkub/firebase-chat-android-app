package com.oakkub.chat.managers;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import com.firebase.client.Firebase;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.fragments.AddFriendActivityFragment;
import com.oakkub.chat.fragments.AuthenticationActivityFragment;
import com.oakkub.chat.fragments.EmailLoginFragment;
import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.modules.AnimationModule;
import com.oakkub.chat.modules.AppControllerModule;
import com.oakkub.chat.modules.NetworkModule;
import com.oakkub.chat.modules.StorageModule;
import com.oakkub.chat.modules.SystemServiceModule;
import com.oakkub.chat.utils.AnimateUtil;
import com.oakkub.chat.utils.FirebaseUtil;

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
    void inject(AuthenticationActivityFragment authenticationActivityFragment);
    void inject(EmailLoginFragment emailLoginActivityFragment);
    void inject(FriendsFragment friendsFragment);
    void inject(AddFriendActivityFragment addFriendActivityFragment);

    Application application();
    Context context();

    InputMethodManager inputMethodManager();
    ConnectivityManager connectivityManager();

    @Named(AnimateUtil.ALPHA)
    Animation alphaAnimation();

    @Named(AnimateUtil.SCALE_ALPHA)
    Animation scaleAlphaAnimation();

    @Named(AnimateUtil.SCALE_UP)
    Animation scaleUpAnimation();

    @Named(AnimateUtil.SCALE_DOWN)
    Animation scaleDownAnimation();

    @Named(FirebaseUtil.NAMED_CURRENT_USER)
    Firebase currentUserFirebase();

    @Named(FirebaseUtil.NAMED_USER_ONLINE)
    Firebase onlineUserFirebase();

}
