package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 3/19/2016.
 */
public class PublicTypeHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.public_type_textview)
    public TextView publicTypeTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public PublicTypeHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnClick(R.id.public_type_textview)
    public void onClick() {
        onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
    }
}
