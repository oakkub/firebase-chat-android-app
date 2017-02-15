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
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.managers.RefreshListener;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusDeleteGroupRoom;
import com.oakkub.chat.models.eventbus.EventBusGroupRoom;
import com.oakkub.chat.models.eventbus.EventBusUpdatedGroupRoom;
import com.oakkub.chat.views.adapters.GroupListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends BaseFragment implements OnAdapterItemClick,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARGS_MY_ID = "args:uid";
    private static final String TAG = GroupListFragment.class.getSimpleName();

    @BindView(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerview)
    RecyclerView groupRecyclerView;

    @BindView(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    private GroupListAdapter groupListAdapter;
    private OnScrolledEventListener onScrolledEventListener;
    private RefreshListener refreshListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
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

        if (groupListAdapter != null) {
            groupListAdapter.onSaveInstanceState("a", outState);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        if (groupListAdapter != null) {
            groupListAdapter.onRestoreInstanceState("a", savedInstanceState);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onScrolledEventListener = null;
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
            refreshListener.onRefresh(MainActivity.GROUP_LIST_FRAGMENT_TAG);
        }
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);

        groupListAdapter = new GroupListAdapter(this);

        groupRecyclerView.setLayoutManager(new GridAutoFitLayoutManager(getActivity(), columnWidth));
        groupRecyclerView.setHasFixedSize(true);
        groupRecyclerView.addOnScrollListener(new RecyclerViewScrollDirectionListener() {
            @Override
            public void onScrollUp() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollUp();
                }
            }

            @Override
            public void onScrollDown() {
                if (onScrolledEventListener != null) {
                    onScrolledEventListener.onScrollDown();
                }
            }
        });

        groupRecyclerView.setAdapter(groupListAdapter);
    }

    private void showEmptyItem() {
        alertTextView.setText(R.string.you_dont_have_groups);
        alertTextView.visible();
    }

    @Subscribe
    public void onEvent(EventBusUpdatedGroupRoom eventBusUpdatedGroupRoom) {
        if (groupListAdapter == null) return;
        groupListAdapter.replace(eventBusUpdatedGroupRoom.room);
    }

    @Subscribe
    public void onEvent(EventBusDeleteGroupRoom eventBusDeleteGroupRoom) {
        if (groupListAdapter == null) return;
        groupListAdapter.remove(eventBusDeleteGroupRoom.room);

        if (groupListAdapter.isEmpty()) {
            showEmptyItem();
        }
    }

    @Subscribe
    public void onEvent(EventBusGroupRoom eventBusGroupRoom) {
        ArrayList<Room> roomList = eventBusGroupRoom.roomList;
        swipeRefreshLayout.hide();

        if (roomList.isEmpty()) {

            showEmptyItem();

        } else {
            alertTextView.gone();

            Collections.reverse(roomList);

            if (groupListAdapter != null) {
                for (int i = 0, size = roomList.size(); i < size; i++) {
                    Room room = roomList.get(i);
                    if (!groupListAdapter.contains(room)) {
                        groupListAdapter.addLast(room);
                    }
                }
            }
        }
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = groupListAdapter.getItem(position);

        Intent groupDetailIntent = GroupDetailDialogActivity.getStartIntent(getActivity(), room,
                true, GroupDetailDialogActivity.ACTION_GROUP);
        startActivity(groupDetailIntent);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

}
