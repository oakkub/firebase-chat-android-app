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
public class AddAdminFragment extends BaseFragment {

    public static final String ARGS_ROOM_ID = "args:roomId";
    private static final String TAG = AddAdminFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_ADMIN_MEMBERS)
    Firebase adminMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Firebase memberFirebase;

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

    private int totalExistedAdmin;

    private boolean promotingFailed;
    private boolean promotingSuccess;
    private boolean isNoAddableAdmin;

    private SparseArray<String> existedAdminKeyList;
    private SparseArray<UserInfo> promotableMemberInfoList;
    private SparseArray<UserInfo> promotableMemberInfoLeaveOutList;

    private OnPromoteAdminListener inviteMemberFetchingListener;

    public static AddAdminFragment newInstance(String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ROOM_ID, roomId);

        AddAdminFragment fragment = new AddAdminFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        inviteMemberFetchingListener = (OnPromoteAdminListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();

        existedAdminKeyList = new SparseArray<>();
        promotableMemberInfoList = new SparseArray<>();
        promotableMemberInfoLeaveOutList = new SparseArray<>();

        fetchExistedAdmin();
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

        if (totalExistedAdmin > 0) {
            inviteMemberFetchingListener.onTotalAdminFetched(totalExistedAdmin);
        }

        int addableFriendInfoLeaveOutSize = promotableMemberInfoLeaveOutList.size();
        if (addableFriendInfoLeaveOutSize > 0) {
            for (int i = 0; i < addableFriendInfoLeaveOutSize; i++) {
                inviteMemberFetchingListener.onAddableAdminFetched(promotableMemberInfoLeaveOutList.valueAt(i));
            }
            promotableMemberInfoLeaveOutList.clear();
        }

        if (promotingFailed) {
            inviteMemberFetchingListener.onPromoteFailed();
            promotingFailed = false;
        }

        if (promotingSuccess) {
            inviteMemberFetchingListener.onPromoteSuccess();
            promotingSuccess = false;
        }

        if (isNoAddableAdmin) {
            inviteMemberFetchingListener.onNoAddableAdmin();
            isNoAddableAdmin = false;
        }

    }

    private void fetchExistedAdmin() {
        adminMemberFirebase.child(roomId).keepSynced(true);
        adminMemberFirebase.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                totalExistedAdmin = (int) dataSnapshot.getChildrenCount();
                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onTotalAdminFetched(totalExistedAdmin);
                }

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String existedFriendKey = children.getKey();

                    if (existedAdminKeyList.get(existedFriendKey.hashCode()) == null) {
                        existedAdminKeyList.put(existedFriendKey.hashCode(), existedFriendKey);
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
        memberFirebase.child(roomId).keepSynced(true);
        memberFirebase.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                int fetchCount = 0;
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String addableFriendKey = children.getKey();

                    if (existedAdminKeyList.get(addableFriendKey.hashCode()) == null) {
                        existedAdminKeyList.put(addableFriendKey.hashCode(), addableFriendKey);
                        fetchAddableFriendInfo(addableFriendKey);
                        fetchCount++;
                    }
                }

                if (fetchCount == 0) {
                    if (inviteMemberFetchingListener != null) {
                        inviteMemberFetchingListener.onNoAddableAdmin();
                    } else {
                        isNoAddableAdmin = true;
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

                promotableMemberInfoList.put(friendKey.hashCode(), friendInfo);

                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onAddableAdminFetched(friendInfo);
                } else {
                    promotableMemberInfoLeaveOutList.put(friendKey.hashCode(), friendInfo);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void promote(final String[] totalFriendKeyToBeInvited) {
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
                        inviteMemberFetchingListener.onPromoteFailed();
                    } else {
                        promotingFailed = true;
                    }
                    return;
                }

                if (inviteMemberFetchingListener != null) {
                    inviteMemberFetchingListener.onPromoteSuccess();
                } else {
                    promotingSuccess = true;
                }

            }
        });
    }

    private void getInviteMap(ArrayMap<String, Object> map, String[] totalFriendKeyToBeInvited) {
        long when = System.currentTimeMillis();

        SharedPreferences prefs = AppController.getComponent(getActivity().getApplicationContext()).sharedPreferences();

        String messageKey = messageFirebase.get().child(roomId).push().getKey();
        Message invitedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        invitedMessage.setLanguageRes(MessageUtil.PROMOTED_MEMBER);
        invitedMessage.setMessage(uid + "/" + TextUtil.implodeArray("/", totalFriendKeyToBeInvited));

        FirebaseMapUtil.mapMessage(map, messageKey, roomId, invitedMessage);

        Message roomPromotedMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
        roomPromotedMessage.setMessage(prefs.getString(UserInfoUtil.DISPLAY_NAME, "").split(" ")[0] +
                MessageUtil.getStringResTwoParam(R.string.n_promoted_n_to_be_admin,
                        invitedMessage, promotableMemberInfoList, uid));

        FirebaseMapUtil.mapRoomMessage(map, roomPromotedMessage, roomId);

        FirebaseMapUtil.mapUserRoom(map, uid, roomId, when);
        for (String friendKey : totalFriendKeyToBeInvited) {
            FirebaseMapUtil.mapUserRoomAdminMember(map, friendKey, roomId, when);
            FirebaseMapUtil.mapUserRoom(map, friendKey, roomId, when);
        }
    }

    public interface OnPromoteAdminListener {
        void onTotalAdminFetched(int totalAdmin);
        void onAddableAdminFetched(UserInfo addableMemberInfo);
        void onNoAddableAdmin();
        void onPromoteSuccess();
        void onPromoteFailed();
    }

}
