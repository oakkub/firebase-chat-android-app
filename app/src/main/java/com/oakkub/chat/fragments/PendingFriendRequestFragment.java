package com.oakkub.chat.fragments;

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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.loaders.RemoveFriendRequestLoader;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusAddPendingFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusPendingFriendRequest;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SimpleInfoButtonListAdapter;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.MyToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import icepick.State;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class PendingFriendRequestFragment extends BaseFragment implements
        OnAdapterItemClick, AlertDialogFragment.OnAlertDialogListener,
        LoaderManager.LoaderCallbacks<Boolean> {

    private static final String PENDING_REQUEST_STATE = "state:pendingRequest";
    private static final String REMOVE_REQUEST_ALERT_DIALOG = "tag:removeRequestAlertDialog";
    private static final int REMOVE_FRIEND_LOADER = 100;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_PENDING_REQUEST)
    Firebase pendingRequestFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Lazy<Firebase> userFriendsFirebase;

    @BindView(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @BindView(R.id.recyclerview)
    RecyclerView pendingRequestList;

    @State
    int currentSelectedPosition;

    @State
    int loaderAction = -1;

    private SimpleInfoButtonListAdapter simpleInfoButtonListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initInstances();
        return rootView;
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });

        simpleInfoButtonListAdapter = new SimpleInfoButtonListAdapter(this, getString(R.string.remove));

        pendingRequestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        pendingRequestList.setHasFixedSize(true);
        pendingRequestList.setAdapter(simpleInfoButtonListAdapter);
        pendingRequestList.setBackgroundColor(getCompatColor(R.color.defaultBackground));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();
        } else {
            simpleInfoButtonListAdapter.onRestoreInstanceState(PENDING_REQUEST_STATE, savedInstanceState);
        }

        if (loaderAction >= 0) {
            getLoaderManager().initLoader(REMOVE_FRIEND_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        simpleInfoButtonListAdapter.onSaveInstanceState(PENDING_REQUEST_STATE, outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void showAlertText() {
        if (simpleInfoButtonListAdapter.isEmpty()) {
            alertTextView.setText(getString(R.string.you_are_not_send_any_friend_request));
            alertTextView.visible();
        } else {
            alertTextView.gone();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void load() {
        pendingRequestFirebase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showAlertText();
                    return;
                }

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String pendingRequestKey = children.getKey();
                    loadPendingUserInfo(pendingRequestKey);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void loadPendingUserInfo(String pendingRequestKey) {
        userInfoFirebase.child(pendingRequestKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                userInfo.setKey(dataSnapshot.getKey());

                if (!simpleInfoButtonListAdapter.contains(userInfo)) {
                    simpleInfoButtonListAdapter.addLast(userInfo);
                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        currentSelectedPosition = position;

        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(
                getString(R.string.remove_request),
                getString(R.string.are_you_sure_to_remove_friend_request_for_n,
                        simpleInfoButtonListAdapter.getItem(currentSelectedPosition).getDisplayName()),
                getString(R.string.remove), "");
        alertDialog.show(getChildFragmentManager(), REMOVE_REQUEST_ALERT_DIALOG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (tag.equals(REMOVE_REQUEST_ALERT_DIALOG) && which == DialogInterface.BUTTON_POSITIVE) {
            loaderAction = REMOVE_FRIEND_LOADER;

            showProgressDialog();
            getLoaderManager().restartLoader(REMOVE_FRIEND_LOADER, null, this);
        }
    }

    @Subscribe
    public void onEvent(EventBusAddPendingFriendRequest eventBusAddPendingFriendRequest) {
        if (simpleInfoButtonListAdapter == null) return;
        simpleInfoButtonListAdapter.addFirst(eventBusAddPendingFriendRequest.userInfo);
        showAlertText();
    }

    @Subscribe
    public void onEvent(EventBusPendingFriendRequest eventBusPendingFriendRequest) {
        if (simpleInfoButtonListAdapter == null) return;
        if (simpleInfoButtonListAdapter.contains(eventBusPendingFriendRequest.userInfo)) return;

        simpleInfoButtonListAdapter.addFirst(eventBusPendingFriendRequest.userInfo);
        showAlertText();
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        UserInfo friendInfo = simpleInfoButtonListAdapter.getItem(currentSelectedPosition);

        switch (id) {
            case REMOVE_FRIEND_LOADER:
                return new RemoveFriendRequestLoader(getActivity(), uid, friendInfo, false);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        hideProgressDialog();

        switch (loader.getId()) {
            case REMOVE_FRIEND_LOADER:
                onRemoveRequest(data);
                break;
        }

        currentSelectedPosition = -1;
        loaderAction = -1;
        showAlertText();
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private void onRemoveRequest(boolean data) {
        if (!data) {
            MyToast.make(getString(R.string.cannot_remove_requst_try_again)).show();
        } else {
            simpleInfoButtonListAdapter.remove(
                    simpleInfoButtonListAdapter.getItem(currentSelectedPosition));
        }
    }

}
