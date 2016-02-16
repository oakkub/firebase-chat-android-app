package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.FriendDetailActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.managers.OnScrolledEventListener;
import com.oakkub.chat.models.EventBusFriendListInfo;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.SortUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewScrollDirectionListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

public class FriendsFragment extends BaseFragment
        implements OnAdapterItemClick {

    private static final String ARGS_MY_ID = "args:myId";
    private static final String FRIEND_LIST_STATE = "state:friendList";
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView friendsList;

    @State
    String myId;

    private FriendListAdapter friendListAdapter;
    private OnScrolledEventListener onScrolledEventListener;

    public static FriendsFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        FriendsFragment friendsFragment = new FriendsFragment();
        friendsFragment.setArguments(args);
        return friendsFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onScrolledEventListener = (OnScrolledEventListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setFriendListAdapter();

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            myId = args.getString(ARGS_MY_ID);
        }

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        friendListAdapter.onSaveInstanceState(FRIEND_LIST_STATE, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        friendListAdapter.onRestoreInstanceState(FRIEND_LIST_STATE, savedInstanceState);
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

    private void setRecyclerView() {
        final int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);

        GridAutoFitLayoutManager gridLayoutManager = new GridAutoFitLayoutManager(getActivity(), columnWidth);
        DefaultItemAnimator itemAnimator = AppController.getComponent(getActivity()).defaultItemAnimator();

        friendsList.setLayoutManager(gridLayoutManager);
        friendsList.setItemAnimator(itemAnimator);
        friendsList.setHasFixedSize(true);
        friendsList.addOnScrollListener(new RecyclerViewScrollDirectionListener() {
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

        friendsList.setAdapter(friendListAdapter);
    }

    private void setFriendListAdapter() {
        friendListAdapter = new FriendListAdapter(this);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        UserInfo friendInfo = friendListAdapter.getItem(position);

        ImageView profileImage = ButterKnife.findById(itemView, R.id.simpleInfoProfileImageView);

        FriendDetailActivity.launch((AppCompatActivity) getActivity(),
                profileImage, friendInfo, myId);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    public void onEvent(EventBusFriendListInfo eventBusFriendListInfo) {
        List<UserInfo> friendListInfo = eventBusFriendListInfo.friendListInfo;
        SortUtil.sortUserInfoAlphabetically(friendListInfo);

        for (int i = 0, size = friendListInfo.size(); i < size; i++) {
            UserInfo friendInfo = friendListInfo.get(i);
            if (!friendListAdapter.contains(friendInfo)) {
                friendListAdapter.addLast(friendInfo);
            }
        }
    }

}
