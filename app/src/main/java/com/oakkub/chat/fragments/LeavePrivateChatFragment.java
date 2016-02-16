package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.oakkub.chat.managers.AppController;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class LeavePrivateChatFragment extends BaseFragment {

    private static final String ARGS_ROOM_ID = "args:roomId";

    private OnLeavePrivateChatListener leavePublicChatListener;

    public static LeavePrivateChatFragment newInstance(String myId, String roomId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putString(ARGS_ROOM_ID, roomId);

        LeavePrivateChatFragment fragment = new LeavePrivateChatFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        leavePublicChatListener = (OnLeavePrivateChatListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        leavePublicChatListener = null;
    }

    public interface OnLeavePrivateChatListener {
        void onPrivateLeaveSuccess();
        void onPrivateLeaveFailed();
    }
}
