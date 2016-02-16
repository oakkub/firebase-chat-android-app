package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.viewholders.ImageViewHolder;

/**
 * Created by OaKKuB on 1/17/2016.
 */
public class UserImageAdapter extends RecyclerViewAdapter<UserInfo> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.partial_circle_image, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserInfo userInfo = getItem(position);

        ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        imageViewHolder.image.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
    }

    public int[] getTotalItemsPosition() {
        int size = getItemCount();
        int[] itemsPosition = new int[size];

        for (int i = 0; i < size; i++) {
            itemsPosition[i] = i;
        }

        return itemsPosition;
    }

}
