package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import icepick.State;

/**
 * Created by OaKKuB on 2/5/2016.
 */
public class IsNodeExistsFirebaseFragment extends BaseFragment {

    private static final String ARGS_NODE = "args:node";
    private static final String TAG = IsNodeExistsFirebaseFragment.class.getSimpleName();
    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> firebase;

    @State
    String key;

    private OnNodeReceivedListener onNodeReceivedListener;

    public static IsNodeExistsFirebaseFragment newInstance() {
        Bundle args = new Bundle();

        IsNodeExistsFirebaseFragment fragment = new IsNodeExistsFirebaseFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onNodeReceivedListener = (OnNodeReceivedListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        getDataArgs();
        setRetainInstance(true);
    }

    private void getDataArgs() {
        Bundle args = getArguments();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (key != null) {
            if (key.isEmpty()) {
                onNodeReceivedListener.nodeNotExist();
            } else {
                onNodeReceivedListener.nodeExists(key);
            }
            key = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onNodeReceivedListener = null;
    }

    public void fetchNode(String node) {
        firebase.get().child(node).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isExists = dataSnapshot.exists();
                String dataSnapshotKey = dataSnapshot.getKey();

                if (onNodeReceivedListener != null) {
                    if (isExists) {
                        onNodeReceivedListener.nodeExists(dataSnapshotKey);
                    } else {
                        onNodeReceivedListener.nodeNotExist();
                    }
                } else {
                    key = dataSnapshotKey;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.getMessage() );
            }
        });
    }


    public interface OnNodeReceivedListener {
        void nodeExists(String key);
        void nodeNotExist();
    }
}
