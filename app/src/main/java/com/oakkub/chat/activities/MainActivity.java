package com.oakkub.chat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.FacebookLoginActivityFragment;
import com.oakkub.chat.fragments.FriendsFetchingFragment;
import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.fragments.GroupListFetchingFragment;
import com.oakkub.chat.fragments.PublicListFetchingFragment;
import com.oakkub.chat.fragments.RoomListFetchingFragment;
import com.oakkub.chat.fragments.UserInfoFetchingFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.managers.RefreshListener;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.UserOnlineInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.adapters.MainViewPagerAdapter;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MyMaterialSheetFab;
import com.oakkub.chat.views.widgets.SheetFab;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import org.parceler.Parcels;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class MainActivity extends BaseActivity implements
        ToolbarCommunicator, ViewPager.OnPageChangeListener,
        OnScrolledEventListener, NavigationView.OnNavigationItemSelectedListener,
        UserInfoFetchingFragment.OnUserInfoReceivedListener,
        AlertDialogFragment.OnAlertDialogListener,
        RefreshListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ALERT_LOGOUT_DIALOG_TAG = "tag:alertDialogLogout";
    private static final String USER_INFO_FETCHING_FRAGMENT_TAG = "tag:userInfoFetching";

    public static final String ROOM_LIST_FRAGMENT_TAG = "tag:roomList";
    public static final String FRIEND_LIST_FRAGMENT_TAG = "tag:friendList";
    public static final String GROUP_LIST_FRAGMENT_TAG = "tag:groupList";
    public static final String PUBLIC_LIST_FRAGMENT_TAG = "tag:publicList";

    private static final String MY_INFO_STATE = "state:myInfo";

    public static final String EXTRA_MY_ID = "extra:uid";
    public static final String EXTRA_PROVIDER = "extra:provider";

    private int[] tabIcons = {
            R.drawable.ic_chat_white_24dp,
            R.drawable.ic_person_white_24dp,
            R.drawable.ic_group_white_24dp,
            R.drawable.ic_public_white_24dp
    };

    @Bind(R.id.main_drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.main_navigation_view)
    NavigationView navigationView;

    @Bind(R.id.main_app_bar_layout)
    AppBarLayout appBarLayout;

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_tablayout)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.fab)
    SheetFab fab;

    private SimpleDraweeView headerImage;
    private TextView headerDisplayNameTextView;
    private TextView headerEmailTextView;

    @Inject
    @Named(FirebaseUtil.NAMED_CONNECTION)
    Firebase connectionFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ONLINE_USERS)
    Firebase onlineUserFirebase;

    @State
    String provider;

    private UserInfo myInfo;

    private String[] tabNames;

    private MyMaterialSheetFab<SheetFab> materialSheetFab;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RoomListFetchingFragment roomListFetchingFragment;
    private FriendsFetchingFragment friendsFetchingFragment;
    private GroupListFetchingFragment groupListFetchingFragment;
    private PublicListFetchingFragment publicListFetchingFragment;

    private MainViewPagerAdapter mainViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(this).inject(this);
        getDataFromIntent(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        findNavigationHeaderViews();

        setToolbar();
        setDrawer();
        setupViewPager();
        setFab();

        addFragments();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        materialSheetFab.saveInstanceState(outState);

        if (myInfo != null) {
            outState.putParcelable(MY_INFO_STATE, Parcels.wrap(myInfo));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        materialSheetFab.restoreInstanceState(savedInstanceState);
        myInfo = Parcels.unwrap(savedInstanceState.getParcelable(MY_INFO_STATE));
    }

    private void findNavigationHeaderViews() {
        View headerView = navigationView.getHeaderView(0);
        headerImage = ButterKnife.findById(headerView, R.id.navigation_header_image);
        headerDisplayNameTextView = ButterKnife.findById(headerView, R.id.navigation_header_display_name_textview);
        headerEmailTextView = ButterKnife.findById(headerView, R.id.navigation_header_email_textview);
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
        String displayName = prefs.getString(UserInfoUtil.DISPLAY_NAME, "");
        String email = prefs.getString(UserInfoUtil.EMAIL, "");

        headerDisplayNameTextView.setText(displayName);
        headerEmailTextView.setText(email);
    }

    private void setFab() {

        View sheetView = ButterKnife.findById(this, R.id.fab_sheet);
        View overlay = ButterKnife.findById(this, R.id.overlay_container);
        int sheetColor = ContextCompat.getColor(this, android.R.color.white);
        int fabColor = ContextCompat.getColor(this, R.color.colorAccent);

        materialSheetFab = new MyMaterialSheetFab<>(
                fab, sheetView, overlay, sheetColor, fabColor);
    }

    private void addFragments() {

        roomListFetchingFragment = (RoomListFetchingFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        new RoomListFetchingFragment(), ROOM_LIST_FRAGMENT_TAG);

        friendsFetchingFragment = (FriendsFetchingFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                    FriendsFetchingFragment.newInstance(FriendsFetchingFragment
                            .FROM_NEW_FRIEND), FRIEND_LIST_FRAGMENT_TAG);

        groupListFetchingFragment = (GroupListFetchingFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        new GroupListFetchingFragment(), GROUP_LIST_FRAGMENT_TAG);

        publicListFetchingFragment = (PublicListFetchingFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        new PublicListFetchingFragment(), PUBLIC_LIST_FRAGMENT_TAG);

        findOrAddFragmentByTag(getSupportFragmentManager(),
                new UserInfoFetchingFragment(),
                    USER_INFO_FETCHING_FRAGMENT_TAG);
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Intent intent = getIntent();

        uid = intent.getStringExtra(EXTRA_MY_ID);
        provider = intent.getStringExtra(EXTRA_PROVIDER);

        if (uid == null || provider == null) {
            Intent launchIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            startActivity(launchIntent);
            finish();
        }
    }

    private void setupViewPager() {
        tabNames = getResources().getStringArray(R.array.main_tab_names);

        mainViewPagerAdapter =
                new MainViewPagerAdapter(getSupportFragmentManager(), uid);

        viewPager.setAdapter(mainViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);

        setTabIcons();
        setToolbarTitle(tabNames[viewPager.getCurrentItem()]);
    }

    private void setTabIcons() {
        for (int i = 0, size = tabLayout.getTabCount(); i < size; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setIcon(tabIcons[i]);
            }
        }
    }

    private void hideMaterialSheet() {
        if (materialSheetFab != null) {
            if (materialSheetFab.isSheetVisible()) {
                materialSheetFab.hideSheet();
            }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        hideMaterialSheet();
    }

    private void startActivityWithMyId(Class<?> cls) {
        Intent intent = getMyIdStartIntent(this, uid, cls);
        startActivity(intent);
    }

    @OnClick(R.id.fab_sheet_item_find_public_chat)
    public void sheetItemFindPublicChatClick() {
        startActivityWithMyId(FindPublicChatActivity.class);
    }

    @OnClick(R.id.fab_sheet_item_public_chat)
    public void sheetItemPublicChatClick() {
        startActivityWithMyId(NewPublicChatActivity.class);
    }

    @OnClick(R.id.fab_sheet_item_add_friend)
    public void sheetItemAddFriendClick() {
        startActivityWithMyId(AddFriendActivity.class);
    }

    @OnClick(R.id.fab_sheet_item_new_messages)
    public void sheetItemPublicNewMessagesClick() {
        startActivityWithMyId(NewMessagesActivity.class);
    }

    @Override
    public void onPageSelected(int position) {
        onSelectedFragment(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            if (!materialSheetFab.isSheetVisible()) {
                fab.show();
            }
        }
    }

    @Override
    public void onScrollUp() {
        if (fab != null) {
            fab.show();
        }
    }

    @Override
    public void onScrollDown() {
        if (fab != null) {
            fab.hide();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.drawer_add_friend:
                startActivityWithMyId(AddFriendActivity.class);
                return true;
            case R.id.drawer_find_public_chat:
                startActivityWithMyId(FindPublicChatActivity.class);
                return true;
            case R.id.drawer_new_public_chat:
                startActivityWithMyId(NewPublicChatActivity.class);
                return true;
            case R.id.drawer_new_messages:
                startActivityWithMyId(NewMessagesActivity.class);
                return true;
            case R.id.drawer_profile:
                Intent profileIntent = ProfileActivity.getStartIntent(this, uid, myInfo);
                startActivity(profileIntent);
                return true;
            case R.id.drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.drawer_logout:
                logout();
                return true;
        }

        return false;
    }

    private void logout() {
        AlertDialogFragment alertDialog = AlertDialogFragment
                .newInstance(getString(R.string.settings_logout),
                        getString(R.string.are_you_sure_to_logout),
                        getString(R.string.settings_logout), "");
        alertDialog.show(getSupportFragmentManager(), ALERT_LOGOUT_DIALOG_TAG);
    }

    private void setupFirebaseComponent() {

        connectionFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);

                if (connected) {

                    Firebase currentOnlineUser = onlineUserFirebase.child(uid);

                    UserOnlineInfo userOnlineInfo = new UserOnlineInfo(true, System.currentTimeMillis());

                    currentOnlineUser.onDisconnect().setValue(userOnlineInfo);
                    currentOnlineUser.child(FirebaseUtil.CHILD_ONLINE).setValue(true);
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
    public void onUserInfoReceived(UserInfo userInfo) {
        myInfo = userInfo;

        SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        headerImage.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
        headerDisplayNameTextView.setText(userInfo.getDisplayName());

        String email = userInfo.getEmail();
        headerEmailTextView.setVisibility(email.isEmpty() ? View.GONE : View.VISIBLE);
        headerEmailTextView.setText(email);

        if (!userInfo.getDisplayName().equals(prefs.getString(UserInfoUtil.DISPLAY_NAME, ""))) {
            editor.putString(UserInfoUtil.DISPLAY_NAME, userInfo.getDisplayName());
        }

        if (!userInfo.getEmail().equals(prefs.getString(UserInfoUtil.EMAIL, ""))) {
            editor.putString(UserInfoUtil.EMAIL, userInfo.getEmail());
        }
        editor.apply();
    }

    @Override
    public void setTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else if (viewPager.getCurrentItem() != 0) {
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

            /*case R.id.action_settings:

                return true;

            case R.id.logout_settings:

                performLogout();

                return true;*/

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

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(ALERT_LOGOUT_DIALOG_TAG) && which == DialogInterface.BUTTON_POSITIVE) {
            performLogout();
        }
    }

    private void performLogout() {

        if (FirebaseUtil.isFacebookLogin(provider)) {

            logout(FacebookLoginActivity.class,
                    FacebookLoginActivity.ACTION_LOGOUT,
                    FacebookLoginActivityFragment.RC_FACEBOOK);

        } else if (FirebaseUtil.isGoogleLogin(provider)) {

            logout(GoogleLoginActivity.class,
                    GoogleLoginActivity.ACTION_LOGOUT,
                    GoogleLoginActivity.RC_GOOGLE);

        } else if (FirebaseUtil.isEmailLogin(provider)) {

            firebaseUnAuthenticate();
        }
    }

    @Override
    public void onRefresh(String tag) {
        switch (tag) {
            case ROOM_LIST_FRAGMENT_TAG:
                fetchRoomList();
                break;
            case FRIEND_LIST_FRAGMENT_TAG:
                if (mainViewPagerAdapter.getRegisteredFragment(1) != null) {
                    FriendsFragment  friendsFragment = (FriendsFragment) mainViewPagerAdapter.getRegisteredFragment(1);
                    friendsFragment.loadFriends();
                }
                break;
            case GROUP_LIST_FRAGMENT_TAG:
                fetchGroups();
                break;
            case PUBLIC_LIST_FRAGMENT_TAG:
                fetchPublicChats();
                break;
        }
    }

    private void fetchRoomList() {
        roomListFetchingFragment.fetchRoomList();
    }

    private void fetchUserFriends() {
        friendsFetchingFragment.fetchUserFriends(uid);
    }

    private void fetchGroups() {
        groupListFetchingFragment.fetchGroupList(uid);
    }

    private void fetchPublicChats() {
        publicListFetchingFragment.fetchPublicList(uid);
    }

    private void onSelectedFragment(int position) {
        setToolbarTitle(tabNames[position]);
        appBarLayout.setExpanded(true);

        switch (position) {

            case 0:
                break;

            case 1:
                fetchUserFriends();
                break;

            case 2:
                fetchGroups();
                break;

            case 3:
                fetchPublicChats();
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
        UserOnlineInfo userOnlineInfo = new UserOnlineInfo(false, System.currentTimeMillis());

        onlineUserFirebase.child(uid).onDisconnect().setValue(userOnlineInfo);
        onlineUserFirebase.child(uid)
                .setValue(userOnlineInfo);
        onlineUserFirebase.unauth();
    }

    private void firebaseUnAuthenticate() {
        firebaseLogout();

        SharedPreferences.Editor editor = AppController.getComponent(this).sharedPreferencesEditor();
        editor.remove(PrefsUtil.PREF_UID);
        editor.apply();

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
    }

}
