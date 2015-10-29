package com.oakkub.chat.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddFriendActivityFragment extends Fragment
        implements FriendListAdapter.OnClickListener, ChildEventListener {

    @Bind(R.id.add_friend_recyclerview)
    RecyclerView addFriendList;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseAddFriend;

    private FriendListAdapter addFriendListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(getActivity()).inject(this);

        setRetainInstance(true);
        firebaseAddFriend.orderByChild(FirebaseUtil.CHILD_REGISTERED_DATE).addChildEventListener(this);

        addFriendListAdapter = new FriendListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_friend, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecyclerView();
    }

    private void setRecyclerView() {
        final int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);

        GridAutoFitLayoutManager gridAutoFitLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);

        addFriendList.setHasFixedSize(true);
        addFriendList.setLayoutManager(gridAutoFitLayoutManager);

        addFriendList.setAdapter(addFriendListAdapter);
        addFriendListAdapter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view, UserInfo userInfo, int position) {
        Log.e("Add Friend On Click", userInfo.getDisplayName());
        addFriend(userInfo);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {

        Log.e("onChildAdded", dataSnapshot.getKey());

        if (!dataSnapshot.getKey().equals(firebaseAddFriend.getAuth().getUid())) {

            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
            userInfo.setType(UserInfo.ADD_FRIEND);
            addFriendListAdapter.add(userInfo);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        firebaseAddFriend.removeEventListener(this);
    }

    private void addFriend(UserInfo userInfo) {



    }
}
