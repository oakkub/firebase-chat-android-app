package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import com.oakkub.chat.managers.AppController;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class Fab extends FloatingActionButton {

    private boolean fabAnimating;

    public Fab(Context context) {
        super(context);
    }

    public Fab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Fab(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void scaleDown() {
        fabScaleAnimation(false);
        fabAnimating = true;
        setVisibility(View.GONE);
    }

    public void scaleUp() {
        setVisibility(View.VISIBLE);
        fabAnimating = false;
        fabScaleAnimation(true);
    }

    public void fabScaleAnimation(final boolean showFab) {
        if (fabAnimating) return;

        Animation animation = AppController.getComponent(getContext()).scaleDownAnimation();
        if (showFab) {
            animation = AppController.getComponent(getContext()).scaleUpAnimation();
        }
        startAnimation(animation);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);

        savedState.visibility = getVisibility();
        savedState.fabAnimating = fabAnimating;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setVisibility(savedState.visibility);
        fabAnimating = savedState.fabAnimating;
    }

    private static class SavedState extends BaseSavedState {

        private int visibility;
        private boolean fabAnimating;

        public SavedState(Parcel source) {
            super(source);

            visibility = source.readInt();
            fabAnimating = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(visibility);
            out.writeInt(fabAnimating ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
