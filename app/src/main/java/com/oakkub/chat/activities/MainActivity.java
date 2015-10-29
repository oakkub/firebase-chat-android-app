package com.oakkub.chat.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.fragments.FacebookLoginActivityFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.widgets.Fab;
import com.oakkub.chat.views.widgets.ToolbarCommunicator;
import com.oakkub.chat.views.adapters.MainViewPagerAdapter;
import com.oakkub.chat.R;
import com.oakkub.chat.views.widgets.ViewPager;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        ToolbarCommunicator, ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int[] FAB_DRAWABLES = {R.drawable.ic_sms_24dp, R.drawable.ic_person_add_24dp};

    public static final String LOGIN_SUCCESS_ACTION = "com.oakkub.chat.activities.MainActivity.LOGIN_SUCCESS_ACTION";

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_tablayout)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.fab)
    Fab fab;

    @Inject
    @Named(FirebaseUtil.NAMED_CURRENT_USER)
    Firebase currentUserFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_CONNECTION)
    Firebase connectionFirebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_ONLINE)
    Firebase onlineUserFirebase;

    private MainViewPagerAdapter mainViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(this).inject(this);
        checkAction();
        checkAuthentication();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setToolbar();
        setupPager();
        setupFirebaseComponent();

    }

    private void checkAction() {
        Intent intent = getIntent();
        if (intent != null) {
            if (!intent.getAction().equals(LOGIN_SUCCESS_ACTION)) {
                finish();
            }
        } else {
            finish();
        }
    }

    private void checkAuthentication() {
        if (connectionFirebase.getAuth() == null) finish();
    }

    private void setupPager() {

        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainViewPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onPageSelected(int position) {
        // change fab image, according to the selected position of viewpager
        if (position < mainViewPagerAdapter.getCount() - 1 &&
            position < FAB_DRAWABLES.length) {

            if (viewPager.getPreviousItem() < FAB_DRAWABLES.length) {
                Drawable[] layers = new Drawable[] {
                        fab.getDrawable(),
                        ContextCompat.getDrawable(this, FAB_DRAWABLES[position])
                };

                TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                transitionDrawable.setCrossFadeEnabled(true);
                fab.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(200);
            } else {

                fab.setImageDrawable(ContextCompat.getDrawable(this, FAB_DRAWABLES[position]));
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        fab.setClickable(positionOffsetPixels == 0 && fab.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            animateFabWithScaleAnimation();
            fab.setClickable(fab.getVisibility() == View.VISIBLE);
        }
    }

    private void animateFabWithScaleAnimation() {
        final int viewPagerCount = mainViewPagerAdapter.getCount();
        final int currentItem = viewPager.getCurrentItem();

        if (fab.getVisibility() == View.GONE && currentItem < viewPagerCount - 1) fab.scaleUp();
        else if (currentItem == viewPagerCount - 1) fab.scaleDown();
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        switch (viewPager.getCurrentItem()) {

            case 0:

                break;
            case 1:

                Intent addFriendIntent = new Intent(this, AddFriendActivity.class);
                startActivity(addFriendIntent);

                break;

        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupFirebaseComponent() {

        connectionFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);

                if (connected) {

                    onlineUserFirebase.child(FirebaseUtil.CHILD_ONLINE).onDisconnect().setValue(Boolean.FALSE);
                    onlineUserFirebase.child(FirebaseUtil.CHILD_ONLINE).setValue(Boolean.TRUE);

                    onlineUserFirebase.child(FirebaseUtil.CHILD_LAST_ONLINE)
                            .onDisconnect().setValue(System.currentTimeMillis());
                    onlineUserFirebase.child(FirebaseUtil.CHILD_LAST_ONLINE)
                            .setValue(System.currentTimeMillis());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Code: " + firebaseError.getCode());
                Log.e(TAG, firebaseError.getMessage());
            }
        });
    }

    @Override
    public void setTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_settings:

                return true;

            case R.id.logout_settings:

                performLogout();

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case FacebookLoginActivityFragment.RC_FACEBOOK:
            case GoogleLoginActivity.RC_GOOGLE:

                firebaseUnAuthenticate();

                break;

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void performLogout() {

        AuthData authData = connectionFirebase.getAuth();

        if (authData != null) {

            if (FirebaseUtil.isFacebookLogin(authData.getProvider())) {
                logout(FacebookLoginActivity.class,
                        FacebookLoginActivityFragment.LOGOUT_ACTION,
                        FacebookLoginActivityFragment.RC_FACEBOOK);
            } else if (FirebaseUtil.isGoogleLogin(authData.getProvider())) {
                logout(GoogleLoginActivity.class,
                        GoogleLoginActivity.ACTION_LOGOUT,
                        GoogleLoginActivity.RC_GOOGLE);
            } else if (FirebaseUtil.isEmailLogin(authData.getProvider())) {
                firebaseUnAuthenticate();
            }
        }
    }

    private void logout(Class<?> cls, String action, int requestCode) {

        Intent intent = new Intent(this, cls);
        intent.setAction(action);

        startActivityForResult(intent, requestCode);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void firebaseLogout() {

        onlineUserFirebase.child(FirebaseUtil.CHILD_ONLINE).setValue(Boolean.FALSE);
        onlineUserFirebase.child(FirebaseUtil.CHILD_LAST_ONLINE).setValue(System.currentTimeMillis());

        onlineUserFirebase.unauth();
    }

    private void firebaseUnAuthenticate() {

        firebaseLogout();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

}
