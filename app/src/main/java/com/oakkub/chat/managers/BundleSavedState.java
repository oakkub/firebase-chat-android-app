package com.oakkub.chat.managers;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * Created by OaKKuB on 1/20/2016.
 */
public class BundleSavedState extends View.BaseSavedState {

    public Bundle bundle;

    public BundleSavedState(Parcel source) {
        super(source);
        bundle = source.readBundle(getClass().getClassLoader());
    }

    public BundleSavedState(Parcelable superState) {
        super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeBundle(bundle);
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new BundleSavedState(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new BundleSavedState[size];
        }
    };
}
