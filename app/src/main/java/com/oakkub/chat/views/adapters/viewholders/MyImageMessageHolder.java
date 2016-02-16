package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 1/19/2016.
 */
public class MyImageMessageHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.my_image_message_imageview)
    public SimpleDraweeView imageMessage;

    @Bind(R.id.visibilityImageView)
    public SimpleDraweeView isReadImageView;

    @Bind(R.id.simple_textview)
    public TextView totalReadTextView;

    @Bind(R.id.message_time_text_view)
    public TextView messageTimeTextView;

    public MyImageMessageHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
