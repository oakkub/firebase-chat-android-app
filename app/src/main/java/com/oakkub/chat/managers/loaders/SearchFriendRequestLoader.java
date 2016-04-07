package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.models.eventbus.EventBusSearchResultFriendRequest;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/26/2016.
 */
public class SearchFriendRequestLoader extends MyAsyncLoader<List<UserInfo>> {

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS)
    Firebase userFriendFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_FRIENDS_PENDING_REQUEST)
    Firebase pendingRequestFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase userInfoFirebase;

    @Inject
    SharedPreferences prefs;

    private String query;
    private String uid;

    public SearchFriendRequestLoader(Context context, String query) {
        super(context);
        AppController.getComponent(getContext()).inject(this);
        this.query = query;

        uid = prefs.getString(PrefsUtil.PREF_UID, null);
    }

    @Override
    public List<UserInfo> loadInBackground() {
        search();
        return null;
    }

    public void search() {
        String[] childNodes = { UserInfoUtil.DISPLAY_NAME, UserInfoUtil.EMAIL };

        for (String childNode : childNodes) {
            userInfoFirebase.orderByChild(childNode)
                    .startAt(query).endAt(query + "~")
                    .limitToLast(30)
                    .addValueEventListener(new QueryValueEventListener(
                            new SearchFriendRequestLoader.OnQueryValueEventResultListener() {
                        @Override
                        public void onResultReceived(List<UserInfo> list) {
                            checkIfFriend(list);
                        }
                    }));
        }
    }

    private void checkIfFriend(final List<UserInfo> list) {
        userFriendFirebase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    for (int i = 0, size = list.size(); i < size; i++) {
                        UserInfo userInfoItem = list.get(i);
                        if (userInfoItem.getKey().equals(key)) {
                            list.remove(i);
                        }
                    }
                }
                checkIfSendPendingRequest(list);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    private void checkIfSendPendingRequest(final List<UserInfo> list) {
        pendingRequestFirebase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    for (int i = 0, size = list.size(); i < size; i++) {
                        UserInfo userInfoItem = list.get(i);
                        if (userInfoItem.getKey().equals(key)) {
                            list.remove(i);
                        }
                    }
                }

                // search completed.
                data = list;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    private class QueryValueEventListener implements ValueEventListener {

        private OnQueryValueEventResultListener listener;

        public QueryValueEventListener(OnQueryValueEventResultListener listener) {
            this.listener = listener;
        }

        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            dataSnapshot.getRef().removeEventListener(this);
            if (!dataSnapshot.exists()) {
                EventBus.getDefault().post(new EventBusSearchResultFriendRequest(null));
                return;
            }
            ArrayList<UserInfo> userInfoList = getItem(dataSnapshot);
            listener.onResultReceived(userInfoList);
        }

        private ArrayList<UserInfo> getItem(DataSnapshot dataSnapshot) {
            ArrayList<UserInfo> userInfoList = new ArrayList<>((int) dataSnapshot.getChildrenCount());

            for (DataSnapshot children : dataSnapshot.getChildren()) {
                String key = children.getKey();

                if (!key.equals(uid)) {

                    UserInfo userInfo = children.getValue(UserInfo.class);
                    userInfo.setKey(key);
                    userInfoList.add(userInfo);
                }
            }

            return userInfoList;
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {}

    }

    public interface OnQueryValueEventResultListener {
        void onResultReceived(List<UserInfo> list);
    }

}
