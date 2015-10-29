package com.oakkub.chat.models;

import com.firebase.client.AuthData;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class LoginWithGoogle {

    public final AuthData authData;

    public LoginWithGoogle(AuthData authData) {
        this.authData = authData;
    }

}
