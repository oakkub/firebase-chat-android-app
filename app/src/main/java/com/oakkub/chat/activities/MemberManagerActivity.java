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
import android.widget.EditText;
import android.widget.Filter;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AddAdminFragment;
import com.oakkub.chat.fragments.InviteMemberFragment;
import com.oakkub.chat.fragments.RemoveAdminFragment;
import com.oakkub.chat.fragments.RemoveMemberFragment;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.FriendSelectableAdapter;
import com.oakkub.chat.views.adapters.UserImageAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.EmptyTextProgressBar;
import com.oakkub.chat.views.widgets.MyToast;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import icepick.State;

/**
 * Created by OaKKuB on 2/8/2016.
 */
public class MemberManagerActivity extends BaseActivity implements OnAdapterItemClick,
        InviteMemberFragment.OnInviteMemberFetchingListener, Filter.FilterListener,
        RemoveMemberFragment.OnRemoveMemberListener, AddAdminFragment.OnPromoteAdminListener,
        RemoveAdminFragment.OnRemoveAdminListener {

    public static final String ACTION_MEMBER_INVITE = "action:memberInvite";
    public static final String ACTION_MEMBER_REMOVE = "action:memberRemove";
    public static final String ACTION_ADMIN_PROMOTE = "action:adminInvite";
    public static final String ACTION_ADMIN_DEMOTE = "action:adminRemove";

    private static final String TAG = MemberManagerActivity.class.getSimpleName();
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";
    private static final String INVITE_MEMBER_FRAG_TAG = "tag:inviteMemberFragment";
    private static final String REMOVE_MEMBER_FRAG_TAG = "tag:removeMemberFragment";
    private static final String REMOVE_ADMIN_FRAG_TAG = "tag:removeAdminFragment";
    private static final String PROMOTE_ADMIN_FRAG_TAG = "tag:promoteAdminFragment";
    private static final String STATE_FRIEND_LIST = "state:friendList";
    private static final String STATE_FRIEND_IMAGE_LIST = "state:friendImageList";
    private static final String EXTRA_ROOM_ID = "extra:roomId";

    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;

    @BindView(R.id.find_friend_edittext)
    EditText findFriendsEditText;

    @BindView(R.id.find_friend_selected_friend_recyclerview)
    RecyclerView selectedFriendImageList;

    @BindView(R.id.find_friend_selected_friend_line)
    View selectedFriendLine;

    @BindView(R.id.find_friend_friend_recyclerview)
    RecyclerView friendList;

    @BindView(R.id.find_friend_emptyTextProgressBar)
    EmptyTextProgressBar progressBarLayout;

    @State
    int totalSelectedItems;

    @State
    String roomId;

    @State
    String action;

    @State
    String errorMessage;

    @State
    int totalExistedFriend;

    @State
    int maxSelectedFriend;

    @State
    String[] totalFriendsKeySelected;

    private RemoveAdminFragment removeAdminFragment;
    private RemoveMemberFragment removeMemberFragment;
    private InviteMemberFragment inviteMemberFragment;
    private AddAdminFragment addAdminFragment;
    private FriendSelectableAdapter friendSelectableAdapter;
    private UserImageAdapter userImageAdapter;

    public static Intent getStartIntent(Context context, String roomId, String action) {
        Intent intent = new Intent(context, MemberManagerActivity.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_ROOM_ID, roomId);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectable_list);
        ButterKnife.bind(this);
        getDataIntent(savedInstanceState);
        initInstances();
        addFragments();

        shouldShowSelectedImageList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        friendSelectableAdapter.onSaveInstanceState(STATE_FRIEND_LIST, outState);
        userImageAdapter.onSaveInstanceState(STATE_FRIEND_IMAGE_LIST, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        friendSelectableAdapter.onRestoreInstanceState(STATE_FRIEND_LIST, savedInstanceState);
        userImageAdapter.onRestoreInstanceState(STATE_FRIEND_IMAGE_LIST, savedInstanceState);

        searchFriend(findFriendsEditText.getText().toString());
        shouldShowSelectedImageList();
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
                onOkButtonClick();
                return true;
        }

        return false;
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        roomId = intent.getStringExtra(EXTRA_ROOM_ID);
        action = intent.getAction();

        Resources res = getResources();
        maxSelectedFriend = res.getInteger(R.integer.max_group_member) - 1;

        if (action == null) {
            fadeOutFinish();
        }
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setInviteMemberToolbarName();
        setSearchHint();

        friendSelectableAdapter = new FriendSelectableAdapter(this, true);
        userImageAdapter = new UserImageAdapter();

        friendList.setItemAnimator(null);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendList.setAdapter(friendSelectableAdapter);

        selectedFriendImageList.setItemAnimator(null);
        selectedFriendImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedFriendImageList.setAdapter(userImageAdapter);
    }

    private void setSearchHint() {
        String hint = "";
        switch (action) {
            case ACTION_ADMIN_PROMOTE:
            case ACTION_ADMIN_DEMOTE:
                hint = getString(R.string.find_admin);
                break;
            case ACTION_MEMBER_INVITE:
            case ACTION_MEMBER_REMOVE:
                hint = getString(R.string.find_friends);
                break;
        }

        findFriendsEditText.setHint(hint);
    }

    private void addFragments() {
        switch (action) {
            case ACTION_MEMBER_INVITE:
                inviteMemberFragment = (InviteMemberFragment)
                        findOrAddFragmentByTag(getSupportFragmentManager(),
                            InviteMemberFragment.newInstance(roomId), INVITE_MEMBER_FRAG_TAG);
                break;
            case ACTION_MEMBER_REMOVE:
                removeMemberFragment = (RemoveMemberFragment)
                        findOrAddFragmentByTag(getSupportFragmentManager(),
                            RemoveMemberFragment.newInstance(roomId), REMOVE_MEMBER_FRAG_TAG);
                break;
            case ACTION_ADMIN_PROMOTE:
                addAdminFragment = (AddAdminFragment)
                        findOrAddFragmentByTag(getSupportFragmentManager(),
                            AddAdminFragment.newInstance(roomId), PROMOTE_ADMIN_FRAG_TAG);
                break;
            case ACTION_ADMIN_DEMOTE:
                removeAdminFragment = (RemoveAdminFragment)
                        findOrAddFragmentByTag(getSupportFragmentManager(),
                            RemoveAdminFragment.newInstance(roomId), REMOVE_ADMIN_FRAG_TAG);
                break;
        }

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
        setInviteMemberToolbarName();
    }

    private void setInviteMemberToolbarName() {
        if (totalSelectedItems > 0) {
            setToolbarTitle(getString(R.string.value_slash_value,
                    totalSelectedItems,
                    action.equals(ACTION_MEMBER_INVITE) ?
                            maxSelectedFriend - totalExistedFriend
                            :
                            totalExistedFriend));
        } else {
            switch (action) {
                case ACTION_ADMIN_PROMOTE:
                    setToolbarTitle(getString(R.string.add_admin));
                    break;
                case ACTION_ADMIN_DEMOTE:
                    setToolbarTitle(getString(R.string.demote_admin));
                    break;
                case ACTION_MEMBER_INVITE:
                    setToolbarTitle(getString(R.string.invite_friends));
                    break;
                case ACTION_MEMBER_REMOVE:
                    setToolbarTitle(getString(R.string.remove_member));
                    break;
            }
        }
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

    private void clearAllSelection() {
        friendSelectableAdapter.clearSelection();
        totalSelectedItems = 0;
        userImageAdapter.clear();

        setToolbarTitle(getString(R.string.new_messages));
        shouldShowSelectedImageList();
    }

    private void onOkButtonClick() {
        int[] totalItems = userImageAdapter.getTotalItemsPosition();
        int size = totalItems.length;

        if (size == 0) {
            MyToast.make(getString(R.string.error_select_at_least_one_person)).show();
            return;
        }

        String[] totalFriendToBeInvited = new String[size];
        for (int i = 0; i < size; i++) {
            totalFriendToBeInvited[i] = userImageAdapter.getItem(i).getKey();
        }
        totalFriendsKeySelected = totalFriendToBeInvited;

        switch (action) {
            case ACTION_MEMBER_INVITE:
                inviteMemberFragment.invite(totalFriendToBeInvited);
                break;
            case ACTION_MEMBER_REMOVE:
                removeMemberFragment.remove(totalFriendToBeInvited);
                break;
            case ACTION_ADMIN_PROMOTE:
                addAdminFragment.promote(totalFriendToBeInvited);
                break;
            case ACTION_ADMIN_DEMOTE:
                removeAdminFragment.remove(totalFriendToBeInvited);
                break;
        }
        showProgressDialog();
    }

    private void addUserInfo(UserInfo userInfo) {
        progressBarLayout.setVisibility(View.GONE);

        friendSelectableAdapter.addLast(userInfo);
        searchFriend(findFriendsEditText.getText().toString());
    }

    private void showEmptyMessage() {
        if (friendSelectableAdapter.getItemCount() == 0) {
            progressBarLayout.setEmptyText(getErrorMessage());
            progressBarLayout.showEmptyText();
        } else {
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    private String getErrorMessage() {
        if (errorMessage != null) return errorMessage;

        switch (action) {
            case ACTION_MEMBER_INVITE:
                errorMessage = getString(R.string.error_no_friend_to_be_invited);
                break;
            case ACTION_MEMBER_REMOVE:
                errorMessage = getString(R.string.error_no_member_to_be_removed);
                break;
            case ACTION_ADMIN_PROMOTE:
                errorMessage = getString(R.string.error_no_addable_admin);
                break;
            case ACTION_ADMIN_DEMOTE:
                errorMessage = getString(R.string.error_no_admin_to_be_demoted);
                break;
        }

        return errorMessage;
    }

    private void searchFriend(String query) {
        friendSelectableAdapter.getFilter().filter(query, this);
    }

    @OnTextChanged(value = R.id.find_friend_edittext, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onFriendFriendEditTextChanged(Editable editable) {
        searchFriend(editable.toString());
    }

    @Override
    public void onFilterComplete(int count) {
        if (friendSelectableAdapter.getPhysicalItemCount() > 0) {
            showEmptyMessage();
        }
    }

    @Override
    public void onTotalFriendFetched(int totalFriend) {
        totalExistedFriend = totalFriend;
        setInviteMemberToolbarName();
    }

    @Override
    public void onAddableFriendFetched(UserInfo addableFriendInfo) {
        addUserInfo(addableFriendInfo);
    }

    @Override
    public void onInvitingSuccess() {
        hideProgressDialog();

        setResult(RESULT_OK, getMemberResultIntent());
        fadeOutFinish();
    }

    @Override
    public void onInvitingFailed() {
        hideProgressDialog();
    }

    @Override
    public void onNoInviteMember() {
        showEmptyMessage();
    }

    @Override
    public void onRemovableTotalMember(int totalFriend) {
        totalExistedFriend = totalFriend;
        setInviteMemberToolbarName();
    }

    @Override
    public void onRemovableFriendInfoFetched(UserInfo addableFriendInfo) {
        addUserInfo(addableFriendInfo);
    }

    @Override
    public void onNoRemovableMember() {
        showEmptyMessage();
    }

    @Override
    public void onRemovableSuccess() {
        hideProgressDialog();

        setResult(RESULT_OK, getMemberResultIntent());
        fadeOutFinish();
    }

    @Override
    public void onRemovableFailed() {
        hideProgressDialog();
    }

    @Override
    public void onTotalAdminFetched(int totalAdmin) {
        totalExistedFriend = totalAdmin;
        setInviteMemberToolbarName();
    }

    @Override
    public void onAddableAdminFetched(UserInfo addableMemberInfo) {
        addUserInfo(addableMemberInfo);
    }

    @Override
    public void onNoAddableAdmin() {
        showEmptyMessage();
    }

    @Override
    public void onPromoteSuccess() {
        hideProgressDialog();

        setResult(RESULT_OK, getMemberResultIntent());
        fadeOutFinish();
    }

    @Override
    public void onPromoteFailed() {
        hideProgressDialog();
    }

    @Override
    public void onRemovableTotalAdmin(int totalAdmin) {
        totalExistedFriend = totalAdmin;
        setInviteMemberToolbarName();
    }

    @Override
    public void onRemovableAdminInfoFetched(UserInfo addableFriendInfo) {
        addUserInfo(addableFriendInfo);
    }

    @Override
    public void onRemovableAdminSuccess() {
        hideProgressDialog();

        setResult(RESULT_OK, getMemberResultIntent());
        fadeOutFinish();
    }

    @Override
    public void onNoRemovableAdmin() {
        showEmptyMessage();
    }

    @Override
    public void onRemovableAdminFailed() {
        hideProgressDialog();
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        setSelectedItem(position);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    private Intent getMemberResultIntent() {
        Intent intent = new Intent();
        intent.putExtra(RoomMemberActivity.EXTRA_MEMBER_KEY_ARRAY, Parcels.wrap(getSelectedUserInfoList()));
        return intent;
    }

    private ArrayList<UserInfo> getSelectedUserInfoList() {
        ArrayList<UserInfo> userInfoList = new ArrayList<>(totalFriendsKeySelected.length);

        for (String keySelected : totalFriendsKeySelected) {
            int keyPosition = friendSelectableAdapter.findByKey(keySelected);
            if (keyPosition != -1) {
                userInfoList.add(friendSelectableAdapter.getItem(keyPosition));
            }
        }

        return userInfoList;
    }
}
