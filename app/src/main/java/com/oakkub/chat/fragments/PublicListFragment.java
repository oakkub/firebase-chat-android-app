package com.oakkub.chat.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.GroupDetailDialogActivity;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.models.EventBusPublicRoom;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.adapters.GroupListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublicListFragment extends BaseFragment implements OnAdapterItemClick {

    private static final String ARGS_MY_ID = "args:myId";
    private static final String STATE_PUBLIC_CHAT_ADAPTER = "state:publicChatAdapter";

    @Bind(R.id.recyclerview)
    RecyclerView publicChatList;

    @State
    String myId;

    private GroupListAdapter publicChatAdapter;

    public static PublicListFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        PublicListFragment publicListFragment = new PublicListFragment();
        publicListFragment.setArguments(args);

        return publicListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getArguments();

            myId = args.getString(ARGS_MY_ID);
        }

        publicChatAdapter = new GroupListAdapter(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        publicChatAdapter.onSaveInstanceState(STATE_PUBLIC_CHAT_ADAPTER, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        publicChatAdapter.onRestoreInstanceState(STATE_PUBLIC_CHAT_ADAPTER, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void setRecyclerView() {
        int columnWidth = getResources().getDimensionPixelSize(R.dimen.spacing_larger);
        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);

        publicChatList.setLayoutManager(gridLayoutManager);
        publicChatList.setHasFixedSize(true);
        publicChatList.setAdapter(publicChatAdapter);
    }

    public void onEvent(EventBusPublicRoom eventBusPublicRoom) {
        ArrayList<Room> publicRoom = eventBusPublicRoom.rooms;
        Collections.reverse(publicRoom);

        if (publicChatAdapter!= null) {
            for (int i = 0, size = publicRoom.size(); i < size; i++) {
                Room room = publicRoom.get(i);
                if (!publicChatAdapter.contains(room)) {
                    publicChatAdapter.addLast(room);
                }
            }
        }
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = publicChatAdapter.getItem(position);

        Intent publicRoomDetailIntent = GroupDetailDialogActivity
                .getStartIntent(getActivity(), room, myId, true, GroupDetailDialogActivity.ACTION_PUBLIC);
        startActivity(publicRoomDetailIntent);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }
}
