package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 3/17/2016.
 */
public class UpdateNodeLoader extends MyLoader<Boolean> {

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private ArrayMap<String, Object> removeMap;

    public UpdateNodeLoader(Context context, ArrayMap<String, Object> removeMap) {
        super(context);
        this.removeMap = removeMap;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        deleteNode();
    }

    private void deleteNode() {
        firebase.updateChildren(removeMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                deliverResult(firebaseError == null);
            }
        });
    }

}