package com.oakkub.chat.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.LeavePublicChatFragment;
import com.oakkub.chat.fragments.RoomAdminAuthenticationFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MyDraweeView;
import com.oakkub.chat.views.widgets.MyToast;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

/**
 * Created by OaKKuB on 1/8/2016.
 */
public class RoomInfoActivity extends BaseActivity implements RoomAdminAuthenticationFragment.OnRoomAdminAuthenticationListener,
    AlertDialogFragment.OnAlertDialogListener, LeavePublicChatFragment.OnLeavePublicChatListener {

    private static final String EXTRA_ROOM = "extra:room";
    private static final String EXTRA_IS_MEMBER ="extra:isMember";
    private static final String RESULT_EXTRA_ROOM = "resultExtra:room";
    private static final String RESULT_EXTRA_IS_ROOM_EDIT = "resultExtra:isRoomEdit";

    public static final String ACTION_PRIVATE = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_PRIVATE";
    public static final String ACTION_GROUP = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_GROUP";
    public static final String ACTION_PUBLIC = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_PUBLIC";
    private static final String ADMIN_AUTHEN_FRAG_TAG = "tag:adminAuthenticationFragment";
    private static final String LEAVE_ROOM_DIALOG_TAG = "tag:leaveRoomDialog";
    private static final String LEAVE_PUBLIC_FRAG_TAG = "tag:leavePublicFragment";
    private static final String LEAVE_GROUP_FRAG_TAG = "tag:leaveGroupFragment";
    private static final String LEAVE_PRIVATE_FRAG_TAG = "tag:leavePrivateFragment";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialogFragment";

    private static final int EDIT_ROOM_REQUEST_CODE = 0;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.group_info_profile_image)
    MyDraweeView profileImage;

    @Bind(R.id.group_info_text_view)
    TextView roomNameTextView;

    @Bind(R.id.room_info_edit_button)
    Button editInfoButton;

    @Bind(R.id.room_info_admin_button)
    Button adminInfoButton;

    @Bind(R.id.room_info_member_button)
    Button memberInfoButton;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @State
    String action;

    @State
    boolean isAuthenticated;

    @State
    boolean isMember;

    @State
    boolean isSetResult;

    private Room room;
    private LeavePublicChatFragment leavePublicChatFragment;

    public static Intent getStartIntent(Context context, Room room, String action, boolean isMember) {
        Intent intent = new Intent(context, RoomInfoActivity.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_IS_MEMBER, isMember);
        intent.putExtra(EXTRA_ROOM, Parcels.wrap(room));

        return intent;
    }

    public static Intent getResultIntent(Room room, boolean isRoomEdit) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_EXTRA_ROOM, Parcels.wrap(room));
        intent.putExtra(RESULT_EXTRA_IS_ROOM_EDIT, isRoomEdit);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        ButterKnife.bind(this);
        getDataIntent(savedInstanceState);

        setToolbar();
        initInstances();
        addFragments();

        if (savedInstanceState == null) {
            if (action.equals(ACTION_PUBLIC)) {
                editInfoButton.setVisibility(View.GONE);
                adminInfoButton.setVisibility(View.GONE);
                memberInfoButton.setVisibility(View.GONE);
            }

            if (!action.equals(ACTION_PUBLIC)) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getDataIntent(Bundle savedInstanceState) {
        Intent intent = getIntent();

        if (savedInstanceState == null) {
            action = intent.getAction();
            isMember = intent.getBooleanExtra(EXTRA_IS_MEMBER, false);
        }
        room = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ROOM));
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.info));
        }
    }

    private void initInstances() {
        int memberVisibility = action.equals(ACTION_PUBLIC) || action.equals(ACTION_GROUP) ?
                View.VISIBLE : View.GONE;
        int publicChatVisibility = action.equals(ACTION_PUBLIC) ?
                View.VISIBLE : View.GONE;

        memberInfoButton.setVisibility(memberVisibility);
        adminInfoButton.setVisibility(publicChatVisibility);
        roomNameTextView.setText(room.getName());
        profileImage.setMatchedSizeImageURI(Uri.parse(room.getImagePath()));
    }

    private void addFragments() {
        if (action.equals(ACTION_PUBLIC)) {
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        RoomAdminAuthenticationFragment.newInstance(uid, room.getRoomId()),
                        ADMIN_AUTHEN_FRAG_TAG);
        }

        leavePublicChatFragment = (LeavePublicChatFragment) findOrAddFragmentByTag(getSupportFragmentManager(),
                LeavePublicChatFragment.newInstance(uid, room.getRoomId()),
                LEAVE_PUBLIC_FRAG_TAG);
    }

    private void leaveChat() {
        if (!isMember) {
            MyToast.make(getString(R.string.not_member_of_this_chat)).show();
            return;
        }
        showProgressDialog();
        leavePublicChatFragment.leave();
    }

    private void startMainIntent() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        isSetResult = true;
        onRoomEditResult(requestCode, data);
    }

    private void onRoomEditResult(int requestCode, Intent data) {
        if (requestCode != EDIT_ROOM_REQUEST_CODE) return;

        Room editRoom = Parcels.unwrap(data.getParcelableExtra(RESULT_EXTRA_ROOM));
        boolean isRoomEdit = data.getBooleanExtra(RESULT_EXTRA_IS_ROOM_EDIT, false);
        if (!isRoomEdit) return;

        if (!room.getName().equals(editRoom.getName())) {
            room.setName(editRoom.getName());
            roomNameTextView.setText(editRoom.getName());
        }

        if (room.getDescription() != null && editRoom.getDescription() != null) {
            if (!room.getDescription().equals(editRoom.getDescription())) {
                room.setDescription(editRoom.getDescription());
            }
        }

        if (editRoom.getDescription() == null) {
            room.setDescription(null);
        }

        if (!room.getImagePath().equals(editRoom.getImagePath())) {
            room.setImagePath(editRoom.getImagePath());
            profileImage.setMatchedSizeImageURI(Uri.parse(editRoom.getImagePath()));
        }
    }

    @Override
    public void finish() {
        setResult(isSetResult ? RESULT_OK : RESULT_CANCELED, ChatRoomActivity.getResultIntent(room));
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_leave_chat:

                AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                        getString(R.string.leave_chat), getString(R.string.leave_chat_message),
                        getString(R.string.leave_chat), "");
                dialog.show(getSupportFragmentManager(), LEAVE_ROOM_DIALOG_TAG);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room_info, menu);

        if (action.equals(ACTION_PRIVATE) || !isMember) {
            menu.removeItem(R.id.action_leave_chat);
        }

        return true;
    }

    @OnClick(R.id.room_info_edit_button)
    public void onEditButtonClick() {
        if (action.equals(ACTION_PUBLIC) && !isAuthenticated) return;
        if (action.equals(ACTION_PRIVATE)) return;

        Intent editRoomIntent = RoomEditActivity.getStartIntent(this, uid, room);
        startActivityForResult(editRoomIntent, EDIT_ROOM_REQUEST_CODE);
    }

    @OnClick(R.id.room_info_member_button)
    public void onMemberButtonClick() {
        if (action.equals(ACTION_PRIVATE)) return;

        Intent groupMemberIntent = RoomMemberActivity.getGroupIntent(this, room.getRoomId(), isMember, isAuthenticated);
        startActivity(groupMemberIntent);
    }

    @OnClick(R.id.room_info_admin_button)
    public void onAdminMemberButtonClick() {
        if (action.equals(ACTION_GROUP) || action.equals(ACTION_PRIVATE)) return;

        Intent adminMemberIntent = RoomMemberActivity.getPublicIntent(this,
                room.getRoomId(), isAuthenticated);
        startActivity(adminMemberIntent);
    }

    @Override
    public void onAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
        progressBar.setVisibility(View.GONE);

        editInfoButton.setVisibility(isAuthenticated || action.equals(ACTION_GROUP) ? View.VISIBLE : View.GONE);
        memberInfoButton.setVisibility(View.VISIBLE);
        adminInfoButton.setVisibility(action.equals(ACTION_PUBLIC) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(LEAVE_ROOM_DIALOG_TAG) && which == DialogInterface.BUTTON_POSITIVE) {
            leaveChat();
        }
    }

    @Override
    public void onPublicLeaveSuccess() {
        hideProgressDialog();
        MyToast.make(getString(R.string.successfullly_leave_n, room.getName())).show();
        startMainIntent();
    }

    @Override
    public void onPublicLeaveFailed() {
        hideProgressDialog();
        MyToast.make(getString(R.string.error_leaving_chat)).show();
    }
}
