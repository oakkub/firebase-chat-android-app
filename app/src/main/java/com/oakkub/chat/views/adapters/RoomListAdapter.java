package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.TimeUtil;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.RoomHolder;

/**
 * Created by OaKKuB on 11/15/2015.
 */
public class RoomListAdapter extends RecyclerViewAdapter<Room>  {

    private static final int ROOM_TYPE = 0;

    private OnAdapterItemClick onAdapterItemClick;
    private String myId;

    public RoomListAdapter(OnAdapterItemClick onAdapterItemClick, String myId) {
        this.onAdapterItemClick = onAdapterItemClick;
        this.myId = myId;
    }

    @Override
    public int getItemViewType(int position) {
        Room room = getItem(position);

        if (room != null) {
            return ROOM_TYPE;
        } else {
            return LOAD_MORE_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case ROOM_TYPE:
                View view = inflateLayout(parent, R.layout.room_list);
                return new RoomHolder(view, onAdapterItemClick);
            case LOAD_MORE_TYPE:
                return getProgressBarHolder(parent);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Room room = getItem(position);

        if (room != null) {
            RoomHolder roomHolder = (RoomHolder) holder;

            setRoomMessage(roomHolder, room);

            long time = room.getLatestMessageTime();
            if (time > 0) {
                roomHolder.latestMessageTime.setText(
                        TimeUtil.readableTime(roomHolder.itemView.getContext(),
                                room.getLatestMessageTime()));
            }

            roomHolder.roomImage.setImageURI(Uri.parse(room.getImagePath()));
            roomHolder.roomName.setText(room.getName());
        }
    }

    private void setRoomMessage(RoomHolder holder, Room room) {
        String latestMessage = room.getLatestMessage();
        if (latestMessage == null) return;

        String message = room.getLatestMessageUser().equals(myId) ?
                holder.itemView.getContext().getString(R.string.latest_message_by,
                        room.getLatestMessage())
                : latestMessage;

        holder.latestMessage.setText(message);
    }
}
