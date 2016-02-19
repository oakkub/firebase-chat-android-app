package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class ChooseImageDialog extends DialogFragment {

    @Bind(R.id.image_intent_camera)
    TextView cameraTextView;

    @Bind(R.id.image_intent_image_viewer)
    TextView galleryTextView;

    private ChooseImageDialogListener listener;

    public static ChooseImageDialog newInstance(ChooseImageDialogListener listener) {
        Bundle args = new Bundle();

        ChooseImageDialog chooseImageDialog = new ChooseImageDialog();
        chooseImageDialog.setArguments(args);
        chooseImageDialog.setChooseImageDialogListener(listener);

        return chooseImageDialog;
    }

    private void setChooseImageDialogListener(ChooseImageDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_intent_dialog, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @OnClick(R.id.image_intent_camera)
    public void onImageCameraClick() {
        dismiss();
        if (listener == null) return;
        listener.onCameraClick();
    }

    @OnClick(R.id.image_intent_image_viewer)
    public void onImageGalleryClick() {
        dismiss();
        if (listener == null) return;
        listener.onGalleryClick();
    }

    public interface ChooseImageDialogListener {
        void onCameraClick();
        void onGalleryClick();
    }
}
