package com.oakkub.chat.views.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.oakkub.chat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 12/2/2015.
 */
public class MyMessageHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.my_message_text_view)
    public TextView message;

    public MyMessageHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
