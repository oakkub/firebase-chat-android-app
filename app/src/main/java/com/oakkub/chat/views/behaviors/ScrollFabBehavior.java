package com.oakkub.chat.views.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by OaKKuB on 1/22/2016.
 */
public class ScrollFabBehavior extends FloatingActionButton.Behavior {

    @SuppressWarnings("unused")
    private static final String TAG = ScrollFabBehavior.class.getSimpleName();

    public ScrollFabBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d(TAG, "onStartNestedScroll: " + nestedScrollAxes);
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        int childVisibility = child.getVisibility();

        if (dyConsumed > 0 && childVisibility == View.VISIBLE) {
            child.hide();
        } else if (dyConsumed < 0 && childVisibility != View.VISIBLE) {
            child.show();
        }
    }
}
