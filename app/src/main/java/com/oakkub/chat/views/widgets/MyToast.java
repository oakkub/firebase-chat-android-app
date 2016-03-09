package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.Contextor;

import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 2/5/2016.
 */
public class MyToast {

    public static Toast make(Object object) {
        Context applicationContext = Contextor.getInstance().getContext();

        View view = LayoutInflater.from(applicationContext)
                .inflate(R.layout.card_view_toast, null);
        TextView message = ButterKnife.findById(view, R.id.card_view_toast_textview);
        message.setText(String.valueOf(object));

        Toast toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT);
        toast.setView(view);

        return toast;
    }

}
