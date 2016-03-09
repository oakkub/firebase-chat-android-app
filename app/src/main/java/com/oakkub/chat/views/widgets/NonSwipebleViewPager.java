package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by OaKKuB on 2/22/2016.
 */
public class NonSwipebleViewPager extends ViewPager {

    public NonSwipebleViewPager(Context context) {
        super(context);
    }

    public NonSwipebleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Never allow swiping to switch between pages
        return false;
    }
}
