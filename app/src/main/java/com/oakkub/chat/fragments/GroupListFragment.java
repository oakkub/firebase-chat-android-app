package com.oakkub.chat.fragments;

import android.content.Context;
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
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.models.EventBusGroupRoom;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.adapters.GroupListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends BaseFragment implements OnAdapterItemClick {

    private static final String ARGS_MY_ID = "args:myId";
    private static final String TAG = GroupListFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView groupRecyclerView;

    @State
    String myId;

    private GroupListAdapter groupListAdapter;
    private OnScrolledEventListener onScrolledEventListener;

    public static GroupListFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        GroupListFragment groupListFragment = new GroupListFragment();
        groupListFragment.setArguments(args);
        return groupListFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromArgs(savedInstanceState);
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

        setGroupRecyclerView();
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
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void getDataFromArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);
    }

    private void setGroupRecyclerView() {
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

    public void onEvent(EventBusGroupRoom eventBusGroupRoom) {
        ArrayList<Room> roomList = eventBusGroupRoom.roomList;
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

    @Override
    public void onAdapterClick(View itemView, int position) {
        Room room = groupListAdapter.getItem(position);

        Intent groupDetailIntent = GroupDetailDialogActivity.getStartIntent(getActivity(), room, myId,
                true, GroupDetailDialogActivity.ACTION_GROUP);
        startActivity(groupDetailIntent);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }
}
