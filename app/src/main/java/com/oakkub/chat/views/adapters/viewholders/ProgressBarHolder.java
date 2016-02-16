package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class ProgressBarHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.center_progress_bar)
    ProgressBar loadingView;

    public ProgressBarHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
