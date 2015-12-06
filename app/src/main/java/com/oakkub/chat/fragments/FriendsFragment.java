package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class FriendsFragment extends BaseFragment
        implements FriendListAdapter.OnClickListener {

    private static final String FRIEND_LIST_STATE = "state:friendList";
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.friendsRecyclerView)
    RecyclerView friendsList;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseFriends;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase firebaseUserFriends;

    @State
    boolean fetchingFriends;

    @State
    ArrayList<String> friendKeyList;

    private FriendListAdapter friendListAdapter;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);

        if (savedInstanceState == null) {
            friendKeyList = new ArrayList<>();
        }
        setFriendListAdapter();
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

    public void getUserFriends(String myId) {
        firebaseUserFriends.child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        getFriendsKey(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    private void getFriendsKey(DataSnapshot dataSnapshot) {
        for (DataSnapshot childrenDataSnapshot : dataSnapshot.getChildren()) {
            final String friendKey = childrenDataSnapshot.getKey();

            if (!friendKeyList.contains(friendKey)) {
                friendKeyList.add(friendKey);
                getFriends(friendKey);
            }
        }
    }

    private void getFriends(String friendKey) {
        firebaseFriends.child(friendKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        addFriendToAdapter(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    private void addFriendToAdapter(DataSnapshot dataSnapshot) {
        final String friendKey = dataSnapshot.getKey();

        UserInfo friendUserInfo = getFriendUserInfo(dataSnapshot, friendKey);
        friendListAdapter.addLast(friendUserInfo);
    }

    private UserInfo getFriendUserInfo(DataSnapshot dataSnapshot, String friendKey) {
        UserInfo friendUserInfo = dataSnapshot.getValue(UserInfo.class);

        friendUserInfo.setUserKey(friendKey);
        friendUserInfo.setType(UserInfo.FRIEND);

        return friendUserInfo;
    }

    @Override
    public void onClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
        UserInfo friendInfo = friendListAdapter.getItem(position);

        FriendListAdapter.FriendHolder friendHolder = (FriendListAdapter.FriendHolder) viewHolder;

        FriendDetailActivity.launch((AppCompatActivity) getActivity(),
                friendHolder.friendProfileImage, friendInfo);
    }

}
