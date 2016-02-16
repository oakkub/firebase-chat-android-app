package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import icepick.State;

/**
 * Created by OaKKuB on 2/2/2016.
 */
public class KeyToValueFirebaseFetchingFragment extends BaseFragment {

    private static final String ARGS_KEY_NODE = "args:firstNode";
    private static final String ARGS_VALUE_NODE = "args:secondNode";
    private static final String TAG = KeyToValueFirebaseFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firstNodeFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase secondNodeFirebase;

    @State
    String firstNode;

    @State
    String secondNode;

    private ArrayList<String> keyNodeItems;

    private String valueNodeKey;
    private HashMap<String, Object> valueNodeMap;

    private OnDataReceivedListener onDataReceivedListener;

    public static KeyToValueFirebaseFetchingFragment newInstance(@NonNull String myId,
                                                                 @NonNull String keyNode,
                                                                 @NonNull String valueNode) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putString(ARGS_KEY_NODE, keyNode);
        args.putString(ARGS_VALUE_NODE, valueNode);

        KeyToValueFirebaseFetchingFragment fragment = new KeyToValueFirebaseFetchingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onDataReceivedListener = (OnDataReceivedListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataIntent();

        keyNodeItems = new ArrayList<>();
        valueNodeMap = new HashMap<>();
    }

    private void getDataIntent() {
        Bundle args = getArguments();

        firstNode = args.getString(ARGS_KEY_NODE);
        secondNode = args.getString(ARGS_VALUE_NODE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (valueNodeMap != null && !valueNodeMap.isEmpty()) {
            onDataReceivedListener.onValueNodeItemReceived(valueNodeKey, valueNodeMap);
            valueNodeMap = null;
            valueNodeKey = null;
        }

        fetchKeyNode();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onDataReceivedListener = null;
    }

    public void fetchKeyNode() {
        firstNodeFirebase.child(firstNode).keepSynced(true);
        firstNodeFirebase.child(firstNode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) return;

                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            String key = children.getKey();

                            if (!keyNodeItems.contains(key)) {
                                keyNodeItems.add(key);
                                fetchValueNode(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void fetchValueNode(String key) {
        secondNodeFirebase.child(secondNode).child(key).keepSynced(true);
        secondNodeFirebase.child(secondNode).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> mapData = (HashMap<String, Object>) dataSnapshot.getValue();
                        String key = dataSnapshot.getKey();

                        if (onDataReceivedListener != null) {
                            onDataReceivedListener.onValueNodeItemReceived(key, mapData);
                        } else {
                            valueNodeKey = key;
                            valueNodeMap = mapData;
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    public interface OnDataReceivedListener {
        void onValueNodeItemReceived(String itemKey, HashMap<String, Object> itemMap);
    }
}
