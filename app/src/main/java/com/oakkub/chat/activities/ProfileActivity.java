package com.oakkub.chat.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.oakkub.chat.BuildConfig;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.Base64ConverterFragment;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.PermissionUtil;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import icepick.State;

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
    private static final int REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION = 101;


    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Lazy<Firebase> userInfoFirebase;

    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;

    @BindView(R.id.user_profile_container)
    NestedScrollView userProfileContainer;

    @BindView(R.id.user_profile_image)
    MyDraweeView profileImage;

    @BindView(R.id.user_profile_display_name)
    TextView displayNameTextView;

    @State
    Uri uriImage;

    private String myId;
    private UserInfo myInfo;

    private Base64ConverterFragment base64ConverterFragment;

    public static Intent getStartIntent(Context context, String myId, UserInfo myInfo) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_MY_ID, myId);
        intent.putExtra(EXTRA_MY_INFO, Parcels.wrap(myInfo));
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        base64ConverterFragment = (Base64ConverterFragment) findOrAddFragmentByTag(
                getSupportFragmentManager(),
                Base64ConverterFragment.newInstance(), BASE64_CONVERTER_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    onCameraResult();
                } else {
                    File file = new File(URI.create(uriImage.toString()));
                    file.delete();
                    uriImage = null;
                }
                break;
            case IMAGE_VIEWER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    onImageViewerResult(data);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestUseCameraPermission(requestCode, grantResults);
        onRequestUseGalleryPermission(requestCode, grantResults);
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

    private void onRequestUseCameraPermission(int requestCode, int[] grantResults) {
        if (requestCode != REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION) return;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            MyToast.make("You denied write storage permission").show();
        } else {
            onCameraClick();
        }
    }

    private void onRequestUseGalleryPermission(int requestCode, int[] grantResults) {
        if (requestCode != REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION) return;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            MyToast.make("You denied write storage permission").show();
        } else {
            onGalleryClick();
        }
    }

    @OnClick(R.id.user_profile_change_image)
    public void onChangeProfileImageClick() {
        ChooseImageDialog chooseImageDialog = ChooseImageDialog.newInstance();
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
        if (!PermissionUtil.isPermissionAllowed(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_CAMERA_WRITE_STORAGE_PERMISSION)) {
            return;
        }

        File cameraStorage = FileUtil.getCameraStorageDirectory();
        if (cameraStorage == null) {
            MyToast.make("Cannot use camera, no sdcard available.").show();
            return;
        }

        uriImage = Uri.fromFile(cameraStorage);

        Uri contentUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                cameraStorage);

        Intent cameraIntent = IntentUtil.openCamera(this, contentUri);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onGalleryClick() {
        if (!PermissionUtil.isPermissionAllowed(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_CODE_GALLERY_WRITE_STORAGE_PERMISSION)) {
            return;
        }

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
