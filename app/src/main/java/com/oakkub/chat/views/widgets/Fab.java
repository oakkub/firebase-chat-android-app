package com.oakkub.chat.views.widgets;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class Fab extends FloatingActionButton {

    private boolean isScaledDown;
    private boolean isScaledUp;

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
        setVisibility(View.GONE);
    }

    public void scaleUp() {
        setVisibility(View.VISIBLE);
        isScaledDown = true;
        fabScaleAnimation(true);
    }

    public void fabScaleAnimation(final boolean showFab) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", showFab ? 0 : 1, showFab ? 1 : 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", showFab ? 0 : 1, showFab ? 1 : 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(showFab ? new OvershootInterpolator() : new AnticipateInterpolator());
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetAnimationData(showFab);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                resetAnimationData(showFab);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        /*Animation animation = AppController.getComponent(getContext()).scaleDownAnimation();
        if (showFab) {
            animation = AppController.getComponent(getContext()).scaleUpAnimation();
        }
        startAnimation(animation);*/
    }

    private void resetAnimationData(boolean showFab) {
        if (showFab) isScaledUp = true;
        else isScaledDown = true;
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);

        savedState.visibility = getVisibility();
        savedState.isScaledDown = isScaledDown;
        savedState.isScaledUp = isScaledUp;

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
        isScaledDown = savedState.isScaledDown;
        isScaledUp = savedState.isScaledUp;
    }

    private static class SavedState extends BaseSavedState {

        private int visibility;
        private boolean isScaledDown;
        private boolean isScaledUp;

        public SavedState(Parcel source) {
            super(source);

            visibility = source.readInt();
            isScaledDown = source.readInt() == 1;
            isScaledUp = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(visibility);
            out.writeInt(isScaledDown ? 1 : 0);
            out.writeInt(isScaledUp ? 1 : 0);
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
