package com.oakkub.chat.managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

/**
 * Created by OaKKuB on 12/23/2015.
 */
public class SparseIntArrayParcelableWrapper extends SparseIntArray implements Parcelable {

    public SparseIntArrayParcelableWrapper() {}

    protected SparseIntArrayParcelableWrapper(Parcel in) {
        int size = in.readInt();
        int[] keys = new int[size];
        int[] values = new int[size];

        in.readIntArray(keys);
        in.readIntArray(values);

        for (int i = 0; i < size; i++) {
            this.put(keys[i], values[i]);
        }
    }

    public SparseIntArrayParcelableWrapper(SparseIntArray sparseIntArray) {
        for (int i = 0, size = sparseIntArray.size(); i < size; i++) {
            this.put(sparseIntArray.keyAt(i), sparseIntArray.valueAt(i));
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

    public static final Creator<SparseIntArrayParcelableWrapper> CREATOR = new Creator<SparseIntArrayParcelableWrapper>() {
        @Override
        public SparseIntArrayParcelableWrapper createFromParcel(Parcel in) {
            return new SparseIntArrayParcelableWrapper(in);
        }

        @Override
        public SparseIntArrayParcelableWrapper[] newArray(int size) {
            return new SparseIntArrayParcelableWrapper[size];
        }
    };
}
