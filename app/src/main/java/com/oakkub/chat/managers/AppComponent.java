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
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.activities.NewMessagesActivity;
import com.oakkub.chat.activities.ProfileActivity;
import com.oakkub.chat.activities.RoomEditActivity;
import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.fragments.AddAdminFragment;
import com.oakkub.chat.fragments.AddFriendFragment;
import com.oakkub.chat.fragments.AuthStateFragment;
import com.oakkub.chat.fragments.AuthenticationFragment;
import com.oakkub.chat.fragments.ChatRoomFragment;
import com.oakkub.chat.fragments.CreatePrivateRoomFragment;
import com.oakkub.chat.fragments.EmailLoginFragment;
import com.oakkub.chat.fragments.FriendsFetchingFragment;
import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.fragments.GroupListFetchingFragment;
import com.oakkub.chat.fragments.InviteMemberFragment;
import com.oakkub.chat.fragments.IsNodeExistsFirebaseFragment;
import com.oakkub.chat.fragments.KeyFirebaseFetchingFragment;
import com.oakkub.chat.fragments.KeyToValueFirebaseFetchingFragment;
import com.oakkub.chat.fragments.LeaveGroupChatFragment;
import com.oakkub.chat.fragments.LeavePrivateChatFragment;
import com.oakkub.chat.fragments.LeavePublicChatFragment;
import com.oakkub.chat.fragments.NewMessagesFragment;
import com.oakkub.chat.fragments.NewPublicChatFragment;
import com.oakkub.chat.fragments.PublicChatSearchFragment;
import com.oakkub.chat.fragments.PublicListFetchingFragment;
import com.oakkub.chat.fragments.RegisterFragment;
import com.oakkub.chat.fragments.RemoveAdminFragment;
import com.oakkub.chat.fragments.RemoveMemberFragment;
import com.oakkub.chat.fragments.RoomAdminAuthenticationFragment;
import com.oakkub.chat.fragments.RoomListFetchingFragment;
import com.oakkub.chat.fragments.RoomListFragment;
import com.oakkub.chat.fragments.RoomMemberFragment;
import com.oakkub.chat.fragments.UserInfoFetchingFragment;
import com.oakkub.chat.modules.AnimationModule;
import com.oakkub.chat.modules.AppControllerModule;
import com.oakkub.chat.modules.NetworkModule;
import com.oakkub.chat.modules.StorageModule;
import com.oakkub.chat.modules.SystemServiceModule;
import com.oakkub.chat.utils.AnimateUtil;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Named;

import dagger.Component;
import okhttp3.OkHttpClient;


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
    void inject(RegisterFragment registerFragment);
    void inject(MainActivity mainActivity);
    void inject(NewMessagesActivity newMessagesActivity);
    void inject(RoomEditActivity roomEditActivity);
    void inject(ProfileActivity profileActivity);

    void inject(ChatRoomActivity chatRoomActivity);

    void inject(FriendDetailActivity friendDetailActivity);
    void inject(AuthenticationFragment authenticationFragment);
    void inject(EmailLoginFragment emailLoginActivityFragment);
    void inject(FriendsFragment friendsFragment);
    void inject(AddFriendFragment addFriendFragment);

    void inject(ChatRoomFragment chatRoomFragment);

    void inject(RoomListFragment roomListFragment);

    void inject(FriendsFetchingFragment friendsFetchingFragment);
    void inject(RoomListFetchingFragment roomListFetchingFragment);

    void inject(NewMessagesFragment newMessagesFragment);
    void inject(AuthStateFragment authStateFragment);
    void inject(GroupListFetchingFragment groupListFetchingFragment);
    void inject(RoomMemberFragment roomMemberFragment);
    void inject(CreatePrivateRoomFragment createPrivateRoomFragment);
    void inject(NewPublicChatFragment newPublicChatFragment);
    void inject(PublicListFetchingFragment publicListFetchingFragment);
    void inject(KeyToValueFirebaseFetchingFragment keyToValueFirebaseFetchingFragment);
    void inject(KeyFirebaseFetchingFragment keyFirebaseFetchingFragment);
    void inject(UserInfoFetchingFragment userInfoFetchingFragment);
    void inject(PublicChatSearchFragment publicChatSearchFragment);
    void inject(IsNodeExistsFirebaseFragment isNodeExistsFirebaseFragment);
    void inject(InviteMemberFragment inviteMemberFragment);
    void inject(RemoveMemberFragment removeMemberFragment);
    void inject(AddAdminFragment AddAdminFragment);
    void inject(RemoveAdminFragment removeAdminFragment);
    void inject(RoomAdminAuthenticationFragment roomAdminAuthenticationFragment);
    void inject(LeavePublicChatFragment leavePublicChatFragment);
    void inject(LeaveGroupChatFragment leaveGroupChatFragment);
    void inject(LeavePrivateChatFragment leavePrivateChatFragment);

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
