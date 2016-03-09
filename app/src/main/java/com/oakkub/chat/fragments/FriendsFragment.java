package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.client.Firebase;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.managers.RefreshListener;
import com.oakkub.chat.managers.loaders.FetchKeyThenUserInfo;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.SortUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class FriendsFragment extends BaseFragment
        implements OnAdapterItemClick, LoaderManager.LoaderCallbacks<List<UserInfo>>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARGS_MY_ID = "args:uid";
    private static final String FRIEND_LIST_STATE = "state:friendList";
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendsFirebase;

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView friendsList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @State
    boolean isFriendsReceived;

    private FriendListAdapter friendListAdapter;
    private OnScrolledEventListener onScrolledEventListener;
    private RefreshListener refreshListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
        refreshListener = (RefreshListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setFriendListAdapter();

//        EventBus.getDefault().register(this);
        userFriendsFirebase.child(uid).keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initInstances();

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            loadFriends();
        }

        if (isFriendsReceived) {
            loadFriends();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onScrolledEventListener = null;
        refreshListener = null;
    }

    @Override
    public void onDestroy() {
//        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh(MainActivity.FRIEND_LIST_FRAGMENT_TAG);
        }
    }

    @Override
    public Loader<List<UserInfo>> onCreateLoader(int id, Bundle args) {
        return new FetchKeyThenUserInfo(getActivity(),
                TextUtil.getPath(FirebaseUtil.KEY_USERS, FirebaseUtil.KEY_USERS_USER_FRIENDS));
    }

    @Override
    public void onLoadFinished(Loader<List<UserInfo>> loader, List<UserInfo> data) {
        onFriendsRecevied(data);
    }

    @Override
    public void onLoaderReset(Loader<List<UserInfo>> loader) {

    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        UserInfo friendInfo = friendListAdapter.getItem(position);

        ImageView profileImage = ButterKnife.findById(itemView, R.id.simpleInfoProfileImageView);

        FriendDetailActivity.launch((AppCompatActivity) getActivity(),
                profileImage, friendInfo);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    public void loadFriends() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);
        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);

        friendsList.setLayoutManager(gridLayoutManager);
        friendsList.setHasFixedSize(true);
        friendsList.addOnScrollListener(new RecyclerViewScrollDirectionListener() {
            @Override
            public void onScrollUp() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollUp();
                }
            }

            @Override
            public void onScrollDown() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollDown();
                }
            }
        });

        friendsList.setAdapter(friendListAdapter);
    }

    private void setFriendListAdapter() {
        friendListAdapter = new FriendListAdapter(this);
    }

    private void onFriendsRecevied(List<UserInfo> userInfoList) {
        swipeRefreshLayout.hide();

        if (userInfoList.isEmpty()) {
            alertTextView.setText(R.string.you_dont_have_any_friends);
            alertTextView.visible();
        } else {
            alertTextView.gone();

            SortUtil.sortUserInfoAlphabetically(userInfoList);
            friendListAdapter.addNotExistLastAll(userInfoList);
            isFriendsReceived = true;
        }
    }

}
