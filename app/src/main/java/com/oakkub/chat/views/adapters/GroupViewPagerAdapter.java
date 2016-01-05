package com.oakkub.chat.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.oakkub.chat.fragments.GroupListFragment;
import com.oakkub.chat.fragments.PublicListFragment;

/**
 * Created by OaKKuB on 12/22/2015.
 */
public class GroupViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int ITEM_COUNT = 2;

    private String myId;

    public GroupViewPagerAdapter(FragmentManager fragmentManager, String myId) {
        super(fragmentManager);
        this.myId = myId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                return GroupListFragment.newInstance(myId);

            case 1:
                return new PublicListFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position);
    }
}
