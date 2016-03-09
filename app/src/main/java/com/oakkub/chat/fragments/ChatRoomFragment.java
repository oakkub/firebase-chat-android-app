package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.managers.OnRecyclerViewInfiniteScrollListener;
import com.oakkub.chat.managers.SparseStringArray;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusDeleteGroupRoom;
import com.oakkub.chat.models.eventbus.EventBusDeletePublicChat;
import com.oakkub.chat.services.GCMNotifyService;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.BitmapUtil;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import de.greenrobot.event.EventBus;

public class ChatRoomFragment extends BaseFragment implements ChildEventListener {

    public static final String EXTRA_MY_ID = "extra:uid";
    public static final String EXTRA_FRIEND_ID = "extra:friendId";
    public static final String EXTRA_IS_MEMBER = "extra:isMember";
    public static final String EXTRA_ROOM = "extra:room";
    private static final String TAG = ChatRoomFragment.class.getSimpleName();
    private static final int DOWNLOAD_MESSAGE_ITEM_LIMIT = 20;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_LIST)
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
    @Named(FirebaseUtil.NAMED_ROOMS_PRESERVED_MEMBERS)
    Lazy<Firebase> preservedRoomMembersFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Lazy<Firebase> roomMembersFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_READ_GROUP_ROOM)
    Lazy<Firebase> readGroupRoomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_TYPING)
    Lazy<Firebase> messagesTypingFirebase;

    private String roomId;
    private String latestMessageKey = "";
    private boolean isMember;
    private boolean isPublicChat;
    private Room extraRoom;

    private String groupNewMemberKey;

    private UserInfo privateFriendInfo;

    private boolean isMessageSending;
    private boolean isLoadMoreFailed;
    private boolean isLoadMoreNoData;
    private boolean isTypingMessage;
    private boolean isPrivateRoom;
    private boolean emptyMessage;
    private boolean isGroupRoomFirebaseInit;
    private boolean isFriendTypingMessage;
    private boolean onJoinRoomSuccess;
    private boolean onJoinRoomFailed;
    private boolean isRemovedByAdmin;
    private long latestMessageSentWhen;

    private int totalGroupMember;

    private String privateFriendId;
    private String privateFriendName;
    private SparseArray<UserInfo> preservedGroupMemberList;
    private SparseStringArray instanceIdList;
    private ArrayMap<String, ValueEventListener> eventListenerMemberList;

    private ArrayList<Message> oldMessages;
    private ArrayList<Message> newMessages;
    private ArrayMap<String, Object> messageMap ;
    private Message sentMessage;
    private Message changedMessage;

    private MessageRequestListener messageRequestListener;
    private OnRecyclerViewInfiniteScrollListener onRecyclerViewInfiniteScrollListener;
    private ToolbarCommunicator toolbarCommunicator;

    private Uri cameraImageUri;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        messageRequestListener = (MessageRequestListener) activity;
        onRecyclerViewInfiniteScrollListener = (OnRecyclerViewInfiniteScrollListener) activity;
        toolbarCommunicator = (ToolbarCommunicator) activity;
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

        if (savedInstanceState == null) {
            getDataIntent();
            checkEmptyMessages();
        }

        if (extraRoom.getName() != null) {
            setTitleToolbar(extraRoom.getName());
        }

        fetchPrivateRoomFriendInfo();

        if (isMessageRequestAvailable()) {

            if (isPrivateRoom) {
                if (privateFriendInfo != null) {
                    messageRequestListener.onPrivateRoomReady(privateFriendInfo, true);
                }
                if (savedInstanceState == null) {
                    isFriendTyping();
                }
            } else {
                // fetch all room member
                fetchFriendsInfo();
            }

            if (groupNewMemberKey != null) {
                messageRequestListener.onNewGroupMember(
                        preservedGroupMemberList.get(groupNewMemberKey.hashCode()));
                groupNewMemberKey = null;
            }

            if (emptyMessage) {
                messageRequestListener.onEmptyMessage();
                emptyMessage = false;
            }

            if (newMessages != null) {
                messageRequestListener.onNewMessage(newMessages);
                newMessages = null;
            }

            if (oldMessages != null) {
                messageRequestListener.onOldMessage(oldMessages);
                oldMessages = null;
            }

            if (sentMessage != null) {
                messageRequestListener.onMessageSent(sentMessage);
                sentMessage = null;
            }

            if (changedMessage != null) {
                messageRequestListener.onMessageChanged(changedMessage);
                changedMessage = null;
            }

            if (isFriendTypingMessage) {
                if (privateFriendInfo != null) {
                    messageRequestListener.onTypingMessagePrivateRoom(false, privateFriendName);
                    isFriendTypingMessage = false;
                }
            }

            if (onJoinRoomFailed) {
                messageRequestListener.onJoinRoomFailed();
                onJoinRoomFailed = false;
            }

            if (onJoinRoomSuccess) {
                messageRequestListener.onJoinRoomSuccess();
                onJoinRoomSuccess = false;
            }

            if (isRemovedByAdmin) {
                messageRequestListener.onRemovedByAdmin();
                isRemovedByAdmin = false;
            }

        }

        if (isLoadMoreFailedAvailable()) {
            if (isLoadMoreFailed) {
                onRecyclerViewInfiniteScrollListener.onLoadMoreFailed();
                isLoadMoreFailed = false;
            }

            if (isLoadMoreNoData) {
                onRecyclerViewInfiniteScrollListener.onLoadMoreNoData();
                isLoadMoreNoData = false;
            }
        }

    }

    private boolean isToolbarCommunicatorAvailable() { return toolbarCommunicator != null; }

    private boolean isMessageRequestAvailable() {
        return messageRequestListener != null;
    }

    private boolean isLoadMoreFailedAvailable() {
        return onRecyclerViewInfiniteScrollListener != null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isPrivateRoom) {
            initFirebaseMessage();
        } else {
            initRoomMemberChecker();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isPrivateRoom) {
            getMessagesFirebase().removeEventListener(this);
        } else {
            getRoomMemberChecker().removeEventListener(roomMemberCheckerListener);
        }
    }

    private void fetchFriendsInfo() {
        // group room preparation
        if (preservedGroupMemberList != null) {
            if (isGroupRoomFirebaseInit) {
                messageRequestListener.onGroupRoomReady(preservedGroupMemberList, isPrivateRoom);
                return;
            }
        }

        initGroupRoomVariables();
        fetchTotalGroupMember();
    }

    private void initGroupRoomVariables() {
        if (preservedGroupMemberList == null) {
            preservedGroupMemberList = new SparseArray<>();
        }
    }

    private void fetchTotalGroupMember() {
        preservedRoomMembersFirebase.get().child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totalGroupMember = (int) dataSnapshot.getChildrenCount();

                        if (!dataSnapshot.exists()) {
                            Toast.makeText(getActivity(),
                                    "Something wrong when fetching group member", Toast.LENGTH_LONG).show();
                        }

                        fetchFriendInfoGroupMember();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });
    }

    private void fetchFriendInfoGroupMember() {
        preservedRoomMembersFirebase.get().child(roomId)
                .addChildEventListener(preservedRoomMembersChildEventListener);
    }

    private ChildEventListener preservedRoomMembersChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
            getMemberInfo(dataSnapshot);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {}
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
        String memberKey = dataSnapshot.getKey();

        userInfoFirebase.get().child(memberKey).keepSynced(true);
        userInfoFirebase.get().child(memberKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        onMemberInfoFetched(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void checkIfCurrentMember(final UserInfo friendInfo) {
        if (eventListenerMemberList.get(friendInfo.getKey()) != null) return;

        eventListenerMemberList.put(friendInfo.getKey(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int keyHashcode = key.hashCode();

                if (dataSnapshot.exists()) {
                    if (instanceIdList.get(keyHashcode) == null && !friendInfo.getKey().equals(uid)) {
                        instanceIdList.put(keyHashcode, friendInfo.getInstanceID());
                    }
                } else {
                    instanceIdList.remove(keyHashcode);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        roomMembersFirebase.get().child(roomId).child(friendInfo.getKey())
                .addValueEventListener(eventListenerMemberList.get(friendInfo.getKey()));
    }

    private void onMemberInfoFetched(DataSnapshot dataSnapshot) {
        if (eventListenerMemberList == null) {
            eventListenerMemberList = new ArrayMap<>();
        }
        if (instanceIdList == null) {
            instanceIdList = new SparseStringArray();
        }

        String friendInfoKey = dataSnapshot.getKey();

        UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
        friendInfo.setKey(friendInfoKey);

        checkIfCurrentMember(friendInfo);
        int friendInfoHashCode = friendInfoKey.hashCode();

        preservedGroupMemberList.put(friendInfoHashCode, friendInfo);

        boolean isNewMember = totalGroupMember < preservedGroupMemberList.size();

        if (isNewMember) {
            newMember(friendInfoKey, friendInfo);
        } else {
            allFriendInfoFetched();
        }
    }

    private void newMember(String memberKey, UserInfo friendInfo) {
        if (isGroupRoomFirebaseInit) {
            totalGroupMember += 1;

            if (isMessageRequestAvailable()) {
                messageRequestListener.onNewGroupMember(friendInfo);
            } else {
                groupNewMemberKey = memberKey;
            }

            if (instanceIdList.get(friendInfo.getKey().hashCode()) != null) {
                instanceIdList.put(friendInfo.getKey().hashCode(), friendInfo.getInstanceID());
            }
        }
    }

    private void allFriendInfoFetched() {
        if (preservedGroupMemberList.size() == totalGroupMember && !isGroupRoomFirebaseInit) {

            if (isMessageRequestAvailable()) {
                messageRequestListener.onGroupRoomReady(preservedGroupMemberList, isPrivateRoom);
            }

            // initialize firebase for group room here,
            // since we don't know when all of our friend info will be fetched.
            initFirebaseMessage();
            isGroupRoomFirebaseInit = true;
        }
    }

    private void getDataIntent() {
        Intent intent = getActivity().getIntent();

        extraRoom = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ROOM));
        privateFriendId = intent.getStringExtra(EXTRA_FRIEND_ID);
        isMember = intent.getBooleanExtra(EXTRA_IS_MEMBER, false);

        roomId = extraRoom.getRoomId();
        isPrivateRoom = extraRoom.getRoomId().startsWith("chat_");
        isPublicChat = !isPrivateRoom && extraRoom.getTag() != null;
    }

    private void setTitleToolbar(String title) {
        if (isToolbarCommunicatorAvailable()) {
            toolbarCommunicator.setTitle(title);
        } else {
            extraRoom.setName(title);
        }
    }

    public void fetchPrivateRoomFriendInfo() {
        if (privateFriendInfo != null || !isPrivateRoom) {
            return;
        }

        String friendKey = FirebaseUtil.getPrivateRoomFriendKey(uid, roomId);
        userInfoFirebase.get().child(friendKey).keepSynced(true);
        userInfoFirebase.get().child(friendKey)
                .addListenerForSingleValueEvent(privateFriendInfoValueEventListener);
    }

    private ValueEventListener privateFriendInfoValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            privateFriendInfo = dataSnapshot.getValue(UserInfo.class);
            privateFriendInfo.setKey(dataSnapshot.getKey());

            extraRoom.setName(privateFriendInfo.getDisplayName());
            privateFriendName = privateFriendInfo.getDisplayName().split(" ")[0];
            setTitleToolbar(extraRoom.getName());

            if (isMessageRequestAvailable()) {
                messageRequestListener.onPrivateRoomReady(privateFriendInfo, isPrivateRoom);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(TAG, "onCancelled: " + firebaseError.getMessage() );
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
                            onRecyclerViewInfiniteScrollListener.onLoadMoreFailed();
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
                onRecyclerViewInfiniteScrollListener.onLoadMoreNoData();
            } else {
                isLoadMoreNoData = true;
            }
        }

        return messageList;
    }

    private void addOlderMessages(ArrayList<Message> messageList, DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Message message = getMessage(postSnapshot);
            message.successfullySent();

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

    private void checkEmptyMessages() {
        messagesFirebase.child(roomId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            if (messageRequestListener != null) {
                                messageRequestListener.onEmptyMessage();
                            } else {
                                emptyMessage = true;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });
    }

    private Query getRoomMemberChecker() {
        return roomMembersFirebase.get().child(roomId).child(uid);
    }

    private void initRoomMemberChecker() {
        if (isPrivateRoom) return;
        getRoomMemberChecker().addValueEventListener(roomMemberCheckerListener);
    }

    private ValueEventListener roomMemberCheckerListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
            if (!dataSnapshot.exists()) {
                if (isPublicChat && !isMember) return;

                if (isMessageRequestAvailable()) {
                    messageRequestListener.onRemovedByAdmin();
                } else {
                    isRemovedByAdmin = true;
                }

                if (isPublicChat) {
                    EventBus.getDefault().post(new EventBusDeletePublicChat(extraRoom));
                } else  {
                    EventBus.getDefault().post(new EventBusDeleteGroupRoom(extraRoom));
                }
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private void initFirebaseMessage() {
        getMessagesFirebase().addChildEventListener(this);
    }

    private Query getMessagesFirebase() {
        if (latestMessageSentWhen <= 0) {
            return messagesFirebase.child(roomId)
                    .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                    .limitToLast(DOWNLOAD_MESSAGE_ITEM_LIMIT);
        } else {
            return messagesFirebase.child(roomId)
                    .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                    .startAt(latestMessageSentWhen);
        }
    }

    public void joinRoom() {
        if (isPrivateRoom || isMember) return;

        ArrayMap<String, Object> joinRoomMap = new ArrayMap<>(3);
        FirebaseMapUtil.mapUserPublicRoom(joinRoomMap, uid, roomId, ServerValue.TIMESTAMP);
        FirebaseMapUtil.mapUserRoom(joinRoomMap, uid, roomId, ServerValue.TIMESTAMP);

        rootFirebase.updateChildren(joinRoomMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                boolean isFailed = false;
                if (firebaseError != null) {
                    Log.e(TAG, "onComplete: " + firebaseError.getMessage() );
                    isFailed = true;
                }

                if (isMessageRequestAvailable()) {
                    if (isFailed) {
                        messageRequestListener.onJoinRoomFailed();
                    } else {
                        messageRequestListener.onJoinRoomSuccess();
                        isMember = true;
                        initRoomMemberChecker();
                    }
                } else {
                    if (isFailed) {
                        onJoinRoomFailed = true;
                    } else {
                        onJoinRoomSuccess = true;
                        isMember = true;
                        initRoomMemberChecker();
                    }
                }
            }
        });
    }

    public void typingMessage(String message) {
        boolean typing = !message.trim().isEmpty();
        if (!isPrivateRoom || (typing && isTypingMessage)) return;

        isTypingMessage = typing;
        messagesTypingFirebase.get().child(roomId).child(uid).onDisconnect().setValue(false);
        messagesTypingFirebase.get().child(roomId).child(uid).setValue(isTypingMessage, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(TAG, "onComplete: " + firebaseError.getMessage() );
                }
            }
        });
    }

    private void isFriendTyping() {
        messagesTypingFirebase.get()
                .child(roomId)
                .child(privateFriendId).addValueEventListener(friendTypingValueEvent);
    }

    private ValueEventListener friendTypingValueEvent = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                if (isMessageRequestAvailable()) {
                    messageRequestListener.onTypingMessagePrivateRoom(
                            dataSnapshot.getValue(Boolean.class), privateFriendName);
                } else {
                    isFriendTypingMessage = true;
                }
            }
        }
        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(TAG, "onCancelled: " + firebaseError.getMessage() );
        }
    };

    public Message getImageMessage() {
        String messageKey = messagesFirebase.child(roomId).push().getKey();
        String messageText = getString(R.string.send_photo);

        Message message = new Message(roomId, messageText, uid);
        message.setKey(messageKey);
        message.setImagePath("");

        return message;
    }

    public ArrayList<Message> onSendViewerImages(Uri[] uris, String[] absolutePaths) {
        int size = uris.length;
        ArrayList<Message> messages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Message imageMessage = getImageMessage();
            messages.add(imageMessage);

            onSendImageViewerImageToFirebase(imageMessage, uris[i], absolutePaths[i]);
        }
        return messages;
    }

    private void onSendImageViewerImageToFirebase(final Message message, final Uri uri, final String absolutePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver()
                            .openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor == null) return;

                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    Bitmap bitmap = BitmapUtil.getResized(fileDescriptor, absolutePath, false);
                    sendMessageBase64ToFirebase(bitmap, message);

                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    Log.e(TAG, "run: " + e.getMessage());
                }
            }
        }).start();
    }

    public Message onSendCameraImage() {

        Message imageMessage = getImageMessage();
        sendCameraImageToFirebase(imageMessage);

        return imageMessage;
    }

    private void sendCameraImageToFirebase(final Message message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(URI.create(cameraImageUri.toString()));
                Bitmap bitmap = BitmapUtil.getResized(file, false);
                sendMessageBase64ToFirebase(bitmap, message);
            }
        }).start();
    }

    private void sendMessageBase64ToFirebase(Bitmap bitmap, final Message message) {
        String uriBase64 = Base64Util.toDataUri(Base64Util.toBase64(bitmap, 50));

        String imageRatio =
                BitmapUtil.getImageRatio(bitmap.getWidth(), bitmap.getHeight());
        message.setImagePath(uriBase64);
        message.setRatio(imageRatio);
        bitmap.recycle();

        mapDataMessage(message);
        rootFirebase.updateChildren(messageMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                onMessageSent(firebaseError, message);
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File createImageFile() throws IOException {
        File cameraStorage = FileUtil.getCameraStorageDirectory();
        if (cameraStorage == null) {
            return null;
        }

        cameraImageUri = Uri.fromFile(cameraStorage);
        return cameraStorage;
    }

    public Message onSendButtonClick(String messageText) {
        if (!isMessageSending) isMessageSending = true;
        else return null;

        String messageKey = messagesFirebase.child(roomId).push().getKey();
        Message message = new Message(roomId, messageText, uid);
        message.setKey(messageKey);

        sendMessage(message);

        return message;
    }

    private void sendMessage(final Message message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mapDataMessage(message);
                sentMessageToFirebase(messageMap, message);
            }
        }).start();
    }

    private void mapDataMessage(Message message) {
        if (messageMap == null) {
            messageMap = new ArrayMap<>();
        }

        String[] usersKey = getUsersKeyArray();

        // private room: size 2
        // group room: size 3 * (total member)
        FirebaseMapUtil.mapUsersRoom(messageMap, usersKey, roomId, message.getSentWhen());

        // size 4
        FirebaseMapUtil.mapMessage(messageMap, message.getKey(), roomId, message);

        // size 3
        FirebaseMapUtil.mapRoomMessage(messageMap, message, roomId);

        /*if (isPublicChat) {
            FirebaseMapUtil.mapPublicRoomList(messageMap, roomId, message.getSentWhen());
        }*/
    }

    private String[] getUsersKeyArray() {
        String[] usersKey = new String[isPrivateRoom ? 2 : preservedGroupMemberList.size()];
        if (isPrivateRoom) {
            usersKey[0] = uid;
            usersKey[1] = privateFriendId;
        } else {
            for (int i = 0, size = preservedGroupMemberList.size(); i < size; i++) {
                usersKey[i] = preservedGroupMemberList.valueAt(i).getKey();
            }
        }
        return usersKey;
    }

    private void sentMessageToFirebase(ArrayMap<String, Object> messageMap, final Message message) {
        rootFirebase.updateChildren(messageMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(final FirebaseError firebaseError, Firebase firebase) {
                onMessageSent(firebaseError, message);
            }
        });
    }

    private void onMessageSent(FirebaseError firebaseError, final Message message) {
        if (firebaseError != null) {
            Log.e(TAG, firebaseError.getMessage());
            MyToast.make(firebaseError.getMessage()).show();
            messageRequestListener.onRemoveMessage(message);
        }

        if (isMessageRequestAvailable()) {
            messageRequestListener.onMessageSent(message);
        } else {
            sentMessage = message;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessageNotification(message);
            }
        }).start();
        isMessageSending = false;
    }

    private void sendMessageNotification(Message message) {
        Intent notifyMessageIntent = new Intent(Contextor.getInstance().getContext(), GCMNotifyService.class);
        notifyMessageIntent.putExtra(GCMUtil.DATA_TITLE, isPrivateRoom ?
                "" : extraRoom.getName());
        notifyMessageIntent.putExtra(GCMUtil.DATA_MESSAGE, message.getMessage());
        notifyMessageIntent.putExtra(GCMUtil.DATA_SENT_BY, uid);
        notifyMessageIntent.putExtra(GCMUtil.DATA_ROOM_ID, roomId);
        notifyMessageIntent.putExtra(GCMUtil.NOTIFY_TYPE, GCMUtil.CHAT_NEW_MESSAGE_NOTIFY_TYPE);

        if (isPrivateRoom) {
            notifyMessageIntent.putExtra(GCMUtil.KEY_TO, privateFriendInfo.getInstanceID());
        } else {
            if (instanceIdList != null) {
                notifyMessageIntent.putExtra(GCMUtil.KEY_REGISTRATION_IDS, instanceIdList);
            }
        }

        Contextor.getInstance().getContext().startService(notifyMessageIntent);
    }

    public void markMessageAsRead(final Message message) {
        String sentBy = message.getSentBy();
        if (sentBy.equals(uid) || sentBy.equals(FirebaseUtil.SYSTEM) || !isMember) return;

        Firebase readTotalFirebase = messagesFirebase.child(message.getRoomId())
                .child(message.getKey()).child(FirebaseUtil.CHILD_READ_TOTAL).getRef();

        if (isPrivateRoom) {
            readTotalFirebase.setValue(1);
        } else {
            markGroupMessageAsRead(readTotalFirebase, message);
        }
    }

    private void markGroupMessageAsRead(Firebase readTotalFirebase, final Message message) {
        readTotalFirebase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed,
                                   DataSnapshot dataSnapshot) {
                if (firebaseError != null) {
                    Log.e(TAG, "onComplete: " + firebaseError.getMessage());
                    return;
                }

                readGroupRoomFirebase.get().child(message.getRoomId()).child(message.getKey())
                        .child(uid).setValue(ServerValue.TIMESTAMP);
            }
        });
    }

    public Message getMessage(DataSnapshot dataSnapshot) {
        Message message = dataSnapshot.getValue(Message.class);
        message.setKey(dataSnapshot.getKey());

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
        message.successfullySent();

        latestMessageSentWhen = message.getSentWhen();
        latestMessageKey = message.getKey();
        sendNewMessage(message);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        Message changedMessage = getMessage(dataSnapshot);
        changedMessage.successfullySent();

        if (isMessageRequestAvailable()) {
            messageRequestListener.onMessageChanged(changedMessage);
        } else {
            this.changedMessage = changedMessage;
        }
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
        onRecyclerViewInfiniteScrollListener = null;
        toolbarCommunicator = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isPrivateRoom) {
            messagesTypingFirebase.get().child(roomId).child(uid).setValue(false);
            messagesTypingFirebase.get().child(roomId).child(privateFriendId).removeEventListener(friendTypingValueEvent);
        } else {
            getMessagesFirebase().removeEventListener(this);

            if (eventListenerMemberList != null) {
                for (int i = 0, size = eventListenerMemberList.size(); i < size; i++) {
                    roomMembersFirebase.get().child(roomId).child(eventListenerMemberList.keyAt(i))
                            .removeEventListener(eventListenerMemberList.valueAt(i));
                }
            }
        }
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public interface MessageRequestListener {
        void onEmptyMessage();
        void onTypingMessagePrivateRoom(boolean isTyping, String friendName);
        void onMessageSent(Message message);
        void onNewMessage(Message newMessage);
        void onNewMessage(ArrayList<Message> newMessages);
        void onOldMessage(ArrayList<Message> oldMessages);
        void onMessageChanged(Message message);
        void onRemoveMessage(Message message);
        void onPrivateRoomReady(UserInfo friendInfo, boolean isPrivateRoom);
        void onGroupRoomReady(SparseArray<UserInfo> userInfoList, boolean isPrivateRoom);
        void onNewGroupMember(UserInfo friendInfo);
        void onJoinRoomSuccess();
        void onJoinRoomFailed();
        void onRemovedByAdmin();
    }

}
