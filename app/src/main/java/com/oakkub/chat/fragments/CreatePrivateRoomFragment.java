package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.eventbus.EventBusNewPrivateRoomMessage;
import com.oakkub.chat.utils.FirebaseMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.RoomUtil;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

/**
 * Created by OaKKuB on 1/10/2016.
 */
public class CreatePrivateRoomFragment extends BaseFragment {

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    private Room room;
    private OnPrivateRoomRequestListener onPrivateRoomRequestListener;
    private boolean useEventBus;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();

        try {
            onPrivateRoomRequestListener = (OnPrivateRoomRequestListener) activity;
        } catch (ClassCastException e) {
            useEventBus = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        AppController.getComponent(getActivity()).inject(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!useEventBus && savedInstanceState == null) {
            onPrivateRoomRequestListener.onCreatePrivateRoomFragmentCreated();
        }

        if (room != null && !useEventBus) {
            onPrivateRoomRequestListener.onPrivateRoomCreated(room);
            room = null;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        onPrivateRoomRequestListener = null;
    }

    public void createPrivateRoom(final String roomKey) {
        // check if room is already created.
        roomInfoFirebase.get().child(roomKey).keepSynced(true);
        roomInfoFirebase.get().child(roomKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeEventListener(this);
                        isRoomAlreadyCreated(dataSnapshot, roomKey);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("FIREBASE ERROR", firebaseError.getMessage());
                    }
                });
    }

    private void isRoomAlreadyCreated(DataSnapshot dataSnapshot, String roomKey) {
        if (dataSnapshot.exists()) {
            Room room = dataSnapshot.getValue(Room.class);
            room.setRoomId(dataSnapshot.getKey());
            // room is already created
            roomCreated(room);
        } else {
            beginCreatingPrivateRoom(roomKey);
        }
    }

    private void beginCreatingPrivateRoom(final String roomKey) {
        final Room room = new Room(RoomUtil.PRIVATE_TYPE);
        room.setRoomId(roomKey);
        room.setLatestMessageTime(room.getCreated());

        ArrayMap<String, Object> map = new ArrayMap<>(2);
        FirebaseMapUtil.mapRoom(map, room, roomKey);
        FirebaseMapUtil.mapUserRoom(map, uid, roomKey, room.getCreated());

        rootFirebase.get().updateChildren(map, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    // TODO handle error
                    return;
                }
                // set room id back after it is stored in firebase.
                room.setRoomId(roomKey);
                roomCreated(room);
            }
        });
    }

    private void roomCreated(Room room) {
        if (!useEventBus) {
            if (onPrivateRoomRequestListener != null) {
                onPrivateRoomRequestListener.onPrivateRoomCreated(room);
            } else {
                this.room = room;
            }
        } else {
            EventBus.getDefault().post(new EventBusNewPrivateRoomMessage(room));
        }
    }

    public interface OnPrivateRoomRequestListener {

        void onCreatePrivateRoomFragmentCreated();
        void onPrivateRoomCreated(Room room);

    }
}
