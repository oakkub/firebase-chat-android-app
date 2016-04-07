package com.oakkub.chat.views.transformers;

import android.view.View;

import com.oakkub.chat.views.widgets.viewpager.ViewPager;

/**
 * Created by OaKKuB on 3/27/2016.
 */
public class ParallaxLoginPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1) {
            page.setAlpha(0);
        } else if (position <= 1) {
            page.setAlpha(1 - Math.abs(position));
        } else {
            page.setAlpha(0);
        }
    }

}
