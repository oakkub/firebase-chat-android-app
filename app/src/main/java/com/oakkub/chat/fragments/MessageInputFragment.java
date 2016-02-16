package com.oakkub.chat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.oakkub.chat.R;
import com.oakkub.chat.views.widgets.MyLinearLayout;
import com.oakkub.chat.views.widgets.TextImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageInputFragment extends BaseFragment {

    private static final String TAG = MessageInputFragment.class.getSimpleName();
    private static final int CAMERA_REQUEST_CODE = 1;

    @Bind(R.id.message_attachment_root)
    MyLinearLayout attachmentLayout;

    @Bind(R.id.textImageCamera)
    TextImageView cameraTextImage;

    @Bind(R.id.textImageGallery)
    TextImageView galleryTextImage;

    @Bind(R.id.textImageBack)
    TextImageView backTextImage;

    @Bind(R.id.message_attachment_button)
    ImageButton attachmentButton;

    @Bind(R.id.message_input_ediitext)
    EditText messageText;

    @Bind(R.id.message_input_button)
    ImageButton sendMessageButton;

    @State
    Uri uriCameraImageFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.partial_message_input, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:

                    File file = new File(URI.create(uriCameraImageFile.toString()));
                    file.delete();
                    uriCameraImageFile = null;

                    break;
            }
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:

                    /*Message message = privateChatRoomActivityFragment.onSendCameraImage(uriCameraImageFile);
                    chatListAdapter.addFirst(message);
                    uriCameraImageFile = null;*/

                    break;
            }
        }
    }

    @OnClick(R.id.message_attachment_button)
    public void onAttachmentButtonClick() {
        displayFileAttachmentLayout();
    }

    @OnClick(R.id.textImageBack)
    public void onTextImageBackClick(View view) {
        displayFileAttachmentLayout();
    }

    @OnClick(R.id.textImageCamera)
    public void onTextImageCameraClick() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            return;
        }

        File imageFile = null;
        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageFile == null) {
            Toast.makeText(getActivity(), "Cannot use camera", Toast.LENGTH_LONG).show();
            return;
        }

        uriCameraImageFile = Uri.fromFile(imageFile);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriCameraImageFile);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @OnClick(R.id.textImageGallery)
    public void onTextImageGalleryClick() {
        Toast.makeText(getActivity(), "987654321", Toast.LENGTH_SHORT).show();
    }

    private void displayFileAttachmentLayout() {
        attachmentLayout.circleReveal();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File createImageFile() throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File finalDir = new File(storageDir, "Chatto");
        if (!finalDir.exists()) {
            finalDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "IMG_" + timeStamp + "_";
        Log.d(TAG, "createImageFile: " + imageName);
        File imageFile = File.createTempFile(imageName, ".jpg", finalDir);
        Log.d(TAG, "createImageFile: " + imageFile.getAbsolutePath());
        uriCameraImageFile = Uri.fromFile(imageFile);

        return imageFile;
    }

    public interface MessageInputRequest {
        void onCameraImageMes();
    }

}
