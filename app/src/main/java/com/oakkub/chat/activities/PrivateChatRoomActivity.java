package com.oakkub.chat.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.PrivateChatRoomActivityFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnInfiniteScrollListener;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.views.adapters.ChatListAdapter;
import com.oakkub.chat.views.widgets.recyclerview.InfiniteScrollListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class PrivateChatRoomActivity extends BaseActivity
        implements PrivateChatRoomActivityFragment.MessageRequestListener,
        OnInfiniteScrollListener {

    public static final String CHAT_LIST_STATE = "state:chatList";
    private static final int POSITION_OFFSET_TO_FIRST_SCROLL = 1;
    private static final String TAG = PrivateChatRoomActivity.class.getSimpleName();
    private static final String CHAT_ROOM_FRAGMENT_TAG = "tag:chatRoom";
    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.private_chat_message_recycler_view)
    RecyclerView messageList;

    @Bind(R.id.private_chat_message_edit_text)
    EditText messageText;

    @Bind(R.id.private_chat_message_button)
    Button sendMessageButton;

    @Bind(R.id.loading_messages_layout)
    LinearLayout loadingMessagesLayout;

    @State
    String roomName;

    private LinearLayoutManager linearLayoutManager;
    private InfiniteScrollListener infiniteScrollListener;
    private ChatListAdapter chatListAdapter;
    private PrivateChatRoomActivityFragment privateChatRoomActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat_room);
        ButterKnife.bind(this);
        findMessageFragment();

        initData(savedInstanceState);

        setToolbar();
        setRecyclerView();
        setInfiniteScroll();
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        roomName = getIntent().getStringExtra(PrivateChatRoomActivityFragment.EXTRA_FRIEND_NAME);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(roomName);
    }

    private void setRecyclerView() {
        DefaultItemAnimator itemAnimator =
                AppController.getComponent(this).defaultItemAnimator();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);

        messageList.setHasFixedSize(true);
        messageList.setItemAnimator(itemAnimator);
        messageList.setLayoutManager(linearLayoutManager);

        messageList.setAdapter(chatListAdapter);
    }

    private void setInfiniteScroll() {
        infiniteScrollListener = new InfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(final int page) {
                if (isNoMoreData()) return;

                Message message = chatListAdapter.getLastItem();
                // if null, it is progress bar or no internet indicator or no value.
                if (message != null) {
                    chatListAdapter.addFooterProgressBar();
                    privateChatRoomActivityFragment.loadItemMore(message.getSentWhen());
                }
            }
        };

        messageList.addOnScrollListener(infiniteScrollListener);
    }

    private void findMessageFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        privateChatRoomActivityFragment = (PrivateChatRoomActivityFragment)
                fragmentManager.findFragmentByTag(CHAT_ROOM_FRAGMENT_TAG);

        if (privateChatRoomActivityFragment == null) {

            privateChatRoomActivityFragment = new PrivateChatRoomActivityFragment();
            fragmentManager.beginTransaction()
                    .add(privateChatRoomActivityFragment, CHAT_ROOM_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        chatListAdapter.onSaveInstanceState(CHAT_LIST_STATE, outState);
        infiniteScrollListener.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        chatListAdapter.onRestoreInstanceState(CHAT_LIST_STATE, savedInstanceState);
        infiniteScrollListener.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        messageList.setAdapter(null);

        super.onDestroy();
    }

    @OnClick(R.id.private_chat_message_button)
    public void onSendButtonClick() {
        String text = messageText.getText().toString().trim();
        privateChatRoomActivityFragment.onSendButtonClick(text);

        messageText.setText("");
    }

    private void scrollToFirstPositionIfNeeded() {
        // check if the last visible position of item is close or equal to the first item.
        // if it is, then scroll to first position
        // if not, TODO notify user that you have new message

        if (isCloseToFirstPosition()) {
            scrollToFirstPosition(true, 0);
        }
    }

    private void scrollToFirstPosition(final boolean smoothScroll, long delayed) {
        if (chatListAdapter.getItemCount() >= 0) {

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

    private boolean isCloseToFirstPosition() {
        return linearLayoutManager.findFirstVisibleItemPosition() <= POSITION_OFFSET_TO_FIRST_SCROLL;
    }

    private void setPreviousMessageOnNewMessage(Message message) {
        Message oldMessage = chatListAdapter.getFirstItem();
        if (oldMessage == null) return;

        oldMessage.setShowImage(!oldMessage.getSentBy().equals(message.getSentBy()));
        chatListAdapter.replace(oldMessage);
    }

    private void setPreviousMessageOnOldMessage(Message message) {
        Message oldMessage = chatListAdapter.getLastItem();
        if (oldMessage == null) return;

        if (!oldMessage.getSentBy().equals(message.getSentBy())) {
            message.setShowImage(true);
        }
    }

    @Override
    public void onNewMessage(Message newMessage) {
        loadingMessagesLayout.setVisibility(View.GONE);

        setPreviousMessageOnNewMessage(newMessage);
        chatListAdapter.addFirst(newMessage);
        scrollToFirstPositionIfNeeded();
    }

    @Override
    public void onNewMessage(ArrayList<Message> newMessages) {
        for (int i = 0, size = newMessages.size(); i < size; i++) {
            onNewMessage(newMessages.get(i));
        }
    }

    @Override
    public void onOldMessage(ArrayList<Message> oldMessages) {
        chatListAdapter.removeFooter();

        for (int i = 0, size = oldMessages.size(); i < size; i++) {
            Message oldMessage = oldMessages.get(i);

            setPreviousMessageOnOldMessage(oldMessage);
            chatListAdapter.addLast(oldMessage);
        }
    }

    @Override
    public void onRemoveMessage(Message message) {

    }

    @Override
    public void onAdapterInitialized(String myId, SparseArray<String> friendProfileImageList) {
        chatListAdapter = new ChatListAdapter(myId, friendProfileImageList);
        messageList.setAdapter(chatListAdapter);
    }

    @Override
    public void onLoadMoreFailed() {
        infiniteScrollListener.setLoadMore(true);
    }

    @Override
    public void onNoMoreOlderData() {
        infiniteScrollListener.noMoreData();
    }
}
