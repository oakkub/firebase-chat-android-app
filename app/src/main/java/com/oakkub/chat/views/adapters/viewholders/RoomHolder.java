package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by OaKKuB on 12/4/2015.
 */
public class RoomHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.room_profile_image_view)
    public SimpleDraweeView roomImage;

    @Bind(R.id.room_name_text_view)
    public TextView roomName;

    @Bind(R.id.room_latest_message_text_view)
    public TextView latestMessage;

    @Bind(R.id.room_latest_time_text_view)
    public TextView latestMessageTime;

    private OnAdapterItemClick onAdapterItemClick;

    public RoomHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.room_root)
    public void onRoomClick() {
        if (onAdapterItemClick == null) return;
        onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
    }

    @OnLongClick(R.id.room_root)
    public boolean onRoomLongClick() {
        if (onAdapterItemClick == null) {
            return false;
        } else {
            onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
            return true;
        }
    }

}