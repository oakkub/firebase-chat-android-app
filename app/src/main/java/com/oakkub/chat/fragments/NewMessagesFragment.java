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
import android.util.Log;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

import static com.oakkub.chat.utils.FirebaseUtil.KEY_MESSAGES;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_INFO;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_ROOMS_MEMBERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS_USER_GROUP_ROOMS;
import static com.oakkub.chat.utils.FirebaseUtil.KEY_USERS_USER_ROOMS;

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

    private OnImageRequestListener onImageRequestListener;
    private String bitmapBase64Result;
    private String message;
    private String myId;

    private long messageTime;

    private UserInfo[] usersInfo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        onImageRequestListener = (OnImageRequestListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (bitmapBase64Result != null) {
            onImageRequestListener.onImageReceived(bitmapBase64Result);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onImageRequestListener = null;
    }

    public void setRoomData(String myId, String message, long messageTime) {
        this.myId = myId;
        this.message = message;
        this.messageTime = messageTime;
    }

    public void createPrivateRoom(String myId, UserInfo friendInfo, String message, long messageTime) {
        Firebase createRoomFirebase = roomFirebase.get().push();
        String roomKey = createRoomFirebase.getKey();

        setRoomData(myId, message, messageTime);
        usersInfo = new UserInfo[] {friendInfo};

        Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE);
    }

    public void createGroupRoom(String myId, UserInfo[] friendsInfo, String message, long messageTime) {
        setRoomData(myId, message, messageTime);
        this.usersInfo = friendsInfo;

        // create bitmap from friend profile images
        // by using 2 of our friends and 1 from user
        getMyUserInfo();
    }

    private void getMyUserInfo() {
        userFirebase.get().child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        getMyUserInfo(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void getMyUserInfo(DataSnapshot dataSnapshot) {
        usersInfo[usersInfo.length - 1] = dataSnapshot.getValue(UserInfo.class);
        usersInfo[usersInfo.length - 1].setUserKey(dataSnapshot.getKey());

        for (UserInfo friendInfo : usersInfo) {
            friendInfo.setProfileImageURL(Base64Util.toDataUri(friendInfo.getProfileImageURL()));
        }

        findFrescoBitmap();
    }

    private void findFrescoBitmap() {
        final Bitmap[] bitmaps = new Bitmap[TOTAL_MERGE_IMAGES];
        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        for (int i = 0; i < TOTAL_MERGE_IMAGES; i++) {

            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(usersInfo[i].getProfileImageURL()))
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

    private void onFrescoBitmapReceived(DataSource<CloseableReference<CloseableImage>> dataSource, Bitmap[] bitmaps) {
        CloseableReference<CloseableImage> closeableRefImage = dataSource.getResult();
        CloseableBitmap closeableBitmap = closeableRefImage != null ?
                (CloseableBitmap) closeableRefImage.get() : null;

        if (closeableBitmap == null) {
            throw new NullPointerException("Closeable Bitmap is Null, WTF?");
        }

        for (int i = 0; i < TOTAL_MERGE_IMAGES; i++) {

            if (bitmaps[i] == null) {

                bitmaps[i] = closeableBitmap.getUnderlyingBitmap();
                CloseableReference.closeSafely(closeableRefImage);

                if (i == TOTAL_MERGE_IMAGES -1) {
                    new MergeThreeBitmapTask(bitmaps).execute();
                }

                break;
            }
        }
    }

    private void onMergedBitmapSuccess(String base64Bitmap) {
        Firebase createRoomFirebase = roomFirebase.get().push();
        String roomKey = createRoomFirebase.getKey();

        String roomName = createGroupRoomName();
        long roomCreated = System.currentTimeMillis();

        Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_GROUP);
        room.setRoomName(roomName);
        room.setRoomImagePath(Base64Util.toDataUri(base64Bitmap));
        room.setLatestMessage(message);
        room.setLatestMessageTime(messageTime);
        room.setLatestMessageUser(myId);

        Message messageRoom = new Message(roomKey, message, myId, roomCreated);

        HashMap<String, Object> groupRoomMap = new HashMap<>();
        putUsersRoomData(groupRoomMap, roomKey, roomCreated);
        putGroupRoomInfoMap(groupRoomMap, room, roomKey, roomCreated);
        putRoomMessage(groupRoomMap, roomKey, messageRoom);

        rootFirebase.get().updateChildren(groupRoomMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e("Group Room Creation", "onComplete: " + firebaseError.getMessage());
                }

                Toast.makeText(getActivity(), "Group Room Created", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void putGroupRoomInfoMap(HashMap<String, Object> groupRoomMap, Room room, String roomKey, long roomCreated) {
        putRoomInfo(groupRoomMap, roomKey, "name", room.getName());
        putRoomInfo(groupRoomMap, roomKey, "imagePath", room.getImagePath());
        putRoomInfo(groupRoomMap, roomKey, "latestMessage", room.getLatestMessage());
        putRoomInfo(groupRoomMap, roomKey, "latestMessageUser", room.getLatestMessageUser());
        putRoomInfo(groupRoomMap, roomKey, "latestMessageTime", room.getLatestMessageTime());
        putRoomInfo(groupRoomMap, roomKey, "type", room.getType());
        putRoomInfo(groupRoomMap, roomKey, "created", room.getCreated());
    }

    private void putRoomInfo(HashMap<String, Object> roomMap, String roomKey, String key, Object value) {
        roomMap.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_INFO, roomKey, key), value);
    }

    private void putUsersRoomData(HashMap<String, Object> groupRoomMap, String roomKey, long roomCreated) {

        for (UserInfo userInfo : usersInfo) {
            String userKey = userInfo.getUserKey();

            groupRoomMap.put(TextUtil.getPath(KEY_ROOMS, KEY_ROOMS_MEMBERS, roomKey, userKey), roomCreated);
            groupRoomMap.put(TextUtil.getPath(KEY_USERS, KEY_USERS_USER_ROOMS, userKey, roomKey), roomCreated);
            groupRoomMap.put(TextUtil.getPath(KEY_USERS, KEY_USERS_USER_GROUP_ROOMS, userKey, roomKey), roomCreated);
        }
    }

    private void putRoomMessage(HashMap<String, Object> roomMap, String roomKey, Message messageRoom) {
        String messageKey = messageFirebase.get().child(roomKey).push().getKey();

        putMessageInfo(roomMap, roomKey, messageKey, "roomId", messageRoom.getRoomId());
        putMessageInfo(roomMap, roomKey, messageKey, "message", messageRoom.getMessage());
        putMessageInfo(roomMap, roomKey, messageKey, "sentBy", messageRoom.getSentBy());
        putMessageInfo(roomMap, roomKey, messageKey, "sentWhen", messageRoom.getSentWhen());
    }

    private void putMessageInfo(HashMap<String, Object> roomMap, String roomKey, String messageKey, String key, Object value) {
        roomMap.put(TextUtil.getPath(KEY_MESSAGES, roomKey, messageKey, key), value);
    }

    private String createGroupRoomName() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0, size = usersInfo.length; i < size; i++) {
            String firstName = usersInfo[i].getDisplayName().split(" ")[0];

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

            if (onImageRequestListener != null) {
                onImageRequestListener.onImageReceived(base64Bitmap);
            }

            bitmapBase64Result = base64Bitmap;
            onMergedBitmapSuccess(base64Bitmap);
        }
    }

    public interface OnImageRequestListener {
        void onImageReceived(String base64Bitmap);
    }

}
