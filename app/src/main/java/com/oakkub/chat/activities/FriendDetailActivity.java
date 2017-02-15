package com.oakkub.chat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.CreatePrivateRoomFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.icepick_bundler.UserInfoBundler;
import com.oakkub.chat.managers.loaders.DeleteFirebaseNodeLoader;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MyToast;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class FriendDetailActivity extends BaseActivity implements
        CreatePrivateRoomFragment.OnPrivateRoomRequestListener,
        AlertDialogFragment.OnAlertDialogListener,
        LoaderManager.LoaderCallbacks<Boolean> {

    public static final String EXTRA_INFO = "FriendDetailActivity:friendInfo";
    public static final String TRANSITION_PROFILE_IMAGE = "transition:profileImage";

    private static final String REMOVE_FRIEND_DIALOG_TAG = "tag:removeFriendDialog";
    private static final String TAG_CREATE_PRIVATE_ROOM = "tag:createPrivateRoom";
    private static final int REMOVE_FRIEND_LOADER_ID = 100;

    @BindView(R.id.friend_detail_profile_image_view)
    SimpleDraweeView profileImage;

    @BindView(R.id.friend_detail_display_name_text_view)
    TextView displayName;

    @BindView(R.id.friend_detail_chat_button)
    Button chatButton;

    @State
    boolean isChatClicked;

    @State
    boolean isSameAsMe;

    @State
    boolean isRemovingFriend;

    @State(UserInfoBundler.class)
    UserInfo friendInfo;

    private CreatePrivateRoomFragment createPrivateRoomFragment;

    public static void launch(AppCompatActivity activity, View imageView, UserInfo friendInfo, int requestCode) {
//        final String imageViewTransitionName = ViewCompat.getTransitionName(imageView);

        Intent intent = new Intent(activity, FriendDetailActivity.class);
        intent.putExtra(EXTRA_INFO, Parcels.wrap(friendInfo));
//        intent.putExtra(TRANSITION_PROFILE_IMAGE, imageViewTransitionName);

        activity.startActivityForResult(intent, requestCode);

        /*Pair<View, String> imageTransitionView =
                Pair.create(imageView, imageViewTransitionName);

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageTransitionView);
        ActivityCompat.startActivity(activity, intent, options.toBundle());*/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(this).inject(this);

        setContentView(R.layout.activity_friend_detail);
        ButterKnife.bind(this);

        initInstances(savedInstanceState);
        addFragments();

        if (isRemovingFriend) {
            getSupportLoaderManager().initLoader(REMOVE_FRIEND_LOADER_ID, null, this);
        }
    }

    private void initInstances(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            friendInfo = Parcels.unwrap(intent.getParcelableExtra(EXTRA_INFO));
            isSameAsMe = friendInfo.getKey().equals(uid);
        }

        setTransitionName();
        profileImage.setImageURI(Uri.parse(friendInfo.getProfileImageURL()));
        displayName.setText(friendInfo.getDisplayName());
        chatButton.setVisibility(isSameAsMe ? View.GONE : View.VISIBLE);
    }

    private void addFragments() {
        if (isSameAsMe) return;

        createPrivateRoomFragment = (CreatePrivateRoomFragment) findFragmentByTag(TAG_CREATE_PRIVATE_ROOM);
        if (createPrivateRoomFragment == null) {
            createPrivateRoomFragment = (CreatePrivateRoomFragment)
                    addFragmentByTag(new CreatePrivateRoomFragment(), TAG_CREATE_PRIVATE_ROOM);
        }
    }

    private void setTransitionName() {
        ViewCompat.setTransitionName(profileImage, getIntent().getStringExtra(TRANSITION_PROFILE_IMAGE));
    }

    private ArrayMap<String, Object> getFriendsMap() {
        ArrayMap<String, Object> friendsMap = new ArrayMap<>(3);

        String roomKey = RoomUtil.getPrivateRoomKey(this, uid, friendInfo.getKey());

        friendsMap.put(TextUtil.getPath(FirebaseUtil.KEY_USERS,
                FirebaseUtil.KEY_USERS_USER_FRIENDS, friendInfo.getKey(), uid), null);
        friendsMap.put(TextUtil.getPath(FirebaseUtil.KEY_USERS,
                FirebaseUtil.KEY_USERS_USER_FRIENDS, uid, friendInfo.getKey()), null);
        friendsMap.put(TextUtil.getPath(FirebaseUtil.KEY_USERS,
                FirebaseUtil.KEY_USERS_USER_ROOMS, uid, roomKey), null);

        return friendsMap;
    }

    @OnClick(R.id.friend_detail_remove_button)
    public void onRemoveFriendClick() {
        if (isSameAsMe) return;

        AlertDialogFragment removeFriendDialog =
                AlertDialogFragment.newInstance(getString(R.string.remove_friend),
                        getString(R.string.are_you_sure_to_remove_n_from_friend_contact,
                                friendInfo.getDisplayName()),
                        getString(R.string.remove), "");
        removeFriendDialog.show(getSupportFragmentManager(), REMOVE_FRIEND_DIALOG_TAG);
    }

    @OnClick(R.id.friend_detail_chat_button)
    public void onChatClick() {
        if (isSameAsMe) return;

        String roomKey = RoomUtil.getPrivateRoomKey(this, uid, friendInfo.getKey());

        showProgressDialog();
        createPrivateRoomFragment.createPrivateRoom(roomKey);
    }

    @Override
    public void onPrivateRoomCreated(Room room) {
        hideProgressDialog();
        room.setName(friendInfo.getDisplayName());
        room.setImagePath(friendInfo.getProfileImageURL());

        Intent privateRoomIntent = ChatRoomActivity.getIntentPrivateRoom(this, room, uid);
        startActivity(privateRoomIntent);
        supportFinishAfterTransition();
    }

    @Override
    public void onCreatePrivateRoomFragmentCreated() {
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        supportFinishAfterTransition();
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        showProgressDialog();

        switch (id) {
            case REMOVE_FRIEND_LOADER_ID:
                return new DeleteFirebaseNodeLoader(this, getFriendsMap());
            default:
                return null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
            }
        });

        switch (loader.getId()) {
            case REMOVE_FRIEND_LOADER_ID:
                onFriendRemoved(data);
                break;
        }
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (which != DialogInterface.BUTTON_POSITIVE) return;

        switch (tag) {
            case REMOVE_FRIEND_DIALOG_TAG:
                isRemovingFriend = true;
                getSupportLoaderManager().restartLoader(REMOVE_FRIEND_LOADER_ID, null, this);
                break;
        }
    }

    private void onFriendRemoved(boolean data) {
        isRemovingFriend = false;

        if (!data) {
            MyToast.make(getString(R.string.error_remove_friend)).show();
            return;
        }

        setResult(RESULT_OK);
        finish();
    }
}
