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
import com.oakkub.chat.utils.ArrayMapUtil;
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
public class RemoveAdminFragment extends BaseFragment {

    public static final String ARGS_ROOM_ID = "args:roomId";
    private static final String TAG = RemoveAdminFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Firebase roomMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_ADMIN_MEMBERS)
    Firebase adminMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_LIST)
    Lazy<Firebase> messageFirebase;

    private String myId;
    private String roomId;

    private int totalRemovableAdmins;

    private boolean removingAdminFailed;
    private boolean removingAdminSuccess;
    private boolean isNoRemovableAdmin;

    private SparseArray<String> removableAdminKeyList;
    private SparseArray<UserInfo> removableAdminInfoList;
    private SparseArray<UserInfo> removableAdminInfoLeavesOutList;

    private OnRemoveAdminListener removingMemberListener;

    public static RemoveAdminFragment newInstance(String myId, String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putString(ARGS_ROOM_ID, roomId);

        RemoveAdminFragment fragment = new RemoveAdminFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        removingMemberListener = (OnRemoveAdminListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();

        removableAdminKeyList = new SparseArray<>();
        removableAdminInfoList = new SparseArray<>();
        removableAdminInfoLeavesOutList = new SparseArray<>();

        fetchRemovableAdminKey();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        removingMemberListener = null;
    }

    private void getDataIntent() {
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
        roomId = args.getString(ARGS_ROOM_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (totalRemovableAdmins > 0) {
            removingMemberListener.onRemovableTotalAdmin(totalRemovableAdmins);
        }

        int addableFriendInfoLeaveOutSize = removableAdminInfoLeavesOutList.size();
        if (addableFriendInfoLeaveOutSize > 0) {
            for (int i = 0; i < addableFriendInfoLeaveOutSize; i++) {
                removingMemberListener.onRemovableAdminInfoFetched(removableAdminInfoLeavesOutList.valueAt(i));
            }
            removableAdminInfoLeavesOutList.clear();
        }

        if (removingAdminFailed) {
            removingMemberListener.onRemovableAdminFailed();
            removingAdminFailed = false;
        }

        if (removingAdminSuccess) {
            removingMemberListener.onRemovableAdminSuccess();
            removingAdminSuccess = false;
        }

        if (isNoRemovableAdmin) {
            removingMemberListener.onNoRemovableAdmin();
            isNoRemovableAdmin = false;
        }

    }

    private void fetchRemovableAdminKey() {
        adminMemberFirebase.child(roomId).keepSynced(true);
        adminMemberFirebase.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                totalRemovableAdmins = (int) dataSnapshot.getChildrenCount() - 1;
                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableTotalAdmin(totalRemovableAdmins);
                }

                int fetchCount = 0;
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String removableFriendKey = children.getKey();

                    if (removableAdminKeyList.get(removableFriendKey.hashCode()) == null && !removableFriendKey.equals(myId)) {
                        removableAdminKeyList.put(removableFriendKey.hashCode(), removableFriendKey);
                        fetchAddableFriendInfo(removableFriendKey);
                        fetchCount++;
                    }
                }

                if (removingMemberListener != null) {
                    if (fetchCount == 0) {
                        removingMemberListener.onNoRemovableAdmin();
                    } else {
                        isNoRemovableAdmin = true;
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

                removableAdminInfoList.put(friendKey.hashCode(), friendInfo);

                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableAdminInfoFetched(friendInfo);
                } else {
                    removableAdminInfoLeavesOutList.put(friendKey.hashCode(), friendInfo);
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
                        removingMemberListener.onRemovableAdminFailed();
                    } else {
                        removingAdminFailed = true;
                    }
                    return;
                }

                if (removingMemberListener != null) {
                    removingMemberListener.onRemovableAdminSuccess();
                } else {
                    removingAdminSuccess = true;
                }

            }
        });
    }

    private void getRemoveMap(ArrayMap<String, Object> map, String[] totalFriendKeyToBeInvited) {
        long when = System.currentTimeMillis();

        SharedPreferences prefs = AppController.getComponent(getActivity().getApplicationContext()).sharedPreferences();

        String messageKey = messageFirebase.get().child(roomId).push().getKey();
        Message removedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        removedMessage.setMessage(myId + "/" + TextUtil.implodeArray("/", totalFriendKeyToBeInvited));
        removedMessage.setLanguageRes(MessageUtil.DEMOTED_ADMIN);

        ArrayMapUtil.mapMessage(map, messageKey, roomId, removedMessage);

        Message roomDemotedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        roomDemotedMessage.setMessage(prefs.getString(UserInfoUtil.DISPLAY_NAME, "").split(" ")[0] +
                MessageUtil.getStringResTwoParam(R.string.n_demoted_n_from_admin_to_member,
                        removedMessage, removableAdminInfoList, myId));

        ArrayMapUtil.mapRoomMessage(map, roomDemotedMessage, roomId);
        ArrayMapUtil.mapUserRoom(map, myId, roomId, when);
        for (String friendKey : totalFriendKeyToBeInvited) {
            ArrayMapUtil.mapUserRoomAdminMember(map, friendKey, roomId, null);
            ArrayMapUtil.mapUserRoom(map, friendKey, roomId, when);
        }
    }

    public interface OnRemoveAdminListener {
        void onRemovableTotalAdmin(int totalAdmin);
        void onRemovableAdminInfoFetched(UserInfo addableFriendInfo);
        void onNoRemovableAdmin();
        void onRemovableAdminSuccess();
        void onRemovableAdminFailed();
    }

}
