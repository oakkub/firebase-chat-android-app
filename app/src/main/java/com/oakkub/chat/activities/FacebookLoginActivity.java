package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.FacebookLoginActivityFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class FacebookLoginActivity extends BaseActivity {

    public static final String ACTION = "extra:action";
    public static final String LOGIN_ACTION = "action:login";
    public static final String LOGOUT_ACTION = "action:logout";

    private static final String FRAGMENT_TAG = "tag:facebookLoginFragment";

    @Bind(R.id.login_process_root_view)
    RelativeLayout rootView;
    @Bind(R.id.logging_in_text_view)
    TextView loggingTextView;

    @State
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_process);
        ButterKnife.bind(this);

        getAction(savedInstanceState);
        setViews();

        findFacebookLoginFragment();
    }

    private void getAction(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            // action should be either LOGIN or LOGOUT.
            action = intent.getAction();
        }
    }

    private void setViews() {

        rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.darkBlue));

        if (action.equals(LOGIN_ACTION)) {
            loggingTextView.setText(getString(R.string.logging_in_with_facebook));
        } else {
            loggingTextView.setText(getString(R.string.logging_out_with_facebook));
        }

    }

    private void findFacebookLoginFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FacebookLoginActivityFragment fragment = (FacebookLoginActivityFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = FacebookLoginActivityFragment.newInstance(action);

            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // only when logging in can tap back button.
        if (action.equals(LOGIN_ACTION)) {
            super.onBackPressed();
        }
    }
}
