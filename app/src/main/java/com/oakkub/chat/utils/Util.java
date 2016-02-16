package com.oakkub.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Contextor;

/**
 * Created by OaKKuB on 10/13/2015.
 */
public class Util {

    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static Intent intentClearActivity(Context context, Class<?> cls) {

        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isLandScape() {
        Display display = ((WindowManager) Contextor.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.x > size.y;
    }

    public static void clearFocus(AppCompatActivity activity) {

        View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView != null) currentFocusView.clearFocus();
    }

    public static void showSoftKeyboard(Activity activity) {

        View currentFocus = activity.getCurrentFocus();

        if (currentFocus != null) {

            InputMethodManager inputMethodManager = AppController.getComponent(activity).inputMethodManager();
            inputMethodManager.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    public static void hideSoftKeyboard(Activity activity) {

        View currentFocus = activity.getCurrentFocus();

        if (currentFocus != null) {

            InputMethodManager inputMethodManager = AppController.getComponent(activity).inputMethodManager();
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }

    }

}
