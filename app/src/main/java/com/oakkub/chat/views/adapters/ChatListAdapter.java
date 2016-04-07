package com.oakkub.chat.views.adapters;

import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.FrescoUtil;
import com.oakkub.chat.utils.MessageUtil;
import com.oakkub.chat.utils.TimeUtil;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.EmptyHolder;
import com.oakkub.chat.views.adapters.viewholders.FriendImageMessageHolder;
import com.oakkub.chat.views.adapters.viewholders.FriendMessageHolder;
import com.oakkub.chat.views.adapters.viewholders.MyImageMessageHolder;
import com.oakkub.chat.views.adapters.viewholders.MyMessageHolder;
import com.oakkub.chat.views.adapters.viewholders.SystemMessageDivider;

public class ChatListAdapter extends RecyclerViewAdapter<Message> {

    public static final int FRIEND_MESSAGE_TYPE = 0;
    public static final int MY_MESSAGE_TYPE = 1;
    public static final int FRIEND_IMAGE_TYPE = 2;
    public static final int MY_IMAGE_TYPE = 3;
    public static final int SYSTEM_MESSAGE_TYPE = 4;
    public static final int SYSTEM_TIME_DIVIDER_TYPE = 5;
    public static final int UN_SPECIFIED_ERROR_TYPE = 6;

    private String uid;
    private boolean isPrivateRoom;
    private SparseArray<UserInfo> friendInfoList;
    private OnAdapterItemClick onAdapterItemClick;

    public ChatListAdapter(String uid, SparseArray<UserInfo> friendInfoList, boolean isPrivateRoom, OnAdapterItemClick onAdapterItemClick) {
        this.uid = uid;
        this.friendInfoList = friendInfoList;
        this.isPrivateRoom = isPrivateRoom;
        this.onAdapterItemClick = onAdapterItemClick;
    }

    public void addMember(UserInfo userInfo) {
        friendInfoList.put(userInfo.hashCode(), userInfo);
    }

    public void removeMember(UserInfo userInfo) {
        friendInfoList.remove(userInfo.hashCode());
    }

    @Override
    public int getItemViewType(int position) {
        Message message = items.get(position);

        if (message != null) {
            String sentBy = message.getSentBy();
            String imagePath = message.getImagePath();
            String messageText = message.getMessage();

            if (sentBy == null) {
                return UN_SPECIFIED_ERROR_TYPE;
            } else if (sentBy.equals(FirebaseUtil.SYSTEM)) {
                return messageText == null ? SYSTEM_TIME_DIVIDER_TYPE : SYSTEM_MESSAGE_TYPE;
            } else if (sentBy.equals(uid)) {
                if (imagePath != null) return MY_IMAGE_TYPE;
                return MY_MESSAGE_TYPE;
            } else if (!sentBy.equals(uid)) {
                if (imagePath != null) return FRIEND_IMAGE_TYPE;
                else return FRIEND_MESSAGE_TYPE;
            }
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
                return new FriendMessageHolder(view, onAdapterItemClick);

            case MY_MESSAGE_TYPE:

                view = inflateLayout(parent, R.layout.my_message_list);
                return new MyMessageHolder(view, onAdapterItemClick);

            case FRIEND_IMAGE_TYPE:

                view = inflateLayout(parent, R.layout.friend_message_image_list);
                return new FriendImageMessageHolder(view, onAdapterItemClick);

            case MY_IMAGE_TYPE:

                view = inflateLayout(parent, R.layout.my_message_image_list);
                return new MyImageMessageHolder(view, onAdapterItemClick);

            case SYSTEM_MESSAGE_TYPE:
            case SYSTEM_TIME_DIVIDER_TYPE:

                view = inflateLayout(parent, R.layout.system_message_list);
                return new SystemMessageDivider(view);

            case LOAD_MORE_TYPE:

                return getProgressBarHolder(parent);

            case UN_SPECIFIED_ERROR_TYPE:
                view = inflateLayout(parent, R.layout.empty_list);
                return new EmptyHolder(view);

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
            onBindMyMessageHolder((MyMessageHolder) holder, message, position);
        } else if (holder instanceof FriendMessageHolder) {
            onBindFriendMessageHolder((FriendMessageHolder) holder, message);
        } else if (holder instanceof MyImageMessageHolder) {
            onBindMyImageMessageHolder((MyImageMessageHolder) holder, message, position);
        } else if (holder instanceof FriendImageMessageHolder) {
            onBindFriendImageMessageHolder((FriendImageMessageHolder) holder, message);
        } else if (holder instanceof SystemMessageDivider){
            onBindSystemMessageHolder((SystemMessageDivider) holder, message);
        }
    }

    private void onBindSystemMessageHolder(SystemMessageDivider holder, Message message) {
        String readableDate = TimeUtil.getReadableDate(message.getSentWhen());
        // default of system message is showing date time
        String systemMessage = message.getMessage() == null ?
                readableDate : message.getMessage();

        if (message.getLanguageRes() != null) {
            switch (message.getLanguageRes()) {
                case MessageUtil.LAST_ADMIN_LEAVED:
                    // message = leaveId:promotedId
                    systemMessage = MessageUtil.lastAdminLeavedRes(message, friendInfoList, uid);
                    break;
                case MessageUtil.REMOVED_MEMBER:
                    systemMessage = getStringRes(R.string.n_removed_n_from_room, message);
                    break;
                case MessageUtil.INVITE_MEMBER:
                    systemMessage = getStringRes(R.string.n_invited_n_to_room, message);
                    break;
                case MessageUtil.PROMOTED_MEMBER:
                    systemMessage = getStringRes(R.string.n_promoted_n_to_be_admin, message);
                    break;
                case MessageUtil.DEMOTED_ADMIN:
                    systemMessage = getStringRes(R.string.n_demoted_n_from_admin_to_member, message);
                    break;
            }
        }

        holder.systemMessageTextView.setText(systemMessage);
    }

    private String getStringRes(int stringRes, Message message) {
        return MessageUtil.getStringResTwoParam(stringRes,
                message, friendInfoList, uid);
    }

    private void onBindMyMessageHolder(MyMessageHolder holder, Message message, int position) {
        holder.message.setText(message.getMessage());
        holder.messageTimeTextView.setText(TimeUtil.getOnlyTime(message.getSentWhen()));
        showReadItem(message, holder.isReadImageView, holder.totalReadTextView, position);

        int backgroundColor =
                ContextCompat.getColor(holder.itemView.getContext(),
                        message.getIsSuccessfullySent() != null ?
                        R.color.colorPrimary : R.color.gray);
        holder.message.setBackgroundColor(backgroundColor);
    }

    private void onBindMyImageMessageHolder(MyImageMessageHolder holder, final Message message, int position) {
        showReadItem(message, holder.isReadImageView, holder.totalReadTextView, position);
        holder.messageTimeTextView.setText(TimeUtil.getOnlyTime(message.getSentWhen()));

        SimpleDraweeView imageMessage = holder.imageMessage;
        setResizeImage(imageMessage, message);
    }

    private void onBindFriendMessageHolder(FriendMessageHolder holder, Message message) {
        holder.message.setText(message.getMessage());
        holder.messageTimeTextView.setText(TimeUtil.getOnlyTime(message.getSentWhen()));
        holder.friendProfileImage.setVisibility(View.VISIBLE);
        holder.friendProfileImage.setImageURI(
                Uri.parse(friendInfoList.get(message.getSentBy().hashCode()).getProfileImageURL()));
    }

    private void onBindFriendImageMessageHolder(FriendImageMessageHolder holder, Message message) {
        holder.messageTimeTextView.setText(TimeUtil.getOnlyTime(message.getSentWhen()));
        holder.profileImage.setImageURI(
                Uri.parse(friendInfoList.get(message.getSentBy().hashCode()).getProfileImageURL()));
        SimpleDraweeView messageImage = holder.messageImage;
        setResizeImage(messageImage, message);
    }

    private boolean shouldShowReadImage(Message message, int position) {
        Message newerMessage = getItem(position - 1);
        int visibility;

        if (newerMessage == null) {
            visibility = message.getReadTotal() > 0 ? View.VISIBLE : View.INVISIBLE;
        } else {
            // if current message is read, and the newer message is not read yet,
            // we will show visibility icon to the current message
            visibility = message.getReadTotal() > 0 && newerMessage.getReadTotal() == 0 ?
                    View.VISIBLE : View.INVISIBLE;
        }

        return visibility == View.VISIBLE;
    }

    private void showReadItem(Message message, SimpleDraweeView imageView, TextView textView, int position) {
//        int visibility = shouldShowReadImage(message, position) ? View.VISIBLE : View.INVISIBLE;
        imageView.setVisibility(message.getReadTotal() > 0 ? View.VISIBLE : View.INVISIBLE);
        if (!isPrivateRoom) {
            textView.setVisibility(message.getReadTotal() > 0 ? View.VISIBLE : View.INVISIBLE);
            textView.setText(String.valueOf(message.getReadTotal()));
        }
    }

    private void setResizeImage(final SimpleDraweeView image, final Message message) {
        setMatchedRatioViewSize(image, message.getRatio());
        image.setImageURI(Uri.parse(message.getImagePath()));
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int width = image.getWidth();
                int height = image.getHeight();

                DraweeController resizeController = FrescoUtil
                        .getResizeController(width, height, Uri.parse(message.getImagePath()),
                                image.getController());

                image.setController(resizeController);
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    private void setMatchedRatioViewSize(SimpleDraweeView image, String ratio) {
        if (ratio == null) return;
        Resources res = image.getContext().getResources();

        int dimenWidth;
        int dimenHeight;

        switch (ratio) {
            case "9:16":
                dimenWidth = R.dimen.image_port_width;
                dimenHeight = R.dimen.image_port_height;
                break;
            case "16:9":
                dimenWidth = R.dimen.image_land_width;
                dimenHeight = R.dimen.image_land_height;
                break;
            case "5:4":
                dimenWidth = R.dimen.spacing_giant1;
                dimenHeight = R.dimen.spacing_super_large3;
                break;
            case "4:3":
                dimenWidth = R.dimen.spacing_giant1;
                dimenHeight = R.dimen.spacing_super_large2;
                break;
            default:
                dimenWidth = R.dimen.spacing_giant1;
                dimenHeight = R.dimen.spacing_giant1;
        }

        int viewWidth = res.getDimensionPixelOffset(dimenWidth);
        int viewHeight = res.getDimensionPixelOffset(dimenHeight);

        ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
        layoutParams.width = viewWidth;
        layoutParams.height = viewHeight;

        image.setLayoutParams(layoutParams);
    }

}
