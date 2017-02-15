package com.oakkub.chat.views.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 1/31/2016.
 */
public class EditTextDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = EditTextDialog.class.getSimpleName();

    private static final String ARGS_TITLE = "args:title";
    private static final String ARGS_BUTTON_OK = "args:buttonOk";
    private static final String ARGS_BUTTON_CANCEL = "args:buttonCancel";
    private static final String ARGS_TEXT = "args:text";
    private static final String ARGS_HINT = "args:hint";

    private EditText editText;
    private EditTextDialogListener editTextDialogListener;

    public static EditTextDialog newInstance(String title,
                                             String text,
                                             String hint,
                                             String buttonOk,
                                             String buttonCancel) {
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_BUTTON_OK, buttonOk);
        args.putString(ARGS_BUTTON_CANCEL, buttonCancel);
        args.putString(ARGS_TEXT, text);
        args.putString(ARGS_HINT, hint);

        EditTextDialog editTextDialog = new EditTextDialog();
        editTextDialog.setArguments(args);

        return editTextDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (getParentFragment() != null) {
                editTextDialogListener = (EditTextDialogListener) getParentFragment();
            } else if (getTargetFragment() != null) {
                editTextDialogListener = (EditTextDialogListener) getTargetFragment();
            } else {
                editTextDialogListener = (EditTextDialogListener) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("You need to implement EditTextDialogListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        editTextDialogListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARGS_TITLE);
        String buttonOk = args.getString(ARGS_BUTTON_OK);
        String buttonCancel = args.getString(ARGS_BUTTON_CANCEL);
        String text = args.getString(ARGS_TEXT);
        String hint = args.getString(ARGS_HINT);

        int padding = getResources().getDimensionPixelOffset(R.dimen.spacing_super_medium);

        TextInputLayout inputLayout = getInputLayout(savedInstanceState, text, hint);
        inputLayout.setPadding(padding, padding, padding, padding);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(inputLayout)
                .setPositiveButton(buttonOk, this)
                .setNegativeButton(buttonCancel, this).create();
    }

    @SuppressWarnings("ResourceType")
    private TextInputLayout getInputLayout(Bundle savedInstanceState, String text, String hint) {
        initEditText(savedInstanceState, text, hint);

        TextInputLayout inputLayout = new TextInputLayout(getActivity());
        inputLayout.setId(View.generateViewId());
        inputLayout.addView(editText);

        return inputLayout;
    }

    @SuppressWarnings("ResourceType")
    private void initEditText(Bundle savedInstanceState, String text, String hint) {
        // Don't know why but layout params have to be LinearLayoutParams
        // Or else TextInputLayout cannot add it since it try to cast it to LinearLayoutParams
        LinearLayout.LayoutParams ediTextLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        editText = new EditText(getActivity());
        editText.setLayoutParams(ediTextLayoutParams);
        editText.setId(View.generateViewId());
        editText.setHint(hint);
        if (savedInstanceState == null) {
            editText.setText(text);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                editTextDialogListener.onEditTextDialogClick(editText.getText().toString().trim());
                break;
        }
    }

    public interface EditTextDialogListener {
        void onEditTextDialogClick(String text);
    }
}
