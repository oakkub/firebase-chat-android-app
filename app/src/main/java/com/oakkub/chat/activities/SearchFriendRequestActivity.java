package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.SearchFriendRequestFragment;

import icepick.State;

/**
 * Created by OaKKuB on 2/25/2016.
 */
public class SearchFriendRequestActivity extends BaseActivity {

    private static final String SEARCH_NEW_FRIEND_TAG = "tag:searchNewFriendFragment";
    private static final String EXTRA_QUERY = "extra:query";

    @State
    String query;

    public static Intent getStartIntent(Context context, String query) {
        Intent intent = new Intent(context, SearchFriendRequestActivity.class);
        intent.putExtra(EXTRA_QUERY, query);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataIntent(savedInstanceState);
        setContentView(R.layout.empty_container);
        initInstances();
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        query = intent.getStringExtra(EXTRA_QUERY);
    }

    private void initInstances() {
        findOrAddFragmentByTag(R.id.empty_container,
                SearchFriendRequestFragment.newInstance(query), SEARCH_NEW_FRIEND_TAG);
    }
}
