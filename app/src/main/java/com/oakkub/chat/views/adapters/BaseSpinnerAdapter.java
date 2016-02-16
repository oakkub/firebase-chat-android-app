package com.oakkub.chat.views.adapters;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 2/1/2016.
 */
public class BaseSpinnerAdapter<K, V> extends ArrayAdapter<ArrayMap<K, V>> {

    private ArrayMap<K, V> items;

    public BaseSpinnerAdapter(Context context, int resource, ArrayMap<K, V> items) {
        super(context, resource);
        this.items = items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return inflateTwoTextView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return inflateTwoTextView(position, convertView, parent);
    }

    public K getKey(int position) {
        return items.keyAt(position);
    }

    public V getValue(int position) {
        return items.valueAt(position);
    }

    private View inflateTwoTextView(int position, View convertView, ViewGroup parent) {
        TwoTextViewViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.partial_two_horizontal_textview, parent, false);
            viewHolder = new TwoTextViewViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TwoTextViewViewHolder) convertView.getTag();
        }

        viewHolder.textViewKey.setText(String.valueOf(items.keyAt(position)));
        viewHolder.textViewValue.setText(String.valueOf(items.valueAt(position)));

        return convertView;
    }

    private static class TwoTextViewViewHolder {
        TextView textViewKey;
        TextView textViewValue;

        public TwoTextViewViewHolder(View convertView) {
            textViewKey = (TextView) convertView.findViewById(R.id.textviewKey);
            textViewValue = (TextView) convertView.findViewById(R.id.textviewValue);
        }
    }

}
