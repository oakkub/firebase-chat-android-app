package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 2/6/2016.
 */
public class SystemMessageDivider extends RecyclerView.ViewHolder {

    @Bind(R.id.system_message_text_view)
    public TextView systemMessageTextView;

    public SystemMessageDivider(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
