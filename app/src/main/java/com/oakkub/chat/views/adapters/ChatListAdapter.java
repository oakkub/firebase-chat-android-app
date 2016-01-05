package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.views.adapters.viewholders.FriendMessageHolder;
import com.oakkub.chat.views.adapters.viewholders.MyMessageHolder;

public class ChatListAdapter extends RecyclerViewAdapter<Message> {

    public static final int FRIEND_MESSAGE_TYPE = 0;
    public static final int MY_MESSAGE_TYPE = 1;

    private String myId;
    private SparseArray<String> friendProfileImageList;
    private SparseArray<String> friendDisplayNameList;

    public ChatListAdapter(String myId, SparseArray<String> friendProfileImageList) {
        // private room
        this.myId = myId;
        this.friendProfileImageList = friendProfileImageList;
    }

    public ChatListAdapter(String myId, SparseArray<String> friendProfileImageList, SparseArray<String> friendDisplayNameList) {
        // group room
        this(myId, friendProfileImageList);
        this.friendDisplayNameList = friendDisplayNameList;
    }

    public void newMember(String newMemberId, String newMemberProfileImage, String newMemberDisplayName) {
        friendProfileImageList.put(newMemberId.hashCode(), newMemberProfileImage);
        friendDisplayNameList.put(newMemberId.hashCode(), newMemberDisplayName);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = items.get(position);

        if (message != null) {
            if (message.getSentBy().equals(myId)) return MY_MESSAGE_TYPE;
            else return FRIEND_MESSAGE_TYPE;
        } else {
            if (loadMore) return LOAD_MORE_TYPE;
        }

        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case FRIEND_MESSAGE_TYPE:

                View view = inflateLayout(parent, R.layout.friend_message_list);
                return new FriendMessageHolder(view);

            case MY_MESSAGE_TYPE:

                view = inflateLayout(parent, R.layout.my_message_list);
                return new MyMessageHolder(view);

            case LOAD_MORE_TYPE:

                return getProgressBarHolder(parent);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = items.get(position);
        if (message != null) {
            bindHolder(message, holder, position);
        }
    }

    private void bindHolder(Message message, RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyMessageHolder) {

            onBindMyMessageHolder((MyMessageHolder) holder, message);
        } else if (holder instanceof FriendMessageHolder) {

            onBindFriendMessageHolder((FriendMessageHolder) holder, message);
        }
    }

    private void onBindMyMessageHolder(MyMessageHolder holder, Message message) {
        holder.message.setText(message.getMessage());
    }

    private void onBindFriendMessageHolder(FriendMessageHolder holder, Message message) {
        holder.message.setText(message.getMessage());

        if (message.isShowImage()) {
            holder.friendProfileImage.setVisibility(View.VISIBLE);
            setImage(holder, message);
        } else {
            holder.friendProfileImage.setVisibility(View.INVISIBLE);
        }
    }

    private void setImage(FriendMessageHolder holder, Message message) {
        holder.friendProfileImage.setVisibility(View.VISIBLE);
        holder.friendProfileImage.setImageURI(
                Uri.parse(friendProfileImageList.get(message.getSentBy().hashCode())));
    }

}
