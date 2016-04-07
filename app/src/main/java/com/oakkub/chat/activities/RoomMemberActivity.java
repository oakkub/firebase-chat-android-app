package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.KeyToValueFirebaseFetchingFragment;
import com.oakkub.chat.fragments.RoomMemberFragment;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.adapters.FriendSelectableAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * Created by OaKKuB on 1/8/2016.
 */
public class RoomMemberActivity extends BaseActivity implements RoomMemberFragment.OnRoomMemberRequest,
        KeyToValueFirebaseFetchingFragment.OnDataReceivedListener, OnAdapterItemClick {

    private static final String TAG = RoomMemberActivity.class.getSimpleName();
    private static final String TAG_MEMBER_FRAG = "tag:memberFragment";
    private static final String TAG_ADMIN_FRAG = "tag:publicFragment";
    private static final String EXTRA_ROOM_ID = "extra:roomId";
    private static final String EXTRA_IS_AUTHENTICATED = "extra:isAuthenticated";
    private static final String EXTRA_IS_MEMBER = "extra:isMember";
    private static final String GROUP_MEMBER_ADAPTER_STATE = "state:groupMemberAdapter";

    public static final String EXTRA_MEMBER_KEY_ARRAY = "extra:memberKeyArray";

    public static final int RC_MEMBER_REMOVED = 0;
    public static final int RC_MEMBER_ADDED = 1;
    public static final int RC_ADMIN_ADDED = 2;
    public static final int RC_ADMIN_REMOVED = 3;

    public static final String ACTION_MEMBER = "action:member";
    public static final String ACTION_PUBLIC_ADMIN = "action:publicAdmin";
    public static final String ACTION_PUBLIC_MEMBER = "action:publicMember";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.recyclerview)
    RecyclerView memberRecyclerView;

    @Bind(R.id.frame_progress_bar_layout)
    FrameLayout frameProgressBarLayout;

    @State
    String roomId;

    @State
    String action;

    @State
    String roomMemberPath;

    @State
    String userInfoPath;

    @State
    boolean isMember;

    @State
    boolean isAuthenticated;

    private FriendSelectableAdapter memberAdapter;

    public static Intent getGroupIntent(Context context, String roomId, boolean isMember, boolean isAuthenticated) {
        Intent intent = new Intent(context, RoomMemberActivity.class);
        intent.setAction(ACTION_MEMBER);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_IS_AUTHENTICATED, isAuthenticated);
        intent.putExtra(EXTRA_IS_MEMBER, isMember);

        return intent;
    }

    public static Intent getPublicIntent(Context context, String roomId, boolean isAuthenticated) {
        Intent intent = new Intent(context, RoomMemberActivity.class);
        intent.setAction(isAuthenticated ? ACTION_PUBLIC_ADMIN : ACTION_PUBLIC_MEMBER);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_IS_AUTHENTICATED, isAuthenticated);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_with_recyclerview);
        ButterKnife.bind(this);
        getDataIntent(savedInstanceState);

        initInstances();
        addFragments();

    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();

            action = intent.getAction();
            roomId = intent.getStringExtra(EXTRA_ROOM_ID);
            isMember = intent.getBooleanExtra(EXTRA_IS_MEMBER, false);
            isAuthenticated = intent.getBooleanExtra(EXTRA_IS_AUTHENTICATED, false);
        }
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(action.equals(ACTION_MEMBER) ? R.string.members : R.string.admin);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberRecyclerView.setHasFixedSize(true);

        memberAdapter = new FriendSelectableAdapter(this, false);
        memberRecyclerView.setAdapter(memberAdapter);
    }

    private void addFragments() {
        if (roomMemberPath == null || userInfoPath == null) {
            roomMemberPath = action.equals(ACTION_MEMBER) ?
                    TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_MEMBERS, roomId)
                    :
                    TextUtil.getPath(FirebaseUtil.KEY_ROOMS, FirebaseUtil.KEY_ROOMS_ADMIN_MEMBERS, roomId);
            userInfoPath = TextUtil.getPath(FirebaseUtil.KEY_USERS, FirebaseUtil.KEY_USERS_USER_INFO);
        }

        if (action.equals(ACTION_MEMBER)) {
            addFragment(TAG_MEMBER_FRAG, roomMemberPath, userInfoPath);
        } else if (action.equals(ACTION_PUBLIC_ADMIN) || action.equals(ACTION_PUBLIC_MEMBER)) {
            addFragment(TAG_ADMIN_FRAG, roomMemberPath, userInfoPath);
        }
    }

    private void addFragment(String tag, String roomMemberPath, String userInfoPath) {
        if (findFragmentByTag(tag) == null) {
            addFragmentByTag(KeyToValueFirebaseFetchingFragment.newInstance(
                    uid, roomMemberPath, userInfoPath), tag);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fadeOutFinish();
                return true;
            case R.id.action_add_member:
                addMemberOptionSelected();
                return true;
            case R.id.action_add_admin:
                addAdminOptionSelected();
                return true;
            case R.id.action_remove_member:
                removeMemberOptionSelected();
                return true;
            case R.id.action_remove_admin:
                removeAdminOptionSelected();
                return true;
        }

        return false;
    }

    private void addMemberOptionSelected() {
        Intent inviteMemberIntent = MemberManagerActivity.getStartIntent(this,
                roomId, MemberManagerActivity.ACTION_MEMBER_INVITE);
        startActivityForResult(inviteMemberIntent, RC_MEMBER_ADDED);
    }

    private void removeMemberOptionSelected() {
        if (!isAuthenticated) return;

        Intent removeMemberIntent = MemberManagerActivity.getStartIntent(this,
                roomId, MemberManagerActivity.ACTION_MEMBER_REMOVE);
        startActivityForResult(removeMemberIntent, RC_MEMBER_REMOVED);
    }

    private void addAdminOptionSelected() {
        if (!isAuthenticated) return;

        Intent addAdminIntent = MemberManagerActivity.getStartIntent(this,
                roomId, MemberManagerActivity.ACTION_ADMIN_PROMOTE);
        startActivityForResult(addAdminIntent, RC_ADMIN_ADDED);
    }

    private void removeAdminOptionSelected() {
        if (!isAuthenticated) return;

        Intent removeAdminIntent = MemberManagerActivity.getStartIntent(this,
                roomId, MemberManagerActivity.ACTION_ADMIN_DEMOTE);
        startActivityForResult(removeAdminIntent, RC_ADMIN_REMOVED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(action.equals(ACTION_MEMBER) ?
                R.menu.menu_member_info : R.menu.menu_admin_info, menu);

        if (action.equals(ACTION_MEMBER) && !isMember) {
            menu.removeItem(R.id.action_add_member);
        }

        if (!isAuthenticated) {
            menu.removeItem(R.id.action_remove_member);
            menu.removeItem(R.id.action_add_admin);
            menu.removeItem(R.id.action_remove_admin);
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (memberAdapter != null) {
            memberAdapter.onSaveInstanceState(GROUP_MEMBER_ADAPTER_STATE, outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        if (memberAdapter != null) {
            memberAdapter.onRestoreInstanceState(GROUP_MEMBER_ADAPTER_STATE, savedInstanceState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case RC_MEMBER_ADDED:
            case RC_ADMIN_ADDED:
                onResultMemberAdded(data);
                break;
            case RC_MEMBER_REMOVED:
            case RC_ADMIN_REMOVED:
                onResultMemberRemoved(data);
                break;
        }
    }

    private void onResultMemberRemoved(Intent data) {
        ArrayList<UserInfo> userInfoList = Parcels.unwrap(data.getParcelableExtra(EXTRA_MEMBER_KEY_ARRAY));
        for (int i = 0, size = userInfoList.size(); i < size; i++) {
            memberAdapter.remove(userInfoList.get(i));
        }
    }

    private void onResultMemberAdded(Intent data) {
        ArrayList<UserInfo> userInfoList = Parcels.unwrap(data.getParcelableExtra(EXTRA_MEMBER_KEY_ARRAY));
        for (int i = 0, size = userInfoList.size(); i < size; i++) {
            memberAdapter.addFirst(userInfoList.get(i));
        }
    }

    @Override
    public void onValueNodeItemReceived(String itemKey, HashMap<String, Object> itemMap) {
        frameProgressBarLayout.setVisibility(View.GONE);
        UserInfo userInfo = UserInfoUtil.get(itemKey, itemMap);
        memberAdapter.addLast(userInfo);
    }

    @Override
    public void onRoomMemberFetched(ArrayList<UserInfo> memberList) {
        memberAdapter.addLastAll(memberList);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        /*UserInfo memberInfo = memberAdapter.getItem(position);
        FriendDetailActivity.launch(this, null, memberInfo, -1);*/
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }
}
