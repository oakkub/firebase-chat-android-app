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
public class FriendImageMessageHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.friend_message_image_profile_image_view)
    public SimpleDraweeView profileImage;

    @Bind(R.id.friend_message_image_imageview)
    public SimpleDraweeView messageImage;

    @Bind(R.id.message_time_text_view)
    public TextView messageTimeTextView;

    public FriendImageMessageHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
