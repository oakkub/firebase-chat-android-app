package com.oakkub.chat.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.oakkub.chat.activities.BaseActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public class BaseFragment extends Fragment {

    protected static final String ARGS_MY_ID = "args:myId";
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";

    @State
    String uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (savedInstanceState == null) {
            SharedPreferences prefs = AppController.getComponent(getActivity()).sharedPreferences();
            uid = prefs.getString(PrefsUtil.PREF_UID, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public int getCompatColor(int color) {
        return ContextCompat.getColor(getActivity(), color);
    }

    public void showProgressDialog() {
        ProgressDialogFragment progressDialog = findProgressDialog();
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
        progressDialog.show(getChildFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    public void hideProgressDialog() {
        ProgressDialogFragment progressDialog = findProgressDialog();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public ProgressDialogFragment findProgressDialog() {
        return (ProgressDialogFragment) getChildFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
    }
}
