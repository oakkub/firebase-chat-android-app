package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.managers.GridAutoFitLayoutManager;
import com.oakkub.chat.managers.loaders.MyLoader;
import com.oakkub.chat.managers.loaders.RemoveFriendRequestLoader;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusNotExistsReceivedFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusRemoveFriendRequest;
import com.oakkub.chat.services.GCMNotifyService;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.adapters.FriendListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.dialogs.AlertDialogFragment;
import com.oakkub.chat.views.widgets.MySwipeRefreshLayout;
import com.oakkub.chat.views.widgets.MyTextView;
import com.oakkub.chat.views.widgets.MyToast;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class ReceivedFriendRequestFragment extends BaseFragment implements
        OnAdapterItemClick, LoaderManager.LoaderCallbacks<Boolean>,
        AlertDialogFragment.OnAlertDialogListener,
        ValueEventListener {

    private static final int ACCEPT_FRIEND_LOADER = 100;
    private static final int REJECT_FRIEND_LOADER = 101;

    private static final String ARGS_LOAD_AUTO = "args:loadAutomatically";
    private static final String PENDING_REQUEST_STATE = "state:pendingRequest";
    private static final String ACCEPT_FRIEND_ALERT_DIALOG_TAG = "tag:acceptFriendAlertDialog";

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_RECEIVED_REQUESTED)
    Firebase friendRequestedFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Bind(R.id.swipe_refresh_progress_bar_recycler_view_layout)
    MySwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recyclerview)
    RecyclerView friendRequestedList;

    @Bind(R.id.swipe_refresh_text_view)
    MyTextView alertTextView;

    @State
    int currentSelectedPosition;

    @State
    boolean loadAutomatically;

    private FriendListAdapter friendListAdapter;

    public static ReceivedFriendRequestFragment newInstance(boolean loadAutomatically) {
        Bundle args = new Bundle();
        args.putBoolean(ARGS_LOAD_AUTO, loadAutomatically);

        ReceivedFriendRequestFragment fragment = new ReceivedFriendRequestFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataArgs(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_refresh_progressbar_recyclerview, container, false);
        ButterKnife.bind(this, rootView);
        initInstances();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            swipeRefreshLayout.show();

            if (loadAutomatically) {
                load();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        friendRequestedFirebase.child(uid).removeEventListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        friendListAdapter.onSaveInstanceState(PENDING_REQUEST_STATE, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        friendListAdapter.onRestoreInstanceState(PENDING_REQUEST_STATE, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void getDataArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Bundle args = getArguments();
        loadAutomatically = args.getBoolean(ARGS_LOAD_AUTO);
    }

    private void initInstances() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });

        friendListAdapter = new FriendListAdapter(this);

        GridAutoFitLayoutManager layoutManager = new GridAutoFitLayoutManager(getActivity(), 0);

        friendRequestedList.setLayoutManager(layoutManager);
        friendRequestedList.setHasFixedSize(true);
        friendRequestedList.setAdapter(friendListAdapter);
        friendRequestedList.setBackgroundColor(getCompatColor(R.color.defaultBackground));
    }

    private void showResultText() {
        if (isDetached()) return;

        swipeRefreshLayout.setRefreshing(false);
        if (friendListAdapter.isEmpty()) {
            alertTextView.setText(getString(R.string.error_no_new_requests));
            alertTextView.visible();
        }
    }

    public void onEvent(EventBusNotExistsReceivedFriendRequest eventBusNotExistsReceivedFriendRequest) {
        if (friendListAdapter == null) return;
        friendListAdapter.remove(eventBusNotExistsReceivedFriendRequest.userInfo);

        showResultText();
    }

    public void load() {
        friendRequestedFirebase.child(uid).addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        swipeRefreshLayout.setRefreshing(false);

        if (!dataSnapshot.exists()) {
            showResultText();
            return;
        }

        for (DataSnapshot children : dataSnapshot.getChildren()) {
            String pendingRequestKey = children.getKey();
            loadPendingUserInfo(pendingRequestKey);
        }

        dataSnapshot.getRef().removeEventListener(this);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        showResultText();
    }

    private void loadPendingUserInfo(String pendingRequestKey) {
        userInfoFirebase.child(pendingRequestKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                userInfo.setKey(dataSnapshot.getKey());

                if (!friendListAdapter.contains(userInfo)) {
                    friendListAdapter.addFirst(userInfo);
                }

                if (friendListAdapter.isEmpty()) {
                    showResultText();
                } else {
                    alertTextView.gone();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    @Override
    public void onAdapterClick(View itemView, int position) {
        currentSelectedPosition = position;

        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(
                getString(R.string.accept_friend),
                getString(R.string.accept_n_as_friend, friendListAdapter.getItem(position).getDisplayName()),
                getString(R.string.accept), getString(R.string.cancel),
                getString(R.string.reject), true);
        alertDialog.show(getChildFragmentManager(), ACCEPT_FRIEND_ALERT_DIALOG_TAG);
    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        return false;
    }

    @Override
    public void onAlertDialogClick(String tag, int which) {
        if (!tag.equals(ACCEPT_FRIEND_ALERT_DIALOG_TAG) || which == DialogInterface.BUTTON_NEUTRAL) return;

        showProgressDialog();
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                getLoaderManager().restartLoader(ACCEPT_FRIEND_LOADER, null, this);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                getLoaderManager().restartLoader(REJECT_FRIEND_LOADER, null, this);
                break;
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        UserInfo acceptedFriendInfo = friendListAdapter.getItem(currentSelectedPosition);

        switch (id) {
            case ACCEPT_FRIEND_LOADER:
                return new AcceptFriendLoader(getActivity(), uid, acceptedFriendInfo);
            case REJECT_FRIEND_LOADER:
                return new RemoveFriendRequestLoader(getActivity(), uid, acceptedFriendInfo, true);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        hideProgressDialog();

        switch (loader.getId()) {
            case ACCEPT_FRIEND_LOADER:
                onAcceptFriendFinished(data);
                break;
            case REJECT_FRIEND_LOADER:
                onRejectFriend(data);
                break;
        }

        showResultText();
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private void onAcceptFriendFinished(Boolean data) {
        UserInfo acceptedFriendInfo = friendListAdapter.getItem(currentSelectedPosition);

        if (data) {
            MyToast.make(getString(R.string.you_accept_n_as_friend, acceptedFriendInfo.getDisplayName())).show();
            friendListAdapter.remove(acceptedFriendInfo);
        } else {
            MyToast.make(getString(R.string.error_accept_friend, acceptedFriendInfo.getDisplayName())).show();
        }
    }

    private void onRejectFriend(Boolean data) {
        if (!data) {
            MyToast.make(getString(R.string.cannot_remove_requst_try_again)).show();
        } else {
            friendListAdapter.remove(friendListAdapter.getItem(currentSelectedPosition));
        }
    }

    public static class AcceptFriendLoader extends MyLoader<Boolean> {

        @Inject
        @Named(FirebaseUtil.NAMED_ROOT)
        Firebase firebase;

        @Inject
        @Named(FirebaseUtil.NAMED_USER_FRIENDS_RECEIVED_REQUESTED)
        Firebase requestReceivedFirebase;

        private String uid;
        private UserInfo acceptedFriendInfo;

        public AcceptFriendLoader(Context context, String uid, UserInfo acceptedFriendInfo) {
            super(context);
            AppController.getComponent(getContext()).inject(this);
            this.uid = uid;
            this.acceptedFriendInfo = acceptedFriendInfo;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onForceLoad() {
            super.onForceLoad();

            requestReceivedFirebase.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().removeEventListener(this);
                    acceptFriendRequest(dataSnapshot);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    deliverResult(false);
                }
            });
        }

        private void acceptFriendRequest(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.exists()) {
                deliverResult(false);
                EventBus.getDefault().post(new EventBusNotExistsReceivedFriendRequest(
                        acceptedFriendInfo));
                return;
            }

            ArrayMap<String, Object> map = new ArrayMap<>(4);
            FirebaseMapUtil.mapFriendReceivedRequest(map, acceptedFriendInfo.getKey(), uid, true);
            FirebaseMapUtil.mapFriendPendingRequest(map, uid, acceptedFriendInfo.getKey(), true);
            FirebaseMapUtil.mapUserFriend(map, uid, acceptedFriendInfo.getKey(), false);
            FirebaseMapUtil.mapUserFriend(map, acceptedFriendInfo.getKey(), uid, false);

            firebase.updateChildren(map, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    deliverResult(firebaseError == null);

                    if (firebaseError == null) {
                        EventBus.getDefault().post(new EventBusRemoveFriendRequest(acceptedFriendInfo));
                        sendNotification();
                    }
                }
            });
        }

        private void sendNotification() {
            String displayName = AppController.getComponent(
                    getContext()).sharedPreferences().getString(UserInfoUtil.DISPLAY_NAME, "");

            Intent intent = new Intent(Contextor.getInstance().getContext(), GCMNotifyService.class);
            intent.putExtra(GCMUtil.KEY_TO, acceptedFriendInfo.getInstanceID());
            intent.putExtra(GCMUtil.DATA_SENT_BY, uid);
            intent.putExtra(GCMUtil.DATA_TITLE, getContext().getString(R.string.accept_friend_request));
            intent.putExtra(GCMUtil.DATA_MESSAGE,
                    getContext().getString(R.string.n_accepted_you_as_friend, displayName));
            intent.putExtra(GCMUtil.NOTIFY_TYPE, GCMUtil.FRIEND_ACCEPTED_NOTIFY_TYPE);

            getContext().startService(intent);
        }
    }
}
