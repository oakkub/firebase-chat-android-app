package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class ProgressDialogFragment extends DialogFragment {

    private static final String ARGS_DIALOG_MESSAGE = "args:dialogMessage";

    public static ProgressDialogFragment newInstance() {
        return newInstance(null);
    }

    public static ProgressDialogFragment newInstance(String dialogMessage) {
        Bundle args = new Bundle();
        args.putString(ARGS_DIALOG_MESSAGE, dialogMessage);

        ProgressDialogFragment progressDialog = new ProgressDialogFragment();
        progressDialog.setArguments(args);

        return progressDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String dialogMessage = args.getString(ARGS_DIALOG_MESSAGE);

        setCancelable(false);
        return MyProgressDialog.newInstance(getActivity(), dialogMessage);
    }

    private static class MyProgressDialog extends ProgressDialog {

        private String dialogMessage;

        public static MyProgressDialog newInstance(Context context, String dialogMessage) {
            return new MyProgressDialog(context, dialogMessage);
        }

        public MyProgressDialog(Context context, String dialogMessage) {
            super(context);

            getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            if (dialogMessage == null) {
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            } else {
                this.dialogMessage = dialogMessage;
            }

//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.progress_bar);

            if (dialogMessage != null) {
                setMessage(dialogMessage);
            }
        }
    }
}
