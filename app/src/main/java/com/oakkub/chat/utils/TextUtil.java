package com.oakkub.chat.utils;

import java.util.regex.Pattern;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class TextUtil {

    /*BD:11:92:B8:75:D2:F5:D5:D3:3D:22:C2:CB:F7:84:DF:C9:DF:F2:7B*/
        /*490800630447-8497ej03gra35bb27sidup6f8alhumrh.apps.googleusercontent.com*/

    public static final String FACEBOOK_PROVIDER = "facebook";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String EMAIL_PROVIDER = "password";

    public static final String EMAIL = "email";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PROFILE_IMAGE_URL = "profileImageURL";
    public static final String TOKEN = "token";

    private static final String FONT_PATH = "fonts/";
    public static final String POETSENONE_FONT = FONT_PATH + "PoetsenOne-Regular.ttf";

    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static boolean checkEmailFormat(String email) {

        final String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        final Pattern emailPattern = Pattern.compile(emailRegex);

        return emailPattern.matcher(email).matches();
    }
}
