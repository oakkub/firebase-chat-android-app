package com.oakkub.chat.views.adapters.viewholders;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.oakkub.chat.activities.SendFriendRequestFragment;
import com.oakkub.chat.fragments.PendingFriendRequestFragment;
import com.oakkub.chat.fragments.ReceivedFriendRequestFragment;
import com.oakkub.chat.views.adapters.SmartFragmentStatePagerAdapter;

/**
 * Created by OaKKuB on 2/22/2016.
 */
public class FriendViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int TOTAL_ITEM = 3;

    private String[] titles;
    private int selectedTab;

    public FriendViewPagerAdapter(FragmentManager fragmentManager, String[] titles, int selectedTab) {
        super(fragmentManager);
        this.titles = titles;
        this.selectedTab = selectedTab;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SendFriendRequestFragment();
            case 1:
                return new PendingFriendRequestFragment();
            case 2:
                return ReceivedFriendRequestFragment.newInstance(selectedTab == 2);
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return TOTAL_ITEM;
    }
}
