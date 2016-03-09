package com.oakkub.chat.managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

/**
 * Created by OaKKuB on 2/18/2016.
 */
public class SparseStringArray extends SparseArray<String> implements Parcelable {

    private static final String TAG = SparseStringArray.class.getSimpleName();

    public SparseStringArray() {
        super();
    }

    private SparseStringArray(Parcel in) {
        int size = in.readInt();
        int[] keys = new int[size];
        String[] values = new String[size];

        in.readIntArray(keys);
        in.readStringArray(values);

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
        String[] values = new String[size];

        for (int i = 0; i < size; i++) {
            keys[i] = keyAt(i);
            values[i] = valueAt(i);
        }

        dest.writeInt(size);
        dest.writeIntArray(keys);
        dest.writeStringArray(values);
    }

    public static final Creator<SparseStringArray> CREATOR = new Creator<SparseStringArray>() {
        @Override
        public SparseStringArray createFromParcel(Parcel in) {
            return new SparseStringArray(in);
        }

        @Override
        public SparseStringArray[] newArray(int size) {
            return new SparseStringArray[size];
        }
    };
}
