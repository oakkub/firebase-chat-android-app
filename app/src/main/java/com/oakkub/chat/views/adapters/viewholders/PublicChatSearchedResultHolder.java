package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 2/4/2016.
 */
public class PublicChatSearchedResultHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.public_room_imageview)
    public SimpleDraweeView roomImage;

    @BindView(R.id.public_room_name_textview)
    public TextView nameTextView;

    @BindView(R.id.public_room_type_textview)
    public TextView typeTextView;

    @BindView(R.id.public_room_latest_time_textview)
    public TextView latestMessageTimeTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public PublicChatSearchedResultHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.public_room_root)
    public void onPublicRoomClick() {
        if (onAdapterItemClick != null) {
            onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
        }
    }

}
