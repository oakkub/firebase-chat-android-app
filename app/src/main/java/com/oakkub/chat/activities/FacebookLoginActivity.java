package com.oakkub.chat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.FacebookLoginActivityFragment;

public class FacebookLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggin_process);
    }

    @Override
    public void onBackPressed() {

        FacebookLoginActivityFragment facebookLoginActivityFragment = (FacebookLoginActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.logging_in_fragment);

        if (facebookLoginActivityFragment != null) {
            if (facebookLoginActivityFragment.getAction().equals(FacebookLoginActivityFragment.LOGIN_ACTION)) {
                super.onBackPressed();
            }
            return;
        }

        super.onBackPressed();
    }
}
