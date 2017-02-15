package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

/**
 * Created by OaKKuB on 1/19/2016.
 */
public class FriendImageMessageHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.friend_message_image_profile_image_view)
    public SimpleDraweeView profileImage;

    @BindView(R.id.friend_message_image_imageview)
    public SimpleDraweeView messageImage;

    @BindView(R.id.message_time_text_view)
    public TextView messageTimeTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public FriendImageMessageHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnLongClick(R.id.friend_image_message_container)
    public boolean onContainerLongClick() {
        onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
        return true;
    }
}
