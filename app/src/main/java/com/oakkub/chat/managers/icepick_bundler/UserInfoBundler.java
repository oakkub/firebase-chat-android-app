package com.oakkub.chat.managers.icepick_bundler;

import android.os.Bundle;

import com.oakkub.chat.models.UserInfo;

import org.parceler.Parcels;

import icepick.Bundler;

/**
 * Created by OaKKuB on 3/17/2016.
 */
public class UserInfoBundler implements Bundler<UserInfo> {

    @Override
    public void put(String key, UserInfo userInfo, Bundle bundle) {
        bundle.putParcelable(key, Parcels.wrap(userInfo));
    }

    @Override
    public UserInfo get(String key, Bundle bundle) {
        return Parcels.unwrap(bundle.getParcelable(key));
    }
}
