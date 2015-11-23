package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.AuthData;
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

import org.magicwerk.brownies.collections.GapList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment
        implements FriendListAdapter.OnClickListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.friendsRecyclerView)
    RecyclerView friendsList;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseFriends;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase firebaseUserFriends;

    private AuthData authData;

    private GapList<String> friendKeyList;
    private FriendListAdapter friendListAdapter;

    private boolean gettingFriendList;

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

        setRetainInstance(true);

        friendKeyList = new GapList<>();

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        authData = firebaseUserFriends.getAuth();
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

    public void getUserFriends() {
        if (gettingFriendList) return;
        else gettingFriendList = true;

        firebaseUserFriends.child(authData.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        getFriendsKey(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        gettingFriendList = false;
                    }
                });
    }

    private void getFriendsKey(DataSnapshot dataSnapshot) {
        for (DataSnapshot childrenDataSnapshot : dataSnapshot.getChildren()) {
            final String friendKey = childrenDataSnapshot.getKey();

            friendKeyList.add(friendKey);
            getFriends(friendKey);
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
