package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by OaKKuB on 11/1/2015.
 */
public class GoogleErrorDialog extends DialogFragment {

    private static final String DIALOG_ERROR_CODE = "dialog_error_code";
    private static final String DIALOG_REQUEST_CODE = "dialog_request_code";

    public static GoogleErrorDialog newInstance(int errorCode, int requestCode) {

        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR_CODE, errorCode);
        args.putInt(DIALOG_REQUEST_CODE, requestCode);

        GoogleErrorDialog googleErrorDialog = new GoogleErrorDialog();
        googleErrorDialog.setArguments(args);

        return googleErrorDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int errorCode = args.getInt(DIALOG_ERROR_CODE);
        int requestCode = args.getInt(DIALOG_REQUEST_CODE);

        return GoogleApiAvailability.getInstance()
                .getErrorDialog(getActivity(), errorCode, requestCode);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
