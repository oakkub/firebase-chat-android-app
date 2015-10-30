package com.oakkub.chat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.PrivateChatRoomActivityFragment;
import com.oakkub.chat.views.widgets.toolbar.ToolbarCommunicator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PrivateChatRoomActivity extends AppCompatActivity implements ToolbarCommunicator {

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat_room);
        ButterKnife.bind(this);

        setToolbar();

        if (savedInstanceState == null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.private_chat_room_container,
                            new PrivateChatRoomActivityFragment())
                    .commit();
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        if (hasActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setTitle(String title) {

        if (hasActionBar()) {
            getSupportActionBar().setTitle(title);
        }
    }

    private boolean hasActionBar() {
        return getSupportActionBar() != null;
    }

}
