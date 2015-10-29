package com.oakkub.chat.models;

import com.firebase.client.AuthData;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class LoginWithFacebook {

    public final AuthData authData;

    public LoginWithFacebook(AuthData authData) {
        this.authData = authData;
    }

}
