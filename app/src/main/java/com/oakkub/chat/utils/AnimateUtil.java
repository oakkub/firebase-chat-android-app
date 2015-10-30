package com.oakkub.chat.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class AnimateUtil {

    public static final String SCALE_ALPHA = "scaleAlpha";
    public static final String ALPHA = "alpha";
    public static final String SCALE_UP = "scaleUp";
    public static final String SCALE_DOWN = "scaleDown";

    public static void alphaScaleAnimation(View view, int durationOffset) {

        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.scale_alpha);
        animation.setStartOffset(durationOffset);
        view.startAnimation(animation);

    }

    public static void alphaFlipAnimation(View view) {

        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translationx_exit_left);
        view.startAnimation(animation);

    }

    public static void playAnimationTogether(Animation animation, int durationOffset, View... views) {
        animation.setStartOffset(durationOffset);
        for (View view : views) view.startAnimation(animation);
    }

}
