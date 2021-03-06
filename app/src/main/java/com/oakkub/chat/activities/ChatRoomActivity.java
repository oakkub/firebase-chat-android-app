package com.oakkub.chat.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oakkub.chat.BuildConfig;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.ChatRoomFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnRecyclerViewInfiniteScrollListener;
import com.oakkub.chat.managers.icepick_bundler.RoomBundler;
import com.oakkub.chat.managers.icepick_bundler.UserInfoBundler;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.UserOnlineInfo;
import com.oakkub.chat.models.eventbus.EventBusRemovedRoom;
import com.oakkub.chat.utils.AnimateUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.PermissionUtil;
import com.oakkub.chat.utils.TimeUtil;
import com.oakkub.chat.utils.UriUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.adapters.ChatListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.dialogs.ListDialogFragment;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.MyLinearLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.TextImageView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewInfiniteScrollListener;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import icepick.State;

public class ChatRoomActivity extends BaseActivity
        implements ChatRoomFragment.MessageRequestListener,
        OnRecyclerViewInfiniteScrollListener, ToolbarCommunicator,
        AlertDialogFragment.OnAlertDialogListener, OnAdapterItemClick,
        ListDialogFragment.OnListDialogClickListener {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();
    private static final int POSITION_OFFSET_TO_SCROLL = 2;
    private static final String CHAT_ROOM_FRAGMENT_TAG = "tag:chatRoom";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";
    private static final String REMOVED_FROM_CHAT_DIALOG_TAG = "tag:removedFromChatDialog";
    private static final String MESSAGE_LIST_DIALOG_TAG = "tag:messageListDialog";
    private static final String REMOVED_BY_FRIEND_DIALOG_TAG = "tag:removedByFriendDialog";
    private static final String IMAGE_MESSAGE_LIST_DIALOG_TAG = "tag:imageMessageListDialog";

    public static final String EXTRA_ROOM = "extra:room";
    public static final String CHAT_LIST_STATE = "state:chatList";

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int IMAGE_VIEWER_REQUEST_CODE = 2;
    private static final int ROOM_INFO_REQUEST_CODE = 3;
    private static final int REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION = 101;

    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;

    @BindView(R.id.chat_room_root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.chat_title_toolbar_textview)
    MyTextView titleTextView;

    @BindView(R.id.chat_sub_title_toolbar_textview)
    MyTextView subTitleTextView;

    @BindView(R.id.private_chat_message_recycler_view)
    RecyclerView messageList;

    @BindView(R.id.message_attachment_root)
    MyLinearLayout attachmentLayout;

    @BindView(R.id.message_input_layout)
    MyLinearLayout messageInputLayout;

    @BindView(R.id.textImageCamera)
    TextImageView cameraTextImage;

    @BindView(R.id.textImageGallery)
    TextImageView galleryTextImage;

    @BindView(R.id.textImageBack)
    TextImageView backTextImage;

    @BindView(R.id.message_attachment_button)
    ImageButton attachmentButton;

    @BindView(R.id.message_input_ediitext)
    EditText messageText;

    @BindView(R.id.message_input_button)
    ImageButton sendMessageButton;

    @BindView(R.id.private_chat_typing_notify_textview)
    TextView headerTextView;

    @BindView(R.id.private_chat_new_message_notify_textview)
    TextView footerTextView;

    @BindView(R.id.private_chat_join_room_to_chat_botton)
    Button joinRoomToChatButton;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.empty_messages_textview)
    MyTextView emptyMessageTextView;

    @State
    Uri uriCameraImageFile;

    @State
    boolean isMember;

    @State
    boolean isPrivateRoom;

    @State
    boolean isRemovedByFriend;

    @State
    String privateFriendId;

    @State
    String[] messageDialogItems;

    @State
    String[] imageMessageDialogItems;

    @State
    int selectedItemPosition;

    @State(RoomBundler.class)
    Room room;

    @State(UserInfoBundler.class)
    UserInfo privateFriendInfo;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewInfiniteScrollListener recyclerViewInfiniteScrollListener;
    private ChatListAdapter chatListAdapter;
    private ChatRoomFragment chatRoomFragment;
    private ProgressDialogFragment progressDialog;

    public static Intent getResultIntent(Room room) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ROOM, Parcels.wrap(room));
        return intent;
    }

    public static Intent getIntentPrivateRoom(Context context, Room room, String uid) {
        Intent roomIntent = new Intent(context, ChatRoomActivity.class);

        setRoomData(roomIntent, room, true);
        roomIntent.putExtra(ChatRoomFragment.EXTRA_FRIEND_ID,
                FirebaseUtil.getPrivateRoomFriendKey(uid, room.getRoomId()));

        return roomIntent;
    }

    public static Intent getIntentGroupRoom(Context context, Room room) {
        Intent roomIntent = new Intent(context, ChatRoomActivity.class);
        setRoomData(roomIntent, room, true);
        return roomIntent;
    }

    public static Intent getIntentPublicRoom(Context context, Room room, boolean isMember) {
        Intent roomIntent = new Intent(context, ChatRoomActivity.class);
        setRoomData(roomIntent, room, isMember);
        return roomIntent;
    }

    private static void setRoomData(Intent intent, Room room, boolean isMember) {
        intent.putExtra(ChatRoomFragment.EXTRA_IS_MEMBER, isMember);
        intent.putExtra(ChatRoomFragment.EXTRA_ROOM, Parcels.wrap(room));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getDataIntent(savedInstanceState);
        clearNotification();
        ButterKnife.bind(this);
        findMessageFragment();

        setToolbar();
        setRecyclerView();
        setInfiniteScroll();

        if (subTitleTextView.getText().toString().isEmpty()) {
            subTitleTextView.gone();
        }

        if (savedInstanceState == null) {
            getViewTreeObserverAttachmentLayout();

            emptyMessageTextView.setVisibility(View.GONE);
            headerTextView.setVisibility(View.GONE);
            footerTextView.setVisibility(View.GONE);
            joinRoomToChatButton.setVisibility(isMember ? View.GONE : View.VISIBLE);
            messageInputLayout.setVisibility(isMember ? View.VISIBLE : View.GONE);

            messageDialogItems = getResources().getStringArray(R.array.message_list_dialog);
            imageMessageDialogItems = getResources().getStringArray(R.array.image_message_list_dialog);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (chatRoomFragment != null) {
            chatRoomFragment.setIntent(intent);
        }
    }

    private void getDataIntent(Bundle savedInstanceState) {
        Intent intent = getIntent();

        if (savedInstanceState == null) {
            isMember = intent.getBooleanExtra(ChatRoomFragment.EXTRA_IS_MEMBER, false);
            privateFriendId = intent.getStringExtra(ChatRoomFragment.EXTRA_FRIEND_ID);
            isPrivateRoom = privateFriendId != null;
        }

        Room intentRoom = Parcels.unwrap(intent.getParcelableExtra(ChatRoomFragment.EXTRA_ROOM));
        if (room == null || intentRoom.fullEquals(room)) {
            room = intentRoom;
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setTitle("");
        }
    }

    private void setRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);

        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(duration);
        itemAnimator.setChangeDuration(duration);
        itemAnimator.setMoveDuration(duration);
        itemAnimator.setRemoveDuration(0);

        messageList.setItemAnimator(itemAnimator);
        messageList.setLayoutManager(linearLayoutManager);
    }

    private void setInfiniteScroll() {
        recyclerViewInfiniteScrollListener = new RecyclerViewInfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(final int page) {
                Message message = chatListAdapter.getLastItem();

                // if null, it is progress bar or no internet indicator or no data.
                if (message == null) return;
                if (chatListAdapter.getItemCount() <
                        ChatRoomFragment.DOWNLOAD_MESSAGE_ITEM_LIMIT) return;

                chatListAdapter.addFooterProgressBar();
                chatRoomFragment.loadItemMore(message.getSentWhen());
            }
        };

        messageList.addOnScrollListener(recyclerViewInfiniteScrollListener);
        messageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (linearLayoutManager.findFirstVisibleItemPosition() == 0) {
                        AnimateUtil.alphaAnimation(footerTextView, false);
                    }
                }
            }
        });
    }

    private void findMessageFragment() {
        chatRoomFragment = (ChatRoomFragment) findFragmentByTag(CHAT_ROOM_FRAGMENT_TAG);
        if (chatRoomFragment == null) {
            chatRoomFragment = new ChatRoomFragment();
            addFragmentByTag(chatRoomFragment, CHAT_ROOM_FRAGMENT_TAG);
        }

        progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
    }

    private void getViewTreeObserverAttachmentLayout() {
        attachmentLayout.setVisibility(View.INVISIBLE);
        attachmentLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                attachmentLayout.setVisibility(View.GONE);
                attachmentLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    private boolean isMember() {
        if (!isMember) {
            MyToast.make(getString(R.string.error_you_have_to_join_to_chat)).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_room_info:

                String roomAction = RoomInfoActivity.ACTION_PRIVATE;

                switch (room.getType()) {
                    case FirebaseUtil.VALUE_ROOM_TYPE_GROUP:
                        roomAction = RoomInfoActivity.ACTION_GROUP;
                        break;
                    case FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC:
                        roomAction = RoomInfoActivity.ACTION_PUBLIC;
                        break;
                }

                Intent roomInfoIntent = RoomInfoActivity.getStartIntent(this, room,
                        roomAction, isMember);
                startActivityForResult(roomInfoIntent, ROOM_INFO_REQUEST_CODE);

                return true;

            case R.id.action_invite_friend:

                Intent newMessageIntent = NewMessagesActivity.getStartIntent(this, privateFriendInfo);
                startActivity(newMessageIntent);

                return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);

        if (!isPrivateRoom) {
            menu.removeItem(R.id.action_invite_friend);
        } else {
            menu.removeItem(R.id.action_room_info);
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (chatListAdapter != null) {
            chatListAdapter.onSaveInstanceState(CHAT_LIST_STATE, outState);
        }

        recyclerViewInfiniteScrollListener.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        if (chatListAdapter != null) {
            chatListAdapter.onRestoreInstanceState(CHAT_LIST_STATE, savedInstanceState);
        }

        recyclerViewInfiniteScrollListener.onRestoreInstanceState(savedInstanceState);

        if (attachmentLayout.getVisibility() != View.VISIBLE) {
            getViewTreeObserverAttachmentLayout();
        }
    }

    @Override
    public void onBackPressed() {
        if (attachmentLayout.isRevealed()) {
            displayFileAttachmentLayout();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        rootContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (attachmentLayout.isRevealed()) {
                    attachmentLayout.circleReveal();
                }

                onRoomInfoIntentResult(resultCode, requestCode, data);
                onCameraIntentResult(resultCode, requestCode);
                onImageViewerIntentResult(resultCode, requestCode, data);

                rootContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    void onRoomInfoIntentResult(int resultCode, int requestCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode != ROOM_INFO_REQUEST_CODE || data == null) {
            return;
        }

        Room room = Parcels.unwrap(data.getParcelableExtra(EXTRA_ROOM));
        this.room = room;

        titleTextView.setText(room.getName());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void onCameraIntentResult(int resultCode, int requestCode) {
        if (requestCode != CAMERA_REQUEST_CODE) return;

        switch (resultCode) {
            case RESULT_CANCELED:
                File file = new File(URI.create(uriCameraImageFile.toString()));
                file.delete();
                uriCameraImageFile = null;
                break;
            case RESULT_OK:
                Message message = chatRoomFragment.onSendCameraImage();
                chatListAdapter.addFirst(message);
                scrollToFirstPositionIfNeeded();

                uriCameraImageFile = null;
                break;
        }
    }

    void onImageViewerIntentResult(int resultCode, int requestCode, Intent data) {
        if (requestCode != IMAGE_VIEWER_REQUEST_CODE) return;

        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case RESULT_OK:

                if (data.getData() != null) {
                    // single selected image.
                    Uri uri = data.getData();

                    String absolutePath = UriUtil.getPath(uri);
                    onImageViewerUriResult(
                            new Uri[]{uri}, new String[]{absolutePath});

                } else if (Build.VERSION.SDK_INT >= 18 && data.getClipData() != null) {
                    // multiple selected images.
                    ClipData clipData = data.getClipData();

                    int size = clipData.getItemCount();
                    Uri[] imagesUri = new Uri[size];
                    String[] absolutePaths = new String[size];

                    for (int i = 0; i < size; i++) {
                        ClipData.Item item = clipData.getItemAt(i);

                        Uri uri = item.getUri();
                        imagesUri[i] = uri;
                        absolutePaths[i] = UriUtil.getPath(uri);
                    }

                    onImageViewerUriResult(imagesUri, absolutePaths);
                }
                break;
        }
    }

    private void onImageViewerUriResult(Uri[] uris, String[] absolutePaths) {
        ArrayList<Message> messages =
                chatRoomFragment.onSendViewerImages(uris, absolutePaths);
        onNewMessage(messages);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestUseCameraPermission(requestCode, grantResults);
        onRequestUseGalleryPermission(requestCode, grantResults);
    }

    @SuppressWarnings("unused")
    @OnTouch(R.id.message_input_ediitext)
    public boolean onMessageEditTextClick(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isMember()) {
                Util.hideSoftKeyboard(this);
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.private_chat_join_room_to_chat_botton)
    public void onJoinChatRoomButtonClick() {
        if (isMember) return;
        chatRoomFragment.joinRoom();

        if (!progressDialog.isVisible()) {
            progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        }
    }

    @OnClick(R.id.message_attachment_button)
    public void onAttachmentButtonClick() {
        displayFileAttachmentLayout();
    }

    @OnClick(R.id.textImageBack)
    public void onTextImageBackClick(View view) {
        displayFileAttachmentLayout();
    }

    @OnClick(R.id.textImageCamera)
    public void onTextImageCameraClick() {
        if (!isMember()) return;

        if (!PermissionUtil.isPermissionAllowed(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION)) {
            return;
        }

        File imageFile = null;
        try {
            imageFile = chatRoomFragment.createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageFile == null) {
            MyToast.make("Cannot use camera, no sdcard").show();
            return;
        }
        uriCameraImageFile = Uri.fromFile(imageFile);

        Uri contentUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                imageFile);

        Intent cameraIntent = IntentUtil.openCamera(this, contentUri);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @OnClick(R.id.textImageGallery)
    public void onTextImageGalleryClick() {
        if (!isMember()) return;

        if (!PermissionUtil.isPermissionAllowed(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION)) {
            return;
        }

        Intent imageViewerIntent = IntentUtil.openImageViewer(this, true);
        if (imageViewerIntent != null) {
            startActivityForResult(imageViewerIntent, IMAGE_VIEWER_REQUEST_CODE);
        }
    }

    private void onRequestUseCameraPermission(int requestCode, int[] grantResults) {
        if (requestCode != REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION) return;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            MyToast.make("You denied write storage permission").show();
        } else {
            onTextImageCameraClick();
        }
    }

    private void onRequestUseGalleryPermission(int requestCode, int[] grantResults) {
        if (requestCode != REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION) return;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            MyToast.make("You denied write storage permission").show();
        } else {
            onTextImageGalleryClick();
        }
    }

    private Message getSystemTimeMessage(Message messageTimeToBeShown) {
        long time = messageTimeToBeShown.getSentWhen();
        GregorianCalendar calendar = TimeUtil.getCalendar(time);
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

        Message systemTimeMessage = new Message(null, null, FirebaseUtil.SYSTEM, time);
        systemTimeMessage.setKey(FirebaseUtil.SYSTEM + "_" + format.format(calendar.getTime()));

        return systemTimeMessage;
    }

    private void addOldSystemTimeDivider(Message newOldMessage) {
        Message oldestMessage = chatListAdapter.getLastItem();
        Message systemTimeMessage = getSystemTimeMessage(oldestMessage);

        if (newOldMessage == null || TimeUtil.isLeftDayGreaterThanRight(oldestMessage.getSentWhen(), newOldMessage.getSentWhen())) {
            if (chatListAdapter.contains(systemTimeMessage)) {
                chatListAdapter.remove(systemTimeMessage);
            }
            chatListAdapter.addLast(systemTimeMessage);
        }
    }

    private void addNewSystemTimeDivider(Message sendingMessage) {
        Message latestMessage = chatListAdapter.getFirstItem();
        Message systemTimeMessage = getSystemTimeMessage(sendingMessage);

        if (latestMessage == null || TimeUtil.isLeftDayGreaterThanRight(sendingMessage.getSentWhen(), latestMessage.getSentWhen())) {
            chatListAdapter.addFirst(systemTimeMessage);
        }
    }

    @OnClick(R.id.message_input_button)
    public void onSendButtonClick() {
        if (progressBar.getVisibility() == View.VISIBLE || !isMember() || isRemovedByFriend) return;

        String text = messageText.getText().toString().trim();
        if (text.isEmpty()) return;

        Message sendingMessage = chatRoomFragment.onSendButtonClick(text);

        if (sendingMessage != null) {
            messageText.setText("");

            addNewSystemTimeDivider(sendingMessage);
            chatListAdapter.addFirst(sendingMessage);
            scrollToFirstPositionIfNeeded();
        }
    }

    @OnClick(R.id.private_chat_new_message_notify_textview)
    public void onNotifyMessageClick() {
        if (!isCloseToFirstPosition()) {
            scrollToFirstPosition(true, 0);
            AnimateUtil.alphaAnimation(footerTextView, false);
        }
    }

    @OnTextChanged(value = R.id.message_input_ediitext, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onMessageTyping(Editable editable) {
        chatRoomFragment.typingMessage(editable.toString());
    }

    private void displayFileAttachmentLayout() {
        if (!isMember()) return;
        attachmentLayout.circleReveal();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        /*if (ev.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "dispatchTouchEvent: ");
            int[] location = new int[2];
            messageList.getLocationOnScreen(location);

            MyRect rect = new MyRect(location[0], location[0] + messageList.getWidth(),
                    location[1], location[1] + messageList.getHeight());

            int x = (int) ev.getX();
            int y = (int) ev.getY();

            if (rect.contains(x, y)) {
                if (attachmentLayout.isRevealed()) {
                    displayFileAttachmentLayout();
                }
            }
        }*/

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setTitle(String title) {
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
    }

    private void scrollToFirstPositionIfNeeded() {
        // check if the last visible position of item is close or equal to the first item.
        // if it is, then scroll to first position
        if (isCloseToFirstPosition()) {
            scrollToFirstPosition(true, 0);
        }
    }

    private boolean isCloseToFirstPosition() {
        return linearLayoutManager.findFirstVisibleItemPosition() <= POSITION_OFFSET_TO_SCROLL;
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

    private void showNewMessageIfNeeded() {
        if (!isCloseToFirstPosition()) {
            footerTextView.setText(getString(R.string.new_messages));
            AnimateUtil.alphaAnimation(footerTextView, true);
        }
    }

    private void setReadMessage(Message message) {
        chatRoomFragment.markMessageAsRead(message);
    }

    @Override
    public void onTypingMessagePrivateRoom(boolean isTyping, String friendName) {
        headerTextView.setText(isTyping ? getString(R.string.friend_typing, friendName) : "");
        AnimateUtil.alphaAnimation(headerTextView, isTyping);
    }

    @Override
    public void onCheckUserOnline(UserOnlineInfo userOnlineInfo) {
        if (subTitleTextView == null) return;

        String text;

        if (userOnlineInfo.isOnline()) {
            text = getString(R.string.online);
        } else {
            text = String.valueOf(getString(R.string.online) + " " +
                    DateUtils.getRelativeTimeSpanString(
                            userOnlineInfo.getLastOnline() - 100000,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS));
        }

        subTitleTextView.setText(text);
        subTitleTextView.visible();
    }

    @Override
    public void onEmptyMessage() {
        progressBar.setVisibility(View.GONE);
        emptyMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageSent(Message message) {
        message.successfullySent();
        chatListAdapter.replace(message);
    }

    @Override
    public void onNewMessage(Message newMessage) {
        progressBar.setVisibility(View.GONE);
        emptyMessageTextView.setVisibility(View.GONE);

        Message firstMessage = chatListAdapter.getFirstItem();
        if (firstMessage != null) {
            if (firstMessage.getSentWhen() > newMessage.getSentWhen()) {
                return;
            }
        }

        if (!chatListAdapter.contains(newMessage)) {
            addNewSystemTimeDivider(newMessage);
            chatListAdapter.addFirst(newMessage);
            scrollToFirstPositionIfNeeded();
            setReadMessage(newMessage);
        }

        showNewMessageIfNeeded();
    }

    @Override
    public void onNewMessage(ArrayList<Message> newMessages) {
        for (int i = 0, size = newMessages.size(); i < size; i++) {
            onNewMessage(newMessages.get(i));
        }
    }

    @Override
    public void onOldMessage(ArrayList<Message> oldMessages) {
        // remove progress bar
        chatListAdapter.removeFooter();

        for (int i = 0, size = oldMessages.size(); i < size; i++) {
            Message oldMessage = oldMessages.get(i);

            addOldSystemTimeDivider(oldMessage);
            chatListAdapter.addLast(oldMessage);
            setReadMessage(oldMessage);
        }

        if (recyclerViewInfiniteScrollListener.isNoMoreData()) {
            addOldSystemTimeDivider(null);
        }
    }

    @Override
    public void onRemoveMessage(Message message) {
        Log.d(TAG, "onRemoveMessage: " + message.getKey());
        chatListAdapter.remove(message);
    }

    @Override
    public void onMessageChanged(Message message) {
        int previousPosition = chatListAdapter.findPosition(message) + 1;
        if (chatListAdapter.getItem(previousPosition) != null) {
            chatListAdapter.notifyItemChanged(previousPosition);
        }

        chatListAdapter.replace(message);
    }

    @Override
    public void onPrivateRoomReady(UserInfo friendInfo, boolean isPrivateRoom) {
        if (chatListAdapter == null) {

            privateFriendInfo = friendInfo;

            SparseArray<UserInfo> friendInfoList = new SparseArray<>(1);
            friendInfoList.put(friendInfo.hashCode(), friendInfo);

            chatListAdapter = new ChatListAdapter(uid, friendInfoList, isPrivateRoom, this);
            messageList.setAdapter(chatListAdapter);
        }
    }

    @Override
    public void onGroupRoomReady(SparseArray<UserInfo> userInfoList, boolean isPrivateRoom) {
        if (chatListAdapter == null) {

            chatListAdapter = new ChatListAdapter(uid, userInfoList, isPrivateRoom, this);
            messageList.setAdapter(chatListAdapter);
        }
    }

    @Override
    public void onNewGroupMember(UserInfo friendInfo) {
        chatListAdapter.addMember(friendInfo);
    }

    @Override
    public void onLoadMoreFailed() {
        recyclerViewInfiniteScrollListener.setLoadMore(true);
    }

    @Override
    public void onLoadMoreNoData() {
        recyclerViewInfiniteScrollListener.noMoreData();
    }

    @Override
    public void onJoinRoomSuccess() {
        progressDialog.dismiss();
        isMember = true;
        chatRoomFragment.setMember(true);
        MyToast.make(getString(R.string.join_room_success)).show();

        messageInputLayout.setVisibility(View.VISIBLE);
        joinRoomToChatButton.setVisibility(View.GONE);
    }

    @Override
    public void onJoinRoomFailed() {
        progressDialog.dismiss();
        MyToast.make(getString(R.string.error_cannot_join_chat)).show();
    }

    @Override
    public void onRemovedByFriend(boolean isRemoved) {
        isRemovedByFriend = isRemoved;
        if (!isRemoved) return;

        headerTextView.setText(getString(R.string.you_and_n_are_not_friend_so_cannot_send_message,
                room.getName().split(" ")[0]));
        AnimateUtil.alphaAnimation(headerTextView, true);
    }

    @Override
    public void onRemovedByAdmin() {
        if (findFragmentByTag(REMOVED_FROM_CHAT_DIALOG_TAG) == null) {
            AlertDialogFragment removedFromChatDialog = AlertDialogFragment
                    .newInstance(getString(R.string.chat),
                            getString(R.string.you_got_removed_from_this_chat),
                            "", null, false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(removedFromChatDialog, REMOVED_FROM_CHAT_DIALOG_TAG)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(REMOVED_FROM_CHAT_DIALOG_TAG) && which == DialogInterface.BUTTON_POSITIVE) {

            EventBus.getDefault().post(new EventBusRemovedRoom(room));

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainIntent);
        }
    }

    @Override
    public void onDeleteMessage(Message message, boolean isSuccess) {
        hideProgressDialog();

        if (isSuccess) {
            // delete system time indicator if present.
            Message aboveDeletedMessage = chatListAdapter.getItem(selectedItemPosition + 1);
            Message belowDeletedMessage = chatListAdapter.getItem(selectedItemPosition - 1);

            chatListAdapter.remove(message);

            Message aboveSystemTimeMessage = getSystemTimeMessage(aboveDeletedMessage);

            if (aboveSystemTimeMessage.equals(aboveDeletedMessage)) {
                if (belowDeletedMessage != null) {
                    if (TimeUtil.isLeftDayGreaterThanRight(
                            belowDeletedMessage.getSentWhen(), aboveDeletedMessage.getSentWhen())) {
                        chatListAdapter.remove(aboveDeletedMessage);
                    }
                } else {
                    chatListAdapter.remove(aboveDeletedMessage);
                }
            }
        } else {
            MyToast.make(getString(R.string.error_delete_message)).show();
        }

        selectedItemPosition = -1;
    }

    @Override
    public void onAdapterClick(View itemView, int position) {

    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        Message message = chatListAdapter.getItem(position);

        if (message == null) return false;
        if (message.getSentBy() == null) return false;
        if (message.getSentBy().equals(FirebaseUtil.SYSTEM)) return false;

        selectedItemPosition = position;

        boolean isImageMessage = message.getImagePath() != null;

        ListDialogFragment messageListDialog = ListDialogFragment.newInstance(
                getString(R.string.message), isImageMessage ? imageMessageDialogItems : messageDialogItems);
        messageListDialog.show(getSupportFragmentManager(),
                isImageMessage ? IMAGE_MESSAGE_LIST_DIALOG_TAG : MESSAGE_LIST_DIALOG_TAG);

        return true;
    }

    @Override
    public void onDialogItemClick(String tag, int position) {
        Message message = chatListAdapter.getItem(selectedItemPosition);
        if (message == null) return;

        switch (tag) {
            case MESSAGE_LIST_DIALOG_TAG:
                onTextMessageDialog(message, position);
                break;
            case IMAGE_MESSAGE_LIST_DIALOG_TAG:
                onImageMessageDialog(message, position);
                break;
        }
    }

    private void onTextMessageDialog(Message message, int position) {
        switch (position) {
            case 0:
                copyTextToClipboard(message.getMessage());
                break;
            case 1:
                chatRoomFragment.deleteMessage(message);
                break;
        }
    }

    private void onImageMessageDialog(Message message, int position) {
        switch (position) {
            case 0:
                chatRoomFragment.deleteMessage(message);
                break;
        }
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboardManager = AppController.getComponent(this).clipboardManager();
        ClipData textClipData = ClipData.newPlainText(getString(R.string.message), text);

        clipboardManager.setPrimaryClip(textClipData);
    }
}
