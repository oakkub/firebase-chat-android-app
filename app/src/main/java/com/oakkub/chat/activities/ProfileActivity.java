package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 2/14/2016.
 */
public class ProfileActivity extends BaseActivity {

    private static final String EXTRA_MY_INFO = "state:myInfo";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    private String myId;
    private UserInfo myInfo;

    public static Intent getStartIntent(Context context, String myId, UserInfo myInfo) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_MY_ID, myId);
        intent.putExtra(EXTRA_MY_INFO, Parcels.wrap(myInfo));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        getDataIntent();
        initInstances();
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
        myInfo = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MY_INFO));
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
