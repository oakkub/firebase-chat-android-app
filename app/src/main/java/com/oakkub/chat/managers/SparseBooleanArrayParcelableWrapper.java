package com.oakkub.chat.managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

/**
 * Created by OaKKuB on 12/23/2015.
 */
public class SparseBooleanArrayParcelableWrapper extends SparseBooleanArray implements Parcelable {

    public SparseBooleanArrayParcelableWrapper() {}

    protected SparseBooleanArrayParcelableWrapper(Parcel in) {}

    public SparseBooleanArrayParcelableWrapper(SparseBooleanArray sparseBooleanArray) {
        for (int i = 0, size = sparseBooleanArray.size(); i < size; i++) {
            this.put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(i));
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

    public static final Creator<SparseBooleanArrayParcelableWrapper> CREATOR = new Creator<SparseBooleanArrayParcelableWrapper>() {
        @Override
        public SparseBooleanArrayParcelableWrapper createFromParcel(Parcel in) {
            SparseBooleanArrayParcelableWrapper wrapper = new SparseBooleanArrayParcelableWrapper();

            int size = in.readInt();
            int[] keys = new int[size];
            boolean[] values = new boolean[size];

            in.readIntArray(keys);
            in.readBooleanArray(values);

            for (int i = 0; i < size; i++) {
                wrapper.put(keys[i], values[i]);
            }

            return wrapper;
        }

        @Override
        public SparseBooleanArrayParcelableWrapper[] newArray(int size) {
            return new SparseBooleanArrayParcelableWrapper[size];
        }
    };
}
