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
public class RemoveMemberFragment extends BaseFragment {

    public static final String ARGS_ROOM_ID = "args:roomId";
    private static final String TAG = RemoveMemberFragment.class.getSimpleName();

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

    private int totalRemovableFriends;

    private boolean removingFailed;
    private boolean removingSuccess;
    private boolean isNoRemovableMember;

    private SparseArray<String> removableFriendKeyList;
    private SparseArray<UserInfo> removableFriendInfoList;
    private SparseArray<UserInfo> removableFriendInfoLeavesOutList;

    private OnRemoveMemberListener removingMemberListener;

    public static RemoveMemberFragment newInstance(String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ROOM_ID, roomId);

        RemoveMemberFragment fragment = new RemoveMemberFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        removingMemberListener = (OnRemoveMemberListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();

        removableFriendKeyList = new SparseArray<>();
        removableFriendInfoList = new SparseArray<>();
        removableFriendInfoLeavesOutList = new SparseArray<>();

        fetchRemovableMemberKey();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        removingMemberListener = null;
    }

    private void getDataIntent() {
        Bundle args = getArguments();
        roomId = args.getString(ARGS_ROOM_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (totalRemovableFriends > 0) {
            removingMemberListener.onRemovableTotalMember(totalRemovableFriends);
        }

        int addableFriendInfoLeaveOutSize = removableFriendInfoLeavesOutList.size();
        if (addableFriendInfoLeaveOutSize > 0) {
            for (int i = 0; i < addableFriendInfoLeaveOutSize; i++) {
                removingMemberListener.onRemovableFriendInfoFetched(removableFriendInfoLeavesOutList.valueAt(i));
            }
            removableFriendInfoLeavesOutList.clear();
        }

        if (removingFailed) {
            removingMemberListener.onRemovableFailed();
            removingFailed = false;
        }

        if (removingSuccess) {
            removingMemberListener.onRemovableSuccess();
            removingSuccess = false;
        }

        if (isNoRemovableMember) {
            removingMemberListener.onNoRemovableMember();
            isNoRemovableMember = false;
        }
    }

    private void fetchRemovableMemberKey() {
        roomMemberFirebase.child(roomId).keepSynced(true);
        roomMemberFirebase.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                totalRemovableFriends = (int) dataSnapshot.getChildrenCount() - 1;
                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableTotalMember(totalRemovableFriends);
                }

                int fetchCount = 0;
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String removableFriendKey = children.getKey();

                    if (removableFriendKeyList.get(removableFriendKey.hashCode()) == null && !removableFriendKey.equals(uid)) {
                        removableFriendKeyList.put(removableFriendKey.hashCode(), removableFriendKey);
                        fetchAddableFriendInfo(removableFriendKey);
                        fetchCount++;
                    }
                }

                if (removingMemberListener != null) {
                    if (fetchCount == 0) {
                        removingMemberListener.onNoRemovableMember();
                    } else {
                        isNoRemovableMember = true;
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

                removableFriendInfoList.put(friendKey.hashCode(), friendInfo);

                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableFriendInfoFetched(friendInfo);
                } else {
                    removableFriendInfoLeavesOutList.put(friendKey.hashCode(), friendInfo);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void remove(final String[] totalFriendKeyToBeRemoved) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                beginRemoving(totalFriendKeyToBeRemoved);
            }
        }).start();
    }

    private void beginRemoving(String[] totalFriendKeyToBeInvited) {
        // check if already a member
        int size = totalFriendKeyToBeInvited.length;
        ArrayMap<String, Object> inviteMap = new ArrayMap<>(size);
        getRemoveMap(inviteMap, totalFriendKeyToBeInvited);

        rootFirebase.get().updateChildren(inviteMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(TAG, "onComplete: " + firebaseError.getMessage() );

                    if (removingMemberListener != null) {
                        removingMemberListener.onRemovableFailed();
                    } else {
                        removingFailed = true;
                    }
                    return;
                }

                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableSuccess();
                } else {
                    removingSuccess = true;
                }

            }
        });
    }

    private void getRemoveMap(ArrayMap<String, Object> map, String[] totalFriendKeyToBeInvited) {
        long when = System.currentTimeMillis();

        SharedPreferences prefs = AppController.getComponent(getActivity()).sharedPreferences();

        String messageKey = messageFirebase.get().child(roomId).push().getKey();
        Message removedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);

        for (String friendKey : totalFriendKeyToBeInvited) {
            FirebaseMapUtil.mapUserRoomMember(map, friendKey, roomId, null);
            FirebaseMapUtil.mapUserRoomAdminMember(map, friendKey, roomId, null);
            FirebaseMapUtil.mapUserGroupRoom(map, friendKey, roomId, null);
            FirebaseMapUtil.mapUserPublicRoom(map, friendKey, roomId, null);
            FirebaseMapUtil.mapUserRoom(map, friendKey, roomId, null);
        }

        removedMessage.setMessage(uid + "/" + TextUtil.implodeArray("/", totalFriendKeyToBeInvited));
        removedMessage.setLanguageRes(MessageUtil.REMOVED_MEMBER);

        FirebaseMapUtil.mapUserRoom(map, uid, roomId, when);
        FirebaseMapUtil.mapMessage(map, messageKey, roomId, removedMessage);

        Message roomInvitedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        roomInvitedMessage.setMessage(prefs.getString(UserInfoUtil.DISPLAY_NAME, "").split(" ")[0] +
                MessageUtil.getStringResTwoParam(R.string.n_removed_n_from_room,
                        removedMessage, removableFriendInfoList, uid));
        FirebaseMapUtil.mapRoomMessage(map, roomInvitedMessage, roomId);
    }

    public interface OnRemoveMemberListener {
        void onRemovableTotalMember(int totalFriend);
        void onRemovableFriendInfoFetched(UserInfo addableFriendInfo);
        void onNoRemovableMember();
        void onRemovableSuccess();
        void onRemovableFailed();
    }

}
