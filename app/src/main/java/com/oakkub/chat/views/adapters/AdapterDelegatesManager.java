package com.oakkub.chat.views.adapters;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class AdapterDelegatesManager<T> {

    private SparseArrayCompat<AdapterDelegate<T>> delegates = new SparseArrayCompat<>();

    public AdapterDelegatesManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {

        if (delegate == null) {
            throw new NullPointerException("AdapterDelegate is null.");
        }

        int viewType = delegate.getItemViewType();
        delegates.put(viewType, delegate);

        return this;
    }

    public int getItemViewType(@NonNull T items, int position) {

        if (items == null) {
            throw new NullPointerException("Items is null");
        }

        for (int i = 0, delegatesCount = delegates.size(); i < delegatesCount; i++) {
            AdapterDelegate<T> delegate = delegates.valueAt(i);

            if (delegate.isForViewType(items, position)) {
                return delegate.getItemViewType();
            }
        }

        throw new IllegalStateException("No AdapterDelegate added that matches position=" + position + " in data source");
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        AdapterDelegate<T> delegate = delegates.get(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for this ViewType " + viewType);
        }

        RecyclerView.ViewHolder holder = delegate.onCreateViewHolder(parent);
        if (holder == null) {
            throw new NullPointerException(
                    "ViewHolder returned from AdapterDelegate " + delegate + " for ViewType =" + viewType
                            + " is null!");
        }

        return holder;
    }

    public void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder holder) {

        AdapterDelegate<T> delegate = delegates.get(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException(
                    "No AdapterDelegate added for ViewType " + holder.getItemViewType());
        }

        delegate.onBindViewHolder(items, position, holder);
    }

}
