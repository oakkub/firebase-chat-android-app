package com.oakkub.chat.views.adapters.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by OaKKuB on 3/14/2016.
 */
public class MyBaseAdapter<I> extends BaseAdapter {

    protected ArrayList<I> items;

    @Override
    public int getCount() {
        return items.size();
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
        return null;
    }

    protected I getItemList(int index) {
        return items.get(index);
    }
}
