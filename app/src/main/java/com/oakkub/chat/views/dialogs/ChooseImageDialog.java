package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.oakkub.chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class ChooseImageDialog extends DialogFragment {

    @BindView(R.id.image_intent_camera)
    TextView cameraTextView;

    @BindView(R.id.image_intent_image_viewer)
    TextView galleryTextView;

    private ChooseImageDialogListener chooseImageDialogListener;

    public static ChooseImageDialog newInstance() {
        Bundle args = new Bundle();

        ChooseImageDialog chooseImageDialog = new ChooseImageDialog();
        chooseImageDialog.setArguments(args);
        return chooseImageDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            chooseImageDialogListener = (ChooseImageDialogListener) targetFragment;
        } else {
            chooseImageDialogListener = (ChooseImageDialogListener) getActivity();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        chooseImageDialogListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.image_intent_dialog, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @OnClick(R.id.image_intent_camera)
    public void onImageCameraClick() {
        dismiss();
        if (chooseImageDialogListener == null) return;

        chooseImageDialogListener.onCameraClick();
    }

    @OnClick(R.id.image_intent_image_viewer)
    public void onImageGalleryClick() {
        dismiss();
        if (chooseImageDialogListener == null) return;

        chooseImageDialogListener.onGalleryClick();
    }

    public interface ChooseImageDialogListener {
        void onCameraClick();

        void onGalleryClick();
    }
}
