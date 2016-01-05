package com.oakkub.chat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.PrivateChatRoomActivityFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;

public class FriendDetailActivity extends AppCompatActivity {

    public static final String EXTRA_INFO = "FriendDetailActivity:friendInfo";
    public static final String TRANSITION_PROFILE_IMAGE = "transition:profileImage";

    @Bind(R.id.friend_detail_root_view)
    CardView rootView;

    @Bind(R.id.friend_detail_container)
    LinearLayout container;

    @Bind(R.id.friend_detail_profile_image_view)
    SimpleDraweeView profileImage;

    @Bind(R.id.friend_detail_display_name_text_view)
    TextView displayName;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Lazy<Firebase> userRoomsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> currentUserFirebase;

    private UserInfo friendInfo;

    public static void launch(AppCompatActivity activity, View imageView, UserInfo friendInfo) {
        final String imageViewTransitionName = ViewCompat.getTransitionName(imageView);

        Intent intent = new Intent(activity, FriendDetailActivity.class);
        intent.putExtra(EXTRA_INFO, Parcels.wrap(friendInfo));
        intent.putExtra(TRANSITION_PROFILE_IMAGE, imageViewTransitionName);

        activity.startActivity(intent);

        /*Pair<View, String> imageTransitionView =
                Pair.create(imageView, imageViewTransitionName);

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageTransitionView);
        ActivityCompat.startActivity(activity, intent, options.toBundle());*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(this).inject(this);

        setContentView(R.layout.activity_friend_detail);
        ButterKnife.bind(this);

        setTransitionName();
        setRootViewPadding();
        setOnTouchOutsideTransition();

        friendInfo = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_INFO));

        profileImage.setImageURI(Uri.parse(friendInfo.getProfileImageURL()));
        displayName.setText(friendInfo.getDisplayName());
    }

    private void setTransitionName() {
        ViewCompat.setTransitionName(profileImage, getIntent().getStringExtra(TRANSITION_PROFILE_IMAGE));
    }

    private void setRootViewPadding() {
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        container.setPadding(space, space, space, space);
    }

    private void setOnTouchOutsideTransition() {
        rootView.setOnClickListener(null);
    }

    @OnClick(android.R.id.content)
    public void onContentClick() {
        supportFinishAfterTransition();
    }

    @OnClick(R.id.friend_detail_chat_button)
    public void onChatClick() {
        beginChattingWithFriend();
    }

    private void beginChattingWithFriend() {
        Firebase roomsRef = roomInfoFirebase.get().getRef();
        AuthData authData = roomsRef.getAuth();

        String roomKey = TextUtil.
                getPrivateRoomKey(this, authData.getUid(), friendInfo.getUserKey());

        creatingPrivateRoom(roomsRef, authData, roomKey);
    }

    /*private void prepareCreatingPrivateRoom(final Firebase roomsRef, final AuthData authData, final String roomKey) {

        currentUserFirebase.get().child(authData.getUid()).child(FirebaseUtil.CHILD_DISPLAY_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    creatingPrivateRoom(roomsRef, authData, roomKey);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("FIREBASE ERROR", firebaseError.getMessage());
            }
        });
    }*/

    private void creatingPrivateRoom(final Firebase roomsRef, final AuthData authData, final String roomKey) {
        // check if room is already created.
        roomsRef.child(roomKey).keepSynced(true);
        roomsRef.child(roomKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isRoomAlreadyCreated(dataSnapshot, roomsRef, authData, roomKey);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("FIREBASE ERROR", firebaseError.getMessage());
                    }
                });
    }

    private void isRoomAlreadyCreated(DataSnapshot dataSnapshot, Firebase roomsRef,
                                      AuthData authData, String roomKey) {
        if (dataSnapshot.exists()) {
            // room is already created
            roomCreated(roomKey);
        } else {
            beginCreatingPrivateRoom(roomsRef, authData, roomKey);
        }
    }

    private void beginCreatingPrivateRoom(Firebase roomsRef, final AuthData authData, final String roomKey) {
        Room privateRoom = new Room(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE);

        roomsRef.child(roomKey).setValue(privateRoom, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    return;
                }
                addUserToRoom(authData, roomKey);
            }
        });
    }

    private Map<String, Object> getRoom(AuthData authData, String roomKey) {
        Map<String, Object> roomMap = new HashMap<>(2);

        roomMap.put(TextUtil.getPath(FirebaseUtil.KEY_ROOMS_INFO, roomKey),
                getRoomInfo());
        roomMap.put(TextUtil.getPath(FirebaseUtil.KEY_ROOMS_MEMBERS, roomKey),
                getRoomMembers(authData));

        return roomMap;
    }

    private Map<String, Object> getRoomInfo() {
        Map<String, Object> roomInfo = new HashMap<>(2);

        roomInfo.put(FirebaseUtil.CHILD_ROOM_CREATED, System.currentTimeMillis());
        roomInfo.put(FirebaseUtil.CHILD_ROOM_TYPE, FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE);

        return roomInfo;
    }

    private Map<String, Object> getRoomMembers(AuthData authData) {
        Map<String, Object> members = new HashMap<>(2);

        members.put(authData.getUid(), Boolean.TRUE);
        members.put(friendInfo.getUserKey(), Boolean.TRUE);

        return members;
    }

    private void addUserToRoom(AuthData authData, final String roomKey) {
        Firebase userRoomsRef = userRoomsFirebase.get().getRef();

        final String userKey = authData.getUid();
        final String friendKey = friendInfo.getUserKey();
        final long timeCreated = System.currentTimeMillis();

        Map<String, Object> roomMap = new HashMap<>(2);
        roomMap.put(TextUtil.getPath(userKey, roomKey), timeCreated);
        roomMap.put(TextUtil.getPath(friendKey, roomKey), timeCreated);

        userRoomsRef.updateChildren(roomMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    return;
                }

                roomCreated(roomKey);
            }
        });
    }

    private void roomCreated(String roomKey) {
        goToChatRoom(roomKey);
        supportFinishAfterTransition();
    }

    private void goToChatRoom(String roomKey) {

        Intent privateRoomIntent = new Intent(this, PrivateChatRoomActivity.class);
        privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_ROOM_ID, roomKey);
        privateRoomIntent.putExtra(PrivateChatRoomActivityFragment.EXTRA_ROOM_NAME, friendInfo.getDisplayName());
        privateRoomIntent.putStringArrayListExtra(PrivateChatRoomActivityFragment.EXTRA_FRIEND_ID,
                new ArrayList<>(Collections.singletonList(friendInfo.getUserKey())));
        privateRoomIntent.putStringArrayListExtra(PrivateChatRoomActivityFragment.EXTRA_FRIEND_PROFILE,
                new ArrayList<>(Collections.singletonList(friendInfo.getProfileImageURL())));

        startActivity(privateRoomIntent);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

}
