package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.TransitionUtil;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.ProgressBarHolder;
import com.oakkub.chat.views.adapters.viewholders.SimpleInfoHolder;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class FriendListAdapter extends RecyclerViewAdapter<UserInfo> {

    private static final int FRIEND_TYPE = 0;
    private static final int ADD_FRIEND_TYPE = 1;

    private OnAdapterItemClick onAdapterItemClick;

    public FriendListAdapter(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public int getItemViewType(int position) {

        final UserInfo userInfo = items.get(position);

        if (userInfo == null) {

            if (noInternet) {
                noInternet = false;
                return NO_INTERNET_TYPE;
            } else {
                loadMore = false;
                return LOAD_MORE_TYPE;
            }

        } else {
            return FRIEND_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {

            case FRIEND_TYPE:

                itemView = layoutInflater.inflate(R.layout.simple_info_list, parent, false);
                return new SimpleInfoHolder(itemView, onAdapterItemClick);

            case LOAD_MORE_TYPE:

                itemView = layoutInflater.inflate(R.layout.progress_bar, parent, false);
                return new ProgressBarHolder(itemView);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SimpleInfoHolder simpleInfoHolder = (SimpleInfoHolder) holder;

        UserInfo userInfo = getItem(position);

        simpleInfoHolder.profileImageTextView.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
        simpleInfoHolder.nameTextView.setText(userInfo.getDisplayName());

        ViewCompat.setTransitionName(simpleInfoHolder.profileImageTextView,
                TransitionUtil.SHARED_TRANSITION + position);
    }
}