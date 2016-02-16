package com.oakkub.chat.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.gcm.GcmListenerService;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.widgets.MyToast;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMListenerService extends GcmListenerService {

    private static final String TAG = GCMListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString(GCMUtil.DATA_TITLE);
        String message = data.getString(GCMUtil.DATA_MESSAGE);
        String roomId = data.getString(GCMUtil.DATA_ROOM_ID);

        debug(from, data);

        if (roomId != null) {
            notifyNewMessage(data, title, message, roomId);
        }
    }

    private void notifyNewMessage(Bundle data, String title, String message, String roomId) {
        String sentBy = data.getString(GCMUtil.DATA_SENT_BY);
        fetchMyProfileImage(title, message, sentBy, roomId);
    }

    private void fetchMyProfileImage(final String title, final String message, final String sentBy, final String roomId) {
        Firebase firebase = AppController.getComponent(this).firebase();
        AuthData authData = firebase.getAuth();
        if (authData == null || sentBy.equals(authData.getUid())) {
            MyToast.make("id is the same as the current user").show();
            return;
        }

        final boolean isPrivateRoom = roomId.startsWith("chat_");
        String imagePath = isPrivateRoom ?
                TextUtil.getPath(FirebaseUtil.KEY_USERS, FirebaseUtil.KEY_USERS_USER_INFO, sentBy, FirebaseUtil.CHILD_PROFILE_IMAGE_URL)
                :
                TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_INFO, roomId, FirebaseUtil.CHILD_IMAGE_PATH);
        firebase.child(imagePath).keepSynced(true);
        firebase.child(imagePath)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String image = dataSnapshot.getValue(String.class);
                        receivedNotification(title, message, image, sentBy, roomId, isPrivateRoom);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    private void receivedNotification(final String title, final String message,
                                      String image, final String sentBy, final String roomId,
                                      final boolean isPrivateRoom) {

        final ImagePipeline imagePipeline = Fresco.getImagePipeline();

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(image))
                .build();

        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                NotificationCompat.Builder newMessageNotification = getDefaultNotification(title, message)
                        .setSmallIcon(R.drawable.ic_sms_24dp)
                        .setLargeIcon(bitmap)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                Room room = new Room();
                room.setName(title);
                room.setRoomId(roomId);

                Log.d(TAG, "onNewResultImpl: " + room.toString());

                newMessageNotification.setContentIntent(getTaskStackRoom(room, isPrivateRoom, sentBy));
                showNotification(room.hashCode(), newMessageNotification.build());
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

            }
        }, CallerThreadExecutor.getInstance());
    }

    private PendingIntent getTaskStackRoom(Room room, boolean isPrivateRoom, String sentBy) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        if (isPrivateRoom) {
            String myId = FirebaseUtil.getPrivateRoomFriendKey(sentBy, room.getRoomId());
            stackBuilder.addNextIntent(ChatRoomActivity
                    .getIntentPrivateRoom(GCMListenerService.this, room, myId));
        } else {
            Firebase firebase = new Firebase(FirebaseUtil.FIREBASE_URL);
            AuthData authData = firebase.getAuth();
            if (authData != null) {
                stackBuilder.addNextIntent(ChatRoomActivity
                        .getIntentGroupRoom(GCMListenerService.this, room, authData.getUid()));
            }
        }
        return stackBuilder
                .getPendingIntent(room.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.Builder getDefaultNotification(String title, String message) {
        return new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
    }

    private void showNotification(int id, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    private void debug(String from, Bundle data) {
        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Title: " + data.getString(GCMUtil.DATA_TITLE));
        Log.e(TAG, "Message: " + data.getString(GCMUtil.DATA_MESSAGE));
        Log.e(TAG, "Profile URL: " + data.getString(GCMUtil.PROFILE_URL));

        if (from.startsWith(GCMUtil.START_TOPICS)) {
            Log.e(TAG, "TOPICS");

        } else {
            Log.e(TAG, "DEFAULT DATA_MESSAGE");
        }
    }

}
