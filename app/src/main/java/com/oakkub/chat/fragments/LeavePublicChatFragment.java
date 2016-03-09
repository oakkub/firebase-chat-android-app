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
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.MessageUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class LeavePublicChatFragment extends BaseFragment {

    private static final String ARGS_ROOM_ID = "args:roomId";
    private static final String TAG = LeavePublicChatFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_ADMIN_MEMBERS)
    Lazy<Firebase> adminMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_MEMBERS)
    Lazy<Firebase> memberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_PUBLIC)
    Lazy<Firebase> roomsPublicFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_PUBLIC)
    Lazy<Firebase> userPublicFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_LIST)
    Lazy<Firebase> messageFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_MESSAGES_READ_GROUP_ROOM)
    Lazy<Firebase> messageReadTotalGroupRoomFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_PRESERVED_MEMBERS)
    Lazy<Firebase> preservedMemberFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    private String myId;
    private String roomId;
    private String promotedMemberId;

    private SparseArray<String> memberIds;

    private boolean isLastMember;
    private boolean isLastAdmin;
    private boolean isAdmin;
    private boolean isLeaveSuccess;
    private boolean isLeaveFailed;

    private OnLeavePublicChatListener leavePublicChatListener;

    public static LeavePublicChatFragment newInstance(String myId, String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putString(ARGS_ROOM_ID, roomId);

        LeavePublicChatFragment fragment = new LeavePublicChatFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        leavePublicChatListener = (OnLeavePublicChatListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataIntent();
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isLeaveFailed) {
            leavePublicChatListener.onPublicLeaveFailed();
            isLeaveFailed = false;
        }

        if (isLeaveSuccess) {
            leavePublicChatListener.onPublicLeaveSuccess();
            isLeaveSuccess = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        leavePublicChatListener = null;
    }

    private void getDataIntent() {
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
        roomId = args.getString(ARGS_ROOM_ID);
    }

    public void leave() {
        if (memberIds == null) {
            memberIds = new SparseArray<>();
        }

        checkLastMember();
    }

    private void checkLastMember() {
        memberFirebase.get().child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int size = (int) dataSnapshot.getChildrenCount();
                        isLastMember = size == 1;

                        if (!isLastMember) {
                            // find member to be promoted as admin if the last admin is leave.
                            for (DataSnapshot children : dataSnapshot.getChildren()) {
                                String key = children.getKey();

                                if (!key.equals(myId)) {
                                    memberIds.put(key.hashCode(), key);
                                }
                                if (!key.equals(myId) && promotedMemberId == null) {
                                    promotedMemberId = key;
                                }
                            }
                        }

                        checkAdmin();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void checkAdmin() {
        adminMemberFirebase.get().child(roomId).child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isAdmin = dataSnapshot.exists();
                        if (!isAdmin) {
                            beginLeaving();
                        } else {
                            checkLastAdmin();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void checkLastAdmin() {
        adminMemberFirebase.get().child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isLastAdmin = dataSnapshot.getChildrenCount() == 1;
                beginLeaving();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private Message getRoomMessage(long when) {
        SharedPreferences prefs = AppController.getComponent(getActivity()).sharedPreferences();

        Message roomInvitedMessage = new Message(roomId,
                getString(R.string.n_leave_this_chat,
                        prefs.getString(UserInfoUtil.DISPLAY_NAME, "").split(" ")[0]),
                FirebaseUtil.SYSTEM, when);
        roomInvitedMessage.setLanguageRes(MessageUtil.LEAVE_CHAT);
        return roomInvitedMessage;
    }

    private void beginLeaving() {
        ArrayMap<String, Object> map = new ArrayMap<>(2);
        String messageKey = messageFirebase.get().child(roomId).push().getKey();
        long when = System.currentTimeMillis();
        boolean isLastAdminNotLastMember = isAdmin && isLastAdmin && !isLastMember;

        if (isAdmin) {
            FirebaseMapUtil.mapUserRoomAdminMember(map, myId, roomId, null);
        }

        if (isLastAdminNotLastMember) {
            Message message = new Message(roomId, TextUtil.implode("/", myId, promotedMemberId),
                    FirebaseUtil.SYSTEM, when);
            message.setLanguageRes(MessageUtil.LAST_ADMIN_LEAVED);

            FirebaseMapUtil.mapMessage(map, messageKey, roomId, message);
            FirebaseMapUtil.mapUserRoomAdminMember(map, promotedMemberId, roomId, when);
        }

        if (isLastMember) {
            FirebaseMapUtil.mapRoom(map, null, roomId);
            roomsPublicFirebase.get().child(roomId).removeValue();
            preservedMemberFirebase.get().child(roomId).removeValue();
            messageFirebase.get().child(roomId).removeValue();
            messageReadTotalGroupRoomFirebase.get().child(roomId).removeValue();
        } else {
            Message message = getRoomMessage(when);
            FirebaseMapUtil.mapRoomMessage(map, message, roomId);

            if (!isAdmin) {
                Message memberMessage = new Message(roomId, "", FirebaseUtil.SYSTEM, when);
                memberMessage.setMessage(myId);
                memberMessage.setLanguageRes(MessageUtil.LEAVE_CHAT);

                FirebaseMapUtil.mapMessage(map, messageKey, roomId, memberMessage);
            }

            if (!isLastAdminNotLastMember) {
                FirebaseMapUtil.mapMessage(map, messageKey, roomId, message);
            }
        }

        for (int i = 0, size = memberIds.size(); i < size; i++) {
            FirebaseMapUtil.mapUserRoom(map, memberIds.valueAt(i), roomId, when);
        }

        FirebaseMapUtil.mapUserGroupRoom(map, myId, roomId, null);
        FirebaseMapUtil.mapUserPublicRoom(map, myId, roomId, null);
        FirebaseMapUtil.mapUserRoomMember(map, myId, roomId, null);
        FirebaseMapUtil.mapUserRoom(map, myId, roomId, null);

        rootFirebase.get().updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                checkLeaveCompletion(firebaseError);
            }
        });
    }

    private void checkLeaveCompletion(FirebaseError firebaseError) {
        if (firebaseError != null) {
            Log.e(TAG, "onComplete: " + firebaseError.getMessage());
            if (leavePublicChatListener != null) {
                leavePublicChatListener.onPublicLeaveFailed();
            } else {
                isLeaveFailed = true;
            }
            return;
        }

        if (leavePublicChatListener != null) {
            leavePublicChatListener.onPublicLeaveSuccess();
        } else {
            isLeaveSuccess = true;
        }
    }

    public interface OnLeavePublicChatListener {
        void onPublicLeaveSuccess();
        void onPublicLeaveFailed();
    }
}
