package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class FriendListAdapter extends RecyclerViewAdapter<UserInfo> {

    private static final int FRIEND_TYPE = 0;
    private static final int ADD_FRIEND_TYPE = 1;

    private OnClickListener onClickListener;
    private boolean loadMore;
    private boolean noInternet;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        final UserInfo userInfo = adapterList.get(position);

        if (userInfo == null) {

            if (noInternet) {
                noInternet = false;
                return NO_INTERNET_TYPE;
            } else {
                loadMore = false;
                return LOAD_MORE_TYPE;
            }

        } else {

            final int type = userInfo.getType();

            switch (type) {
                case UserInfo.FRIEND:
                    return FRIEND_TYPE;
                case UserInfo.ADD_FRIEND:
                    return ADD_FRIEND_TYPE;
                default:
                    return LOAD_MORE_TYPE;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {

            case FRIEND_TYPE:

                itemView = layoutInflater.inflate(R.layout.friends_list, parent, false);
                return new FriendHolder(itemView);

            case ADD_FRIEND_TYPE:

                itemView = layoutInflater.inflate(R.layout.add_friends_list, parent, false);
                return new AddFriendHolder(itemView);

            case LOAD_MORE_TYPE:

                itemView = layoutInflater.inflate(R.layout.progress_bar, parent, false);
                return new ProgressBarHolder(itemView);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FriendHolder ||
            holder instanceof AddFriendHolder) {

            final UserInfo userInfo = adapterList.get(position);

            if (holder instanceof FriendHolder) {
                final FriendHolder friendHolder = (FriendHolder) holder;

                final String profileImageURI = userInfo.getProfileImageURL();
                friendHolder.friendProfileImage.setImageURI(Uri.parse(profileImageURI));

                friendHolder.friendName.setText(userInfo.getDisplayName());

            } else {
                final AddFriendHolder addFriendHolder = (AddFriendHolder) holder;

                final String profileImageURI = userInfo.getProfileImageURL();
                addFriendHolder.addFriendProfileImage.setImageURI(Uri.parse(profileImageURI));

                addFriendHolder.addFriendName.setText(userInfo.getDisplayName());
            }

        }

    }

    class FriendHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.friendProfileRoot)
        LinearLayout friendProfileRoot;

        @Bind(R.id.friendProfileImageView)
        SimpleDraweeView friendProfileImage;

        @Bind(R.id.friendNameTextView)
        TextView friendName;

        public FriendHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.friendProfileRoot)
        public void onClick(View view) {
            onClickListener.onClick(view, adapterList.get(getAdapterPosition()), getAdapterPosition());
        }
    }

    class AddFriendHolder extends RecyclerView.ViewHolder {

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

        @OnClick(R.id.addFriendProfileRoot)
        public void onClick(View view) {
            onClickListener.onClick(view, adapterList.get(getAdapterPosition()), getAdapterPosition());
        }

    }

    static class ProgressBarHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.progressBar)
        ProgressBar loading;

        public ProgressBarHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnClickListener {
        void onClick(View view, UserInfo userInfo, int position);
    }

}
