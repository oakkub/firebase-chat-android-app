package com.oakkub.chat.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;

public class FriendsFragment extends BaseFragment
        implements OnAdapterItemClick, LoaderManager.LoaderCallbacks<List<UserInfo>>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();
    private static final int FRIEND_DETAIL_REQUEST_CODE = 1;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendsFirebase;

    @BindView(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerview)
    RecyclerView friendsList;

    @BindView(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @State
    boolean isFriendsReceived;

    @State
    int selectedItemPosition;

    private FriendListAdapter friendListAdapter;
    OnScrolledEventListener onScrolledEventListener;
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

        loadFriends();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRemoveFriendResult(requestCode, resultCode);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onScrolledEventListener = null;
        refreshListener = null;
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
        onFriendsReceived(data);
    }

    @Override
    public void onLoaderReset(Loader<List<UserInfo>> loader) {

    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        selectedItemPosition = position;

        UserInfo friendInfo = friendListAdapter.getItem(position);

        /*ImageView profileImage = ButterKnife.findById(itemView, R.id.simpleInfoProfileImageView);

        FriendDetailActivity.launch((AppCompatActivity) getActivity(),
                profileImage, friendInfo, FRIEND_DETAIL_REQUEST_CODE);*/

        Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
        intent.putExtra(FriendDetailActivity.EXTRA_INFO, Parcels.wrap(friendInfo));
        startActivityForResult(intent, FRIEND_DETAIL_REQUEST_CODE);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    private void onRemoveFriendResult(int requestCode, int resultCode) {
        if (requestCode != FRIEND_DETAIL_REQUEST_CODE || resultCode != Activity.RESULT_OK) return;

        friendListAdapter.remove(selectedItemPosition);
    }

    public void loadFriends() {
        getLoaderManager().initLoader(0, null, this);
    }

    public void restartFriends() {
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);
        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);
        gridLayoutManager.setAutoMeasureEnabled(false);

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

    private void onFriendsReceived(List<UserInfo> userInfoList) {
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
