package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class SimpleInfoHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.simpleInfoProfileImageView)
    public ImageView profileImageTextView;

    @Bind(R.id.simpleInfoTextView)
    public TextView nameTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public SimpleInfoHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.simpleInfoRoot)
    public void onClick() {
        onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
    }
}
