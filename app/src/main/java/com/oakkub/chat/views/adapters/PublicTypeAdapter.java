package com.oakkub.chat.views.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.PublicTypeHolder;

/**
 * Created by OaKKuB on 3/19/2016.
 */
public class PublicTypeAdapter extends RecyclerViewSingleSelectionAdapter<String> {

    private OnAdapterItemClick onAdapterItemClick;

    public PublicTypeAdapter(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.public_type_item, parent, false);
        return new PublicTypeHolder(view, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String type = getItem(position);

        PublicTypeHolder hold = (PublicTypeHolder) holder;
        hold.publicTypeTextView.setText(type);

        if (isSelected(type.hashCode())) {
            hold.itemView.setBackgroundColor(
                    ContextCompat.getColor(hold.itemView.getContext(), R.color.colorPrimary));
        } else {
            hold.itemView.setBackgroundResource(R.drawable.primary_ripple);
        }

    }
}
