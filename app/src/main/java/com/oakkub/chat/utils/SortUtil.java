package com.oakkub.chat.utils;

import com.oakkub.chat.models.UserInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by OaKKuB on 12/25/2015.
 */
public class SortUtil {

    public static void sortUserInfoAlphabetically(List<UserInfo> list) {
        Collections.sort(list, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo lhs, UserInfo rhs) {
                return lhs.getDisplayName().compareTo(rhs.getDisplayName());
            }
        });
    }

}
