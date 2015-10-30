package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.services.GCMNotificationService;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.adapters.FriendListAdapter;
import com.oakkub.chat.views.dialogs.BottomSheetDialog;

import org.magicwerk.brownies.collections.GapList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddFriendActivityFragment extends Fragment
        implements FriendListAdapter.OnClickListener,
        BottomSheetDialog.OnClickListener,
        Firebase.CompletionListener {

    private static final String TAG = AddFriendActivityFragment.class.getSimpleName();

    @Bind(R.id.add_friend_recyclerview)
    RecyclerView addFriendList;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseAddFriend;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase firebaseUserFriends;

    private AuthData authData;

    private FriendListAdapter addFriendListAdapter;
    private GapList<String> friendKeyList;

    private UserInfo myUserInfo;
    private int selectedFriendPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(getActivity()).inject(this);
        authData = firebaseUserFriends.getAuth();

        setRetainInstance(true);

        addFriendListAdapter = new FriendListAdapter();
        friendKeyList = new GapList<>();

        getUserFriend();
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
        DefaultItemAnimator itemAnimator = AppController.getComponent(getActivity()).defaultItemAnimator();

        addFriendList.setHasFixedSize(true);
        addFriendList.setLayoutManager(gridAutoFitLayoutManager);
        addFriendList.setItemAnimator(itemAnimator);

        addFriendList.setAdapter(addFriendListAdapter);
        addFriendListAdapter.setOnClickListener(this);
    }

    private void getUserFriend() {
        firebaseUserFriends.child(authData.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot childrenDataSnapshot : dataSnapshot.getChildren()) {
                            friendKeyList.add(childrenDataSnapshot.getKey());
                        }

                        getRecommendedFriend();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void getRecommendedFriend() {
        firebaseAddFriend
                .orderByChild(FirebaseUtil.CHILD_REGISTERED_DATE)
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        getRecommendedFriendList(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void getRecommendedFriendList(DataSnapshot dataSnapshot) {
        final String uid = authData.getUid();

        // -1 because we don't need to store out info (owner id).
        GapList<UserInfo> recommendedFriendList =
                new GapList<>((int) dataSnapshot.getChildrenCount() - 1);

        for (DataSnapshot childrenSnapshot : dataSnapshot.getChildren()) {
            final String friendKey = childrenSnapshot.getKey();

            if (!friendKey.equals(uid) && !isAlreadyFriend(friendKey)) {

                UserInfo friendUserInfo = getUserInfo(childrenSnapshot, friendKey);
                recommendedFriendList.add(friendUserInfo);
            } else {

                myUserInfo = getUserInfo(childrenSnapshot, friendKey);
            }
        }

        reverseFriendData(recommendedFriendList);
    }

    private void reverseFriendData(GapList<UserInfo> recommendedFriendList) {
        if (recommendedFriendList.size() > 0) {
            Collections.reverse(recommendedFriendList);
            addFriendListAdapter.addAll(recommendedFriendList);
        }
    }

    private boolean isAlreadyFriend(String friendKey) {
        return friendKeyList.contains(friendKey);
    }

    private UserInfo getUserInfo(DataSnapshot dataSnapshot, String friendKey) {
        UserInfo friendUserInfo = dataSnapshot.getValue(UserInfo.class);

        friendUserInfo.setUserKey(friendKey);
        friendUserInfo.setType(UserInfo.ADD_FRIEND);

        return friendUserInfo;
    }

    @Override
    public void onClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
        selectedFriendPosition = position;

        showAddFriendDialog(addFriendListAdapter.getItem(position));
    }

    /*@Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        final String friendKey = dataSnapshot.getKey();

        Log.e(TAG, friendKey);

        if (!friendKey.equals(authData.getUid()) && !isAlreadyFriend(friendKey)) {

            UserInfo friendUserInfo = getUserInfo(dataSnapshot, friendKey);
            addFriendListAdapter.add(friendUserInfo);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        final String friendKey = dataSnapshot.getKey();

        UserInfo friendUserInfo = getUserInfo(dataSnapshot, friendKey);
        addFriendListAdapter.replace(friendUserInfo);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.e(TAG, firebaseError.getMessage());
    }
*/
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onOkClick(View view) {
        Log.e("DIALOG", "ok");

        UserInfo friendUserInfo = addFriendListAdapter.getItem(selectedFriendPosition);
        checkIfFriendExisted(friendUserInfo);
    }

    @Override
    public void onCancelClick(View view) {
        Log.e("DIALOG", "cancel");
    }

    private void showAddFriendDialog(UserInfo userInfo) {
        BottomSheetDialog bottomSheetDialog =
                BottomSheetDialog.newInstance(
                        getString(R.string.dialog_message_add_friend, userInfo.getDisplayName()),
                        getString(R.string.add_friend),
                        R.drawable.ic_person_add_24dp, this);

        bottomSheetDialog.show(getActivity().getSupportFragmentManager(), BottomSheetDialog.TAG);
    }

    private void checkIfFriendExisted(final UserInfo friendUserInfo) {

        firebaseUserFriends.child(authData.getUid())
                .child(friendUserInfo.getUserKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        prepareToAddFriend(dataSnapshot, friendUserInfo);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void prepareToAddFriend(DataSnapshot dataSnapshot, UserInfo friendUserInfo) {

        if (dataSnapshot.getValue() != null) {

            addFriendListAdapter.remove(friendUserInfo);
            Util.showSnackBar(getView(),
                    getString(R.string.error_message_already_friend,
                            friendUserInfo.getDisplayName()));
        } else {

            postFriendDataToServer(friendUserInfo, false, true);
        }

    }

    private void postFriendDataToServer(UserInfo friendUserInfo, boolean removeFriend, boolean hasListener) {
        // removeFriend flag use for check if we gonna add friend or remove friend from server
        final Map<String, Object> friendKey = getFriendKey(friendUserInfo, removeFriend);
        firebaseUserFriends.updateChildren(friendKey, hasListener ? this : null);
    }

    private Map<String, Object> getFriendKey(UserInfo friendUserInfo, boolean removeFriend) {
        final String myKey = myUserInfo.getUserKey();
        final String friendKey = friendUserInfo.getUserKey();

        Map<String, Object> addFriendMap = new HashMap<>(2);
        addFriendMap.put(TextUtil.getPath(myKey, friendKey), removeFriend ? null : true);
        addFriendMap.put(TextUtil.getPath(friendKey, myKey), removeFriend ? null : true);

        return addFriendMap;
    }

    @Override
    public void onComplete(FirebaseError firebaseError, Firebase firebase) {

        if (firebaseError != null) {
            Util.showSnackBar(getView(), getString(R.string.error_message_network));
            return;
        }

        if (selectedFriendPosition >= 0) {

            UserInfo friendUserInfo = addFriendListAdapter.getItem(selectedFriendPosition);
            addFriendSuccess(addFriendListAdapter.remove(friendUserInfo));
            sendAddFriendNotification();
        }
    }

    private void addFriendSuccess(final UserInfo friendUserInfo) {

        Snackbar.make(getView(), getString(R.string.success_message_you_are_now_friend,
                friendUserInfo.getDisplayName()), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postFriendDataToServer(friendUserInfo, true, false);
                        addFriendListAdapter.add(selectedFriendPosition, friendUserInfo);
                    }
                }).show();
    }

    private void sendAddFriendNotification() {

        Intent notificationService = new Intent(getActivity(), GCMNotificationService.class);
        notificationService.putExtra(GCMNotificationService.TITLE,
                getString(R.string.new_friend));
        notificationService.putExtra(GCMNotificationService.MESSAGE,
                getString(R.string.notification_message_you_are_added_as_friend, myUserInfo.getDisplayName()));
        notificationService.putExtra(GCMNotificationService.PROFILE_URL, myUserInfo.getProfileImageURL());

        getActivity().startService(notificationService);
    }

}
