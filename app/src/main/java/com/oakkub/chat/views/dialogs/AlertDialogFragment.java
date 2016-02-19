package com.oakkub.chat.views.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARGS_TITLE = "args:title";
    private static final String ARGS_MESSAGE = "args:message";
    private static final String ARGS_BUTTON_OK = "args:buttonOk";
    private static final String ARGS_BUTTON_CANCEL = "args:buttonCancel";
    private static final String ARGS_CANCELABLE = "args:cancelable";

    private OnAlertDialogListener listener;

    public static AlertDialogFragment newInstance(@NonNull String title,
                                                  @NonNull String message) {
        return newInstance(title, message, "", "");
    }

    public static AlertDialogFragment newInstance(@NonNull String title,
                                                  @NonNull String message,
                                                  boolean cancelable) {
        return newInstance(title, message, "", "", cancelable);
    }

    public static AlertDialogFragment newInstance(@NonNull String title,
                                                  @NonNull String message,
                                                  @Nullable String buttonOk,
                                                  @Nullable String buttonCancel) {
        return AlertDialogFragment.newInstance(title, message, buttonOk, buttonCancel, true);
    }

    public static AlertDialogFragment newInstance(@NonNull String title,
                                                  @NonNull String message,
                                                  @Nullable String buttonOk,
                                                  @Nullable String buttonCancel,
                                                  boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        args.putString(ARGS_BUTTON_OK, buttonOk);
        args.putString(ARGS_BUTTON_CANCEL, buttonCancel);
        args.putBoolean(ARGS_CANCELABLE, cancelable);

        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (OnAlertDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("You did not implement OnAlertDialogListener.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARGS_TITLE);
        String message = args.getString(ARGS_MESSAGE);
        String btnOK = args.getString(ARGS_BUTTON_OK);
        String btnCancel = args.getString(ARGS_BUTTON_CANCEL);
        boolean cancelable = args.getBoolean(ARGS_CANCELABLE);

        setCancelable(cancelable);

        if (btnOK != null && btnOK.isEmpty()) {
            btnOK = getString(R.string.ok);
        }

        if (btnCancel != null && btnCancel.isEmpty()) {
            btnCancel = getString(R.string.cancel);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        btnOK == null ? null : btnOK,
                        btnOK == null ? null : this)
                .setNegativeButton(
                        btnCancel == null ? null : btnCancel,
                        btnCancel == null ? null : this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                listener.onOkClick(getTag());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                listener.onCancelClick(getTag());
                break;
        }
    }

    public interface OnAlertDialogListener {
        void onOkClick(String tag);
        void onCancelClick(String tag);
    }
}
