package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 1/5/2016.
 */
public class AuthStateFragment extends BaseFragment implements Firebase.AuthStateListener {

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private AuthData authData;
    private OnFirebaseAuthentication onFirebaseAuthentication;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onFirebaseAuthentication = (OnFirebaseAuthentication) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (authData != null) {
            auth(authData);
            authData = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        firebase.addAuthStateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        firebase.removeAuthStateListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onFirebaseAuthentication = null;
    }

    @Override
    public void onAuthStateChanged(AuthData authData) {
        if (onFirebaseAuthentication != null) {
            auth(authData);
        } else {
            this.authData = authData;
        }
    }

    private void auth(AuthData authData) {
        SharedPreferences.Editor editor =
                AppController.getComponent(getActivity()).sharedPreferencesEditor();

        if (authData == null) {
            editor.remove(PrefsUtil.PREF_UID);
            onFirebaseAuthentication.onUnauthenticated();
        } else {
            editor.putString(PrefsUtil.PREF_UID, authData.getUid());
            onFirebaseAuthentication.onAuthenticated(authData);
        }

        editor.apply();
    }

    public interface OnFirebaseAuthentication {
        void onAuthenticated(AuthData authData);
        void onUnauthenticated();
    }
}
