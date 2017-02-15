package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 1/17/2016.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.circle_image)
    public SimpleDraweeView image;

    public ImageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
