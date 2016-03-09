package com.oakkub.chat.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.loaders.SendFriendRequestLoader;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusRemoveFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusSearchResultFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusSearchingFriendRequest;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SimpleInfoButtonListAdapter;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewInfiniteScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * Created by OaKKuB on 2/25/2016.
 */
public class SearchFriendRequestFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, OnAdapterItemClick,
        SearchView.OnQueryTextListener, AlertDialogFragment.OnAlertDialogListener,
        LoaderManager.LoaderCallbacks<List<UserInfo>> {

    private static final int SEARCH_RESULT_PER_PAGE = 20;

    private static final int CODE_MAKE_REQUEST_LOADER = 0;
    private static final int CODE_SEARCH_REQUEST_LOADER = 1;

    private static final String ARGS_QUERY = "args:query";
    private static final String FRIEND_REQUEST_DIALOG_TAG = "tag:friendRequestDialog";
    private static final String FRIEND_REQUEST_STATE = "state:friendRequestAdapter";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView searchResultList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @State
    String query;

    @State
    boolean shouldResetList;

    @State
    int currentSelectedPosition;

    @State
    boolean isSendingFriendRequest;

    @State
    boolean isSearchingFriendRequest;

    private SimpleInfoButtonListAdapter friendRequestAdapter;
    private RecyclerViewInfiniteScrollListener infiniteScrollListener;

    public static SearchFriendRequestFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(ARGS_QUERY, query);

        SearchFriendRequestFragment fragment = new SearchFriendRequestFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataArgs(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void getDataArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Bundle args = getArguments();
        query = args.getString(ARGS_QUERY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.toolbar_swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initInstances();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
//            sendSearchQuery(query);
            getLoaderManager().initLoader(CODE_SEARCH_REQUEST_LOADER, null, this);
        }

        if (isSendingFriendRequest) {
            getLoaderManager().initLoader(CODE_MAKE_REQUEST_LOADER, null, this);
            isSendingFriendRequest = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        friendRequestAdapter.onSaveInstanceState(FRIEND_REQUEST_STATE, outState);
        infiniteScrollListener.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        friendRequestAdapter.onRestoreInstanceState(FRIEND_REQUEST_STATE, savedInstanceState);
        infiniteScrollListener.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.query = query;
        this.shouldResetList = true;

        swipeRefreshLayout.show();
        sendSearchQuery(query);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void initInstances() {
        getBaseActivity().setSupportActionBar(toolbar);
        ActionBar actionBar = getBaseActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.result_n, query));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        friendRequestAdapter = new SimpleInfoButtonListAdapter(this, getString(R.string.add));
        searchResultList.setHasFixedSize(true);
        searchResultList.setAdapter(friendRequestAdapter);
        searchResultList.setLayoutManager(linearLayoutManager);

        infiniteScrollListener = new RecyclerViewInfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                if (isNoMoreData()) return;

                final UserInfo lastUserInfo = friendRequestAdapter.getLastItem();
                if (lastUserInfo != null) {
                    getActivity().getWindow().getDecorView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            friendRequestAdapter.addFooterProgressBar();
                            sendSearchQuery(lastUserInfo.getDisplayName());
                        }
                    });
                }
            }
        };
        searchResultList.addOnScrollListener(infiniteScrollListener);
    }

    private void sendSearchQuery(String query) {
        EventBus.getDefault().post(new EventBusSearchingFriendRequest(query));
    }

    private void clearResultList() {
        if (shouldResetList) {
            friendRequestAdapter.clear();
            shouldResetList = false;

            if (friendRequestAdapter.isEmpty()) {
                infiniteScrollListener.reset();
            }
        }
    }

    private void checkItemList() {
        if (friendRequestAdapter.isEmpty()) {
            alertTextView.setText(getString(R.string.no_friend_to_be_added));
            alertTextView.visible();
        } else {
            alertTextView.gone();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    public void onEvent(EventBusSearchResultFriendRequest eventBusSearchResultFriendRequest) {
        clearResultList();

        if (friendRequestAdapter.getLastItem() == null) {
            friendRequestAdapter.removeLast();
        }

        ArrayList<UserInfo> userInfoList = eventBusSearchResultFriendRequest.userInfoResultList;
        if (userInfoList != null &&
            !userInfoList.isEmpty()) {

            for (int i = 0, size = userInfoList.size(); i < size; i++) {
                UserInfo userInfo = userInfoList.get(i);
                if (!friendRequestAdapter.contains(userInfo)) {
                    friendRequestAdapter.addLast(userInfoList.get(i));
                }
            }
        }
        checkItemList();
    }

    @Override
    public void onRefresh() {
        sendSearchQuery(query);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        currentSelectedPosition = position;

        currentSelectedPosition = position;
        UserInfo friendInfo = friendRequestAdapter.getItem(position);

        AlertDialogFragment dialogFragment = AlertDialogFragment
                .newInstance(getString(R.string.friend_request),
                        getString(R.string.send_friend_request_to_n, friendInfo.getDisplayName()),
                        getString(R.string.send), "");
        dialogFragment.show(getChildFragmentManager(), FRIEND_REQUEST_DIALOG_TAG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(FRIEND_REQUEST_DIALOG_TAG) && which == DialogInterface.BUTTON_POSITIVE) {
            showProgressDialog();
            getLoaderManager().restartLoader(0, null, this);
            isSendingFriendRequest = true;
        }
    }

    @Override
    public Loader<List<UserInfo>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CODE_MAKE_REQUEST_LOADER:
                return new SendFriendRequestLoader(getActivity(),
                        friendRequestAdapter.getItem(currentSelectedPosition));
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<UserInfo>> loader, List<UserInfo> data) {
        hideProgressDialog();
        onSendFriendRequestLoaderFinished(data, ((SendFriendRequestLoader) loader).getResultCode());
    }

    @Override
    public void onLoaderReset(Loader<List<UserInfo>> loader) {}

    private void onSendFriendRequestLoaderFinished(List<UserInfo> data, int code) {
        switch (code) {
            case SendFriendRequestLoader.CODE_SUCCESS:
                friendRequestAdapter.remove(data.get(0));
                checkItemList();

                // post to SendFriendRequestFragment to remove this UserInfo
                EventBus.getDefault().post(new EventBusRemoveFriendRequest(data.get(0)));
                break;
        }
    }

}
