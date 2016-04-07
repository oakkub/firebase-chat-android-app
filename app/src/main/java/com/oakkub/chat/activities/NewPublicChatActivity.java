package com.oakkub.chat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.NewPublicChatFragment;
import com.oakkub.chat.fragments.PublicRoomCreationFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.widgets.MyToast;

import butterknife.ButterKnife;

public class NewPublicChatActivity extends BaseActivity implements
        PublicRoomCreationFragment.OnRoomCreationListener,
        NewPublicChatFragment.NewPublicChatListener {

    private static final String TAG = NewPublicChatActivity.class.getSimpleName();
    private static final String NEW_PUBLIC_CHAT_FRAGMENT_TAG = "tag:publicChatFragment";
    private static final String TAG_PROGRESS_DIALOG = "tag:progressDialogFragment";
    private static final String ROOM_CREATION_TAG = "tag:roomCreationFragment";

    private NewPublicChatFragment newPublicChatFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_container);
        getDataIntent(savedInstanceState);
        ButterKnife.bind(this);

        findOrAddFragmentByTag(R.id.empty_container,
                PublicRoomCreationFragment.newInstance(getString(R.string.new_public_chat)),
                ROOM_CREATION_TAG);

        newPublicChatFragment = (NewPublicChatFragment) findOrAddFragmentByTag(
                getSupportFragmentManager(),
                new NewPublicChatFragment(),
                NEW_PUBLIC_CHAT_FRAGMENT_TAG);
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        uid = intent.getStringExtra(EXTRA_MY_ID);
    }

    @Override
    public void onInputSend(Room room, Uri uriImage, String imageAbsolutePath) {
        showProgressDialog();
        newPublicChatFragment.createPublicChat(room, uriImage, imageAbsolutePath);
    }

    @Override
    public void onPublicChatCreated(Room room) {
        hideProgressDialog();

        Intent roomIntent = ChatRoomActivity.getIntentPublicRoom(this, room, true);
        startActivity(roomIntent);
        fadeOutFinish();
    }

    @Override
    public void onPublicChatFailed() {
        hideProgressDialog();
        MyToast.make(getString(R.string.error_creating_room)).show();
    }
}
