package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.Message;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerViewAdapter<Message, RecyclerView.ViewHolder> {

    public static final int FRIEND_MESSAGE_TYPE = 0;
    public static final int MY_MESSAGE_TYPE = 1;

    private static int[] LAYOUT = {
            R.layout.friend_message_list,
            R.layout.my_message_list
    };

    private final String myId;
    private final Map<String, String> friendProfileImageList;

    public ChatListAdapter(String myId, Map<String, String> friendProfileImageList) {
        this.myId = myId;
        this.friendProfileImageList = friendProfileImageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = items.get(position);

        if (message.getSentBy().equals(myId)) {
            return MY_MESSAGE_TYPE;
        } else {
            return FRIEND_MESSAGE_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case FRIEND_MESSAGE_TYPE:

                View view = inflateLayout(parent, LAYOUT[FRIEND_MESSAGE_TYPE]);
                return new FriendMessageHolder(view);

            case MY_MESSAGE_TYPE:

                view = inflateLayout(parent, LAYOUT[MY_MESSAGE_TYPE]);
                return new MyMessageHolder(view);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Message message = items.get(position);
        bindHolder(message, holder, position);
    }

    private void bindHolder(Message message, RecyclerView.ViewHolder holder, int position) {
        if (message != null && holder != null) {

            if (holder instanceof MyMessageHolder) {

                onBindMyMessageHolder((MyMessageHolder) holder, message);
            } else if (holder instanceof FriendMessageHolder) {

                onBindFriendMessageHolder((FriendMessageHolder) holder, message, position);
            }
        }
    }

    private void onBindMyMessageHolder(MyMessageHolder holder, Message message) {
        holder.message.setText(message.getMessage());
    }

    private void onBindFriendMessageHolder(FriendMessageHolder holder, Message message, int position) {
        final Message previousMessage = getItem(position == 0 ? 0 : position - 1);

        if (!previousMessage.getSentBy().equals(message.getSentBy())) {
            holder.friendProfileImage.setVisibility(View.VISIBLE);

            holder.friendProfileImage.setImageURI(
                    Uri.parse(friendProfileImageList.get(message.getSentBy())));
        } else {
            holder.friendProfileImage.setVisibility(View.INVISIBLE);
        }

        holder.message.setText(message.getMessage());
    }

    public static class FriendMessageHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.friend_message_profile_image_view)
        SimpleDraweeView friendProfileImage;

        @Bind(R.id.friend_message_text_view)
        TextView message;

        public FriendMessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public static class MyMessageHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.my_message_text_view)
        TextView message;

        public MyMessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
