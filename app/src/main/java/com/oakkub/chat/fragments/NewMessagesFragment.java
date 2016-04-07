package com.oakkub.chat.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusNewPrivateRoomMessage;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 12/24/2015.
 */
public class NewMessagesFragment extends BaseFragment {

    private static final int TOTAL_MERGE_IMAGES = 3;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Lazy<Firebase> roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES)
    Lazy<Firebase> messageFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userFirebase;

    private boolean isPrivateRoom;

    private UserInfo[] membersInfo;

    private Room createdRoom;
    private OnRoomRequest onRoomRequest;

    private CreatePrivateRoomFragment createPrivateRoomFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        onRoomRequest = (OnRoomRequest) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (createdRoom != null) {
            onRoomRequest.onRoomCreated(createdRoom);
            createdRoom = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onRoomRequest = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    public void createPrivateRoom(UserInfo friendInfo) {
        isPrivateRoom = true;

        UserInfo myInfo = new UserInfo();
        myInfo.setKey(uid);

        membersInfo = new UserInfo[] {
                friendInfo, myInfo
        };

        onRoomRequest.onShowLoading();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(EventBusNewPrivateRoomMessage eventBusRoom) {
        onPrivateRoomCreated(eventBusRoom.room);
    }

    public void onPrivateRoomCreated(Room room) {
        room.setLatestMessageUser(uid);

        // if message not null and message time > 0
        // means we already chat with this friend (room is already created)
        if (room.getLatestMessage() != null && room.getLatestMessageTime() > 0) {
            roomSuccessfullyCreated(room);
        } else {
            mapRoomData(room);
        }
    }

    public void createGroupRoom(UserInfo[] membersInfo) {
        this.isPrivateRoom = false;
        this.membersInfo = membersInfo;

        onRoomRequest.onShowLoading();

        // create bitmap from friend profile images
        // by using 2 of our friends and 1 from user
        fetchMyInfoGroupRoom();
    }

    private void fetchMyInfoGroupRoom() {
        userFirebase.get().child(uid).keepSynced(true);
        userFirebase.get().child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        myInfoGroupRoomFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void myInfoGroupRoomFetched(DataSnapshot dataSnapshot) {
        membersInfo[0] = dataSnapshot.getValue(UserInfo.class);
        membersInfo[0].setKey(dataSnapshot.getKey());

        setImageURLToDataUri();
        findFrescoBitmaps();
    }

    private void setImageURLToDataUri() {
        for (UserInfo friendInfo : membersInfo) {
            friendInfo.setProfileImageURL(Base64Util.toDataUri(friendInfo.getProfileImageURL()));
            Log.d("123456789", "setImageURLToDataUri: " + friendInfo.getProfileImageURL().substring(0, 30));
        }
    }

    private void findFrescoBitmaps() {
        final Bitmap[] bitmaps = new Bitmap[TOTAL_MERGE_IMAGES];
        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        for (int i = 0; i < TOTAL_MERGE_IMAGES; i++) {

            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(membersInfo[i].getProfileImageURL()))
                    .setRequestPriority(Priority.HIGH)
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                    .build();

            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

            dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                @Override
                protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    if (!dataSource.isFinished()) return;
                    onFrescoBitmapReceived(dataSource, bitmaps);
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                }
            }, CallerThreadExecutor.getInstance());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void onFrescoBitmapReceived(DataSource<CloseableReference<CloseableImage>> dataSource, Bitmap[] bitmaps) {
        CloseableReference<CloseableImage> closeableRefImage = dataSource.getResult();

        CloseableBitmap closeableBitmap = (CloseableBitmap) closeableRefImage.get();

        for (int i = 0; i < TOTAL_MERGE_IMAGES; i++) {

            if (bitmaps[i] == null) {

                bitmaps[i] = closeableBitmap.getUnderlyingBitmap();
                CloseableReference.closeSafely(closeableRefImage);

                if (i == TOTAL_MERGE_IMAGES - 1) {
                    new MergeThreeBitmapTask(bitmaps).execute();
                }

                break;
            }
        }
    }

    private void mapRoomData(Room room) {
        int size = 8 + (isPrivateRoom ? 2 : membersInfo.length * 3);
        ArrayMap<String, Object> roomMap = new ArrayMap<>(size);
        FirebaseMapUtil.mapRoom(roomMap, room, room.getRoomId());

        // group room will send message that tell us when this room is created
        if (!isPrivateRoom) {
            String messageKey = messageFirebase.get().child(room.getRoomId()).push().getKey();
            Message messageRoom = new Message(room.getRoomId(), room.getLatestMessage(),
                    FirebaseUtil.SYSTEM, room.getCreated());
            FirebaseMapUtil.mapMessage(roomMap, messageKey, room.getRoomId(), messageRoom);
            FirebaseMapUtil.mapUsersGroupRoom(roomMap, membersInfo, room.getRoomId(), room.getCreated());

            for (UserInfo userInfo : membersInfo) {
                FirebaseMapUtil.mapUserPreservedMemberRoom(roomMap, userInfo.getKey(), room.getRoomId(), room.getCreated());
            }
        }

        FirebaseMapUtil.mapUserRoom(
                roomMap, uid, room.getRoomId(), room.getCreated());

        insertRoom(roomMap, room);
    }

    private void onImageRoomCreated(String base64Bitmap) {
        String roomKey = roomFirebase.get().push().getKey();
        String roomName = createGroupRoomName();

        // if room has no message, it's a new room,
        // we will use message time to show time instead of message.
        Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_GROUP);
        room.setRoomId(roomKey);
        room.setName(roomName);
        room.setImagePath(Base64Util.toDataUri(base64Bitmap));
        room.setLatestMessageTime(room.getCreated());
        room.setLatestMessage(getString(R.string.room_created));
        room.setLatestMessageUser(FirebaseUtil.SYSTEM);

        mapRoomData(room);
    }

    private void insertRoom(ArrayMap<String, Object> map, final Room room) {
        rootFirebase.get().updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

                roomSuccessfullyCreated(room);
            }
        });
    }

    private void roomSuccessfullyCreated(Room room) {
        if (onRoomRequest != null) {
            onRoomRequest.onRoomCreated(room);
        } else {
            createdRoom = room;
        }
    }

    private String createGroupRoomName() {
        int size = membersInfo.length;
        StringBuilder builder = new StringBuilder(size * 4);

        for (int i = 0; i < size; i++) {
            String firstName = membersInfo[i].getDisplayName().split(" ")[0];

            builder.append(firstName);
            builder.append(i == (size - 1) ? "" : ", ");
        }

        return builder.toString();
    }

    private class MergeThreeBitmapTask extends AsyncTask<Void, Void, String> {

        private Bitmap[] bitmaps;

        public MergeThreeBitmapTask(Bitmap[] bitmaps) {
            this.bitmaps = bitmaps;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Bitmap resultBitmap = Bitmap.createBitmap(
                    100, 100, Bitmap.Config.ARGB_8888
            );

            int resultWidth = resultBitmap.getWidth();
            int resultHeight = resultBitmap.getHeight();

            Canvas canvas = new Canvas(resultBitmap);
            Paint paint = new Paint();

            for (int i = 0; i < TOTAL_MERGE_IMAGES; i++) {
                Bitmap bitmap = bitmaps[i];
                Bitmap scaledBitmap;

                if (i == 0) {
                    scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            resultWidth, resultHeight, false);
                } else {
                    scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            resultWidth / 2, resultHeight / 2, false);
                }

                switch (i) {
                    case 0:
                        canvas.drawBitmap(scaledBitmap, -(resultWidth / 4f), 0, paint);
                        break;
                    case 1:
                        canvas.drawBitmap(scaledBitmap, resultWidth / 2, 0, paint);
                        break;
                    case 2:
                        canvas.drawBitmap(scaledBitmap, resultWidth / 2, resultHeight / 2, paint);
                        break;
                }

                scaledBitmap.recycle();
            }

            return Base64Util.toBase64(resultBitmap);
        }

        @Override
        protected void onPostExecute(String base64Bitmap) {
            super.onPostExecute(base64Bitmap);

            onImageRoomCreated(base64Bitmap);
        }
    }

    public interface OnRoomRequest {
        void onRoomCreated(Room room);
        void onShowLoading();
    }

}
