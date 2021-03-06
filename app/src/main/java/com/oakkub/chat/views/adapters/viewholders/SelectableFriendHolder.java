package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by OaKKuB on 12/25/2015.
 */
public class SelectableFriendHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.selectable_root)
    public LinearLayout root;

    @BindView(R.id.selectable_profile_image)
    public SimpleDraweeView profileImage;

    @BindView(R.id.selectable_friend_name_textview)
    public TextView friendNameTextView;

    @BindView(R.id.selectable_friend_checkbox)
    public CheckBox friendCheckBox;

    private OnAdapterItemClick onAdapterItemClick;

    public SelectableFriendHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        if (onAdapterItemClick == null) {
            root.setBackgroundResource(0);
        }

        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.selectable_root)
    public void onClick() {
        if (onAdapterItemClick != null) {
            onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
        }
    }

    @OnLongClick(R.id.selectable_root)
    public boolean onLongClick() {
        return onAdapterItemClick != null && onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
    }

}
