package com.oakkub.chat.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by OaKKuB on 10/15/2015.
 */
public class ListAdapter extends RecyclerViewMultipleSelectionAdapter<String> {

    private OnAdapterItemClick onAdapterItemClick;

    public ListAdapter(ArrayList<String> list, OnAdapterItemClick onAdapterItemClick) {
        super();
        this.onAdapterItemClick = onAdapterItemClick;
        items = list;
    }

    public void setOnAdapterItemClick(OnAdapterItemClick onAdapterItemClick) {
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, parent, false);

        return new ViewHolder(itemView, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {

            ViewHolder h = (ViewHolder) holder;

            h.list.setText(String.valueOf(items.get(position)));
            h.backgroundOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.list_textview)
        TextView list;

        @Bind(R.id.background_overlay)
        View backgroundOverlay;

        private OnAdapterItemClick onAdapterItemClick;

        public ViewHolder(View itemView, OnAdapterItemClick onAdapterItemClick) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.onAdapterItemClick = onAdapterItemClick;
        }

        @OnClick(R.id.list_root)
        public void onClick() {
            onAdapterItemClick.onAdapterClick(itemView, getAdapterPosition());
        }

        @OnLongClick(R.id.list_root)
        public boolean onLongClick() {
            return onAdapterItemClick != null && onAdapterItemClick.onAdapterLongClick(itemView, getAdapterPosition());
        }
    }

}
