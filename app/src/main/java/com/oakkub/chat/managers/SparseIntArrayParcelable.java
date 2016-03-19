package com.oakkub.chat.managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

/**
 * Created by OaKKuB on 3/4/2016.
 */
public class SparseIntArrayParcelable extends SparseIntArray implements Parcelable {

    public SparseIntArrayParcelable() {
        super();
    }

    public SparseIntArrayParcelable(int capacity) {
        super(capacity);
    }

    private SparseIntArrayParcelable(Parcel in) {
        int size = in.readInt();
        int[] keys = new int[size];
        int[] values = new int[size];

        in.readIntArray(keys);
        in.readIntArray(values);

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
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            keys[i] = keyAt(i);
            values[i] = valueAt(i);
        }

        dest.writeInt(size);
        dest.writeIntArray(keys);
        dest.writeIntArray(values);
    }

    public static final Creator<SparseIntArrayParcelable> CREATOR = new Creator<SparseIntArrayParcelable>() {
        @Override
        public SparseIntArrayParcelable createFromParcel(Parcel in) {
            return new SparseIntArrayParcelable(in);
        }

        @Override
        public SparseIntArrayParcelable[] newArray(int size) {
            return new SparseIntArrayParcelable[size];
        }
    };
}
