package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 11/15/2015.
 */
public class RoomListAdapter extends RecyclerViewAdapter<Room, RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflateLayout(parent, R.layout.room_list);
        return new RoomHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Room room = getItem(position);
        RoomHolder roomHolder = (RoomHolder) holder;

        roomHolder.roomImage.setImageURI(Uri.parse(room.getImagePath()));
        roomHolder.latestMessage.setText(room.getLatestMessage());
        roomHolder.latestMessageTime.setText(String.valueOf(room.getLatestMessageTime()));
        roomHolder.roomName.setText(room.getName());
    }

    public static class RoomHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.room_profile_image_view)
        SimpleDraweeView roomImage;

        @Bind(R.id.room_name_text_view)
        TextView roomName;

        @Bind(R.id.room_latest_message_text_view)
        TextView latestMessage;

        @Bind(R.id.room_latest_message_time_text_view)
        TextView latestMessageTime;

        public RoomHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
