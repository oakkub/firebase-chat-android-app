package com.oakkub.chat.views.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by OaKKuB on 3/3/2016.
 */
public class MyListPreference extends ListPreference {

    public MyListPreference(Context context) {
        super(context);
    }

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public MyListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MyListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CharSequence getEntryFromValue(String value) {
        int index = findIndexOfValue(value);
        return getEntries()[index];
    }

    public CharSequence getEntryValue(String value) {
        int index = findIndexOfValue(value);
        return getEntryValues()[index];
    }

}
