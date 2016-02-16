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
import com.oakkub.chat.models.EventBusNewPrivateRoomMessage;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.ArrayMapUtil;
import com.oakkub.chat.utils.FirebaseUtil;

import org.parceler.Parcels;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import de.greenrobot.event.EventBus;

/**
 * Created by OaKKuB on 1/10/2016.
 */
public class CreatePrivateRoomFragment extends BaseFragment {

    private static final String ARGS_FRIEND_INFO = "args:friendInfo";

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Lazy<Firebase> rootFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOMS_INFO)
    Lazy<Firebase> roomInfoFirebase;

    private String myId;
    private UserInfo friendInfo;

    private Room room;
    private OnPrivateRoomRequestListener onPrivateRoomRequestListener;
    private boolean useEventBus;

    public static CreatePrivateRoomFragment newInstance(UserInfo friendInfo, String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);
        args.putParcelable(ARGS_FRIEND_INFO, Parcels.wrap(friendInfo));

        CreatePrivateRoomFragment createPrivateRoomFragment = new CreatePrivateRoomFragment();
        createPrivateRoomFragment.setArguments(args);

        return createPrivateRoomFragment;
    }

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

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);
        friendInfo = Parcels.unwrap(args.getParcelable(ARGS_FRIEND_INFO));
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

    public void createPrivateRoom(String roomKey, UserInfo friendInfo) {
        this.friendInfo = friendInfo;
        createPrivateRoom(roomKey);
    }

    public void createPrivateRoom(final String roomKey) {
        // check if room is already created.
        roomInfoFirebase.get().child(roomKey).keepSynced(true);
        roomInfoFirebase.get().child(roomKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
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
        final Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_PRIVATE);
        room.setRoomId(roomKey);
        room.setLatestMessageTime(room.getCreated());

        ArrayMap<String, Object> map = new ArrayMap<>();
        ArrayMapUtil.mapRoom(map, room, room.getRoomId());
        ArrayMapUtil.mapUserRoom(map, myId, roomKey, room.getCreated());

        // room id back to null.
        // since we don't want it to be written in firebase
        room.setRoomId(null);
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
