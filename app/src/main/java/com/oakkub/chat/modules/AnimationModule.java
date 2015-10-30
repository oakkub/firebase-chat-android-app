package com.oakkub.chat.modules;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.oakkub.chat.R;
import com.oakkub.chat.dagger.PerApp;
import com.oakkub.chat.utils.AnimateUtil;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by OaKKuB on 10/23/2015.
 */
@Module
public class AnimationModule {

    @PerApp
    @Named(AnimateUtil.SCALE_ALPHA)
    @Provides
    Animation provideScaleAlphaAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.scale_alpha);
    }

    @PerApp
    @Named(AnimateUtil.ALPHA)
    @Provides
    Animation provideAlphaAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.alpha);
    }

    @PerApp
    @Named(AnimateUtil.SCALE_UP)
    @Provides
    Animation provideScaleUpAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.scale_up);
    }

    @PerApp
    @Named(AnimateUtil.SCALE_DOWN)
    @Provides
    Animation provideScaleDownAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.scale_down);
    }

    @Provides
    DefaultItemAnimator provideSimpleItemAnimator() {
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();

        itemAnimator.setChangeDuration(1000);
        itemAnimator.setMoveDuration(700);
        itemAnimator.setRemoveDuration(500);
        itemAnimator.setAddDuration(500);

        return itemAnimator;
    }

}
