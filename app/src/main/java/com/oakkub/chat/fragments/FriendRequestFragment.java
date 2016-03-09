package com.oakkub.chat.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.SparseStringArray;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusFriendRequestList;
import com.oakkub.chat.models.eventbus.EventBusFriendRequestListLoadingMore;
import com.oakkub.chat.models.eventbus.EventBusLoadSendFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusSearchResultFriendRequest;
import com.oakkub.chat.models.eventbus.EventBusSearchingFriendRequest;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendRequestFragment extends BaseFragment {

    public static final int FRIEND_REQUEST_RESULT_PER_PAGE = 2;

    private static final String TAG = FriendRequestFragment.class.getSimpleName();

    private static final String REMOVE_FRIEND_INFO_STATE = "state:removeFriendInfo";
    private static final String FAILED_FRIEND_INFO_STATE = "state:failedFriendInfo";
    private static final String SUCCESS_FRIEND_INFO_STATE = "state:successFriendInfo";
    private static final String REQUEST_SUCCESS_INFO_STATE = "state:requestSuccessInfo";
    private static final String REQUEST_FAILED_INFO_STATE = "state:requestFailedInfo";

    private static final int LOADING_LIMIT = 2;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendsFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_RECEIVED_REQUESTED)
    Firebase friendRequestedFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_PENDING_REQUEST)
    Firebase pendingRequestedFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @Inject
    Lazy<SharedPreferences> prefs;

    @State
    SparseStringArray pendingRequestKeyList;

    @State
    SparseStringArray friendKeyList;

    @State
    SparseStringArray friendFetchedKeyList;

    @State
    SparseStringArray fetchedKeyList;

    @State
    ArrayList<UserInfo> friendInfoList;

    private long oldestFriendRegisteredDate = -1;
    private boolean isLoadingMore;

    public static FriendRequestFragment newInstance(String uid) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, uid);

        FriendRequestFragment fragment = new FriendRequestFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        if (savedInstanceState == null) {
            pendingRequestKeyList = new SparseStringArray();
            friendKeyList = new SparseStringArray();
            friendFetchedKeyList = new SparseStringArray();
            fetchedKeyList = new SparseStringArray();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getPendingRequestFriend();
    }

    private class ArrayValueEventListener implements ValueEventListener {

        public static final int FRIEND_MODE = 0;
        public static final int PENDING_FRIEND_MODE = 1;

        private SparseStringArray list;
        private int mode;

        public ArrayValueEventListener(SparseStringArray list, int mode) {
            this.list = list;
            this.mode = mode;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dataSnapshot.getRef().removeEventListener(this);

            if (dataSnapshot.exists()) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();
                    list.put(key.hashCode(), key);
                }
            }

            switch (mode) {
                case PENDING_FRIEND_MODE:
                    getFriendsKey();
                    break;
                case FRIEND_MODE:
                    getRecommendedFriend();
                    break;
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }

    private class FriendRequestValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dataSnapshot.getRef().removeEventListener(this);
            getRecommendedFriendList(dataSnapshot);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }

    private void getPendingRequestFriend() {
        pendingRequestedFirebase.child(uid).keepSynced(true);
        pendingRequestedFirebase.child(uid)
                .addValueEventListener(new ArrayValueEventListener(pendingRequestKeyList,
                        ArrayValueEventListener.PENDING_FRIEND_MODE));
    }

    private void getFriendsKey() {
        userFriendsFirebase.child(uid).keepSynced(true);
        userFriendsFirebase.child(uid)
                .addValueEventListener(new ArrayValueEventListener(friendKeyList,
                        ArrayValueEventListener.FRIEND_MODE));
    }

    private void getRecommendedFriend() {
        userInfoFirebase
                .orderByChild(UserInfoUtil.REGISTERED_DATE)
                .limitToLast(LOADING_LIMIT
                        + friendKeyList.size() + pendingRequestKeyList.size())
                .addValueEventListener(new FriendRequestValueEventListener());
    }

    private void getOlderRecommendedFriend(long registeredDate) {
        isLoadingMore = true;

        userInfoFirebase
                .orderByChild(UserInfoUtil.REGISTERED_DATE)
                .endAt(oldestFriendRegisteredDate > -1 ?
                        oldestFriendRegisteredDate : registeredDate)
                .limitToLast(LOADING_LIMIT + 1)
                .addValueEventListener(new FriendRequestValueEventListener());
    }

    private void getRecommendedFriendList(DataSnapshot dataSnapshot) {
        new AsyncTask<DataSnapshot, Void, ArrayList<UserInfo>>() {

            private int size = 0;

            @Override
            protected ArrayList<UserInfo> doInBackground(DataSnapshot... params) {
                DataSnapshot dataSnapshot = params[0];

                size = (int) dataSnapshot.getChildrenCount();
                ArrayList<UserInfo> recommendedFriendList =
                        new ArrayList<>((int) dataSnapshot.getChildrenCount());

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String userKey = children.getKey();
                    int userKeyHashCode = userKey.hashCode();

                    UserInfo friendUserInfo = UserInfoUtil.get(userKey, children);
                    if (oldestFriendRegisteredDate > friendUserInfo.getRegisteredDate() ||
                        oldestFriendRegisteredDate == -1) {
                        oldestFriendRegisteredDate = friendUserInfo.getRegisteredDate();
                    }

                    if (!userKey.equals(uid) &&
                            friendKeyList.get(userKeyHashCode) == null &&
                            pendingRequestKeyList.get(userKeyHashCode) == null &&
                            friendFetchedKeyList.get(userKeyHashCode) == null) {

                        recommendedFriendList.add(friendUserInfo);
                        friendFetchedKeyList.put(userKeyHashCode, userKey);
                    }
                }

                Collections.reverse(recommendedFriendList);
                return recommendedFriendList;
            }

            @Override
            protected void onPostExecute(ArrayList<UserInfo> userInfoList) {
                super.onPostExecute(userInfoList);

                if (isLoadingMore) {
                    size -= 1;
                    isLoadingMore = false;
                }

                sendData(userInfoList, size);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataSnapshot);
    }

    public void onEvent(EventBusFriendRequestListLoadingMore eventBusFriendRequestListLoadingMore) {
        getOlderRecommendedFriend(eventBusFriendRequestListLoadingMore.lastRegisteredDate);
    }

    private void sendData(ArrayList<UserInfo> recommendedFriendList, int totalFetched) {
        EventBus.getDefault().post(new EventBusFriendRequestList(recommendedFriendList, totalFetched));
    }

    public void onEvent(EventBusSearchingFriendRequest eventBusSearchingFriendRequest) {
        search(eventBusSearchingFriendRequest.query.trim());
    }

    public void search(String query) {
        String[] childNodes = {UserInfoUtil.DISPLAY_NAME, UserInfoUtil.EMAIL};

        for (String childNode : childNodes) {
            userInfoFirebase.orderByChild(childNode)
                    .startAt(query).endAt(query + "\uf8ff")
                    .limitToLast(2)
                    .addValueEventListener(new QueryValueEventListener());
        }
    }

    private class QueryValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            dataSnapshot.getRef().removeEventListener(this);
            if (!dataSnapshot.exists()) {
                EventBus.getDefault().post(new EventBusSearchResultFriendRequest(null));
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<UserInfo> userInfoList = getItem(dataSnapshot);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new EventBusSearchResultFriendRequest(userInfoList));
                        }
                    });
                }
            }).start();
        }

        private ArrayList<UserInfo> getItem(DataSnapshot dataSnapshot) {
            ArrayList<UserInfo> userInfoList = new ArrayList<>((int) dataSnapshot.getChildrenCount());

            for (DataSnapshot children : dataSnapshot.getChildren()) {
                String key = children.getKey();
                int keyHash = key.hashCode();

                if (!key.equals(uid) &&
                        pendingRequestKeyList.get(keyHash) == null &&
                        friendKeyList.get(keyHash) == null) {

                    UserInfo userInfo = children.getValue(UserInfo.class);
                    userInfo.setKey(key);
                    userInfoList.add(userInfo);
                }
            }

            return userInfoList;
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    }

    public void onEvent(EventBusLoadSendFriendRequest eventBusLoadSendFriendRequest) {
        getPendingRequestFriend();
    }

}
