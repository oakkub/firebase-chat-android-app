package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.models.Room;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

/**
 * Created by OaKKuB on 1/8/2016.
 */
public class GroupDetailDialogActivity extends BaseActivity {

    private static final String EXTRA_MY_ID = "extra:myId";
    private static final String EXTRA_ROOM = "extra:room";
    private static final String EXTRA_IS_MEMBER = "extra:isMember";

    public static final String ACTION_GROUP = "com.oakkub.chat.activities.GroupDetailDialogActivity.ACTION_GROUP";
    public static final String ACTION_PUBLIC = "com.oakkub.chat.activities.GroupDetailDialogActivity.ACTION_PUBLIC";

    @Bind(R.id.group_detail_profile_image_view)
    SimpleDraweeView roomImage;

    @Bind(R.id.group_detail_display_name_text_view)
    TextView roomNameTextView;

    @State
    String myId;

    @State
    String action;

    @State
    boolean isMember;

    private Room room;

    public static Intent getStartIntent(Context context, Room room, String myId, boolean isMember, String action) {
        Intent intent = new Intent(context, GroupDetailDialogActivity.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_MY_ID, myId);
        intent.putExtra(EXTRA_IS_MEMBER, isMember);
        intent.putExtra(EXTRA_ROOM, Parcels.wrap(room));

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        getDataFromIntent(savedInstanceState);

        roomNameTextView.setText(room.getName());
        roomImage.setImageURI(Uri.parse(room.getImagePath()));
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        Intent intent = getIntent();

        if (savedInstanceState == null) {
            action = intent.getAction();
            myId = intent.getStringExtra(EXTRA_MY_ID);
            isMember = intent.getBooleanExtra(EXTRA_IS_MEMBER, false);
        }
        room = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ROOM));
    }

    @OnClick(R.id.group_detail_chat_button)
    public void onChatButtonClick() {
        Intent groupRoomIntent = ChatRoomActivity.getIntentGroupRoom(this, room, myId);
        startActivity(groupRoomIntent);
        fadeOutFinish();
    }

    @OnClick(R.id.group_detail_info_button)
    public void onInfoButtonClick() {
        String intentAction = action.equals(ACTION_GROUP) ?
                RoomInfoActivity.ACTION_GROUP : RoomInfoActivity.ACTION_PUBLIC;

        Intent groupInfoIntent = RoomInfoActivity.getStartIntent(this, room, myId, intentAction, isMember);
        startActivity(groupInfoIntent);
        fadeOutFinish();
    }
}
