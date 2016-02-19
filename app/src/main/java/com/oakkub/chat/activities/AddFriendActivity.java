package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AddFriendFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.AddFriendListAdapter;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.EmptyTextProgressBar;
import com.oakkub.chat.views.widgets.MyToast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class AddFriendActivity extends BaseActivity implements OnAdapterItemClick,
        AlertDialogFragment.OnAlertDialogListener, AddFriendFragment.OnAddFriendListener {

    private static final String TAG = AddFriendActivity.class.getSimpleName();
    private static final String ADD_FRIEND_FRAG_TAG = "tag:addFriendFragment";
    private static final String ADD_FRIEND_DIALOG_TAG = "tag:addFriendDialog;";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";
    private static final String ADDED_FRIEND_LIST_STATE = "state:addFriendList";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.recyclerview)
    RecyclerView addFriendList;

    @Bind(R.id.add_friend_empty_text_progress_bar)
    EmptyTextProgressBar emptyTextProgressBar;

    @State
    String myId;

    @State
    int currentSelectedPosition;

    private AddFriendListAdapter addFriendListAdapter;

    private AddFriendFragment addFriendFragment;
    private ProgressDialogFragment progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);
        getDataIntent();
        setToolbar();
        setRecyclerView();
        addFragments();
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_friend);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
    }

    private void setRecyclerView() {
        int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);
        GridAutoFitLayoutManager gridAutoFitLayoutManager = new GridAutoFitLayoutManager(this, columnWidth);
        DefaultItemAnimator itemAnimator = AppController.getComponent(this).defaultItemAnimator();

        int padding = getResources().getDimensionPixelOffset(R.dimen.spacing_medium);
        addFriendList.setPadding(padding, 0, padding, 0);
        addFriendList.setHasFixedSize(true);
        addFriendList.setLayoutManager(gridAutoFitLayoutManager);
        addFriendList.setItemAnimator(itemAnimator);

        addFriendListAdapter = new AddFriendListAdapter(this);
        addFriendList.setAdapter(addFriendListAdapter);
    }

    private void addFragments() {
        addFriendFragment = (AddFriendFragment) findFragmentByTag(ADD_FRIEND_FRAG_TAG);
        if (addFriendFragment == null) {
            addFriendFragment = (AddFriendFragment) addFragmentByTag(
                    AddFriendFragment.newInstance(myId), ADD_FRIEND_FRAG_TAG);
        }

        progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
    }

    private void dismissProgressDialog() {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        addFriendListAdapter.onSaveInstanceState(ADDED_FRIEND_LIST_STATE, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        addFriendListAdapter.onRestoreInstanceState(ADDED_FRIEND_LIST_STATE, savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        currentSelectedPosition = position;
        UserInfo friendInfo = addFriendListAdapter.getItem(position);

        AlertDialogFragment dialogFragment = AlertDialogFragment
                .newInstance(getString(R.string.add_friend),
                        getString(R.string.dialog_message_add_friend, friendInfo.getDisplayName(),
                        getString(R.string.add_friend), ""));
        dialogFragment.show(getSupportFragmentManager(), ADD_FRIEND_DIALOG_TAG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void onOkClick(String tag) {
        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);

        UserInfo friendInfo = addFriendListAdapter.getItem(currentSelectedPosition);
        addFriendFragment.addFriend(friendInfo);
    }

    @Override
    public void onCancelClick(String tag) {

    }

    @Override
    public void onFriendListAdded(ArrayList<UserInfo> friendList) {
        emptyTextProgressBar.hideProgressBar();
        addFriendListAdapter.addLastAll(friendList);
    }

    @Override
    public void onFriendAddedSuccess(UserInfo friendInfo) {
        dismissProgressDialog();
        MyToast.make(getString(R.string.success_message_you_are_now_friend,
                friendInfo.getDisplayName())).show();
    }

    @Override
    public void onRemovedFriend(UserInfo friendInfo) {
        dismissProgressDialog();
        MyToast.make(getString(R.string.error_message_already_friend,
                friendInfo.getDisplayName())).show();
    }

    @Override
    public void onFriendAddedFailed(UserInfo friendInfo) {
        dismissProgressDialog();
        MyToast.make(getString(R.string.error_message_network)).show();
    }
}
