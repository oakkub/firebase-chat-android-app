package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.utils.Util;

import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 10/29/2015.
 */
public class BottomSheetDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = BottomSheetDialog.class.getSimpleName();
    private static final String TITLE_ARGS = "titleArgs";
    private static final String OK_DRAWABLE_ARGS = "okDrawableArgs";
    private static final String POSITIVE_BUTTON_NAME = "positiveButtonNameArgs";

    private OnClickListener onClickListener;

    public static BottomSheetDialog newInstance(String title, String positiveButtonName, int okDrawableId, OnClickListener onClickListener) {

        Bundle args = new Bundle();
        args.putString(TITLE_ARGS, title);
        args.putString(POSITIVE_BUTTON_NAME, positiveButtonName);
        if (okDrawableId != -1) {
            args.putInt(OK_DRAWABLE_ARGS, okDrawableId);
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
        bottomSheetDialog.setArguments(args);
        bottomSheetDialog.setOnClickListener(onClickListener);

        return bottomSheetDialog;
    }

    private void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        final String title = args.getString(TITLE_ARGS, "");
        final String positiveButtonName = args.getString(POSITIVE_BUTTON_NAME, "");
        final int okDrawableId = args.getInt(OK_DRAWABLE_ARGS, -1);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_dialog, null);
        TextView descriptionTextView = ButterKnife.findById(view, R.id.description_textview_bottom_dialog);
        TextView okButton = ButterKnife.findById(view, R.id.ok_bottom_dialog);
        TextView cancelButton = ButterKnife.findById(view, R.id.cancel_bottom_dialog);

        if (!positiveButtonName.equals("")) {
            okButton.setText(positiveButtonName);
        }
        if (!title.equals("")) {
            descriptionTextView.setText(title);
        }

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        if (okDrawableId != -1) {
            setLeftDrawableTextView(okButton, okDrawableId);
        }

        return createDialog(view);
    }

    private void setLeftDrawableTextView(TextView textView, int drawableId) {
        Drawable okDrawable = ContextCompat.getDrawable(getActivity(), drawableId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(okDrawable, null, null, null);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(okDrawable, null, null, null);
        }
    }

    private Dialog createDialog(View view) {
        final Dialog bottomSheetDialog = new Dialog(getActivity(), R.style.MaterialDialogSheet);

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.getWindow()
                .setLayout(Util.isLandScape(getActivity()) ?
                                LinearLayout.LayoutParams.WRAP_CONTENT
                                :
                                LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

        return  bottomSheetDialog;
    }

    @Override
    public void onClick(View view) {
        dismiss();
        if (onClickListener == null) return;

        switch (view.getId()) {

            case R.id.ok_bottom_dialog:
                onClickListener.onOkClick(view);
                break;

            case R.id.cancel_bottom_dialog:
                onClickListener.onCancelClick(view);
                break;

        }
    }

    public interface OnClickListener {
        void onOkClick(View view);
        void onCancelClick(View view);
    }
}
