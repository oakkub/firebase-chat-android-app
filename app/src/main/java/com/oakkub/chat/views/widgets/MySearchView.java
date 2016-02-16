package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 1/16/2016.
 */
public class MySearchView extends SearchView {

    private static final String STATE_QUERY = "state:query";

    public MySearchView(Context context) {
        super(context);
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(parcelable);

        Bundle bundle = new Bundle();
        bundle.putString(STATE_QUERY, getQuery().toString().trim());

        savedState.bundle = bundle;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        final String query = savedState.bundle.getString(STATE_QUERY, "");
        if (!query.isEmpty()) {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    setQuery(query, false);
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

}
