package com.oakkub.chat.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.FriendsFetchingFragment;
import com.oakkub.chat.fragments.NewMessagesFragment;
import com.oakkub.chat.models.EventBusNewMessagesFriendInfo;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.SortUtil;
import com.oakkub.chat.views.adapters.SelectableFriendAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * Created by OaKKuB on 12/24/2015.
 */
public class NewMessagesActivity extends BaseActivity implements OnAdapterItemClick, NewMessagesFragment.OnImageRequestListener {

    public static final String EXTRA_MY_ID = "extra:myId";

    private static final String STATE_LIST_ADAPTER = "state:listAdapter";
    private static final String TAG_FRIENDS_FETCHING = "tag:friendsFetching";
    private static final String TAG_CREATING_NEW_MESSAGES = "tag:creatingNewMessages";
    private static final String TAG = NewMessagesActivity.class.getSimpleName();

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.new_messages_edittext)
    EditText newMessageEditText;

    @Bind(R.id.test_image)
    SimpleDraweeView testImage;

    @Bind(R.id.recyclerview)
    RecyclerView friendList;

    @State
    int totalSelectedItems;

    @State
    String myId;

    private FriendsFetchingFragment friendsFetchingFragment;
    private NewMessagesFragment newMessagesFragment;
    private SelectableFriendAdapter selectableFriendAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromIntent(savedInstanceState);
        setContentView(R.layout.activity_new_messages);
        ButterKnife.bind(this);

        setToolbar();
        initRecyclerView();

        EventBus.getDefault().register(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        addFragments();
    }

    private void addFragments() {
        friendsFetchingFragment = (FriendsFetchingFragment) findFragmentByTag(TAG_FRIENDS_FETCHING);
        if (friendsFetchingFragment == null) {
            friendsFetchingFragment = (FriendsFetchingFragment) addFragmentByTag(
                    FriendsFetchingFragment.newInstance(FriendsFetchingFragment.FROM_NEW_MESSAGES),
                    TAG_FRIENDS_FETCHING);
        }

        newMessagesFragment = (NewMessagesFragment) findFragmentByTag(TAG_CREATING_NEW_MESSAGES);
        if (newMessagesFragment == null) {
            newMessagesFragment = (NewMessagesFragment) addFragmentByTag(
                    new NewMessagesFragment(), TAG_CREATING_NEW_MESSAGES);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            friendsFetchingFragment.fetchUserFriends(myId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        selectableFriendAdapter.onSaveInstanceState(STATE_LIST_ADAPTER, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        selectableFriendAdapter.onRestoreInstanceState(STATE_LIST_ADAPTER, savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (totalSelectedItems <= 0) {
            super.onBackPressed();
        } else {
            selectableFriendAdapter.clearSelection();
            totalSelectedItems = 0;
            setToolbarTitle(getString(R.string.new_messages));
        }
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        myId = getIntent().getStringExtra(EXTRA_MY_ID);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        if (totalSelectedItems > 0) {
            setToolbarTitle(getString(R.string.total_friends, totalSelectedItems));
        } else {
            setToolbarTitle(getString(R.string.new_messages));
        }
    }

    private void initRecyclerView() {
        selectableFriendAdapter = new SelectableFriendAdapter(this);

        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendList.setAdapter(selectableFriendAdapter);
    }

    private void findFriendsFetchingFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        friendsFetchingFragment = (FriendsFetchingFragment) fragmentManager.findFragmentByTag(TAG_FRIENDS_FETCHING);

        if (friendsFetchingFragment == null) {
            friendsFetchingFragment = FriendsFetchingFragment.newInstance(FriendsFetchingFragment.FROM_NEW_MESSAGES);

            fragmentManager.beginTransaction()
                    .add(friendsFetchingFragment, TAG_FRIENDS_FETCHING)
                    .commit();
        }

    }

    private void setTotalSelectedItemToolbar(int position) {
        selectableFriendAdapter.toggleSelection(position);
        totalSelectedItems = selectableFriendAdapter.getSelectedItemCount();

        if (totalSelectedItems > 0) {
            setToolbarTitle(getString(R.string.total_friends, totalSelectedItems));
        } else {
            setToolbarTitle(getString(R.string.new_messages));
        }
    }

    @OnClick(R.id.new_messages_button)
    public void onNewMessagesButtonClick() {
        String message = newMessageEditText.getText().toString().trim();
        if (message.isEmpty()) return;

        long messageTime = System.currentTimeMillis();
        int[] totalSelectedItemsPosition = selectableFriendAdapter.getSelectedItems();
        if (totalSelectedItemsPosition.length >= 2) {
            // group room
            createGroupRoom(totalSelectedItemsPosition, message, messageTime);
        } else {
            // 1 - 1 room (private)
            createPrivateRoom(totalSelectedItemsPosition, message, messageTime);
        }
    }

    private void createGroupRoom(int[] totalSelectedItemsPosition, String message, long messageTime) {
        int size = 3;

        UserInfo[] friendsInfo = new UserInfo[size];

        // array index 3 will be the head of room (our profile image)
        // we will fetch profile image header with firebase in NewMessageFragment
        for (int i = 0; i < (size - 1); i++) {
            friendsInfo[i] = selectableFriendAdapter.getItem(totalSelectedItemsPosition[i]);
        }

        newMessagesFragment.createGroupRoom(myId, friendsInfo, message, messageTime);
    }

    private void createPrivateRoom(int[] totalSelectedItemsPosition, String message, long messageTime) {
        UserInfo friendInfo = selectableFriendAdapter.getItem(totalSelectedItemsPosition[0]);

        newMessagesFragment.createPrivateRoom(myId, friendInfo, message, messageTime);
    }

    @Override
    public void onImageReceived(String base64Bitmap) {
        testImage.setImageURI(Uri.parse(Base64Util.toDataUri(base64Bitmap)));
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        setTotalSelectedItemToolbar(position);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        setTotalSelectedItemToolbar(position);

        return true;
    }

    public void onEvent(EventBusNewMessagesFriendInfo eventBusNewMessagesFriendInfo) {
        List<UserInfo> friendListInfo = eventBusNewMessagesFriendInfo.friendListInfo;
        SortUtil.sortUserInfoAlphabetically(friendListInfo);
        selectableFriendAdapter.addFirstAll(friendListInfo);
    }

}
