package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.firebase.client.Firebase;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.Base64ConverterFragment;
import com.oakkub.chat.fragments.PublicRoomCreationFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.icepick_bundler.RoomBundler;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusRoomListEdited;
import com.oakkub.chat.models.eventbus.EventBusUpdatedGroupRoom;
import com.oakkub.chat.models.eventbus.EventBusUpdatedPublicRoom;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.io.File;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import dagger.Lazy;
import icepick.State;

/**
 * Created by OaKKuB on 2/17/2016.
 */
public class RoomEditActivity extends BaseActivity implements
        PublicRoomCreationFragment.OnRoomCreationListener,
        Base64ConverterFragment.OnBase64ConverterListener {

    private static final String TAG = RoomEditActivity.class.getSimpleName();
    private static final String ROOM_CREATE_TAG = "tag:roomCreateFragment";
    private static final String BASE64_CONVERTER_TAG = "tag:base64ConverterFragment";
    private static final String EXTRA_ROOM = "extra:room";

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    @State
    boolean isRoomEdit;

    @State
    boolean isPublicChat;

    @State(RoomBundler.class)
    Room room;

    private PublicRoomCreationFragment publicRoomCreationFragment;
    private Base64ConverterFragment base64ConverterFragment;

    public static Intent getStartIntent(Context context, Room room) {
        Intent intent = new Intent(context, RoomEditActivity.class);
        intent.putExtra(EXTRA_ROOM, Parcels.wrap(room));
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(this).inject(this);
        setContentView(R.layout.empty_container);
        initInstances(savedInstanceState);
    }

    private void initInstances(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (savedInstanceState == null) {
            uid = intent.getStringExtra(EXTRA_MY_ID);
        }
        room = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ROOM));
        isPublicChat = room.getType().equals(RoomUtil.PUBLIC_TYPE);

        ButterKnife.findById(this, R.id.empty_container)
                .setBackgroundColor(getCompatColor(android.R.color.white));

        publicRoomCreationFragment = (PublicRoomCreationFragment) findOrAddFragmentByTag(
                R.id.empty_container, PublicRoomCreationFragment.newInstance(getString(R.string.edit), room), ROOM_CREATE_TAG);

        base64ConverterFragment = (Base64ConverterFragment) findOrAddFragmentByTag(
                getSupportFragmentManager(),
                Base64ConverterFragment.newInstance(), BASE64_CONVERTER_TAG);
    }

    private void sendResult(Room editedRoom) {
        room = editedRoom;

        EventBus.getDefault().post(new EventBusRoomListEdited(room));
        setResult(RESULT_OK, RoomInfoActivity.getResultIntent(room, isRoomEdit));
        fadeOutFinish();
    }

    @Override
    public void onInputSend(Room editedRoom, Uri uriImage, String absolutePath) {
        editedRoom.setRoomId(this.room.getRoomId());
        editedRoom.setLatestMessage(this.room.getLatestMessage());
        editedRoom.setLatestMessageUser(this.room.getLatestMessageUser());
        editedRoom.setLatestMessageTime(this.room.getLatestMessageTime());
        editedRoom.setImagePath(uriImage == null ? this.room.getImagePath() : uriImage.toString());
        editedRoom.setCreated(this.room.getCreated());

        isRoomEdit = !this.room.fullEquals(editedRoom);
        if (!isRoomEdit) {
            fadeOutFinish();
            return;
        }

        showProgressDialog();
        saveRoomData(editedRoom);
        boolean isEditedImage = false;

        if (absolutePath == null && uriImage != null) {
            File file = new File(URI.create(uriImage.toString()));
            base64ConverterFragment.convert(file, true);
            isEditedImage = true;
        } else if (absolutePath != null && uriImage != null) {
            base64ConverterFragment.convert(uriImage, absolutePath, true);
            isEditedImage = true;
        }

        if (!isEditedImage) {
            hideProgressDialog();
            sendResult(editedRoom);
        }

        if (isPublicChat) {
            EventBus.getDefault().post(new EventBusUpdatedPublicRoom(editedRoom));
        } else {
            EventBus.getDefault().post(new EventBusUpdatedGroupRoom(editedRoom));
        }
    }

    private void saveRoomData(Room editedRoom) {
        String editedName = editedRoom.getName();
        String editedDesc= editedRoom.getDescription();
        String editedTag = editedRoom.getTag();

        if (!room.getName().equals(editedName)) {
            updateRoomInfo(RoomUtil.KEY_NAME, editedName);
            room.setName(editedName);
        }

        if (editedDesc != null) {
            if (room.getDescription() == null || !room.getDescription().equals(editedDesc)) {
                updateRoomInfo(RoomUtil.KEY_DESC, editedDesc);
                room.setDescription(editedDesc);
            }
        } else {
            updateRoomInfo(RoomUtil.KEY_DESC, null);
            room.setDescription(null);
        }

        if (editedTag != null && room.getTag() != null) {
            if (!room.getTag().equals(editedTag)) {
                updateRoomInfo(RoomUtil.KEY_TAG, editedTag);
                room.setTag(editedTag);
            }
        }
    }

    private void updateRoomInfo(String key, Object specificInfo) {
        roomInfoFirebase.get().child(room.getRoomId()).child(key).setValue(specificInfo);
    }

    @Override
    public void onBase64Received(String base64) {
        room.setImagePath(base64);
        updateRoomInfo(RoomUtil.KEY_IMAGE_PATH, base64);
        hideProgressDialog();

        sendResult(room);
    }
}
