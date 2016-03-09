package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.MessageUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/8/2016.
 */
public class InviteMemberFragment extends BaseFragment {

    public static final String ARGS_ROOM_ID = "args:roomId";
    private static final String TAG = InviteMemberFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Firebase roomMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_LIST)
    Lazy<Firebase> messageFirebase;

    private String roomId;

    private int totalExistedFriends;

    private boolean invitingFailed;
    private boolean invitingSuccess;
    private boolean noInviteFriend;

    private SparseArray<String> existedFriendKeyList;
    private SparseArray<UserInfo> addableFriendInfoList;
    private SparseArray<UserInfo> addableFriendInfoLeaveOutList;

    private OnInviteMemberFetchingListener inviteMemberFetchingListener;

    public static InviteMemberFragment newInstance(String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ROOM_ID, roomId);

        InviteMemberFragment fragment = new InviteMemberFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        inviteMemberFetchingListener = (OnInviteMemberFetchingListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();

        existedFriendKeyList = new SparseArray<>();
        addableFriendInfoList = new SparseArray<>();
        addableFriendInfoLeaveOutList = new SparseArray<>();

        fetchExistedMember();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        inviteMemberFetchingListener = null;
    }

    private void getDataIntent() {
        Bundle args = getArguments();
        roomId = args.getString(ARGS_ROOM_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (totalExistedFriends > 0) {
            inviteMemberFetchingListener.onTotalFriendFetched(totalExistedFriends);
        }

        int addableFriendInfoLeaveOutSize = addableFriendInfoLeaveOutList.size();
        if (addableFriendInfoLeaveOutSize > 0) {
            for (int i = 0; i < addableFriendInfoLeaveOutSize; i++) {
                inviteMemberFetchingListener.onAddableFriendFetched(addableFriendInfoLeaveOutList.valueAt(i));
            }
            addableFriendInfoLeaveOutList.clear();
        }

        if (invitingFailed) {
            inviteMemberFetchingListener.onInvitingFailed();
            invitingFailed = false;
        }

        if (invitingSuccess) {
            inviteMemberFetchingListener.onInvitingSuccess();
            invitingSuccess = false;
        }

        if (noInviteFriend) {
            inviteMemberFetchingListener.onNoInviteMember();
            noInviteFriend = false;
        }
    }

    private void fetchExistedMember() {
        roomMemberFirebase.child(roomId).keepSynced(true);
        roomMemberFirebase.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                totalExistedFriends = (int) dataSnapshot.getChildrenCount() - 1;
                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onTotalFriendFetched(totalExistedFriends);
                }

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String existedFriendKey = children.getKey();

                    if (existedFriendKeyList.get(existedFriendKey.hashCode()) == null) {
                        existedFriendKeyList.put(existedFriendKey.hashCode(), existedFriendKey);
                    }
                }

                fetchAddableFriendKey();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void fetchAddableFriendKey() {
        userFriendFirebase.child(uid).keepSynced(true);
        userFriendFirebase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                int fetchCount = 0;
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String addableFriendKey = children.getKey();

                    if (existedFriendKeyList.get(addableFriendKey.hashCode()) == null) {
                        existedFriendKeyList.put(addableFriendKey.hashCode(), addableFriendKey);
                        fetchAddableFriendInfo(addableFriendKey);
                        fetchCount++;
                    }
                }

                if (fetchCount == 0) {
                    if (inviteMemberFetchingListener != null) {
                        inviteMemberFetchingListener.onNoInviteMember();
                    } else {
                        noInviteFriend = true;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void fetchAddableFriendInfo(final String friendKey) {
        userInfoFirebase.child(friendKey).keepSynced(true);
        userInfoFirebase.child(friendKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
                friendInfo.setKey(friendKey);

                addableFriendInfoList.put(friendKey.hashCode(), friendInfo);

                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onAddableFriendFetched(friendInfo);
                } else {
                    addableFriendInfoLeaveOutList.put(friendKey.hashCode(), friendInfo);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void invite(final String[] totalFriendKeyToBeInvited) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                beginInviting(totalFriendKeyToBeInvited);
            }
        }).start();
    }

    private void beginInviting(String[] totalFriendKeyToBeInvited) {
        // check if already a member
        int size = totalFriendKeyToBeInvited.length;
        ArrayMap<String, Object> inviteMap = new ArrayMap<>(size);
        getInviteMap(inviteMap, totalFriendKeyToBeInvited);

        rootFirebase.get().updateChildren(inviteMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(TAG, "onComplete: " + firebaseError.getMessage() );

                    if (inviteMemberFetchingListener != null) {
                        inviteMemberFetchingListener.onInvitingFailed();
                    } else {
                        invitingFailed = true;
                    }
                    return;
                }

                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onInvitingSuccess();
                } else {
                    invitingSuccess = true;
                }

            }
        });
    }

    private void getInviteMap(ArrayMap<String, Object> map, String[] totalFriendKeyToBeInvited) {
        long when = System.currentTimeMillis();

        SharedPreferences prefs = AppController.getComponent(getActivity()).sharedPreferences();

        String messageKey = messageFirebase.get().child(roomId).push().getKey();
        Message invitedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);

        for (String friendKey : totalFriendKeyToBeInvited) {
            FirebaseMapUtil.mapUserRoomMember(map, friendKey, roomId, when);
            FirebaseMapUtil.mapUserPreservedMemberRoom(map, friendKey, roomId, when);
            FirebaseMapUtil.mapUserRoom(map, friendKey, roomId, when);
        }
        invitedMessage.setMessage(uid + "/" + TextUtil.implodeArray("/", totalFriendKeyToBeInvited));
        invitedMessage.setLanguageRes(MessageUtil.INVITE_MEMBER);

        FirebaseMapUtil.mapUserRoom(map, uid, roomId, when);
        FirebaseMapUtil.mapMessage(map, messageKey, roomId, invitedMessage);

        Message roomInvitedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        roomInvitedMessage.setMessage(prefs.getString(UserInfoUtil.DISPLAY_NAME, "").split(" ")[0] +
                MessageUtil.getStringResTwoParam(R.string.n_invited_n_to_room,
                        invitedMessage, addableFriendInfoList, uid));
        FirebaseMapUtil.mapRoomMessage(map, roomInvitedMessage, roomId);
    }

    public interface OnInviteMemberFetchingListener {
        void onTotalFriendFetched(int totalFriend);
        void onAddableFriendFetched(UserInfo addableFriendInfo);
        void onInvitingSuccess();
        void onInvitingFailed();
        void onNoInviteMember();
    }

}
