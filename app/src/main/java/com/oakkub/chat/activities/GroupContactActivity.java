package com.oakkub.chat.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.GroupViewPagerAdapter;
import com.oakkub.chat.views.widgets.Fab;
import com.oakkub.chat.views.widgets.viewpager.ViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class GroupContactActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final String EXTRA_MY_ID = "extra:myId";
    private static final String TAG_NEW_MESSAGES = "tag:newMessages";

    @Bind(R.id.group_contact_toolbar)
    Toolbar toolbar;

    @Bind(R.id.groupc_contact_tablayout)
    TabLayout tabLayout;

    @Bind(R.id.group_contact_viewpager)
    ViewPager viewPager;

    @Bind(R.id.add_group_fab)
    Fab addGroupFab;

    @State
    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contact);
        ButterKnife.bind(this);
        getDataFromIntent(savedInstanceState);

        setToolbar();
        setViewPager();
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        myId = getIntent().getStringExtra(EXTRA_MY_ID);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setViewPager() {

        GroupViewPagerAdapter groupViewPagerAdapter = new GroupViewPagerAdapter(getSupportFragmentManager(), myId);
        viewPager.setAdapter(groupViewPagerAdapter);
        viewPager.addOnPageChangeListener(this);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPageSelected(int position) {
        setFabImageAnimation(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        addGroupFab.setClickable(positionOffsetPixels == 0);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setFabImageAnimation(int position) {

        int[] drawables = {
                R.drawable.ic_person_add_24dp,
                R.drawable.ic_sms_24dp
        };

        Drawable[] fabImages = new Drawable[] {
                addGroupFab.getDrawable(),
                ContextCompat.getDrawable(this, drawables[position])
        };

        TransitionDrawable transitionDrawable = new TransitionDrawable(fabImages);
        transitionDrawable.setCrossFadeEnabled(true);

        addGroupFab.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(200);

    }

    @OnClick(R.id.add_group_fab)
    public void onAddGroupFabClick() {
        switch (viewPager.getCurrentItem()) {

            case 0:
                onNewMessagesFabClick();
                break;

            case 1:
                break;

        }
    }

    public void onNewMessagesFabClick() {
        Intent newMessagesIntent = new Intent(this, NewMessagesActivity.class);
        newMessagesIntent.putExtra(NewMessagesActivity.EXTRA_MY_ID, myId);

        startActivity(newMessagesIntent);
    }
}
