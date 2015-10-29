package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class ProgressDialogFragment extends DialogFragment {

    private static final String ARGS_DIM_SCREEN = "argsClearDimScreen";

    public static ProgressDialogFragment newInstance(boolean clearDimScreen) {

        Bundle args = new Bundle();
        args.putBoolean(ARGS_DIM_SCREEN, clearDimScreen);

        ProgressDialogFragment progressDialog = new ProgressDialogFragment();
        progressDialog.setArguments(args);

        return progressDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        final boolean clearDimScreen = args.getBoolean(ARGS_DIM_SCREEN);

        setCancelable(false);
        return ProgressDialog.newInstance(getActivity(), clearDimScreen);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void dismiss() {
        if (getDialog() != null && !isRemoving()) {
            super.dismiss();
        }
    }

    private static class ProgressDialog extends android.app.ProgressDialog {

        public static ProgressDialog newInstance(Context context, boolean clearDimScreen) {
            return new ProgressDialog(context, clearDimScreen);
        }

        public ProgressDialog(Context context, boolean clearDimScreen) {
            super(context);

            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (clearDimScreen) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.progress_bar);

        }
    }
}
