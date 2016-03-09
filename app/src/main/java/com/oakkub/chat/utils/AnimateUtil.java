package com.oakkub.chat.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class AnimateUtil {

    public static final String SCALE_ALPHA = "scaleAlpha";
    public static final String ALPHA = "alpha";
    public static final String SCALE_UP = "scaleUp";
    public static final String SCALE_DOWN = "scaleDown";

    public static void alphaAnimation(View view, final boolean show) {
        final WeakReference<View> weakView = new WeakReference<>(view);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(weakView.get(), "alpha", show ? 0 : 1, show ? 1 : 0);
        alpha.setDuration(200);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    if (weakView.get() != null) {
                        weakView.get().setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    if (weakView.get() != null) {
                        weakView.get().setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.cancel();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        alpha.start();
    }

}
