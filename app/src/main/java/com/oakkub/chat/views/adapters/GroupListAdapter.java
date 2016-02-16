package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SimpleInfoHolder;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class GroupListAdapter extends RecyclerViewAdapter<Room> {

    private OnAdapterItemClick onAdapterItemClick;

    public GroupListAdapter(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View simpleInfoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_info_list, parent, false);
        return new SimpleInfoHolder(simpleInfoView, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Room room = getItem(position);
        SimpleInfoHolder simpleInfoHolder = (SimpleInfoHolder) holder;

        simpleInfoHolder.nameTextView.setText(room.getName());
        simpleInfoHolder.profileImageTextView.setImageURI(Uri.parse(room.getImagePath()));
    }

}
