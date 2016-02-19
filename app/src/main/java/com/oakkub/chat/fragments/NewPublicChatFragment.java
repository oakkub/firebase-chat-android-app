package com.oakkub.chat.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.ArrayMapUtil;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.BitmapUtil;
import com.oakkub.chat.utils.FirebaseUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class NewPublicChatFragment extends BaseFragment {

    private static final String TAG = NewPublicChatFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Lazy<Firebase> roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_PUBLIC)
    Lazy<Firebase> userPublicRoomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES)
    Lazy<Firebase> messageFirebase;

    private NewPublicChatListener newPublicChatListener;
    private Room createdRoom;
    private String myId;
    private boolean roomFailedCreated;

    public static NewPublicChatFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        NewPublicChatFragment newPublicChatFragment = new NewPublicChatFragment();
        newPublicChatFragment.setArguments(args);

        return newPublicChatFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        newPublicChatListener = (NewPublicChatListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataFromArgs(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (createdRoom != null) {
            newPublicChatListener.onPublicChatCreated(createdRoom);
            createdRoom = null;
        }

        if (roomFailedCreated) {
            failedCreatePublicChat();
            roomFailedCreated = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        newPublicChatListener = null;
    }

    private void getDataFromArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);
    }

    private void failedCreatePublicChat() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newPublicChatListener != null) {
                    newPublicChatListener.onPublicChatFailed();
                } else {
                    roomFailedCreated = true;
                }
            }
        });
    }

    public void createPublicChat(final Room room, final Uri imageUri, final String absolutePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap imageBitmap = getBitmapPublicImage(imageUri, absolutePath);
                if (imageBitmap == null) {
                    failedCreatePublicChat();
                    return;
                }

                String base64 = Base64Util.toBase64(imageBitmap, 50);
                String uriBase64 =  Base64Util.toDataUri(base64);

                imageBitmap.recycle();
                onRoomImageCreated(room, uriBase64);
            }
        }).start();
    }

    private Bitmap getBitmapPublicImage(Uri imageUri, String absolutePath) {
        Bitmap bitmap;
        if (absolutePath == null) bitmap = createPublicImageFromFile(imageUri);
        else bitmap = createPublicImageFromFileDescriptor(imageUri, absolutePath);
        return bitmap == null ? null : bitmap;
    }

    private Bitmap createPublicImageFromFile(Uri imageUri) {
        File file = new File(URI.create(imageUri.toString()));
        return BitmapUtil.getResized(file, true);
    }

    private Bitmap createPublicImageFromFileDescriptor(Uri imageUri, String absolutePath) {
        try {
            Context context = Contextor.getInstance().getContext();

            ParcelFileDescriptor parcelFileDescriptor = context
                    .getContentResolver().openFileDescriptor(imageUri, "r");
            if (parcelFileDescriptor == null) return null;

            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap result = BitmapUtil.getResized(fileDescriptor, absolutePath, true);

            parcelFileDescriptor.close();
            return result;
        } catch (IOException e) {
            Log.e(TAG, "createPublicImageFromFileDescriptor: " + e.getMessage());
            return null;
        }
    }

    private void onRoomImageCreated(final Room room, String uriBase64) {
        String roomKey = roomFirebase.get().push().getKey();
        room.setRoomId(roomKey);
        room.setImagePath(uriBase64);
        room.setLatestMessage(getString(R.string.room_created));
        room.setLatestMessageUser(FirebaseUtil.SYSTEM);
        room.setLatestMessageTime(room.getCreated());

        String messageKey = messageFirebase.get().child(room.getRoomId()).push().getKey();
        Message message = new Message(room.getRoomId(),
                room.getLatestMessage(), room.getLatestMessageUser(), room.getCreated());

        ArrayMap<String, Object> map = new ArrayMap<>(7);
        ArrayMapUtil.mapUserRoom(map, myId, room.getRoomId(), room.getCreated());
        ArrayMapUtil.mapMessage(map, messageKey, room.getRoomId(), message);
        ArrayMapUtil.mapRoom(map, room, room.getRoomId());
        ArrayMapUtil.mapPublicRoomList(map, room);
        ArrayMapUtil.mapUserPublicRoom(map, myId, room.getRoomId(), room.getCreated());
        ArrayMapUtil.mapUserPreservedMemberRoom(map, myId, room.getRoomId(), room.getCreated());
        ArrayMapUtil.mapUserRoomAdminMember(map, myId, room.getRoomId(), room.getCreated());

        firebase.get().updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    failedCreatePublicChat();
                    return;
                }

                onPublicRoomCreated(room);
            }
        });
    }

    private void onPublicRoomCreated(Room room) {
        if (newPublicChatListener != null) {
            newPublicChatListener.onPublicChatCreated(room);
        } else {
            createdRoom = room;
        }
    }


    public interface NewPublicChatListener {
        void onPublicChatCreated(Room room);
        void onPublicChatFailed();
    }

}
