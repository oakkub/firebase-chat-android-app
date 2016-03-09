package com.oakkub.chat.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.oakkub.chat.fragments.FriendsFragment;
import com.oakkub.chat.fragments.GroupListFragment;
import com.oakkub.chat.fragments.PublicListFragment;
import com.oakkub.chat.fragments.RoomListFragment;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class MainViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int TOTAL_ITEM = 4;

    private String myId;

    public MainViewPagerAdapter(FragmentManager fragmentManager, String myId) {
        super(fragmentManager);
        this.myId = myId;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:

                return new RoomListFragment();

            case 1:

                return new FriendsFragment();

            case 2:

                return new GroupListFragment();

            case 3:

                return new PublicListFragment();

        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return TOTAL_ITEM;
    }
}
