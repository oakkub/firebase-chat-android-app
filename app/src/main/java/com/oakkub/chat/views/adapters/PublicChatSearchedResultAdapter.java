package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.TimeUtil;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.PublicChatSearchedResultHolder;

/**
 * Created by OaKKuB on 2/4/2016.
 */
public class PublicChatSearchedResultAdapter extends RecyclerViewAdapter<Room> {

    private OnAdapterItemClick onAdapterItemClick;

    public PublicChatSearchedResultAdapter(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.public_chat_searched_result_list, parent, false);
        return new PublicChatSearchedResultHolder(itemView, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Room room = getItem(position);
        PublicChatSearchedResultHolder publicHolder = (PublicChatSearchedResultHolder) holder;

        View itemView = publicHolder.itemView;
//        String roomTagType = itemView.getContext().getString(R.string.type_colon) + room.getTag();

        publicHolder.roomImage.setImageURI(Uri.parse(room.getImagePath()));
        publicHolder.nameTextView.setText(room.getName());
        publicHolder.typeTextView.setText(room.getTag());
        publicHolder.latestMessageTimeTextView.setText(
                TimeUtil.readableTime(itemView.getContext(),
                room.getLatestMessageTime()));
    }
}
