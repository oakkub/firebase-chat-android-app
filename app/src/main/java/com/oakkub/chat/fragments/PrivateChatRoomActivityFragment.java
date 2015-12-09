package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnInfiniteScrollListener;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class PrivateChatRoomActivityFragment extends BaseFragment implements ChildEventListener {

    public static final String EXTRA_ROOM_ID = "extra:roomId";
    public static final String EXTRA_FRIEND_ID = "extra:friendId";
    public static final String EXTRA_FRIEND_NAME = "extra:friendName";
    public static final String EXTRA_FRIEND_PROFILE = "extra:friendProfile";
    private static final String TAG = PrivateChatRoomActivityFragment.class.getSimpleName();
    private static final int DOWNLOAD_MESSAGE_ITEM_LIMIT = 20;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES)
    Firebase messagesFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Firebase roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomFirebase;

    private String myId;
    private String roomId;
    private String friendId;
    private String friendProfileImage;
    private String latestMessageKey = "";

    private boolean isMessageSending;
    private boolean isLoadMoreFailed;
    private boolean isLoadMoreNoData;

    private long latestSentWhenMessage;

    private SparseArray<String> friendsId;
    private ArrayList<Message> oldMessages;
    private ArrayList<Message> newMessages;
    private MessageRequestListener messageRequestListener;
    private OnInfiniteScrollListener onInfiniteScrollListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        messageRequestListener = (MessageRequestListener) getActivity();
        onInfiniteScrollListener = (OnInfiniteScrollListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataFromIntent();
        setRetainInstance(true);

        myId = messagesFirebase.getAuth().getUid();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isMessageRequestAvailable()) {
            messageRequestListener.onAdapterInitialized(myId, getFriendsImages());

            if (newMessages != null) {
                messageRequestListener.onNewMessage(newMessages);
                newMessages = null;
            }

            if (oldMessages != null) {
                messageRequestListener.onOldMessage(oldMessages);
                oldMessages = null;
            }
        }

        if (isLoadMoreFailedAvailable()) {
            if (isLoadMoreFailed) {
                onInfiniteScrollListener.onLoadMoreFailed();
                isLoadMoreFailed = false;
            }

            if (isLoadMoreNoData) {
                onInfiniteScrollListener.onNoMoreOlderData();
                isLoadMoreNoData = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        initFirebaseMessage();
    }

    @Override
    public void onPause() {
        super.onPause();

        messagesFirebase.removeEventListener(this);
    }

    private boolean isMessageRequestAvailable() {
        return messageRequestListener != null;
    }

    private boolean isLoadMoreFailedAvailable() {
        return onInfiniteScrollListener != null;
    }

    private SparseArray<String> getFriendsImages() {
        if (friendsId != null) return friendsId;

        friendsId = new SparseArray<>();
        friendsId.put(friendId.hashCode(), friendProfileImage);

        return friendsId;
    }

    private void getDataFromIntent() {
        Intent intent = getActivity().getIntent();

        roomId = intent.getStringExtra(EXTRA_ROOM_ID);
        friendId = intent.getStringExtra(EXTRA_FRIEND_ID);
        friendProfileImage = intent.getStringExtra(EXTRA_FRIEND_PROFILE);
    }

    public void loadItemMore(long oldestSentTime) {
        messagesFirebase
                .child(roomId)
                .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                .endAt(oldestSentTime)
                .limitToLast(DOWNLOAD_MESSAGE_ITEM_LIMIT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<Message> messageList = createMessageList(dataSnapshot);
                        addOlderMessages(messageList, dataSnapshot);
                        messageList.remove(0);

                        if (isMessageRequestAvailable()) {
                            messageRequestListener.onOldMessage(messageList);
                        } else {
                            oldMessages = messageList;
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, "Cannot load more: " + firebaseError.getMessage());

                        if (isLoadMoreFailedAvailable()) {
                            onInfiniteScrollListener.onLoadMoreFailed();
                        } else {
                            isLoadMoreFailed = true;
                        }
                    }
                });

    }

    private ArrayList<Message> createMessageList(DataSnapshot dataSnapshot) {
        final int count = (int) dataSnapshot.getChildrenCount();
        ArrayList<Message> messageList = new ArrayList<>(count);

        if (count < DOWNLOAD_MESSAGE_ITEM_LIMIT) {
            if (isLoadMoreFailedAvailable()) {
                onInfiniteScrollListener.onNoMoreOlderData();
            } else {
                isLoadMoreNoData = true;
            }
        }

        return messageList;
    }

    private void addOlderMessages(ArrayList<Message> messageList, DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Message message = getMessage(postSnapshot);
            messageList.add(0, message);
        }
    }

    private void removeDuplicateMessages(ArrayList<Message> messageList, Message lastMessage) {
        for (int i = 0, size = messageList.size(); i < size; i++) {
            if (messageList.get(i).equals(lastMessage)) {
                messageList.remove(i);
                break;
            }
        }
    }

    private void initFirebaseMessage() {
        if (latestSentWhenMessage <= 0) {
            messagesFirebase.child(roomId)
                    .limitToLast(DOWNLOAD_MESSAGE_ITEM_LIMIT)
                    .addChildEventListener(this);
        } else {
            messagesFirebase.child(roomId)
                    .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                    .startAt(latestSentWhenMessage)
                    .addChildEventListener(this);
        }
    }

    public void onSendButtonClick(String messageText) {
        if (!isMessageSending) isMessageSending = true;
        else return;

        if (messageText.length() == 0) {
            isMessageSending = false;
            return;
        }

        sendMessage(messageText);
    }

    private void sendMessage(final String messageText) {
        final Message message = new Message(roomId, messageText, myId);

        messagesFirebase.child(roomId).push().setValue(message, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(TAG, firebaseError.getMessage());
                    messageRequestListener.onRemoveMessage(message);
                }

                updateRoomInfo(message, false);
                isMessageSending = false;
            }
        });
    }

    private void updateRoomInfo(Message message, boolean itemRemoveUpdate) {
        Map<String, Object> latestMessageMap = getMessageMap(message, itemRemoveUpdate);
        notifyNewMessageToRoom(message.getSentWhen());

        roomFirebase.child(roomId).updateChildren(latestMessageMap);
    }

    private void notifyNewMessageToRoom(long sentWhen) {
        userRoomFirebase.child(friendId).child(roomId).setValue(sentWhen);
    }

    private Map<String, Object> getMessageMap(Message message, boolean itemRemoveUpdate) {
        Map<String, Object> messageMap = new HashMap<>(3);
        messageMap.put(FirebaseUtil.CHILD_LATEST_MESSAGE_TIME, message.getSentWhen());
        messageMap.put(FirebaseUtil.CHILD_LATEST_MESSAGE, message.getMessage());
        messageMap.put(FirebaseUtil.CHILD_LATEST_MESSAGE_USER, itemRemoveUpdate ? message.getSentBy() : myId);

        return messageMap;
    }

    private void updateRoomInfoWhenDeleted() {
        messagesFirebase.child(roomId)
                .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                .endAt(System.currentTimeMillis())
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot childrenDataSnapshot = dataSnapshot.getChildren().iterator().next();

                        Message latestMessageAfterDeleted = getMessage(childrenDataSnapshot);
                        updateRoomInfo(latestMessageAfterDeleted, true);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    public Message getMessage(DataSnapshot dataSnapshot) {
        Message message = dataSnapshot.getValue(Message.class);
        message.setMessageKey(dataSnapshot.getKey());

        return message;
    }

    private void sendNewMessage(Message message) {
        if (isMessageRequestAvailable()) {
            messageRequestListener.onNewMessage(message);
        } else {
            if (newMessages == null) {
                newMessages = new ArrayList<>();
            }
            newMessages.add(message);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        if (latestMessageKey.equals(dataSnapshot.getKey())) return;
        Message message = getMessage(dataSnapshot);

        latestSentWhenMessage = message.getSentWhen();
        latestMessageKey = message.getMessageKey();
        sendNewMessage(message);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
    }

    @Override
    public void onDetach() {
        super.onDetach();

        messageRequestListener = null;
        onInfiniteScrollListener = null;
    }

    public interface MessageRequestListener {
        void onNewMessage(Message newMessage);

        void onNewMessage(ArrayList<Message> newMessages);

        void onOldMessage(ArrayList<Message> oldMessages);

        void onRemoveMessage(Message message);

        void onAdapterInitialized(String myId, SparseArray<String> friendId);
    }

}
