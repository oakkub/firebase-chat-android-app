package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SelectableFriendHolder;

/**
 * Created by OaKKuB on 12/25/2015.
 */
public class SelectableFriendAdapter extends RecyclerViewMultipleSelectionAdapter<UserInfo> {

    private OnAdapterItemClick onAdapterItemClick;

    public SelectableFriendAdapter(OnAdapterItemClick onAdapterItemClick) {
        super();
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_friend_list, parent, false);

        return new SelectableFriendHolder(view, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final UserInfo userInfo = getItem(position);
        final SelectableFriendHolder selectableFriendHolder = (SelectableFriendHolder) holder;

        selectableFriendHolder.profileImage.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
        selectableFriendHolder.friendNameTextView.setText(userInfo.getDisplayName());
        selectableFriendHolder.selectionIndicatorImage.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);

    }

}
