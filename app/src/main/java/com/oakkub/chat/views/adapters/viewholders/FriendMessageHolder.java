package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class FriendMessageHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.friend_message_profile_image_view)
    public SimpleDraweeView friendProfileImage;

    @Bind(R.id.friend_message_text_view)
    public TextView message;

    public FriendMessageHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
