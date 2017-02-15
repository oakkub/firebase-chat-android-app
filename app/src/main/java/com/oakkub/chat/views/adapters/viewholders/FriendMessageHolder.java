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
 * Created by OaKKuB on 12/2/2015.
 */
public class FriendMessageHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.friend_message_profile_image_view)
    public SimpleDraweeView friendProfileImage;

    @BindView(R.id.friend_message_text_view)
    public TextView message;

    @BindView(R.id.message_time_text_view)
    public TextView messageTimeTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public FriendMessageHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnLongClick(R.id.friend_message_container)
    public boolean onContainerLongClick() {
        onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
        return true;
    }

}
