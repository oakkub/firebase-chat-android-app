package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

public class PrivateChatRoomActivityFragment extends BaseFragment implements ChildEventListener {

    public static final String EXTRA_MY_ID = "extra:myId";
    public static final String EXTRA_ROOM_ID = "extra:roomId";
    public static final String EXTRA_FRIEND_ID = "extra:friendId";
    public static final String EXTRA_ROOM_NAME = "extra:friendName";
    public static final String EXTRA_FRIEND_PROFILE = "extra:friendProfile";
    public static final String EXTRA_ROOM_IMAGE = "extra:roomImage";
    private static final String TAG = PrivateChatRoomActivityFragment.class.getSimpleName();
    private static final int DOWNLOAD_MESSAGE_ITEM_LIMIT = 20;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES)
    Firebase messagesFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Firebase roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Lazy<Firebase> roomMembersFirebase;

    private String myId;
    private String roomId;
    private String roomName;
    private String latestMessageKey = "";

    private String privateFriendInstanceId;
    private String groupNewMemberKey;

    private boolean isMessageSending;

    private boolean isLoadMoreFailed;
    private boolean isLoadMoreNoData;
    private long latestMessageSentWhen;

    private int totalGroupMember;
    private SparseArray<String> friendImages;

    private SparseArray<String> friendDisplayNames;
    private SparseArray<String> friendInfoKeyList;
    private SparseArray<UserInfo> friendInfoList;
    private ArrayList<String> userIdInRoom;

    private ArrayList<String> profileImagesInRoom;
    private ArrayList<Message> oldMessages;

    private ArrayList<Message> newMessages;
    private HashMap<String, Object> updatedRoomMap;

    private MessageRequestListener messageRequestListener;

    private OnInfiniteScrollListener onInfiniteScrollListener;
    private ToolbarCommunicator toolbarCommunicator;

    private boolean isPrivateRoom;
    private boolean groupFirstTime = true;

    private boolean isGroupRoomFirebaseInit;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        messageRequestListener = (MessageRequestListener) activity;
        onInfiniteScrollListener = (OnInfiniteScrollListener) activity;
        toolbarCommunicator = (ToolbarCommunicator) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataFromIntent();
        setRetainInstance(true);

        updatedRoomMap = new HashMap<>();
        friendInfoKeyList = new SparseArray<>();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitleFromFriendNameByRoomId();

        if (isToolbarCommunicatorAvailable()) {

            if (roomName != null) {
                toolbarCommunicator.setTitle(roomName);
            }
        }

        if (isMessageRequestAvailable()) {

            if (isPrivateRoom) {
                messageRequestListener.onPrivateRoomReady(myId, initPrivateFriendInfo());
            } else {
                // fetch all room member
                fetchFriendsInfo();
            }

            if (groupNewMemberKey != null) {
                messageRequestListener.onNewGroupMember(myId,
                        friendImages.get(groupNewMemberKey.hashCode()),
                        friendDisplayNames.get(groupNewMemberKey.hashCode()));
                groupNewMemberKey = null;
            }

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

    private boolean isToolbarCommunicatorAvailable() { return toolbarCommunicator != null; }

    private boolean isMessageRequestAvailable() {
        return messageRequestListener != null;
    }

    private boolean isLoadMoreFailedAvailable() {
        return onInfiniteScrollListener != null;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isPrivateRoom) {
            initFirebaseMessage();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isPrivateRoom) {
            messagesFirebase.removeEventListener(this);
        }
    }

    private SparseArray<String> initPrivateFriendInfo() {
        if (friendImages != null) return friendImages;

        friendImages = new SparseArray<>();
        friendImages.put(userIdInRoom.get(0).hashCode(), profileImagesInRoom.get(0));

        return friendImages;
    }

    private void fetchFriendsInfo() {
        if (friendInfoKeyList.size() > 0) {
            messageRequestListener.onGroupRoomReady(myId, friendImages, friendDisplayNames);
            return;
        }

        initGroupRoomVariables();
        fetchTotalGroupMember();
    }

    private void initGroupRoomVariables() {
        if (friendImages == null) {
            friendImages = new SparseArray<>();
        }

        if (friendDisplayNames == null) {
            friendDisplayNames = new SparseArray<>();
        }

        if (friendInfoList == null) {
            friendInfoList = new SparseArray<>();
        }
    }

    private void fetchTotalGroupMember() {
        roomMembersFirebase.get().child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totalGroupMember = (int) dataSnapshot.getChildrenCount();

                        if (groupFirstTime) {
                            fetchFriendInfoGroupMember();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });
    }

    private void fetchFriendInfoGroupMember() {
        roomMembersFirebase.get().child(roomId)
                .addChildEventListener(roomMembersChildEventListener);
    }

    private ChildEventListener roomMembersChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
            getMemberInfo(dataSnapshot);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {

        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            totalGroupMember -= 1;
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {}
        @Override
        public void onCancelled(FirebaseError firebaseError) {}
    };

    private void getMemberInfo(DataSnapshot dataSnapshot) {
        final String memberKey = dataSnapshot.getKey();
        final int memberKeyHashcode = memberKey.hashCode();

        userInfoFirebase.get().child(memberKey).keepSynced(true);
        userInfoFirebase.get().child(memberKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (groupFirstTime) groupFirstTime = true;
                        onMemberInfoFetched(memberKey, memberKeyHashcode, dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void onMemberInfoFetched(String memberKey, int memberKeyHashcode, DataSnapshot dataSnapshot) {
        friendInfoKeyList.put(memberKeyHashcode, memberKey);

        UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
        friendInfoList.put(memberKeyHashcode, friendInfo);
        friendImages.put(memberKeyHashcode, friendInfo.getProfileImageURL());
        friendDisplayNames.put(memberKeyHashcode, friendInfo.getDisplayName());

        boolean isNewMember = totalGroupMember < friendInfoList.size();

        friendInfoNewMember(isNewMember, memberKey, friendInfo);
        allFriendInfoFetched();
    }

    private void friendInfoNewMember(boolean isNewMember, String memberKey, UserInfo friendInfo) {
        if (isNewMember && isGroupRoomFirebaseInit) {
            totalGroupMember += 1;

            if (isMessageRequestAvailable()) {
                messageRequestListener.onNewGroupMember(memberKey,
                        friendInfo.getProfileImageURL(), friendInfo.getDisplayName());
            } else {
                groupNewMemberKey = memberKey;
            }
        }
    }

    private void allFriendInfoFetched() {
        if (friendInfoList.size() == totalGroupMember && !isGroupRoomFirebaseInit) {
            if (isMessageRequestAvailable()) {
                messageRequestListener.onGroupRoomReady(myId, friendImages, friendDisplayNames);
            }

            if (!isGroupRoomFirebaseInit) {
                // initialize firebase for group room here,
                // since we don't know when all of our friend info will be fetched.
                initFirebaseMessage();
                isGroupRoomFirebaseInit = true;
            }
        }
    }

    private void fetchGroupMemberKey(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChildren()) {

            for (DataSnapshot children : dataSnapshot.getChildren()) {

                String groupMemberKey = children.getValue(String.class);
                final int groupMemberHashCode = groupMemberKey.hashCode();
                friendInfoKeyList.put(groupMemberHashCode, groupMemberKey);

                userInfoFirebase.get().child(groupMemberKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
                                friendImages.put(groupMemberHashCode, friendInfo.getProfileImageURL());
                                friendDisplayNames.put(groupMemberHashCode, friendInfo.getDisplayName());

                                if (friendImages.size() == totalGroupMember) {
                                    if(isMessageRequestAvailable()) {
                                        messageRequestListener.onGroupRoomReady(myId, friendImages, friendDisplayNames);
                                    } else {

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {}
                        });
            }
        }
    }

    private void getDataFromIntent() {
        Intent intent = getActivity().getIntent();

        myId = intent.getStringExtra(EXTRA_MY_ID);
        roomId = intent.getStringExtra(EXTRA_ROOM_ID);
        roomName = intent.getStringExtra(EXTRA_ROOM_NAME);
        profileImagesInRoom = intent.getStringArrayListExtra(EXTRA_FRIEND_PROFILE);
        userIdInRoom = intent.getStringArrayListExtra(EXTRA_FRIEND_ID);

        isPrivateRoom = userIdInRoom != null && profileImagesInRoom != null;
    }

    private void setTitleToolbar(String title) {
        if (isToolbarCommunicatorAvailable()) {
            toolbarCommunicator.setTitle(title);
        } else {
            roomName = title;
        }
    }

    public void setToolbarTitleFromFriendNameByRoomId() {
        if (roomName != null) {
            setTitleToolbar(roomName);
            return;
        }

        String friendKey = FirebaseUtil.privateRoomFriendKey(myId, roomId);

        userInfoFirebase.get().child(friendKey).keepSynced(true);
        userInfoFirebase.get().child(friendKey)
                .addListenerForSingleValueEvent(privateFriendInfoValueEventListener);
    }

    private ValueEventListener privateFriendInfoValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);

            roomName = friendInfo.getDisplayName();
            setTitleToolbar(roomName);

            privateFriendInstanceId = friendInfo.getInstanceID();
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

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
        if (latestMessageSentWhen <= 0) {
            messagesFirebase.child(roomId)
                    .limitToLast(DOWNLOAD_MESSAGE_ITEM_LIMIT)
                    .addChildEventListener(this);
        } else {
            messagesFirebase.child(roomId)
                    .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                    .startAt(latestMessageSentWhen)
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
        roomFirebase.child(roomId).updateChildren(latestMessageMap);

        updateMessageSentWhen(message.getSentWhen());
        userRoomFirebase.updateChildren(updatedRoomMap);
    }

    private void updateMessageSentWhen(long sentWhen) {
        updatedRoomMap.put(TextUtil.getPath(myId, roomId), sentWhen);

        if (isPrivateRoom) {
            updatedRoomMap.put(TextUtil.getPath(userIdInRoom.get(0), roomId), sentWhen);
        } else {
            for (int i = 0, size = friendInfoKeyList.size(); i < size; i++) {
                updatedRoomMap.put(TextUtil.getPath(friendInfoKeyList.valueAt(i), roomId), sentWhen);
            }
        }
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

        latestMessageSentWhen = message.getSentWhen();
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
        toolbarCommunicator = null;
    }

    @Override
    public void onDestroy() {
        if (!isPrivateRoom) {
            roomMembersFirebase.get().child(roomId).removeEventListener(roomMembersChildEventListener);
        }

        super.onDestroy();
    }

    public interface MessageRequestListener {
        void onNewMessage(Message newMessage);
        void onNewMessage(ArrayList<Message> newMessages);
        void onOldMessage(ArrayList<Message> oldMessages);
        void onRemoveMessage(Message message);
        void onPrivateRoomReady(String myId, SparseArray<String> friendId);
        void onGroupRoomReady(String myId, SparseArray<String> friendProfileImageList, SparseArray<String> friendDisplayNameList);
        void onNewGroupMember(String newMemberId, String newMemberProfileImage, String newMemberDisplayName);
    }

}
