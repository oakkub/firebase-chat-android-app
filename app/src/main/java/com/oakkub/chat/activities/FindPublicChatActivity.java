package com.oakkub.chat.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.IsNodeExistsFirebaseFragment;
import com.oakkub.chat.fragments.PublicChatSearchFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.adapters.PublicChatSearchedResultAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.EmptyTextProgressBar;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * Created by OaKKuB on 2/3/2016.
 */
public class FindPublicChatActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
    PublicChatSearchFragment.OnPublicRoomSearchResultListener, OnAdapterItemClick,
    SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener,
    IsNodeExistsFirebaseFragment.OnNodeReceivedListener {

    private static final String TAG = FindPublicChatActivity.class.getSimpleName();
    private static final String FIND_PUBLIC_CHAT_TAG = "tag:findPublicChat";
    private static final String PUBLIC_CHAT_LIST_STATE = "state:publicChatList";
    private static final String IS_NODE_EXISTS_TAG = "tag:isNodeExits";
    private static final String STATE_SELECTED_ROOM = "state:selectedRoom";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";

    @State
    String myId;

    @Bind(R.id.find_public_drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.content_find_public_chat)
    CoordinatorLayout contentView;

    @Bind(R.id.find_public_navigation_view)
    NavigationView navigationView;

    @Bind(R.id.find_public_swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    @Bind(R.id.recyclerview)
    RecyclerView publicChatList;

    @Bind(R.id.find_public_empty_text_progress_bar)
    EmptyTextProgressBar emptyTextProgressBar;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    private SearchView searchView;

    @State
    String[] roomTypeValues;

    @State
    String[] roomTypeKeys;

    @State
    String selectedRoomType;

    @State
    String searchViewQuery;

    @State
    boolean isSearching;

    @State
    int selectedPosition;

    private Room selectedRoom;

    private ProgressDialogFragment progressDialog;
    private IsNodeExistsFirebaseFragment isNodeExistsFirebaseFragment;
    private PublicChatSearchFragment publicChatSearchFragment;
    private PublicChatSearchedResultAdapter publicChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_public_chat_layout);
        ButterKnife.bind(this);
        getData(savedInstanceState);
        initInstances(savedInstanceState);
    }

    private void getData(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
    }

    private void initInstances(Bundle savedInstanceState) {
        setToolbar();
        setDrawerLayout(savedInstanceState);
        setRecyclerView();
        setSwipeRefresh(savedInstanceState);
        addFragments();
    }

    private void setSwipeRefresh(Bundle savedInstanceState) {
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeColors(getCompatColor(R.color.blue));

        if (savedInstanceState == null) {
            swipeRefresh.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    swipeRefresh.setRefreshing(true);
                    swipeRefresh.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(selectedRoomType == null ? getString(R.string.all) : selectedRoomType);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setDrawerLayout(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Resources res = getResources();
            roomTypeKeys = res.getStringArray(R.array.public_chat_tag_keys);
            roomTypeValues = res.getStringArray(R.array.public_chat_tag_values);
        }

        int groupId = "PublicChatGroup".hashCode();
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();

        String all = getString(R.string.all);
        menu.add(groupId, all.hashCode(), Menu.NONE, all);
        for (String key : roomTypeKeys) {
            menu.add(groupId, key.hashCode(), Menu.NONE, key);
        }
        menu.setGroupCheckable(groupId, true, true);
        navigationView.setCheckedItem(all.hashCode());
    }

    private void setRecyclerView() {
        publicChatAdapter = new PublicChatSearchedResultAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        publicChatList.setHasFixedSize(true);
        publicChatList.setLayoutManager(linearLayoutManager);
        publicChatList.setAdapter(publicChatAdapter);
    }

    private void addFragments() {
        publicChatSearchFragment = (PublicChatSearchFragment) findFragmentByTag(FIND_PUBLIC_CHAT_TAG);
        if (publicChatSearchFragment == null) {
            publicChatSearchFragment = (PublicChatSearchFragment)
                    addFragmentByTag(PublicChatSearchFragment.newInstance(myId),
                    FIND_PUBLIC_CHAT_TAG);
        }

        isNodeExistsFirebaseFragment = (IsNodeExistsFirebaseFragment) findFragmentByTag(IS_NODE_EXISTS_TAG);
        if (isNodeExistsFirebaseFragment == null) {
            isNodeExistsFirebaseFragment = (IsNodeExistsFirebaseFragment)
                    addFragmentByTag(IsNodeExistsFirebaseFragment.newInstance(),
                    IS_NODE_EXISTS_TAG);
        }

        progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
    }

    /*private TextView getCustomSubMenuTextView(String text) {
        int padding = getResources().getDimensionPixelOffset(R.dimen.spacing_medium);

        TextView textView = new TextView(this);
        textView.setFreezesText(true);
        textView.setText(text);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setPadding(padding, padding, padding, padding);
        if (Build.VERSION.SDK_INT >= 16) {
            textView.setBackground(getSelectableItemDrawable());
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);

        return textView;
    }*/

    private Drawable getSelectableItemDrawable() {
        int[] attrs = new int[] {R.attr.selectableItemBackground};

        TypedArray typedArray = obtainStyledAttributes(attrs);
        Drawable selectableDrawable = typedArray.getDrawable(0);
        typedArray.recycle();

        return selectableDrawable;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedRoom != null) {
            outState.putParcelable(STATE_SELECTED_ROOM, Parcels.wrap(selectedRoom));
        }

        publicChatAdapter.onSaveInstanceState(PUBLIC_CHAT_LIST_STATE, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        Parcelable selectedRoomParcelable = savedInstanceState.getParcelable(STATE_SELECTED_ROOM);
        if (selectedRoomParcelable != null) {
            selectedRoom = Parcels.unwrap(selectedRoomParcelable);
        }

        publicChatAdapter.onRestoreInstanceState(PUBLIC_CHAT_LIST_STATE, savedInstanceState);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectedRoomType = String.valueOf(item.getTitle());
        setToolbarTitle(selectedRoomType);
        return searchByTag();
    }

    @Override
    public void onRefresh() {
        // for swipe refresh layout
        searchByTag();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return searchByQuery(query.trim());
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchViewQuery = newText.trim();
        return true;
    }

    private void prepareSearching() {
        isSearching = true;
        publicChatSearchFragment.clear();

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }

        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }
    }

    private boolean searchByTag() {
        // if tag:all fetch all
        if (selectedRoomType == null || selectedRoomType.equals(getString(R.string.all))) {
            prepareSearching();
            publicChatSearchFragment.fetchAll();
            return true;
        }

        // else loop through to find the chosen tag
        for (int i = 0, size = roomTypeValues.length; i < size; i++) {
            if (roomTypeKeys[i].equals(selectedRoomType)) {
                prepareSearching();
                selectedRoomType = roomTypeValues[i];
                publicChatSearchFragment.search(RoomUtil.KEY_TAG, selectedRoomType);
                return true;
            }
        }

        return false;
    }

    private boolean searchByQuery(String query) {
        prepareSearching();
        publicChatSearchFragment.search(RoomUtil.KEY_NAME, query);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_public_chat, menu);

        searchView = (SearchView) menu.findItem(R.id.action_public_chat_search).getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (searchViewQuery != null && !searchViewQuery.isEmpty()) {
            searchView.setQuery(searchViewQuery, false);
            searchView.setIconified(searchViewQuery.isEmpty());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_public_chat_filter:
                showOrCloseDrawer();
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (!searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private void showOrCloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void intentToRoomInfo(boolean isMember) {
        progressDialog.dismiss();

        Intent roomInfoIntent = ChatRoomActivity.getIntentPublicRoom(this, selectedRoom, myId, isMember);
        startActivity(roomInfoIntent);
    }

    @Override
    public void onPublicRoomReceived(Room room) {
        if (isSearching) {
            publicChatAdapter.clear();
            isSearching = false;
        }

        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }

        if (!publicChatAdapter.contains(room)) {
            /*int size = publicChatAdapter.getItemCount();
            Room beforeNewRoom = publicChatAdapter.getLastItem();
            if (beforeNewRoom != null) {
                if (room.getLatestMessageTime() > beforeNewRoom.getLatestMessageTime()) {
                    publicChatAdapter.add(size - 1, room);
                } else {
                    publicChatAdapter.addLast(room);
                }
            } else {
                publicChatAdapter.addLast(room);
            }*/
            publicChatAdapter.addFirst(room);
        }
    }

    @Override
    public void onNoResult() {
        swipeRefresh.setRefreshing(false);


        Snackbar.make(contentView, getString(R.string.error_no_result_for, searchViewQuery),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        selectedRoom = publicChatAdapter.getItem(position);
        isNodeExistsFirebaseFragment.fetchNode(TextUtil.getPath(FirebaseUtil.KEY_ROOMS,
                FirebaseUtil.KEY_ROOMS_MEMBERS, selectedRoom.getRoomId(), myId));

        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void nodeExists(String key) {
        intentToRoomInfo(true);
    }

    @Override
    public void nodeNotExist() {
        intentToRoomInfo(false);
    }
}
