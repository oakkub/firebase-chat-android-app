package com.oakkub.chat.views.adapters.presenter;

import android.view.View;

/**
 * Created by OaKKuB on 12/16/2015.
 */
public interface OnAdapterItemClick {
    void onAdapterClick(View itemView, int position);
    boolean onAdapterLongClick(View itemView, int position);
}
