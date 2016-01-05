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

    private OnAdapterItemClick onAdapterItemClick;

    public RoomListAdapter(OnAdapterItemClick onAdapterItemClick) {
        super();
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflateLayout(parent, R.layout.room_list);
        return new RoomHolder(view, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Room room = getItem(position);
        RoomHolder roomHolder = (RoomHolder) holder;

        roomHolder.roomImage.setImageURI(Uri.parse(room.getImagePath()));
        roomHolder.latestMessage.setText(room.getLatestMessage());
        roomHolder.latestMessageTime.setText(
                TimeUtil.timeInMillisToDate("dd/MM/yyyy", room.getLatestMessageTime(), true));
        roomHolder.roomName.setText(room.getName());
    }

}
