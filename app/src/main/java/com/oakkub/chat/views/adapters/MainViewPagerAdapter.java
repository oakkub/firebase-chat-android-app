package com.oakkub.chat.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.fragments.MainActivityFragment;
import com.oakkub.chat.fragments.RoomListFragment;

import javax.inject.Inject;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class MainViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int TOTAL_ITEM = 3;

    @Inject
    public MainViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:

                return RoomListFragment.newInstance();

            case 1:

                return FriendsFragment.newInstance();

            case 2:

                return MainActivityFragment.newInstance();

        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page: " + position;
    }

    @Override
    public int getCount() {
        return TOTAL_ITEM;
    }
}
