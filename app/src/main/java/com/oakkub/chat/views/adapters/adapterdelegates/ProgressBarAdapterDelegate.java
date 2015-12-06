package com.oakkub.chat.views.adapters.adapterdelegates;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.models.ProgressBarFooter;
import com.oakkub.chat.views.adapters.AdapterDelegate;
import com.oakkub.chat.views.adapters.viewholders.ProgressBarHolder;

import java.util.List;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class ProgressBarAdapterDelegate implements AdapterDelegate<List> {

    private final int viewType;

    public ProgressBarAdapterDelegate(int viewType) {
        this.viewType = viewType;
    }

    @Override
    public int getItemViewType() {
        return viewType;
    }

    @Override
    public boolean isForViewType(@NonNull List items, int position) {
        return items.get(position) instanceof ProgressBarFooter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ProgressBarHolder(layoutInflater.inflate(R.layout.progress_bar, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }
}
