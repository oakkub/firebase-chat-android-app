package com.oakkub.chat.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
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
import com.oakkub.chat.activities.AddFriendActivity;
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.activities.SplashScreenActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.MyLifeCycleHandler;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GCMListenerService extends GcmListenerService {

    private static final String TAG = GCMListenerService.class.getSimpleName();

    private static final int CODE_ACCEPT_FRIEND = 100;
    private static final int CODE_REJECT_FRIEND = 101;
    private static final int CODE_FRIEND_REQUEST_CONTENT = 102;
    private static final int CODE_FRIEND_REQUEST_NOTIFY = 103;
    private static final int CODE_ACCEPTED_FRIEND = 104;

    private static final int MODE_NEW_MESSAGE = 0;
    private static final int MODE_NEW_FRIEND_REQUEST = 1;
    private static final int MODE_FRIEND_ACCEPTED = 2;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    private String uid;

    private String title;
    private String message;
    private String sentBy;
    private String sentByDisplayName;

    private String notifyType;

    private String roomId;
    private boolean isPrivateRoom;

    private String ringtoneUri;
    private boolean isNotificationEnabled;
    private boolean isNotificationVibrateEnabled;

    private AuthData authData;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        AppController.getComponent(this).inject(this);
        getNotificationSettings();
        getDataBundle(data);
        debug(from, data);

        if (!shouldShowNotification()) return;

        authData = firebase.getAuth();

        if (authData == null || sentBy.equals(authData.getUid())) return;
        else uid = authData.getUid();

        switch (notifyType) {
            case GCMUtil.CHAT_NEW_MESSAGE_NOTIFY_TYPE:
                notifyNewMessage();
                break;
            case GCMUtil.FRIEND_REQUEST_NOTIFY_TYPE:
                fetchImageUri(MODE_NEW_FRIEND_REQUEST);
                break;
            case GCMUtil.FRIEND_ACCEPTED_NOTIFY_TYPE:
                fetchImageUri(MODE_FRIEND_ACCEPTED);
                break;
            default:
                break;
        }
    }

    private boolean shouldShowNotification() {
        if (!isNotificationEnabled) return false;

        if (MyLifeCycleHandler.isForeground()) {
            String currentActivity = MyLifeCycleHandler.getCurrentActivityName();

            if (currentActivity.equals(ChatRoomActivity.class.getSimpleName())) {
                if (notifyType.equals(GCMUtil.CHAT_NEW_MESSAGE_NOTIFY_TYPE)) return false;
            } else if (currentActivity.equals(AddFriendActivity.class.getSimpleName())) {
                if (notifyType.equals(GCMUtil.FRIEND_REQUEST_NOTIFY_TYPE) ||
                    notifyType.equals(GCMUtil.FRIEND_ACCEPTED_NOTIFY_TYPE)) return false;
            }
        }

        return true;
    }

    private void getDataBundle(Bundle data) {
        title = data.getString(GCMUtil.DATA_TITLE);
        message = data.getString(GCMUtil.DATA_MESSAGE);
        sentBy = data.getString(GCMUtil.DATA_SENT_BY);
        roomId = data.getString(GCMUtil.DATA_ROOM_ID);
        sentByDisplayName = data.getString(GCMUtil.DATA_DISPLAY_NAME);
        isPrivateRoom = roomId != null && roomId.startsWith("chat_");

        notifyType = data.getString(GCMUtil.NOTIFY_TYPE, "");
    }

    private void getNotificationSettings() {
        SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
        isNotificationEnabled = prefs.getBoolean(getString(R.string.pref_notification_enable),
                false);
        if (!isNotificationEnabled) return;

        ringtoneUri = prefs.getString(getString(R.string.pref_notification_ringtone), "");
        isNotificationVibrateEnabled = prefs.getBoolean(
                getString(R.string.pref_notification_vibrate), false);
    }

    private class ImageUriFirebase implements ValueEventListener {

        private int mode;

        public ImageUriFirebase(int mode) {
            this.mode = mode;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String uri = dataSnapshot.getValue(String.class);
            subscribeBitmap(uri, mode);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    }

    private class BitmapSubscriber extends BaseBitmapDataSubscriber {

        private int mode;

        public BitmapSubscriber(int mode) {
            this.mode = mode;
        }

        @Override
        protected void onNewResultImpl(Bitmap bitmap) {
            if (authData == null || sentBy.equals(authData.getUid())) return;

            switch (mode) {
                case MODE_NEW_MESSAGE:
                    onNewMessageReceivedBitmap(bitmap);
                    break;
                case MODE_NEW_FRIEND_REQUEST:
                    onNewFriendRequestReceivedBitmap(bitmap);
                    break;
                case MODE_FRIEND_ACCEPTED:
                    onFriendAcceptedReceivedBitmap(bitmap);
                    break;
            }
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
        }
    }

    private void notifyNewMessage() {
        String imagePath = isPrivateRoom ?
                TextUtil.getPath(FirebaseUtil.KEY_USERS, FirebaseUtil.KEY_USERS_USER_INFO, sentBy, FirebaseUtil.CHILD_PROFILE_IMAGE_URL)
                :
                TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_INFO, roomId, FirebaseUtil.CHILD_IMAGE_PATH);
        firebase.child(imagePath)
                .addListenerForSingleValueEvent(new ImageUriFirebase(MODE_NEW_MESSAGE));
    }

    private void fetchImageUri(int mode) {
        userInfoFirebase.get().child(sentBy).child(UserInfoUtil.PROFILE_IMAGE_URL)
                .addListenerForSingleValueEvent(new ImageUriFirebase(mode));
    }

    private void subscribeBitmap(String uri, int mode) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = getImageRequest(uri);

        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline
                .fetchDecodedImage(imageRequest, this);
        dataSource.subscribe(new BitmapSubscriber(mode), CallerThreadExecutor.getInstance());
    }

    private ImageRequest getImageRequest(String uri) {
        return ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri)).build();
    }

    private void onNewMessageReceivedBitmap(Bitmap bitmap) {
        final NotificationCompat.Builder newMessageNotification = getDefaultNotification(bitmap);
        newMessageNotification.setSmallIcon(R.drawable.ic_chat);

        roomInfoFirebase.get().child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                Room room = dataSnapshot.getValue(Room.class);
                room.setRoomId(roomId);

                newMessageNotification.setContentIntent(getNewMessageTaskStack(room));
                GCMListenerService.this.notify(room.hashCode(), newMessageNotification.build());

                dataSnapshot.getRef().removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void onNewFriendRequestReceivedBitmap(Bitmap bitmap) {
        NotificationCompat.Builder newFriendRequestNotification = getDefaultNotification(bitmap);
        newFriendRequestNotification.setSmallIcon(R.drawable.ic_person_add);

        Intent acceptService = getFriendRequestService(FriendRequestActionService.CODE_ACCEPT_FRIEND);
        Intent rejectService = getFriendRequestService(FriendRequestActionService.CODE_REJECT_FRIEND);

        PendingIntent pendingAccept = PendingIntent.getService(this,
                sentBy.hashCode() + FriendRequestActionService.CODE_ACCEPT_FRIEND,
                acceptService, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingReject = PendingIntent.getService(this,
                sentBy.hashCode() + FriendRequestActionService.CODE_REJECT_FRIEND,
                rejectService, PendingIntent.FLAG_UPDATE_CURRENT);

        newFriendRequestNotification.addAction(R.drawable.ic_check_white_24dp,
                getString(R.string.accept), pendingAccept);
        newFriendRequestNotification.addAction(R.drawable.ic_close_white_24dp,
                getString(R.string.reject), pendingReject);

        newFriendRequestNotification.setContentIntent(getNewFriendRequestTaskStack());
        notify(sentBy.hashCode(), newFriendRequestNotification.build());
    }

    private void onFriendAcceptedReceivedBitmap(Bitmap bitmap) {
        NotificationCompat.Builder acceptedFriendNotification = getDefaultNotification(bitmap);
        acceptedFriendNotification.setSmallIcon(R.drawable.ic_person_add);

        TaskStackBuilder stackBuilder = getDefaultTaskStackBuilder();

        acceptedFriendNotification.setContentIntent(
                stackBuilder.getPendingIntent(sentBy.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT));
        notify(sentBy.hashCode(), acceptedFriendNotification.build());
    }

    private TaskStackBuilder getDefaultTaskStackBuilder() {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(new Intent(this, SplashScreenActivity.class));
        return taskStackBuilder;
    }

    private Intent getFriendRequestService(int code) {
        Intent intent = new Intent(this, FriendRequestActionService.class);
        intent.putExtra(FriendRequestActionService.EXTRA_CODE, code);
        intent.putExtra(FriendRequestActionService.EXTRA_FRIEND_KEY, sentBy);
        intent.putExtra(FriendRequestActionService.EXTRA_DISPLAY_NAME, sentByDisplayName);
        return intent;
    }

    private PendingIntent getNewFriendRequestTaskStack() {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(AddFriendActivity.class);

        Intent intent = AddFriendActivity.getStartIntent(this, uid, 2);
        stackBuilder.addNextIntent(intent);

        return stackBuilder.getPendingIntent(
                sentBy.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getNewMessageTaskStack(Room room) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        Intent messageIntent;
        if (isPrivateRoom) {
            messageIntent = ChatRoomActivity
                    .getIntentPrivateRoom(this, room, uid);
        } else {
            messageIntent = ChatRoomActivity
                    .getIntentGroupRoom(this, room);
        }
        // For some unspecified reason, extras will be delivered only if you've set some action
        messageIntent.setAction(room.getRoomId());
        messageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        stackBuilder.addNextIntent(messageIntent);

        return stackBuilder.getPendingIntent(
                room.hashCode(), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationCompat.Builder getDefaultNotification(Bitmap bitmap) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(bitmap)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (isNotificationVibrateEnabled) {
            builder.setVibrate(new long[] { 0, 100 });
        }

        if (!TextUtils.isEmpty(ringtoneUri)) {
            builder.setSound(Uri.parse(ringtoneUri));
        }

        return builder;
    }

    private void notify(int id, Notification notification) {
        if (!isNotificationEnabled) return;

        NotificationManagerCompat notificationManager = AppController.getComponent(this).notificationManager();
        notificationManager.notify(id, notification);
    }

    private void debug(String from, Bundle data) {
        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Title: " + data.getString(GCMUtil.DATA_TITLE));
        Log.e(TAG, "Message: " + data.getString(GCMUtil.DATA_MESSAGE));
        Log.e(TAG, "Profile URL: " + data.getString(GCMUtil.PROFILE_URL));
        Log.e(TAG, "shouldShowNotification: " + shouldShowNotification());

        if (from.startsWith(GCMUtil.START_TOPICS)) {
            Log.e(TAG, "TOPICS");

        } else {
            Log.e(TAG, "DEFAULT DATA_MESSAGE");
        }
    }

}
