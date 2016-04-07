package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class MyMessageHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.my_message_text_view)
    public TextView message;

    @Bind(R.id.visibilityImageView)
    public SimpleDraweeView isReadImageView;

    @Bind(R.id.simple_textview)
    public TextView totalReadTextView;

    @Bind(R.id.message_time_text_view)
    public TextView messageTimeTextView;

    private OnAdapterItemClick onAdapterItemClick;

    public MyMessageHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @OnLongClick(R.id.my_message_container)
    public boolean onContainerClick() {
        if (onAdapterItemClick == null) return false;

        onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
        return true;
    }

}
