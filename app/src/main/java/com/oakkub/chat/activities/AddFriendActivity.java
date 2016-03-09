package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AddFriendPagerFragment;
import com.oakkub.chat.fragments.FriendRequestFragment;
import com.oakkub.chat.managers.AppController;

import butterknife.ButterKnife;
import icepick.State;

/**
 * Created by OaKKuB on 2/22/2016.
 */
public class AddFriendActivity extends BaseActivity {

    private static final String TAG = AddFriendActivity.class.getSimpleName();

    private static final String FRIEND_REQUEST_TAB_TAG = "tag:friendRequestTabFragment";
    private static final String FRIEND_REQUEST_FRAG_TAG = "tag:friendRequestFragment";

    public static final String EXTRA_SELECTED_TAG = "extra:selectedTab";

    @State
    String myId;

    @State
    int selectedTab;

    public static Intent getStartIntent(Context context, String myId, int selectedTabPosition) {
        Intent intent = getMyIdStartIntent(context, myId, AddFriendActivity.class);
        intent.putExtra(EXTRA_SELECTED_TAG, selectedTabPosition);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearNotification();
        getDataIntent();
        AppController.getComponent(this).inject(this);
        setContentView(R.layout.empty_container);
        ButterKnife.bind(this);
        initInstances();
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
        selectedTab = intent.getIntExtra(EXTRA_SELECTED_TAG, -1);
    }

    private void initInstances() {
        setStatusBarColor(getCompatColor(R.color.colorPrimaryDark));

        addFragments();
    }

    private void addFragments() {
        findOrAddFragmentByTag(R.id.empty_container,
                AddFriendPagerFragment.newInstance(selectedTab), FRIEND_REQUEST_TAB_TAG);

        findOrAddFragmentByTag(
                getSupportFragmentManager(),
                FriendRequestFragment.newInstance(myId), FRIEND_REQUEST_FRAG_TAG);
    }

}
