package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.FriendListAdapter;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment implements FriendListAdapter.OnClickListener {

    @Bind(R.id.friendsRecyclerView)
    RecyclerView friendsList;

    FriendListAdapter friendListAdapter;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(getActivity()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecyclerView();
        setFriendListAdapter();
    }

    private void setRecyclerView() {

        GridAutoFitLayoutManager gridLayoutManager =
                new GridAutoFitLayoutManager(getActivity(), GridLayoutManager.DEFAULT_SPAN_COUNT);
        friendsList.setLayoutManager(gridLayoutManager);
    }

    private void setFriendListAdapter() {
        friendListAdapter = new FriendListAdapter();
        friendListAdapter.setOnClickListener(this);
        friendsList.setAdapter(friendListAdapter);
    }

    @Override
    public void onClick(View view, UserInfo userInfo, int position) {



    }
}
