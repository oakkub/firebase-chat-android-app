package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class ViewPager extends android.support.v4.view.ViewPager {

    private int previousItem;

    public ViewPager(Context context) {
        super(context);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getPreviousItem() {
        return previousItem;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        previousItem = item;
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);

        savedState.visibility = getVisibility();
        savedState.previousItem = previousItem;

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
        previousItem = savedState.previousItem;
    }

    private static class SavedState extends BaseSavedState {

        private int visibility;
        private int previousItem;

        public SavedState(Parcel source) {
            super(source);

            visibility = source.readInt();
            previousItem = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(visibility);
            out.writeInt(previousItem);
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
