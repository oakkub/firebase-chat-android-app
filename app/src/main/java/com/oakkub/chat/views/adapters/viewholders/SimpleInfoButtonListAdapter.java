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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class SimpleInfoButtonListAdapter extends RecyclerViewAdapter<UserInfo> {

    private static final int FRIEND_TYPE = 0;

    private OnAdapterItemClick onAdapterItemClick;
    private String buttonText;

    public SimpleInfoButtonListAdapter(OnAdapterItemClick onAdapterItemClick, String buttonText) {
        this.onAdapterItemClick = onAdapterItemClick;
        this.buttonText = buttonText;
    }

    @Override
    public int getItemViewType(int position) {
        UserInfo userInfo = getItem(position);

        if (userInfo != null) {
            return FRIEND_TYPE;
        } else if (loadMore){
            return LOAD_MORE_TYPE;
        }

        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FRIEND_TYPE:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friends_list, parent, false);
                return new SimpleInfoButtonHolder(itemView, buttonText);
            case LOAD_MORE_TYPE:
                return getProgressBarHolder(parent);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof SimpleInfoButtonHolder) {
            UserInfo userInfo = items.get(position);
            SimpleInfoButtonHolder simpleInfoButtonHolder = (SimpleInfoButtonHolder) holder;
            simpleInfoButtonHolder.bind(userInfo);
        }
    }

    public class SimpleInfoButtonHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.simpleInfoButtonContainer)
        CardView simpleInfoContainer;

        @BindView(R.id.simpleInfoButtonProfileImageView)
        SimpleDraweeView simpleInfoProfileImageView;

        @BindView(R.id.simpleInfoButtonTextView)
        TextView simpleInfoTextView;

        @BindView(R.id.simpleInfoButton)
        Button simpleInfoButton;

        public SimpleInfoButtonHolder(View itemView, String buttonText) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            simpleInfoButton.setText(buttonText);
        }

        public void bind(UserInfo userInfo) {
            simpleInfoProfileImageView.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
            simpleInfoTextView.setText(userInfo.getDisplayName());
        }

        @OnClick(R.id.simpleInfoButton)
        public void onAddFriendClick(View view) {
            onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
        }

    }

}
