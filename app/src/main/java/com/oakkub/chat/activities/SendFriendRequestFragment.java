package com.oakkub.chat.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.BaseFragment;
import com.oakkub.chat.managers.loaders.SendFriendRequestLoader;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusAddPendingFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusFriendRequestList;
import com.oakkub.chat.models.eventbus.EventBusFriendRequestListLoadingMore;
import com.oakkub.chat.models.eventbus.EventBusLoadSendFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusRemoveFriendRequest;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SimpleInfoButtonListAdapter;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.recyclerview.RecyclerViewInfiniteScrollListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class SendFriendRequestFragment extends BaseFragment implements OnAdapterItemClick,
        AlertDialogFragment.OnAlertDialogListener, SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<List<UserInfo>> {

    private static final String TAG = SendFriendRequestFragment.class.getSimpleName();
    private static final String FRIEND_REQUEST_DIALOG_TAG = "tag:addFriendDialog;";
    private static final String FRIEND_REQUEST_LIST_STATE = "state:addFriendList";
    private static final String ARGS_QUERY = "args:query";

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView addFriendList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @State
    int currentSelectedPosition;

    @State
    boolean isSendingFriendRequest;

    private RecyclerViewInfiniteScrollListener infiniteScrollListener;
    private SimpleInfoButtonListAdapter simpleInfoButtonListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initInstances();

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isSendingFriendRequest) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new EventBusLoadSendFriendRequest());
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(this);

        /*int columnWidth = (int) getResources().getDimension(R.dimen.cardview_width);
        GridAutoFitLayoutManager gridAutoFitLayoutManager = new GridAutoFitLayoutManager(this, columnWidth);
        DefaultItemAnimator itemAnimator = AppController.getComponent(this).defaultItemAnimator();*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        simpleInfoButtonListAdapter = new SimpleInfoButtonListAdapter(this, getString(R.string.add));
        addFriendList.setHasFixedSize(true);
        addFriendList.setLayoutManager(linearLayoutManager);
        addFriendList.setAdapter(simpleInfoButtonListAdapter);
        addFriendList.setBackgroundColor(getCompatColor(R.color.defaultBackground));

        infiniteScrollListener = new RecyclerViewInfiniteScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                final UserInfo lastUserInfo = simpleInfoButtonListAdapter.getLastItem();

                if (lastUserInfo != null) {
                    getActivity().getWindow().getDecorView().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            simpleInfoButtonListAdapter.addFooterProgressBar();
                            EventBus.getDefault().post(new EventBusFriendRequestListLoadingMore(
                                    lastUserInfo.getRegisteredDate()));
                        }
                    });
                }
            }
        };
        addFriendList.addOnScrollListener(infiniteScrollListener);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        simpleInfoButtonListAdapter.onSaveInstanceState(FRIEND_REQUEST_LIST_STATE, outState);
        infiniteScrollListener.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        simpleInfoButtonListAdapter.onRestoreInstanceState(FRIEND_REQUEST_LIST_STATE, savedInstanceState);
        infiniteScrollListener.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        currentSelectedPosition = position;
        UserInfo friendInfo = simpleInfoButtonListAdapter.getItem(position);

        AlertDialogFragment dialogFragment = AlertDialogFragment
                .newInstance(getString(R.string.friend_request),
                        getString(R.string.send_friend_request_to_n, friendInfo.getDisplayName()),
                        getString(R.string.send), "");
        dialogFragment.show(getChildFragmentManager(), FRIEND_REQUEST_DIALOG_TAG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(FRIEND_REQUEST_DIALOG_TAG) && which == DialogInterface.BUTTON_POSITIVE) {
            showProgressDialog();
            getLoaderManager().restartLoader(0, null, this);
            isSendingFriendRequest = true;
        }
    }

    private void checkItemList() {
        swipeRefreshLayout.hide();

        if (simpleInfoButtonListAdapter == null || simpleInfoButtonListAdapter.isEmpty()) {
            alertTextView.setText(getString(R.string.no_friend_to_be_added));
            alertTextView.visible();
        } else {
            alertTextView.gone();
        }
    }

    @Override
    public Loader<List<UserInfo>> onCreateLoader(int id, Bundle args) {
        return new SendFriendRequestLoader(
                getActivity(), simpleInfoButtonListAdapter.getItem(currentSelectedPosition));
    }

    @Override
    public void onLoadFinished(Loader<List<UserInfo>> loader, List<UserInfo> data) {
        isSendingFriendRequest = false;
        hideProgressDialog();
        onSendFriendRequestLoaderFinished(data, ((SendFriendRequestLoader) loader).getResultCode());
    }

    private void onSendFriendRequestLoaderFinished(List<UserInfo> data, int code) {
        switch (code) {
            case SendFriendRequestLoader.CODE_SUCCESS:
                simpleInfoButtonListAdapter.remove(data.get(0));
                checkItemList();
                EventBus.getDefault().post(new EventBusAddPendingFriendRequest(data.get(0)));
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<UserInfo>> loader) {
    }

    @Subscribe
    public void onEvent(EventBusFriendRequestList eventBusFriendRequestList) {
        simpleInfoButtonListAdapter.removeFooter();

        if (eventBusFriendRequestList.userInfoList.size() > 0) {
            simpleInfoButtonListAdapter.addLastAll(eventBusFriendRequestList.userInfoList);
        }

        if (eventBusFriendRequestList.totalFetched == 0) {
            infiniteScrollListener.noMoreData();
        } else {
            infiniteScrollListener.setLoadMore(true);
        }

        checkItemList();
    }

    @Subscribe
    public void onEvent(EventBusRemoveFriendRequest eventBusRemoveFriendRequest) {
        if (simpleInfoButtonListAdapter == null) return;
        simpleInfoButtonListAdapter.remove(eventBusRemoveFriendRequest.userInfo);
        checkItemList();
    }

}
