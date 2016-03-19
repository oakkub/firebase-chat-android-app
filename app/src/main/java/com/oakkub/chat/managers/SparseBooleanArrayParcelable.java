package com.oakkub.chat.managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

/**
 * Created by OaKKuB on 3/4/2016.
 */
public class SparseBooleanArrayParcelable extends SparseBooleanArray implements Parcelable {

    public SparseBooleanArrayParcelable() {
        super();
    }

    public SparseBooleanArrayParcelable(int capacity) {
        super(capacity);
    }

    private SparseBooleanArrayParcelable(Parcel in) {
        int size = in.readInt();
        int[] keys = new int[size];
        boolean[] values = new boolean[size];

        in.readIntArray(keys);
        in.readBooleanArray(values);

        for (int i = 0; i < size; i++) {
            put(keys[i], values[i]);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int size = size();
        int[] keys = new int[size];
        boolean[] values = new boolean[size];

        for (int i = 0; i < size; i++) {
            keys[i] = keyAt(i);
            values[i] = valueAt(i);
        }

        dest.writeInt(size);
        dest.writeIntArray(keys);
        dest.writeBooleanArray(values);
    }

    public static final Creator<SparseBooleanArrayParcelable> CREATOR = new Creator<SparseBooleanArrayParcelable>() {
        @Override
        public SparseBooleanArrayParcelable createFromParcel(Parcel in) {
            return new SparseBooleanArrayParcelable(in);
        }

        @Override
        public SparseBooleanArrayParcelable[] newArray(int size) {
            return new SparseBooleanArrayParcelable[size];
        }
    };
}
