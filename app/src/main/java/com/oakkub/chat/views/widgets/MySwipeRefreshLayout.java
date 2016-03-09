package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 2/4/2016.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {

    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    public MySwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setColorSchemeResources(R.color.blue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(superState);

        Bundle bundle = new Bundle(1);
        bundle.putBoolean(STATE_IS_REFRESHING, isRefreshing());
        savedState.bundle = bundle;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.bundle.getBoolean(STATE_IS_REFRESHING)) {
            show();
        }
    }

    public void show() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                setRefreshing(true);
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    public void hide() {
        setRefreshing(false);
    }
}
