package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.oakkub.chat.models.ImageMessage;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.ChatListAdapter;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrivateChatRoomActivityFragment extends Fragment
        implements ChildEventListener {

    public static final String EXTRA_ROOM_ID = "extra:roomId";
    public static final String EXTRA_FRIEND_ID = "extra:friendId";
    public static final String EXTRA_FRIEND_NAME = "extra:friendName";
    public static final String EXTRA_FRIEND_PROFILE = "extra:friendProfile";
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

    private String myId;
    private String roomId;
    private String friendId;
    private String friendDisplayName;
    private String friendProfileImage;

    private boolean isSend;

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
            addListenerBindToRoomId();
        }

        setRecyclerView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scrollToLastPosition(false);
    }

    private void scrollToLastPosition(boolean smoothScroll) {
        final int position = chatListAdapter.getItemCount() - 1;

        if (smoothScroll) {
            messageList.smoothScrollToPosition(position);
        } else {
            messageList.scrollToPosition(position);
        }
    }

    private void setRecyclerView() {
        DefaultItemAnimator itemAnimator =
                AppController.getComponent(getActivity()).defaultItemAnimator();

        messageList.setHasFixedSize(true);
        messageList.setItemAnimator(itemAnimator);
        messageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageList.setAdapter(chatListAdapter);
    }

    private void setRecyclerViewAdapter() {
        myId = messagesFirebase.getAuth().getUid();

        HashMap<String, String> friendImageMap = new HashMap<>();
        friendImageMap.put(friendId, friendProfileImage);

        chatListAdapter = new ChatListAdapter(myId, friendImageMap);
    }

    private void addListenerBindToRoomId() {
        messagesFirebase
                .child(roomId)
                .addChildEventListener(this);
    }

    @OnClick(R.id.private_chat_message_button)
    public void onSendButtonClick() {
        if (!isSend) isSend = true;
        else return;

        final String message = messageText.getText().toString().trim();
        if (message.length() == 0) {
            isSend = false;
            return;
        }

        sendMessage(message);
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
                isSend = false;
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

                        Message latestMessageAfterDeleted = getTypeOfMessage(childrenDataSnapshot);
                        updateRoomInfo(latestMessageAfterDeleted, true);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    public Message getTypeOfMessage(DataSnapshot dataSnapshot) {
        Map<String, Object> dataSnapshotMap = (Map<String, Object>) dataSnapshot.getValue();
        Message message;

        // check if key:"imagePath" is presented
        if (!dataSnapshotMap.containsKey(FirebaseUtil.CHILD_MESSAGE_IMAGE_PATH)) {
            message = dataSnapshot.getValue(Message.class);
        } else {
            message = dataSnapshot.getValue(ImageMessage.class);
        }

        message.setMessageKey(dataSnapshot.getKey());

        return message;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        Log.e(TAG, String.valueOf(dataSnapshot.getValue()));

        Message message = getTypeOfMessage(dataSnapshot);
        chatListAdapter.add(message);

        scrollToLastPosition(false);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        Message message = getTypeOfMessage(dataSnapshot);
        chatListAdapter.replace(message);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Message message = getTypeOfMessage(dataSnapshot);
        chatListAdapter.remove(message);

        updateRoomInfoWhenDeleted();
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
}
