package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.Base64ConverterFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.UriUtil;
import com.oakkub.chat.utils.UserInfoUtil;
import com.oakkub.chat.views.dialogs.ChooseImageDialog;
import com.oakkub.chat.views.dialogs.EditTextDialog;
import com.oakkub.chat.views.widgets.MyDraweeView;
import com.oakkub.chat.views.widgets.MyToast;

import org.parceler.Parcels;

import java.io.File;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;

/**
 * Created by OaKKuB on 2/14/2016.
 */
public class ProfileActivity extends BaseActivity implements
        Base64ConverterFragment.OnBase64ConverterListener,
        ChooseImageDialog.ChooseImageDialogListener,
        EditTextDialog.EditTextDialogListener {

    private static final String EXTRA_MY_INFO = "state:myInfo";
    private static final String CHOOSE_IMAGE_DIALOG = "tag:chooseImageDialog";
    private static final String EDITTEXT_DIALOG = "tag:editTextDialog";
    private static final String BASE64_CONVERTER_TAG = "tag:base64Converter";
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int IMAGE_VIEWER_REQUEST_CODE = 1;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.user_profile_image)
    MyDraweeView profileImage;

    @Bind(R.id.user_profile_display_name)
    TextView displayNameTextView;

    private String myId;
    private UserInfo myInfo;

    private Base64ConverterFragment base64ConverterFragment;
    private Uri uriImage;

    public static Intent getStartIntent(Context context, String myId, UserInfo myInfo) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_MY_ID, myId);
        intent.putExtra(EXTRA_MY_INFO, Parcels.wrap(myInfo));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(this).inject(this);
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
        setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(getString(R.string.profile));

        profileImage.setMatchedSizeImageURI(Uri.parse(myInfo.getProfileImageURL()));
        displayNameTextView.setText(myInfo.getDisplayName());

        base64ConverterFragment = (Base64ConverterFragment) findOrCreateFragmentByTag(
               Base64ConverterFragment.newInstance(), BASE64_CONVERTER_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                onCameraResult();
                break;
            case IMAGE_VIEWER_REQUEST_CODE:
                onImageViewerResult(data);
                break;
        }
    }

    private void onImageViewerResult(Intent data) {
        showProgressDialog();

        uriImage = data.getData();
        base64ConverterFragment.convert(uriImage, UriUtil.getPath(uriImage), true);
    }

    private void onCameraResult() {
        showProgressDialog();

        File file = new File(URI.create(uriImage.toString()));
        base64ConverterFragment.convert(file, true);
    }

    @OnClick(R.id.user_profile_change_image)
    public void onChangeProfileImageClick() {
        ChooseImageDialog chooseImageDialog = ChooseImageDialog.newInstance(this);
        chooseImageDialog.show(getSupportFragmentManager(), CHOOSE_IMAGE_DIALOG);
    }

    @OnClick(R.id.user_profile_change_display_name)
    public void onChangeDisplayNameClick() {
        EditTextDialog editTextDialog = EditTextDialog.newInstance(
                getString(R.string.edit_name), displayNameTextView.getText().toString(),
                getString(R.string.display_name), getString(R.string.change),
                getString(R.string.cancel));
        editTextDialog.show(getSupportFragmentManager(), EDITTEXT_DIALOG);
    }

    @Override
    public void onEditTextDialogClick(String text) {
        String currentName = displayNameTextView.getText().toString();

        if (!currentName.equals(text)) {
            userInfoFirebase.get().child(myId).child(UserInfoUtil.DISPLAY_NAME).setValue(text);
            displayNameTextView.setText(text);
        }
    }

    @Override
    public void onCameraClick() {
        File cameraStorage = FileUtil.getCameraStorageDirectory();
        if (cameraStorage == null) {
            MyToast.make("Cannot use camera, no sdcard available.").show();
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
    public void onBase64Received(String base64) {
        userInfoFirebase.get().child(myId).child(UserInfoUtil.PROFILE_IMAGE_URL).setValue(base64);
        profileImage.setMatchedSizeImageURI(Uri.parse(base64));
        hideProgressDialog();
    }
}
