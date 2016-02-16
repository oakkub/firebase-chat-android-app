package com.oakkub.chat.views.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by OaKKuB on 12/1/2015.
 */
public class MyLinearLayout extends LinearLayout {

    private boolean isRevealed;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void circleReveal() {
        boolean isVisible = getVisibility() == View.VISIBLE;
        isRevealed = !isVisible;

        int cx = 0;
        int cy = getTop() + getBottom();
        int dx = Math.max(cx, getWidth() - cx);
        int dy = Math.max(cy, getHeight() - cy);

        float radius = (float) Math.hypot(dx, dy);
        float startRadius = isVisible ? radius : 0;
        float finalRadius = isVisible ? 0 : radius;

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                this, cx, cy, startRadius, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));

        if (!isVisible) {
            setVisibility(View.VISIBLE);
        } else {
            animator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {}
                @Override
                public void onAnimationEnd() {
                    setVisibility(View.GONE);
                }
                @Override
                public void onAnimationCancel() {
                    setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat() {}
            });
        }
        animator.start();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);

        savedState.visibility = getVisibility();
        savedState.isRevealed = isRevealed;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setVisibility(savedState.visibility);
        isRevealed = savedState.isRevealed;
    }

    private static class SavedState extends BaseSavedState {

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
        private int visibility;
        private boolean isRevealed;

        public SavedState(Parcel source) {
            super(source);

            visibility = source.readInt();
            isRevealed = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(visibility);
            out.writeInt(isRevealed ? 1 : 0);
        }
    }
}
