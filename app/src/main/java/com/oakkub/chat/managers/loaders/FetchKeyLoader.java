package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/26/2016.
 */
public class FetchKeyLoader extends MyLoader<List<String>> {

    private static final String TAG = FetchKeyLoader.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    @Inject
    SharedPreferences prefs;

    private String uid;
    private String keyPath;

    public FetchKeyLoader(Context context, String keyPath) {
        super(context);
        AppController.getComponent(getContext()).inject(this);
        uid = prefs.getString(PrefsUtil.PREF_UID, null);

        this.keyPath = keyPath;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        fetchKey();
    }

    private void fetchKey() {
        firebase.child(keyPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeEventListener(this);

                int size = (int) dataSnapshot.getChildrenCount();
                ArrayList<String> friendInfoList = new ArrayList<>(size);

                if (!dataSnapshot.exists()) {
                    deliverResult(friendInfoList);
                    return;
                }

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    friendInfoList.add(children.getKey());
                }

                deliverResult(friendInfoList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
