package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.NewPublicChatFragment;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.FrescoUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.UriUtil;
import com.oakkub.chat.views.dialogs.ChooseImageDialog;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.spinner.MySpinner;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class NewPublicChatActivity extends BaseActivity implements
        ChooseImageDialog.ChooseImageDialogListener, NewPublicChatFragment.NewPublicChatListener {

    private static final String TAG = NewPublicChatActivity.class.getSimpleName();
    private static final String NEW_PUBLIC_CHAT_FRAGMENT_TAG = "tag:publicChatFragment";
    private static final String CHOOSE_IMAGE_DIALOG_TAG = "tag:chooseImageDialog";
    private static final String TAG_PROGRESS_DIALOG = "tag:progressDialogFragment";
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int IMAGE_VIEWER_REQUEST_CODE = 1;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.public_chat_image)
    SimpleDraweeView publicChatImage;

    @Bind(R.id.public_chat_name_edittext)
    EditText nameEditText;

    @Bind(R.id.public_chat_optional_desc_edittext)
    EditText descriptionEditText;

    @Bind(R.id.public_chat_type_spinner)
    MySpinner tagSpinner;

    @State
    String myId;

    @State
    String absolutePath;

    @State
    int maxLengthName;

    @State
    int maxLengthDescription;

    @State
    Uri uriImage;

    @State
    String[] spinnerKeys;

    @State
    String[] spinnerValues;

    private NewPublicChatFragment newPublicChatFragment;

    public static Intent getStartIntent(Context context, String myId) {
        Intent intent = new Intent(context, NewPublicChatActivity.class);
        intent.putExtra(EXTRA_MY_ID, myId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_chat_creation);
        getDataIntent(savedInstanceState);
        getDataResources(savedInstanceState);
        ButterKnife.bind(this);

        setToolbar();
        setSpinner();

        newPublicChatFragment = (NewPublicChatFragment) findFragmentByTag(NEW_PUBLIC_CHAT_FRAGMENT_TAG);
        if (newPublicChatFragment == null) {
            newPublicChatFragment = (NewPublicChatFragment) addFragmentByTag(
                    NewPublicChatFragment.newInstance(myId), NEW_PUBLIC_CHAT_FRAGMENT_TAG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onCameraResult(requestCode, resultCode, data);
        onImageViewerResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (uriImage != null) {
            publicChatImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    showResizedImageByUri();
                    publicChatImage.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                return handleOkActionClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDataIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        myId = intent.getStringExtra(EXTRA_MY_ID);
    }

    private void getDataResources(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Resources res = getResources();

        maxLengthName = res.getInteger(R.integer.max_length_public_chat_name);
        maxLengthDescription = res.getInteger(R.integer.max_length_optional_description);
        spinnerKeys = res.getStringArray(R.array.public_chat_tag_keys);
        spinnerValues = res.getStringArray(R.array.public_chat_tag_values);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.new_public_chat));
        }
    }

    private void setSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerKeys);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tagSpinner.setAdapter(spinnerAdapter);
    }

    @OnClick(R.id.public_chat_image)
    public void onPublicChatImageClick() {
        ChooseImageDialog chooseImageDialog = ChooseImageDialog.newInstance(this);
        chooseImageDialog.show(getSupportFragmentManager(), CHOOSE_IMAGE_DIALOG_TAG);
    }

    @Override
    public void onCameraClick() {
        File cameraStorage = FileUtil.getCameraStorage();
        if (cameraStorage == null) {
            Toast.makeText(this, "Cannot use camera, no sdcard available.", Toast.LENGTH_LONG).show();
            return;
        }

        uriImage = Uri.fromFile(cameraStorage);

        Intent cameraIntent = IntentUtil.openCamera(this, uriImage);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onGalleryClick() {
        Intent imageViewerIntent = IntentUtil.openImageViewer(this, false);
        if (imageViewerIntent != null) {
            startActivityForResult(imageViewerIntent, IMAGE_VIEWER_REQUEST_CODE);
        }
    }

    @Override
    public void onPublicChatCreated(Room room) {
        Intent roomIntent = ChatRoomActivity.getIntentPublicRoom(this, room, myId, true);
        startActivity(roomIntent);
        finish();
    }

    @Override
    public void onPublicChatFailed() {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) findFragmentByTag(TAG_PROGRESS_DIALOG);
        Toast.makeText(NewPublicChatActivity.this, "" + (progressDialog == null), Toast.LENGTH_SHORT).show();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, R.string.error_creating_room, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("unchecked")
    private boolean handleOkActionClick() {
        String roomName = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (roomName.isEmpty()) {
            nameEditText.setError(getString(R.string.error_room_name_empty));
            return false;
        }

        if (roomName.length() > maxLengthName) {
            nameEditText.setError(getString(R.string.error_room_name_exceeds_limit));
            return false;
        }

        if (description.length() > maxLengthDescription) {
            descriptionEditText.setError(getString(R.string.error_room_description_exceeds_limit));
            return false;
        }

        ProgressDialogFragment progressDialog = ProgressDialogFragment.newInstance();
        progressDialog.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);

        String tagValue = spinnerValues[tagSpinner.getSelectedItemPosition()];
        Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC);
        room.setName(roomName);
        room.setTag(tagValue);
        if (!description.isEmpty()) {
            room.setDescription(description);
        }
        newPublicChatFragment.createPublicChat(room, uriImage, absolutePath);

        return true;
    }

    private void onImageViewerResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != IMAGE_VIEWER_REQUEST_CODE || resultCode != RESULT_OK) return;

        absolutePath = UriUtil.getPath(data.getData());
        uriImage = data.getData();
        showResizedImageByUri();
    }

    private void onCameraResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAMERA_REQUEST_CODE || resultCode != RESULT_OK) return;
        showResizedImageByUri();
    }

    private void showResizedImageByUri() {
        DraweeController controller = FrescoUtil.getResizeController(
                publicChatImage.getWidth(), publicChatImage.getHeight(),
                uriImage, publicChatImage.getController());
        publicChatImage.setController(controller);
    }

}
