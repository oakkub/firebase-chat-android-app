package com.oakkub.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.models.Message;
import com.oakkub.chat.models.UserInfo;

/**
 * Created by OaKKuB on 2/15/2016.
 */
public class MessageUtil {

    public static final String LAST_ADMIN_LEAVED = "0";
    public static final String REMOVED_MEMBER = "1";
    public static final String INVITE_MEMBER = "2";
    public static final String LEAVE_CHAT = "3";
    public static final String PROMOTED_MEMBER = "4";
    public static final String DEMOTED_ADMIN = "5";

    public static String lastAdminLeavedRes(Message messageRes, SparseArray<UserInfo> friendInfoList, String myId) {
        Context context = Contextor.getInstance().getContext();

        String leaveMemberKey = messageRes.getMessage().split("/")[0];
        String promotedAdminKey = messageRes.getMessage().split("/")[1];

        if (friendInfoList.get(promotedAdminKey.hashCode()) != null) {
            String leaveMember = isMe(myId, leaveMemberKey, friendInfoList);
            String promotedMember = isMe(myId, promotedAdminKey, friendInfoList);
            String you = context.getString(R.string.you);

            return context.getString(leaveMember.equals(you) ?
                    R.string.n_leave_chat_n_is_now_admin :
                    R.string.n_admin_leave_chat_n_is_now_admin,
                    leaveMember, promotedMember);
        }

        return "";
    }

    public static String getStringResOneParam(int stringRes, Message messageRes, String myId) {
        Context context = Contextor.getInstance().getContext();
        SharedPreferences prefs = AppController.getComponent(context).sharedPreferences();
        String displayName = prefs.getString(UserInfoUtil.DISPLAY_NAME, "");

        return context.getString(stringRes, myId.equals(messageRes.getSentBy()) ?
                context.getString(R.string.you) : displayName.split(" ")[0]);
    }

    public static String getStringResTwoParam(int stringRes, Message messageRes, SparseArray<UserInfo> friendInfoList, String myId) {
        Context context = Contextor.getInstance().getContext();

        String[] removedMemberKeys = messageRes.getMessage().split("/");
        StringBuilder displayNamesBuilder = new StringBuilder(removedMemberKeys.length * 12);

        for (int i = friendInfoList.get(removedMemberKeys[0].hashCode()) != null ? 1 : 0,
             size = removedMemberKeys.length; i < size; i++) {

            UserInfo removedMemberInfo = friendInfoList.get(removedMemberKeys[i].hashCode());
            if (removedMemberInfo != null) {
                displayNamesBuilder.append(removedMemberInfo.getKey().equals(myId) ?
                        context.getString(R.string.you).toLowerCase() : removedMemberInfo.getFirstDisplayName());
                displayNamesBuilder.append(i == size - 1 ? "" : ", ");
            }
        }
        return context.getString(stringRes,
                isMe(myId, removedMemberKeys[0], friendInfoList), displayNamesBuilder.toString());
    }

    public static String isMe(String myId, String comparedId, SparseArray<UserInfo> friendInfoList) {
        Context context = Contextor.getInstance().getContext();

        UserInfo comparedInfo = friendInfoList.get(comparedId.hashCode());
        if (comparedInfo != null) {
            return comparedId.equals(myId) ?
                    context.getString(R.string.you) : comparedInfo.getFirstDisplayName();
        }

        return "";
    }
}
