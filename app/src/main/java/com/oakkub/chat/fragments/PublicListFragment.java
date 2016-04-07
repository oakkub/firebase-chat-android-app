package com.oakkub.chat.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.GroupDetailDialogActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.managers.RefreshListener;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusDeletePublicChat;
import com.oakkub.chat.models.eventbus.EventBusPublicRoom;
import com.oakkub.chat.models.eventbus.EventBusUpdatedPublicRoom;
import com.oakkub.chat.views.adapters.GroupListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublicListFragment extends BaseFragment implements OnAdapterItemClick,
    SwipeRefreshLayout.OnRefreshListener {

    private static final String ARGS_MY_ID = "args:uid";
    private static final String STATE_PUBLIC_CHAT_ADAPTER = "state:publicChatAdapter";

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView publicChatList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    private GroupListAdapter publicChatAdapter;
    private RefreshListener refreshListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        refreshListener = (RefreshListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initInstances();

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
        }
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
    public void onDetach() {
        super.onDetach();

        refreshListener = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh(MainActivity.PUBLIC_LIST_FRAGMENT_TAG);
        }
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        int columnWidth = getResources().getDimensionPixelSize(R.dimen.spacing_larger);
        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);

        publicChatAdapter = new GroupListAdapter(this);

        publicChatList.setLayoutManager(gridLayoutManager);
        publicChatList.setHasFixedSize(true);
        publicChatList.setAdapter(publicChatAdapter);
    }

    private void showEmptyItem() {
        alertTextView.setText(R.string.you_dont_have_public_chat);
        alertTextView.visible();
    }

    @Subscribe
    public void onEvent(EventBusUpdatedPublicRoom eventBusUpdatedPublicRoom) {
        if (publicChatAdapter == null) return;
        publicChatAdapter.replace(eventBusUpdatedPublicRoom.room);
    }

    @Subscribe
    public void onEvent(EventBusDeletePublicChat eventBusDeletePublicChat) {
        if (publicChatAdapter == null) return;
        publicChatAdapter.remove(eventBusDeletePublicChat.room);

        if (publicChatAdapter.isEmpty()) {
            showEmptyItem();
        }
    }

    @Subscribe
    public void onEvent(EventBusPublicRoom eventBusPublicRoom) {
        ArrayList<Room> publicRoom = eventBusPublicRoom.rooms;
        swipeRefreshLayout.hide();

        if (publicRoom.isEmpty()) {

            showEmptyItem();

        } else {
            alertTextView.gone();

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
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = publicChatAdapter.getItem(position);

        Intent publicRoomDetailIntent = GroupDetailDialogActivity
                .getStartIntent(getActivity(), room, true, GroupDetailDialogActivity.ACTION_PUBLIC);
        startActivity(publicRoomDetailIntent);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

}
