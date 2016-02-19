package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.NewPublicChatFragment;
import com.oakkub.chat.fragments.PublicRoomCreationFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.widgets.MyToast;

import butterknife.ButterKnife;
import icepick.State;

public class NewPublicChatActivity extends BaseActivity implements
        PublicRoomCreationFragment.OnRoomCreationListener, NewPublicChatFragment.NewPublicChatListener {

    private static final String TAG = NewPublicChatActivity.class.getSimpleName();
    private static final String NEW_PUBLIC_CHAT_FRAGMENT_TAG = "tag:publicChatFragment";
    private static final String TAG_PROGRESS_DIALOG = "tag:progressDialogFragment";
    private static final String ROOM_CREATION_TAG = "tag:roomCreationFragment";

    private NewPublicChatFragment newPublicChatFragment;

    @State
    String myId;

    public static Intent getStartIntent(Context context, String myId) {
        Intent intent = new Intent(context, NewPublicChatActivity.class);
        intent.putExtra(EXTRA_MY_ID, myId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_container);
        getDataIntent(savedInstanceState);
        ButterKnife.bind(this);

        if (findFragmentByTag(ROOM_CREATION_TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.empty_container, PublicRoomCreationFragment.newInstance(
                            myId, getString(R.string.new_public_chat)), ROOM_CREATION_TAG)
                    .commit();
        }

        newPublicChatFragment = (NewPublicChatFragment) findFragmentByTag(NEW_PUBLIC_CHAT_FRAGMENT_TAG);
        if (newPublicChatFragment == null) {
            newPublicChatFragment = (NewPublicChatFragment)
                    addFragmentByTag(NewPublicChatFragment.newInstance(
                            myId), NEW_PUBLIC_CHAT_FRAGMENT_TAG);
        }
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
    }

    @Override
    public void onInputSend(Room room, Uri uriImage, String imageAbsolutePath) {
        showProgressDialog();
        newPublicChatFragment.createPublicChat(room, uriImage, imageAbsolutePath);
    }

    @Override
    public void onPublicChatCreated(Room room) {
        hideProgressDialog();

        Intent roomIntent = ChatRoomActivity.getIntentPublicRoom(this, room, myId, true);
        startActivity(roomIntent);
        fadeOutFinish();
    }

    @Override
    public void onPublicChatFailed() {
        hideProgressDialog();
        MyToast.make(getString(R.string.error_creating_room)).show();
    }
}
