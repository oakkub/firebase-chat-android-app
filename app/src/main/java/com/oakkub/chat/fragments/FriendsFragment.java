package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.EventBusFriendListInfo;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.SortUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class FriendsFragment extends BaseFragment
        implements FriendListAdapter.OnClickListener {

    private static final String FRIEND_LIST_STATE = "state:friendList";
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView friendsList;

    private FriendListAdapter friendListAdapter;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setFriendListAdapter();

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        friendListAdapter.onSaveInstanceState(FRIEND_LIST_STATE, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        friendListAdapter.onRestoreInstanceState(FRIEND_LIST_STATE, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void setRecyclerView() {
        final int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);

        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);
        DefaultItemAnimator itemAnimator = AppController.getComponent(getActivity()).defaultItemAnimator();

        friendsList.setLayoutManager(gridLayoutManager);
        friendsList.setItemAnimator(itemAnimator);
        friendsList.setHasFixedSize(true);
        friendsList.setAdapter(friendListAdapter);
    }

    private void setFriendListAdapter() {
        friendListAdapter = new FriendListAdapter();
        friendListAdapter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
        UserInfo friendInfo = friendListAdapter.getItem(position);

        FriendListAdapter.FriendHolder friendHolder = (FriendListAdapter.FriendHolder) viewHolder;

        FriendDetailActivity.launch((AppCompatActivity) getActivity(),
                friendHolder.friendProfileImage, friendInfo);
    }

    public void onEvent(EventBusFriendListInfo eventBusFriendListInfo) {
        List<UserInfo> friendListInfo = eventBusFriendListInfo.friendListInfo;
        SortUtil.sortUserInfoAlphabetically(friendListInfo);
        friendListAdapter.addFirstAll(friendListInfo);
    }

}
