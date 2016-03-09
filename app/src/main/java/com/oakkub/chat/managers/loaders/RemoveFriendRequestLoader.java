package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 3/5/2016.
 */
public class RemoveFriendRequestLoader extends MyLoader<Boolean> {

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private String uid;
    private UserInfo friendInfo;
    private boolean swapKey;

    public RemoveFriendRequestLoader(Context context, String uid, UserInfo friendInfo, boolean swapKey) {
        super(context);
        AppController.getComponent(getContext()).inject(this);

        this.uid = uid;
        this.friendInfo = friendInfo;
        this.swapKey = swapKey;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public void forceLoad() {
        super.forceLoad();

        String friendKey = friendInfo.getKey();

        ArrayMap<String, Object> map = new ArrayMap<>(2);
        FirebaseMapUtil.mapSendFriendRequested(
                map, swapKey ? uid : friendKey, swapKey ? friendKey : uid, true);
        FirebaseMapUtil.mapFriendPendingRequest(
                map, swapKey ? uid : friendKey, swapKey ? friendKey : uid, true);

        firebase.updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                deliverResult(firebaseError == null);
            }
        });
    }
}
