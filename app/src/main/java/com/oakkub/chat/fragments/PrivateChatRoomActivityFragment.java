package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.ChatListAdapter;
import com.oakkub.chat.views.widgets.recyclerview.InfiniteScrollListener;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import org.magicwerk.brownies.collections.GapList;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrivateChatRoomActivityFragment extends Fragment implements ChildEventListener {

    public static final String EXTRA_ROOM_ID = "extra:roomId";
    public static final String EXTRA_FRIEND_ID = "extra:friendId";
    public static final String EXTRA_FRIEND_NAME = "extra:friendName";
    public static final String EXTRA_FRIEND_PROFILE = "extra:friendProfile";
    private static final int MESSAGE_ITEM_LIMIT = 20;
    private static final int POSITION_OFFSET_TO_SCROLL = 1;
    private static final String TAG = PrivateChatRoomActivityFragment.class.getSimpleName();
    @Bind(R.id.private_chat_message_recycler_view)
    RecyclerView messageList;

    @Bind(R.id.private_chat_message_edit_text)
    EditText messageText;

    @Bind(R.id.private_chat_message_button)
    Button sendMessageButton;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES)
    Firebase messagesFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS)
    Firebase roomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ROOMS)
    Firebase userRoomFirebase;

    private ToolbarCommunicator toolbarCommunicator;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager linearLayoutManager;
    private InfiniteScrollListener infiniteScrollListener;

    private String myId;
    private String roomId;
    private String friendId;
    private String friendDisplayName;
    private String friendProfileImage;

    private boolean isMessageSending;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        toolbarCommunicator = (ToolbarCommunicator) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(getActivity()).inject(this);

        setRetainInstance(true);

        getDataFromIntent();
    }

    private void getDataFromIntent() {
        Intent intent = getActivity().getIntent();

        roomId = intent.getStringExtra(EXTRA_ROOM_ID);
        friendId = intent.getStringExtra(EXTRA_FRIEND_ID);
        friendDisplayName = intent.getStringExtra(EXTRA_FRIEND_NAME);
        friendProfileImage = intent.getStringExtra(EXTRA_FRIEND_PROFILE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_private_chat_room, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbarCommunicator.setTitle(friendDisplayName);

        if (savedInstanceState == null) {
            setRecyclerViewAdapter();
            addFirebaseBindToRoomId();
        }

        setRecyclerView();
        setInfiniteScroll();
    }

    private void scrollToFirstPosition(final boolean smoothScroll, long delayed) {
        final int position = chatListAdapter.getItemCount() - 1;

        if (position >= 0) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scroll(0, smoothScroll);
                }
            }, delayed);

        }
    }

    private void scroll(int position, boolean smoothScroll) {
        if (smoothScroll) {
            messageList.smoothScrollToPosition(position);
        } else {
            messageList.scrollToPosition(position);
        }
    }

    private void setRecyclerView() {
        DefaultItemAnimator itemAnimator =
                AppController.getComponent(getActivity()).defaultItemAnimator();
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);

        messageList.setHasFixedSize(true);
        messageList.setItemAnimator(itemAnimator);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(chatListAdapter);

    }

    private void setInfiniteScroll() {
        infiniteScrollListener = new InfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(final int page) {
                Message lastMessage = chatListAdapter.getLastItem();
                if (lastMessage == null) return;

                chatListAdapter.addProgressBar();

                messagesFirebase
                        .child(roomId)
                        .orderByChild(FirebaseUtil.CHILD_SENT_WHEN)
                        .endAt(lastMessage.getSentWhen())
                        .limitToLast(MESSAGE_ITEM_LIMIT)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                chatListAdapter.removeLast();

                                GapList<Message> messageList = createMessageList(dataSnapshot);
                                messageList = getLoadMoreMessages(messageList, dataSnapshot);
                                checkDuplicateMessage(messageList);

                                chatListAdapter.addLastAll(messageList);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });


            }
        };

        messageList.addOnScrollListener(infiniteScrollListener);
    }

    private GapList<Message> createMessageList(DataSnapshot dataSnapshot) {
        final int count = (int) dataSnapshot.getChildrenCount();
        GapList<Message> messageList = new GapList<>(count);

        if (count < MESSAGE_ITEM_LIMIT) {
            infiniteScrollListener.noMoreData();
        }

        return messageList;
    }

    private GapList<Message> getLoadMoreMessages(GapList<Message> messages, DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Message message = getMessage(postSnapshot);
            messages.addFirst(message);
        }
        return messages;
    }

    private void checkDuplicateMessage(GapList<Message> messageList) {
        if (messageList.getFirst().getRoomId().equals(chatListAdapter.getLastItem().getRoomId())) {
            messageList.removeFirst();
        }
    }

    private void setRecyclerViewAdapter() {
        myId = messagesFirebase.getAuth().getUid();

        HashMap<String, String> friendImageMap = new HashMap<>();
        friendImageMap.put(friendId, friendProfileImage);

        chatListAdapter = new ChatListAdapter(myId, friendImageMap);
    }

    private void addFirebaseBindToRoomId() {
        messagesFirebase
                .child(roomId)
                .limitToLast(MESSAGE_ITEM_LIMIT)
                .addChildEventListener(this);
    }

    @OnClick(R.id.private_chat_message_button)
    public void onSendButtonClick() {
        if (!isMessageSending) isMessageSending = true;
        else return;

        final String message = messageText.getText().toString().trim();
        if (message.length() == 0) {
            isMessageSending = false;
            return;
        }

        sendMessage(message);
        scrollToFirstPosition(false, 100);
        messageText.setText("");
    }

    private void sendMessage(final String messageText) {
        final Message message = new Message(roomId, messageText, myId);

        messagesFirebase.child(roomId).push().setValue(message, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(TAG, firebaseError.getMessage());
                    chatListAdapter.remove(message);
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
        Map<String, Object> messageMap = new HashMap<>(1);
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

    private void scrollToFirstPositionIfNeeded() {
        // check if the list visible position of item is close or equal to the last item.
        // if is it, then scroll to last position
        // if not, TODO notify user that we have new message

        if (isCloseToFirstPosition()) {
            scrollToFirstPosition(true, 0);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        Message message = getMessage(dataSnapshot);
        chatListAdapter.addFirst(message);

        scrollToFirstPositionIfNeeded();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        Message message = getMessage(dataSnapshot);
        chatListAdapter.replace(message);
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
    public void onDestroy() {
        super.onDestroy();

        messagesFirebase.removeEventListener(this);
    }

    private boolean isCloseToFirstPosition() {
        return linearLayoutManager.findFirstVisibleItemPosition() <= POSITION_OFFSET_TO_SCROLL;
    }
}
