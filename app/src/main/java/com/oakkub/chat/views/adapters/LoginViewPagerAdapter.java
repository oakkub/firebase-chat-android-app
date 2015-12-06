package com.oakkub.chat.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.oakkub.chat.fragments.EmailLoginFragment;
import com.oakkub.chat.fragments.LoginActivityFragment;

import javax.inject.Inject;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class LoginViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int TOTAL_ITEM = 2;

    @Inject
    public LoginViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new LoginActivityFragment();

            case 1:
                return new EmailLoginFragment();

        }

        return null;
    }

    @Override
    public int getCount() {
        return TOTAL_ITEM;
    }
}
