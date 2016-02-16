package com.oakkub.chat.activities;

import android.content.Context;
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

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.LeavePublicChatFragment;
import com.oakkub.chat.fragments.RoomAdminAuthenticationFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
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

    public static final String ACTION_PRIVATE = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_PRIVATE";
    public static final String ACTION_GROUP = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_GROUP";
    public static final String ACTION_PUBLIC = "com.oakkub.chat.activities.RoomInfoActivity.ACTION_PUBLIC";
    private static final String ADMIN_AUTHEN_FRAG_TAG = "tag:adminAuthenticationFragment";
    private static final String LEAVE_ROOM_DIALOG_TAG = "tag:leaveRoomDialog";
    private static final String LEAVE_PUBLIC_FRAG_TAG = "tag:leavePublicFragment";
    private static final String LEAVE_GROUP_FRAG_TAG = "tag:leaveGroupFragment";
    private static final String LEAVE_PRIVATE_FRAG_TAG = "tag:leavePrivateFragment";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialogFragment";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.group_info_profile_image)
    SimpleDraweeView profileImage;

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
    String myId;

    @State
    String action;

    @State
    boolean isAuthenticated;

    @State
    boolean isMember;

    private Room room;

    private RoomAdminAuthenticationFragment authenticationFragment;
    private LeavePublicChatFragment leavePublicChatFragment;
    private ProgressDialogFragment progressDialog;

    public static Intent getStartIntent(Context context, Room room, String myId, String action, boolean isMember) {
        Intent intent = new Intent(context, RoomInfoActivity.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_MY_ID, myId);
        intent.putExtra(EXTRA_IS_MEMBER, isMember);
        intent.putExtra(EXTRA_ROOM, Parcels.wrap(room));

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        ButterKnife.bind(this);
        getDataIntent(savedInstanceState);

        setToolbar();
        initInstances();
        addFragments();

        if (savedInstanceState == null) {
            if (action.equals(ACTION_PUBLIC)) {
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
            myId = intent.getStringExtra(EXTRA_MY_ID);
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
        int memberVisibility = action.equals(ACTION_PUBLIC) || action.equals(ACTION_GROUP) ? View.VISIBLE : View.GONE;
        int publicChatVisibility = action.equals(ACTION_PUBLIC) ? View.VISIBLE : View.GONE;

        memberInfoButton.setVisibility(memberVisibility);
        adminInfoButton.setVisibility(publicChatVisibility);
        roomNameTextView.setText(room.getName());
        profileImage.setImageURI(Uri.parse(room.getImagePath()));
    }

    private void addFragments() {
        progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }

        if (action.equals(ACTION_PUBLIC)) {
            authenticationFragment = (RoomAdminAuthenticationFragment) findFragmentByTag(ADMIN_AUTHEN_FRAG_TAG);
            if (authenticationFragment == null) {
                authenticationFragment = (RoomAdminAuthenticationFragment) addFragmentByTag(
                        RoomAdminAuthenticationFragment.newInstance(myId, room.getRoomId()), ADMIN_AUTHEN_FRAG_TAG);
            }

        }

        leavePublicChatFragment = (LeavePublicChatFragment) findFragmentByTag(LEAVE_PUBLIC_FRAG_TAG);
        if (leavePublicChatFragment == null) {
            leavePublicChatFragment = (LeavePublicChatFragment) addFragmentByTag(
                    LeavePublicChatFragment.newInstance(myId, room.getRoomId()), LEAVE_PUBLIC_FRAG_TAG);
        }
    }

    private void leaveChat() {
        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        leavePublicChatFragment.leave();
    }

    private void startMainIntent() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

        if (action.equals(ACTION_PRIVATE)) {
            menu.removeItem(R.id.action_leave_chat);
        }

        return true;
    }

    @OnClick(R.id.room_info_member_button)
    public void onMemberButtonClick() {
        if (action.equals(ACTION_PRIVATE)) return;

        Intent groupMemberIntent = RoomMemberActivity.getGroupIntent(this, room.getRoomId(), myId, isMember);
        startActivity(groupMemberIntent);
    }

    @OnClick(R.id.room_info_admin_button)
    public void onAdminMemberButtonClick() {
        if (action.equals(ACTION_GROUP) || action.equals(ACTION_PRIVATE)) return;

        Intent adminMemberIntent = RoomMemberActivity.getPublicIntent(this,
                room.getRoomId(), myId, isAuthenticated);
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
    public void onOkClick(String tag) {
        switch (tag) {
            case LEAVE_ROOM_DIALOG_TAG:
                leaveChat();
                break;
        }
    }

    @Override
    public void onCancelClick(String tag) {
    }

    @Override
    public void onPublicLeaveSuccess() {
        progressDialog.dismiss();
        MyToast.make(getString(R.string.successfullly_leave_n, room.getName())).show();
        startMainIntent();
    }

    @Override
    public void onPublicLeaveFailed() {
        progressDialog.dismiss();
        MyToast.make(getString(R.string.error_leaving_chat)).show();
    }
}
