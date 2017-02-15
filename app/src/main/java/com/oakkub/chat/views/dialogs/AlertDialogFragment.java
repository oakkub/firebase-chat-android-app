package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB onAlertDialogListener 2/11/2016.
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARGS_TITLE = "args:title";
    private static final String ARGS_MESSAGE = "args:message";
    private static final String ARGS_BUTTON_OK = "args:buttonOk";
    private static final String ARGS_BUTTON_NEUTRAL = "args:buttonNatural";
    private static final String ARGS_BUTTON_CANCEL = "args:buttonCancel";
    private static final String ARGS_CANCELABLE = "args:cancelable";

    private OnAlertDialogListener onAlertDialogListener;

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
        return newInstance(title, message, buttonOk, null, buttonCancel, cancelable);
    }

    public static AlertDialogFragment newInstance(@NonNull String title,
                                                  @NonNull String message,
                                                  @Nullable String buttonOk,
                                                  @Nullable String buttonNewtral,
                                                  @Nullable String buttonCancel,
                                                  boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        args.putString(ARGS_BUTTON_OK, buttonOk);
        args.putString(ARGS_BUTTON_NEUTRAL, buttonNewtral);
        args.putString(ARGS_BUTTON_CANCEL, buttonCancel);
        args.putBoolean(ARGS_CANCELABLE, cancelable);

        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (getTargetFragment() != null) {
                onAlertDialogListener = (OnAlertDialogListener) getTargetFragment();
            } else if (getParentFragment() != null) {
                onAlertDialogListener = (OnAlertDialogListener) getParentFragment();
            } else {
                onAlertDialogListener = (OnAlertDialogListener) getActivity();
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onAlertDialogListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARGS_TITLE);
        String message = args.getString(ARGS_MESSAGE);
        String btnOK = args.getString(ARGS_BUTTON_OK);
        String btnNeutral = args.getString(ARGS_BUTTON_NEUTRAL);
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
                .setNeutralButton(
                        btnNeutral == null ? null : btnNeutral,
                        btnNeutral == null ? null : this)
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
                onAlertDialogListener.onAlertDialogClick(getTag(), DialogInterface.BUTTON_POSITIVE);
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                onAlertDialogListener.onAlertDialogClick(getTag(), DialogInterface.BUTTON_NEUTRAL);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onAlertDialogListener.onAlertDialogClick(getTag(), DialogInterface.BUTTON_NEGATIVE);
                break;
        }
    }

    public interface OnAlertDialogListener {
        void onAlertDialogClick(String tag, int which);
    }
}
