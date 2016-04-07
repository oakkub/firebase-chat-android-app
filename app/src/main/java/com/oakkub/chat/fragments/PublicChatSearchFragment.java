package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/2/2016.
 */
public class PublicChatSearchFragment extends BaseFragment {

    private static final String TAG = KeyToValueFirebaseFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_PUBLIC)
    Firebase roomsPublicFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Firebase roomInfoFirebase;

    private boolean isResultNotExist;

    private ArrayList<String> publicRoomKeyList;
    private Room publicRoom;

    private OnPublicRoomSearchResultListener onPublicRoomSearchResultListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onPublicRoomSearchResultListener = (OnPublicRoomSearchResultListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);

        publicRoomKeyList = new ArrayList<>();
        fetchAll();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (publicRoom != null) {
            onPublicRoomSearchResultListener.onPublicRoomReceived(publicRoom);
            publicRoom = null;
        }

        if (isResultNotExist) {
            onPublicRoomSearchResultListener.onNoResult();
            isResultNotExist = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onPublicRoomSearchResultListener = null;
    }

    public void fetchAll() {
        roomsPublicFirebase.orderByValue().limitToLast(20)
                .addValueEventListener(fetchAllListener);
    }

    private ValueEventListener fetchAllListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!isResultExists(dataSnapshot)) return;

            for (DataSnapshot children : dataSnapshot.getChildren()) {
                String key = children.getKey();
                if (!publicRoomKeyList.contains(key)) {
                    publicRoomKeyList.add(key);
                    fetchValueNode(key);
                }
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private void fetchValueNode(String key) {
        roomInfoFirebase.child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!isResultExists(dataSnapshot)) return;

                        getRoomDataSnapshot(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    public void search(String keyToBeSearched, String query) {
        roomInfoFirebase.orderByChild(keyToBeSearched)
                .startAt(query).endAt(query + "\uf8ff")
                .addValueEventListener(fetchRoomListener);
    }

    private ValueEventListener fetchRoomListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!isResultExists(dataSnapshot)) return;

            for (DataSnapshot children : dataSnapshot.getChildren()) {
                getRoomDataSnapshot(children);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private void getRoomDataSnapshot(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        Room room = dataSnapshot.getValue(Room.class);
        room.setRoomId(key);

        if (onPublicRoomSearchResultListener != null) {
            onPublicRoomSearchResultListener.onPublicRoomReceived(room);
        } else {
            publicRoom = room;
        }
    }

    private boolean isResultExists(DataSnapshot dataSnapshot) {
        boolean exists = dataSnapshot.exists();

        if (onPublicRoomSearchResultListener != null) {
            if (!exists) {
                onPublicRoomSearchResultListener.onNoResult();
            }
        } else {
            if (!exists) {
                isResultNotExist = true;
            }
        }

        return exists;
    }

    public void clear() {
        publicRoomKeyList.clear();
        publicRoom = null;
    }

    public interface OnPublicRoomSearchResultListener {
        void onPublicRoomReceived(Room room);
        void onNoResult();
    }
}
