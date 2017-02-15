package com.oakkub.chat.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.IsNodeExistsFirebaseFragment;
import com.oakkub.chat.fragments.PublicChatSearchFragment;
import com.oakkub.chat.managers.icepick_bundler.RoomBundler;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.adapters.PublicChatSearchedResultAdapter;
import com.oakkub.chat.views.adapters.PublicTypeAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;

/**
 * Created by OaKKuB on 2/3/2016.
 */
public class FindPublicChatActivity extends BaseActivity implements
    PublicChatSearchFragment.OnPublicRoomSearchResultListener,
    SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener,
    IsNodeExistsFirebaseFragment.OnNodeReceivedListener {

    private static final String TAG = FindPublicChatActivity.class.getSimpleName();
    private static final String FIND_PUBLIC_CHAT_TAG = "tag:findPublicChat";
    private static final String PUBLIC_CHAT_LIST_STATE = "state:publicChatList";
    private static final String PUBLIC_TYPE_LIST_STATE = "state:publicType";
    private static final String IS_NODE_EXISTS_TAG = "tag:isNodeExits";
    private static final String STATE_SELECTED_ROOM = "state:selectedRoom";

    @BindView(R.id.find_public_drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.content_find_public_chat)
    CoordinatorLayout contentView;

    @BindView(R.id.find_public_navigation_view)
    RecyclerView publicTypeList;

    @BindView(R.id.find_public_swipe_refresh)
    MySwipeRefreshLayout swipeRefresh;

    @BindView(R.id.recyclerview)
    RecyclerView publicChatList;

    @BindView(R.id.find_public_empty_text_progress_bar)
    MyTextView emptyTextTextView;

    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;

    private SearchView searchView;

    @State
    String[] roomTypeValues;

    @State
    String[] roomTypeKeys;

    @State
    String selectedRoomTypeKey;

    @State
    String selectedRoomTypeValue;

    @State
    String searchViewQuery;

    @State
    boolean isSearching;

    @State
    int selectedPosition;

    @State
    boolean isSearchByQuery;

    @State
    boolean isSearchByTag;

    @State(RoomBundler.class)
    Room selectedRoom;

    private LinearLayoutManager publicChatLayoutManager;

    PublicTypeAdapter publicTypeAdapter;
    PublicChatSearchedResultAdapter publicChatAdapter;

    IsNodeExistsFirebaseFragment isNodeExistsFirebaseFragment;
    private PublicChatSearchFragment publicChatSearchFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_public_chat_layout);
        ButterKnife.bind(this);
        initInstances(savedInstanceState);

        if (savedInstanceState == null) {
            emptyTextTextView.gone();
        }
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

        if (savedInstanceState == null) {
            swipeRefresh.show();
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.find_public_chat));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setDrawerLayout(Bundle savedInstanceState) {
        Resources res = getResources();
        roomTypeKeys = res.getStringArray(R.array.public_chat_tag_keys);
        roomTypeValues = res.getStringArray(R.array.public_chat_tag_values);

        publicTypeAdapter = new PublicTypeAdapter(onTypeAdapterItemClick);
        String all = getString(R.string.all);
        publicTypeAdapter.addLast(all);
        for (String item : roomTypeKeys) {
            publicTypeAdapter.addLast(item);
        }
        publicTypeList.setHasFixedSize(true);
        publicTypeList.setLayoutManager(new LinearLayoutManager(this));
        publicTypeList.setAdapter(publicTypeAdapter);

        if (savedInstanceState == null) {
            publicTypeAdapter.toggleSelection(0, all.hashCode());
        }
    }

    private void setRecyclerView() {
        publicChatAdapter = new PublicChatSearchedResultAdapter(onChatAdapterItemClick);

        publicChatLayoutManager = new LinearLayoutManager(this);

        publicChatList.setHasFixedSize(true);
        publicChatList.setLayoutManager(publicChatLayoutManager);
        publicChatList.setAdapter(publicChatAdapter);
    }

    private void addFragments() {
        publicChatSearchFragment = (PublicChatSearchFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        new PublicChatSearchFragment(),
                        FIND_PUBLIC_CHAT_TAG);

        isNodeExistsFirebaseFragment = (IsNodeExistsFirebaseFragment)
                findOrAddFragmentByTag(getSupportFragmentManager(),
                        IsNodeExistsFirebaseFragment.newInstance(),
                        IS_NODE_EXISTS_TAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        publicTypeAdapter.onSaveInstanceState(PUBLIC_TYPE_LIST_STATE, outState);
        publicChatAdapter.onSaveInstanceState(PUBLIC_CHAT_LIST_STATE, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;

        publicTypeAdapter.onRestoreInstanceState(PUBLIC_TYPE_LIST_STATE, savedInstanceState);
        publicChatAdapter.onRestoreInstanceState(PUBLIC_CHAT_LIST_STATE, savedInstanceState);
    }

    @Override
    public void onRefresh() {
        // for swipe refresh layout
        searchByTag();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        swipeRefresh.setRefreshing(true);
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
    }

    boolean searchByTag() {
        isSearchByQuery = false;

        // if tag:all fetch all
        if (selectedRoomTypeKey == null || selectedRoomTypeKey.equals(getString(R.string.all))) {
            prepareSearching();
            publicChatSearchFragment.fetchAll();
            return true;
        }

        prepareSearching();
        publicChatSearchFragment.search(RoomUtil.KEY_TAG, selectedRoomTypeValue);

        return false;
    }

    private boolean searchByQuery(String query) {
        isSearchByQuery = true;

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
        hideProgressDialog();

        Intent roomInfoIntent = ChatRoomActivity.getIntentPublicRoom(this, selectedRoom, isMember);
        startActivity(roomInfoIntent);
    }

    @Override
    public void onPublicRoomReceived(Room room) {
        if (isSearching) {
            isSearching = false;
        }

        swipeRefresh.hide();
        emptyTextTextView.gone();

        if (isSearchByQuery || isSearchByTag) {
            publicChatAdapter.clear();

            isSearchByQuery = false;
            isSearchByTag = false;
        }

        if (!publicChatAdapter.contains(room)) {
            publicChatAdapter.addFirst(room);

            if (publicChatLayoutManager.findFirstVisibleItemPosition() <= 1) {
                publicChatList.scrollToPosition(0);
            }
        }
    }

    @Override
    public void onNoResult() {
        swipeRefresh.hide();
        publicChatAdapter.clear();

        emptyTextTextView.setText(R.string.no_result);
        emptyTextTextView.visible();
    }

    @Override
    public void nodeExists(String key) {
        intentToRoomInfo(true);
    }

    @Override
    public void nodeNotExist() {
        intentToRoomInfo(false);
    }

    private OnAdapterItemClick onTypeAdapterItemClick = new OnAdapterItemClick() {
        @Override
        public void onAdapterClick(View itemView, int position) {
            selectedRoomTypeKey = publicTypeAdapter.getItem(position);
            isSearchByTag = true;

            if (position > 0) {
                selectedRoomTypeValue = roomTypeValues[position - 1];
            }

            publicTypeAdapter.toggleSelection(position, selectedRoomTypeKey.hashCode());

            drawerLayout.closeDrawer(GravityCompat.END);

            emptyTextTextView.gone();
            searchByTag();
            swipeRefresh.show();
        }

        @Override
        public boolean onAdapterLongClick(View itemView, int position) {
            return false;
        }
    };

    private OnAdapterItemClick onChatAdapterItemClick = new OnAdapterItemClick() {
        @Override
        public void onAdapterClick(View itemView, int position) {
            selectedRoom = publicChatAdapter.getItem(position);
            isNodeExistsFirebaseFragment.fetchNode(TextUtil.getPath(FirebaseUtil.KEY_ROOMS,
                    FirebaseUtil.KEY_ROOMS_MEMBERS, selectedRoom.getRoomId(), uid));

            showProgressDialog();
        }

        @Override
        public boolean onAdapterLongClick(View itemView, int position) {
            return false;
        }
    };
}
