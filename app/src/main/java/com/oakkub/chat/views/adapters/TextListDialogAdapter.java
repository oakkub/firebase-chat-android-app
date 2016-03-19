package com.oakkub.chat.views.adapters;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oakkub.chat.views.adapters.viewholders.MyBaseAdapter;

/**
 * Created by OaKKuB on 3/14/2016.
 */
public class TextListDialogAdapter extends MyBaseAdapter<String> {

    public TextListDialogAdapter() {
        super();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(parent.getContext());
            textView.setGravity(Gravity.START);
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(getItemList(position));

        return textView;
    }
}
