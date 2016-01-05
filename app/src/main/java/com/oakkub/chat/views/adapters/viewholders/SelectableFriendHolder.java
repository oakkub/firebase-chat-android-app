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
 * Created by OaKKuB on 12/25/2015.
 */
public class SelectableFriendHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.selectable_profile_image)
    public SimpleDraweeView profileImage;

    @Bind(R.id.selectable_friend_name_textview)
    public TextView friendNameTextView;

    @Bind(R.id.selectable_indicator_image)
    public SimpleDraweeView selectionIndicatorImage;

    private OnAdapterItemClick onAdapterItemClick;

    public SelectableFriendHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.selectable_root)
    public void onClick() {
        onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
    }

    @OnLongClick(R.id.selectable_root)
    public boolean onLongClick() {
        onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());

        return false;
    }

}
