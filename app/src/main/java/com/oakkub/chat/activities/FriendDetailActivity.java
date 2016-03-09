package com.oakkub.chat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.CreatePrivateRoomFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.RoomUtil;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class FriendDetailActivity extends BaseActivity implements 
        CreatePrivateRoomFragment.OnPrivateRoomRequestListener {

    public static final String EXTRA_INFO = "FriendDetailActivity:friendInfo";
    public static final String TRANSITION_PROFILE_IMAGE = "transition:profileImage";

    private static final String TAG_CREATE_PRIVATE_ROOM = "tag:createPrivateRoom";

    @Bind(R.id.friend_detail_profile_image_view)
    SimpleDraweeView profileImage;

    @Bind(R.id.friend_detail_display_name_text_view)
    TextView displayName;

    @Bind(R.id.friend_detail_chat_button)
    Button chatButton;

    @State
    boolean isChatClicked;

    @State
    boolean isSameAsMe;

    private CreatePrivateRoomFragment createPrivateRoomFragment;
    private UserInfo friendInfo;

    public static void launch(AppCompatActivity activity, View imageView, UserInfo friendInfo) {
//        final String imageViewTransitionName = ViewCompat.getTransitionName(imageView);

        Intent intent = new Intent(activity, FriendDetailActivity.class);
        intent.putExtra(EXTRA_INFO, Parcels.wrap(friendInfo));
//        intent.putExtra(TRANSITION_PROFILE_IMAGE, imageViewTransitionName);

        activity.startActivity(intent);

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

        Intent intent = getIntent();
        friendInfo = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_INFO));
        isSameAsMe = friendInfo.getKey().equals(uid);

        setTransitionName();
        profileImage.setImageURI(Uri.parse(friendInfo.getProfileImageURL()));
        displayName.setText(friendInfo.getDisplayName());
        chatButton.setVisibility(isSameAsMe ? View.GONE : View.VISIBLE);

        addFragments();
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

    @OnClick(R.id.friend_detail_chat_button)
    public void onChatClick() {
        if (isSameAsMe) return;

        String roomKey = RoomUtil.getPrivateRoomKey(this, uid, friendInfo.getKey());
        createPrivateRoomFragment.createPrivateRoom(roomKey);
    }

    @Override
    public void onPrivateRoomCreated(Room room) {
        room.setName(friendInfo.getDisplayName());
        room.setImagePath(friendInfo.getProfileImageURL());

        Intent privateRoomIntent = ChatRoomActivity.getIntentPrivateRoom(this, room, uid);
        startActivity(privateRoomIntent);
        supportFinishAfterTransition();
    }

    @Override
    public void onCreatePrivateRoomFragmentCreated() {}

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

}
