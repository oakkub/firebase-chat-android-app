package com.oakkub.chat.views.adapters.viewholders;

import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.RecyclerViewAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class AddFriendListAdapter extends RecyclerViewAdapter<UserInfo> {

    private OnAdapterItemClick onAdapterItemClick;

    public AddFriendListAdapter(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friends_list, parent, false);
        return new AddFriendHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AddFriendHolder addFriendHolder = (AddFriendHolder) holder;
        UserInfo userInfo = items.get(position);

        addFriendHolder.addFriendProfileImage.setImageURI(Uri.parse(userInfo.getProfileImageURL()));

        addFriendHolder.addFriendName.setText(userInfo.getDisplayName());
    }

    public class AddFriendHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.addFriendProfileRoot)
        CardView addFriendProfileRoot;

        @Bind(R.id.addFriendProfileImageView)
        SimpleDraweeView addFriendProfileImage;

        @Bind(R.id.addFriendNameTextView)
        TextView addFriendName;

        @Bind(R.id.addFriendButton)
        Button addFriendButton;

        public AddFriendHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.addFriendButton)
        public void onAddFriendClick(View view) {
            onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
        }

    }

}
