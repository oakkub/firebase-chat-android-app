package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.SearchFriendRequestActivity;
import com.oakkub.chat.models.eventbus.EventBusSearchingFriendRequest;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.adapters.viewholders.FriendViewPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import icepick.State;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class AddFriendPagerFragment extends BaseFragment implements
        ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener {

    private static final String ARGS_SELECTED_TAG = "args:selectedTab";

    public static final int RECOMMENDED_FRIEND_TAB = 0;
    public static final int FRIEND_REQUEST_TAB = 1;
    public static final int RECEIVED_REQUREST_TAB = 2;

    @Bind(R.id.main_app_bar_layout)
    AppBarLayout appBarLayout;

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_tablayout)
    TabLayout tabLayout;

    @Bind(R.id.friend_view_pager)
    ViewPager viewPager;

    @State
    int selectedTab;

    private FriendViewPagerAdapter viewPagerAdapter;

    public static AddFriendPagerFragment newInstance() {
        return newInstance(-1);
    }

    public static AddFriendPagerFragment newInstance(int selectedTab) {
        Bundle args = new Bundle();
        args.putInt(ARGS_SELECTED_TAG, selectedTab);

        AddFriendPagerFragment fragment = new AddFriendPagerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataArgs();
        setHasOptionsMenu(true);
    }

    private void getDataArgs() {
        Bundle args = getArguments();
        selectedTab = args.getInt(ARGS_SELECTED_TAG, -1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_tab, container, false);
        ButterKnife.bind(this, rootView);
        initInstances();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            if (selectedTab > -1) {
                viewPager.setCurrentItem(selectedTab);
                selectedTab = -1;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (viewPager.getCurrentItem() == 0) {
            inflateSendFriendRequestMenu(menu, inflater);
        }
    }

    private void initInstances() {
        initToolbar();
        String[] titles = getResources().getStringArray(R.array.friend_tab_names);

        viewPagerAdapter =
                new FriendViewPagerAdapter(getChildFragmentManager(), titles, selectedTab);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(this);

        tabLayout.setupWithViewPager(viewPager);
    }

    private void initToolbar() {
        AppCompatActivity activity = getBaseActivity();

        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.add_friend);
        }
    }

    private void inflateSendFriendRequestMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (viewPager.getCurrentItem() != 0) return false;
        query = query.trim();

        EventBus.getDefault().post(new EventBusSearchingFriendRequest(query));

        Intent searchRecommendedFriendIntent = SearchFriendRequestActivity
                .getStartIntent(getActivity(), query);
        startActivity(searchRecommendedFriendIntent);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Util.hideSoftKeyboard(getActivity());

        getActivity().invalidateOptionsMenu();
        onFragmentSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    private void onFragmentSelected(int position) {
        appBarLayout.setExpanded(true);
        if (viewPagerAdapter.getRegisteredFragment(position) == null) return;

        switch (position) {
            case 1:
                onPendingFriendRequestFragmentSelected(position);
                break;
            case 2:
                onReceivedFriendRequestFragmentSelected(position);
        }
    }

    public void onPendingFriendRequestFragmentSelected(int position) {
        ((PendingFriendRequestFragment)
                viewPagerAdapter.getRegisteredFragment(position)).load();
    }

    public void onReceivedFriendRequestFragmentSelected(int position) {
        ((ReceivedFriendRequestFragment)
                viewPagerAdapter.getRegisteredFragment(position)).load();
    }

}
