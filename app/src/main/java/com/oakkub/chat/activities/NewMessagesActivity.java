package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.CreatePrivateRoomFragment;
import com.oakkub.chat.fragments.FriendsFetchingFragment;
import com.oakkub.chat.fragments.NewMessagesFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusNewMessagesFriendInfo;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.utils.SortUtil;
import com.oakkub.chat.views.adapters.FriendSelectableAdapter;
import com.oakkub.chat.views.adapters.UserImageAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.EmptyTextProgressBar;
import com.oakkub.chat.views.widgets.MyToast;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * Created by OaKKuB on 12/24/2015.
 */
public class NewMessagesActivity extends BaseActivity implements OnAdapterItemClick,
        NewMessagesFragment.OnRoomRequest, Filter.FilterListener {

    public static final String EXTRA_FRIEND_ID = "extra:friendId";

    private static final String STATE_FRIEND_INFO = "state:friendInfo";
    private static final String STATE_LIST_ADAPTER = "state:listAdapter";
    private static final String STATE_SELECTED_LIST_ADAPTER = "state:selectedlistAdapter";
    private static final String TAG_CREATING_ROOM_DIALOG = "tag:creatingRoomDialog";
    private static final String TAG_FRIENDS_FETCHING = "tag:friendsFetching";
    private static final String TAG_CREATING_NEW_MESSAGES = "tag:creatingNewMessages";
    private static final String CREATE_PRIVATE_ROOM_TAG = "tag:createPrivateRoom";
    private static final String TAG = NewMessagesActivity.class.getSimpleName();

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.find_friend_edittext)
    EditText findFriendsEditText;

    @Bind(R.id.find_friend_selected_friend_recyclerview)
    RecyclerView selectedFriendImageList;

    @Bind(R.id.find_friend_selected_friend_line)
    View selectedFriendLine;

    @Bind(R.id.find_friend_friend_recyclerview)
    RecyclerView friendList;

    @Bind(R.id.find_friend_emptyTextProgressBar)
    EmptyTextProgressBar progressBarLayout;

    @State
    int maxSelectedItems;

    @State
    int totalSelectedItems;

    @State
    String myId;

    @State
    boolean filteredFirstTime;

    private UserInfo friendInfo;

    private FriendsFetchingFragment friendsFetchingFragment;
    private NewMessagesFragment newMessagesFragment;
    private CreatePrivateRoomFragment createPrivateRoomFragment;
    private FriendSelectableAdapter friendSelectableAdapter;
    private UserImageAdapter userImageAdapter;

    public static Intent getStartIntent(Context context, String myId, UserInfo friendInfo) {
        Intent newMessageIntent = new Intent(context, NewMessagesActivity.class);
        newMessageIntent.putExtra(EXTRA_MY_ID, myId);
        newMessageIntent.putExtra(EXTRA_FRIEND_ID, Parcels.wrap(friendInfo));

        return newMessageIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataIntent(savedInstanceState);
        setContentView(R.layout.activity_selectable_list);
        ButterKnife.bind(this);

        setToolbar();
        initRecyclerView();

        EventBus.getDefault().register(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        addFragments();

        if (savedInstanceState == null) {
            if (friendInfo != null) {
                friendSelectableAdapter.setSelection(0, friendInfo.hashCode());
                showSelectedItem(friendInfo);
            }
        }
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

        createPrivateRoomFragment = (CreatePrivateRoomFragment) findFragmentByTag(CREATE_PRIVATE_ROOM_TAG);
        if (createPrivateRoomFragment == null) {
            createPrivateRoomFragment = (CreatePrivateRoomFragment)
                    addFragmentByTag(CreatePrivateRoomFragment.newInstance(null, myId),
                    CREATE_PRIVATE_ROOM_TAG);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        friendsFetchingFragment.fetchUserFriends(myId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (friendInfo != null) {
            outState.putParcelable(STATE_FRIEND_INFO, Parcels.wrap(friendInfo));
        }

        friendSelectableAdapter.onSaveInstanceState(STATE_LIST_ADAPTER, outState);
        userImageAdapter.onSaveInstanceState(STATE_SELECTED_LIST_ADAPTER, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        friendSelectableAdapter.onRestoreInstanceState(STATE_LIST_ADAPTER, savedInstanceState);
        userImageAdapter.onRestoreInstanceState(STATE_SELECTED_LIST_ADAPTER, savedInstanceState);
        searchFriend(findFriendsEditText.getText().toString());

        friendInfo = Parcels.unwrap(savedInstanceState.getParcelable(STATE_FRIEND_INFO));

        shouldShowSelectedImageList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBack();
                return true;
            case R.id.action_ok:

                if (totalSelectedItems > maxSelectedItems) {
                    MyToast.make(getString(R.string.error_select_friend_only_n_amount, maxSelectedItems)).show();
                    return true;
                }
                onNewMessagesButtonClick();
                return true;
        }

        return false;
    }

    private void onBack() {
        if (!findFriendsEditText.getText().toString().isEmpty()) {
            findFriendsEditText.setText("");
            searchFriend(findFriendsEditText.getText().toString());
        } else if (totalSelectedItems > 0) {
            clearAllSelection();
        } else {
            super.onBackPressed();
        }
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Intent intent = getIntent();

        myId = intent.getStringExtra(EXTRA_MY_ID);
        friendInfo = Parcels.unwrap(intent.getParcelableExtra(EXTRA_FRIEND_ID));

        Resources res = getResources();
        maxSelectedItems = res.getInteger(R.integer.max_group_member) - 1;
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setToolbarName();
    }

    private void setToolbarName() {
        if (totalSelectedItems > 0) {
            setToolbarTitle(getString(R.string.total_selected, totalSelectedItems));
        } else {
            setToolbarTitle(getString(R.string.new_messages));
        }
    }

    private void initRecyclerView() {
        friendSelectableAdapter = new FriendSelectableAdapter(this, true);

        friendList.setItemAnimator(null);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendList.setAdapter(friendSelectableAdapter);

        userImageAdapter = new UserImageAdapter();

        selectedFriendImageList.setItemAnimator(null);
        selectedFriendImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedFriendImageList.setAdapter(userImageAdapter);
        shouldShowSelectedImageList();
    }

    private void shouldShowSelectedImageList() {
        int visibility = friendSelectableAdapter.getSelectedItemCount() > 0 ? View.VISIBLE : View.GONE;

        selectedFriendImageList.setVisibility(visibility);
        selectedFriendLine.setVisibility(visibility);
    }

    private void showSelectedItem(UserInfo userInfo) {
        if (!userImageAdapter.contains(userInfo)) {
            userImageAdapter.addLast(userInfo);
        } else {
            userImageAdapter.remove(userInfo);
        }
        shouldShowSelectedImageList();
    }

    private void setSelectedItem(int position) {
        // add or remove item from selected image list
        UserInfo userInfo = friendSelectableAdapter.getItem(position);

        friendSelectableAdapter.toggleSelection(position, userInfo.hashCode());
        totalSelectedItems = friendSelectableAdapter.getSelectedItemCount();

        showSelectedItem(userInfo);
        setToolbarName();
    }

    private void searchFriend(String query) {
        friendSelectableAdapter.getFilter().filter(query, this);
    }

    @Override
    public void onFilterComplete(int count) {
    }

    private void clearAllSelection() {
        friendSelectableAdapter.clearSelection();
        totalSelectedItems = 0;
        userImageAdapter.clear();

        setToolbarTitle(getString(R.string.new_messages));
        shouldShowSelectedImageList();
    }

    @OnTextChanged(value = R.id.find_friend_edittext, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onFriendFriendEditTextChanged(Editable editable) {
        searchFriend(editable.toString());
    }

    private boolean checkNewMessageError(int totalSelectedItem) {
        if (friendInfo == null && totalSelectedItem == 0) {
            MyToast.make(getString(R.string.error_select_at_least_one_person));
            return true;
        }
        if (friendInfo != null && totalSelectedItem <= 1) {
            MyToast.make(getString(R.string.error_select_at_least_two_to_make_a_group)).show();
            return true;
        }
        return false;
    }

    public void onNewMessagesButtonClick() {
        int[] totalSelectedItemsPosition = userImageAdapter.getTotalItemsPosition();
        int totalSelected = totalSelectedItemsPosition.length;

        if (checkNewMessageError(totalSelected)) {
            return;
        }

        if (totalSelected >= 2) {
            // group room
            createGroupRoom(totalSelectedItemsPosition);
        } else if (totalSelected == 1) {
            // 1 - 1 room (private)
            createPrivateRoom(totalSelectedItemsPosition);
        }
    }

    private void createGroupRoom(int[] totalSelectedItemsPosition) {
        int size = totalSelectedItemsPosition.length;

        // we gonna use our image to combine with the rest.
        UserInfo[] membersInfo = new UserInfo[size + 1];
        int membersSize = membersInfo.length;

        for (int i = 0; i < membersSize; i++) {
            if (i > 0) {
                membersInfo[i] = userImageAdapter.getItem(totalSelectedItemsPosition[i - 1]);
            } else {
                UserInfo myInfo = new UserInfo();
                myInfo.setKey(myId);

                membersInfo[i] = myInfo;
            }
        }

        newMessagesFragment.createGroupRoom(myId, membersInfo);
    }

    private void createPrivateRoom(int[] totalSelectedItemsPosition) {
        UserInfo friendInfo = userImageAdapter.getItem(totalSelectedItemsPosition[0]);
        newMessagesFragment.createPrivateRoom(myId, friendInfo);
        createPrivateRoomFragment.createPrivateRoom(
                RoomUtil.getPrivateRoomKey(this, myId, friendInfo.getKey()), friendInfo);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        setSelectedItem(position);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return true;
    }

    @Override
    public void onRoomCreated(Room room) {
        ProgressDialogFragment dialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_CREATING_ROOM_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }

        if (room.getName() != null) {
            Intent privateRoomIntent = ChatRoomActivity.getIntentGroupRoom(this, room, myId);
            startActivity(privateRoomIntent);
        } else {
            Intent groupRoomIntent = ChatRoomActivity.getIntentPrivateRoom(this, room, myId);
            startActivity(groupRoomIntent);
        }

        fadeOutFinish();
    }

    @Override
    public void onShowLoading() {
        ProgressDialogFragment dialog = ProgressDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), TAG_CREATING_ROOM_DIALOG);
    }

    public void onEvent(EventBusNewMessagesFriendInfo eventBusNewMessagesFriendInfo) {
        progressBarLayout.hideProgressBar();

        List<UserInfo> friendListInfo = eventBusNewMessagesFriendInfo.friendListInfo;
        SortUtil.sortUserInfoAlphabetically(friendListInfo);
        friendSelectableAdapter.addLastAll(friendListInfo);

        searchFriend(findFriendsEditText.getText().toString());
    }

}
