package com.oakkub.chat.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.FacebookLoginActivityFragment;
import com.oakkub.chat.fragments.FriendsFetchingFragment;
import com.oakkub.chat.fragments.RoomListFetchingFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.MainViewPagerAdapter;
import com.oakkub.chat.views.widgets.Fab;
import com.oakkub.chat.views.widgets.spinner.SpinnerInteractionListener;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;
import com.oakkub.chat.views.widgets.viewpager.ViewPager;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class MainActivity extends BaseActivity implements
        ToolbarCommunicator, ViewPager.OnPageChangeListener, SpinnerInteractionListener.OnSpinnerClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ROOM_LIST_FRAGMENT_TAG = "tag:roomList";
    private static final String FRIEND_LIST_FRAGMENT_TAG = "tag:friendList";

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_tablayout)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.fab)
    Fab fab;

    @Inject
    @Named(FirebaseUtil.NAMED_CONNECTION)
    Firebase connectionFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ONLINE_USERS)
    Firebase onlineUserFirebase;

    @State
    String myId;

    @State
    String provider;

    private SpinnerInteractionListener spinnerInteractionListener;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private FriendsFetchingFragment friendsFetchingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(this).inject(this);
        checkAuthentication(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setToolbar();
        setupViewPager();

        if (savedInstanceState == null) {
            setupFirebaseComponent();
        }

        addFragments();
    }

    private void addFragments() {
        RoomListFetchingFragment roomListFetchingFragment = (RoomListFetchingFragment) findFragmentByTag(ROOM_LIST_FRAGMENT_TAG);
        if (roomListFetchingFragment == null) {
            roomListFetchingFragment = (RoomListFetchingFragment)
                    addFragmentByTag(RoomListFetchingFragment.newInstance(myId), ROOM_LIST_FRAGMENT_TAG);
        }

        friendsFetchingFragment = (FriendsFetchingFragment) findFragmentByTag(FRIEND_LIST_FRAGMENT_TAG);
        if (friendsFetchingFragment == null) {
            friendsFetchingFragment = (FriendsFetchingFragment)
                    addFragmentByTag(FriendsFetchingFragment.newInstance(FriendsFetchingFragment.FROM_NEW_FRIEND), FRIEND_LIST_FRAGMENT_TAG);
        }
    }

    private void checkAuthentication(Bundle savedInstanceState) {
        AuthData authData = connectionFirebase.getAuth();

        if (authData == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        } else {
            initValues(savedInstanceState, authData);
        }
    }

    private void initValues(Bundle savedInstanceState, AuthData authData) {
        if (savedInstanceState == null) {
            myId = authData.getUid();
            provider = authData.getProvider();
        }
    }

    private void setupViewPager() {
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), myId);

        viewPager.setAdapter(mainViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onPageSelected(int position) {
        onSelectedFragment(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        fab.setClickable(positionOffsetPixels == 0 && fab.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            int position = viewPager.getCurrentItem();

            animateFabWithScaleAnimation(position);
            onPageSelectedFabAnimation(position);
            fab.setClickable(fab.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onSpinnerItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e(TAG, "onSpinnerItemClick: " + position);
    }

    private void onPageSelectedFabAnimation(int position) {
        // change fab image, according to the selected position of viewpager
        if (position < mainViewPagerAdapter.getCount() - 1) {

            int[] fabDrawables = {
                    R.drawable.ic_sms_24dp,
                    R.drawable.ic_person_add_24dp
            };

            if (viewPager.getPreviousPosition() < fabDrawables.length) {
                Drawable[] layers = new Drawable[] {
                        fab.getDrawable(),
                        ContextCompat.getDrawable(this, fabDrawables[position])
                };

                TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                transitionDrawable.setCrossFadeEnabled(true);
                fab.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(200);
            } else {

                fab.setImageDrawable(ContextCompat.getDrawable(this, fabDrawables[position]));
            }
        }
    }

    private void animateFabWithScaleAnimation(int position) {
        final int viewPagerCount = mainViewPagerAdapter.getCount();

        if (fab.getVisibility() == View.GONE && position < viewPagerCount - 1) fab.scaleUp();
        else if (position == viewPagerCount - 1) fab.scaleDown();
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        switch (viewPager.getCurrentItem()) {

            case 0:

                break;
            case 1:

                Intent addFriendIntent = new Intent(this, AddFriendActivity.class);
                startActivity(addFriendIntent);

                break;

        }
    }

    @OnClick(R.id.group_contact_fab)
    void onGroupContactFab() {

        Intent groupContactIntent = new Intent(this, GroupContactActivity.class);
        groupContactIntent.putExtra(GroupContactActivity.EXTRA_MY_ID, myId);

        startActivity(groupContactIntent);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupFirebaseComponent() {

        connectionFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);

                if (connected) {

                    Firebase currentOnlineUser = onlineUserFirebase.child(myId);

                    currentOnlineUser.child(FirebaseUtil.CHILD_ONLINE).onDisconnect().setValue(Boolean.FALSE);
                    currentOnlineUser.child(FirebaseUtil.CHILD_ONLINE).setValue(Boolean.TRUE);

                    currentOnlineUser.child(FirebaseUtil.CHILD_LAST_ONLINE)
                            .onDisconnect()
                            .setValue(System.currentTimeMillis());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Code: " + firebaseError.getCode());
                Log.e(TAG, firebaseError.getMessage());
            }
        });
    }

    @Override
    public void setTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_settings:

                return true;

            case R.id.logout_settings:

                performLogout();

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case FacebookLoginActivityFragment.RC_FACEBOOK:
            case GoogleLoginActivity.RC_GOOGLE:

                firebaseUnAuthenticate();

                break;

        }

    }

    private void performLogout() {

        if (FirebaseUtil.isFacebookLogin(provider)) {

            logout(FacebookLoginActivity.class,
                    FacebookLoginActivity.LOGOUT_ACTION,
                    FacebookLoginActivityFragment.RC_FACEBOOK);

        } else if (FirebaseUtil.isGoogleLogin(provider)) {

            logout(GoogleLoginActivity.class,
                    GoogleLoginActivity.ACTION_LOGOUT,
                    GoogleLoginActivity.RC_GOOGLE);

        } else if (FirebaseUtil.isEmailLogin(provider)) {

            firebaseUnAuthenticate();
        }
    }

    private void onSelectedFragment(int position) {
        switch (position) {

            case 0:
                break;

            case 1:
                friendsFetchingFragment.fetchUserFriends(myId);
                break;

            case 2:
                break;

        }
    }

    private void logout(Class<?> cls, String action, int requestCode) {

        Intent intent = new Intent(this, cls);
        intent.setAction(action);

        startActivityForResult(intent, requestCode);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void firebaseLogout() {

        onlineUserFirebase.child(myId)
                .child(FirebaseUtil.CHILD_ONLINE)
                .setValue(Boolean.FALSE);
        onlineUserFirebase.child(myId)
                .child(FirebaseUtil.CHILD_LAST_ONLINE)
                .setValue(System.currentTimeMillis());

        onlineUserFirebase.unauth();
    }

    private void firebaseUnAuthenticate() {

        firebaseLogout();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

}
