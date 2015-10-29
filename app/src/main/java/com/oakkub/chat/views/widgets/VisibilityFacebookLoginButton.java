package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.facebook.login.widget.LoginButton;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class VisibilityFacebookLoginButton extends LoginButton {

    public VisibilityFacebookLoginButton(Context context) {
        super(context);
    }

    public VisibilityFacebookLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VisibilityFacebookLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);

        savedState.visibility = getVisibility();

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
    }

    private static class SavedState extends BaseSavedState {

        private int visibility;

        public SavedState(Parcel source) {
            super(source);

            visibility = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(visibility);
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
